package org.ungoogled.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;

/**
 * A bounded display-only grace window at Maps' My Location UI listener.
 * Never touches provider Location objects, global position, navigation or stores.
 * Obfuscated member access is covered by exact input-method gates in the patch.
 */
public final class LocationSmoothing {
    public static final String KEY = "smooth_location_updates";
    static final long HOLD_MS = 3000, FRESH_MS = 5000;
    private static volatile boolean on;
    private static final WeakHashMap<Object, State> listeners = new WeakHashMap<>();
    private static final WeakHashMap<Object, State> renderers = new WeakHashMap<>();
    private static Handler handler;

    private LocationSmoothing() {}

    public static boolean enabled(Context c) {
        return Shapes.locationSmoothingPatched() &&
                c.getSharedPreferences(Shapes.PREFS, Context.MODE_PRIVATE).getBoolean(KEY, false);
    }

    public static void refresh(Context c) { on = enabled(c); }

    public static void setEnabled(Context c, boolean value) {
        c.getSharedPreferences(Shapes.PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, value).commit();
        on = value && Shapes.locationSmoothingPatched();
        if (!on) {
            List<State> pending;
            synchronized (LocationSmoothing.class) { pending = new ArrayList<>(listeners.values()); }
            for (State s : pending) release(s, true);
        }
    }

    private static Object field(Object target, String name) throws ReflectiveOperationException {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field f = type.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException missing) { type = type.getSuperclass(); }
        }
        throw new NoSuchFieldException(name);
    }

    private static boolean active(Object controller, Object renderer) throws ReflectiveOperationException {
        String kind = renderer.getClass().getName();
        if (!(Boolean) field(controller, "q") || field(controller, "m") == null ||
                field(controller, "x") != renderer ||
                !(kind.equals("amdc") || kind.equals("amcy"))) return false;
        Object painter = field(field(controller, "s"), "s");
        // Navigation's snapped renderer/chevron is outside this experiment.
        return painter == null || !painter.getClass().getName().equals("amga");
    }

    /** Returns true only when this UI event should wait; every other branch is stock. */
    public static boolean defer(Object listener, Object event) {
        if (!on || Shapes.playLocation()) { forget(listener); return false; }
        try {
            if ((Integer) field(listener, "a") != 1) return false;
            Object controller = field(field(listener, "d"), "a");
            Object renderer = field(controller, "x");
            if (renderer == null || !active(controller, renderer)) { forget(listener); return false; }
            Object executor = field(listener, "c");
            if (!(executor instanceof Executor)) { forget(listener); return false; }
            Object fix = event.getClass().getMethod("c").invoke(event);
            if (fix == null) { forget(listener); return false; } // no fix/permission loss is not concealed
            float accuracy = (Float) fix.getClass().getMethod("a").invoke(fix);
            Object elapsed = field(fix, "l");
            long fixTime = (Long) elapsed.getClass().getMethod("toMillis").invoke(elapsed);
            return decide(listener, controller, renderer, event, accuracy, fixTime, SystemClock.elapsedRealtime());
        } catch (ReflectiveOperationException | RuntimeException unsupported) {
            forget(listener);
            return false; // stock display is safer than guessing an unsupported object layout
        }
    }

    // Package-private, Android-object-free policy inputs keep timing regressions deterministic.
    static synchronized boolean decide(Object listener, Object controller, Object renderer,
            Object event, float accuracy, long fixTime, long now) {
        State s = listeners.get(listener);
        if (s == null || s.renderer.get() != renderer) {
            if (s != null) cancel(s);
            s = new State(listener, controller, renderer);
            listeners.put(listener, s);
        }
        if (s.replay == event) { s.replay = null; return false; }
        boolean fresh = fixTime <= now && now - fixTime <= FRESH_MS;
        boolean valid = Float.isFinite(accuracy) && accuracy > 0 && fresh;
        boolean baselineFresh = s.goodAt >= 0 && now - s.goodAt <= FRESH_MS;
        boolean degraded = valid && baselineFresh && accuracy > Math.max(75f, s.goodAccuracy * 3f);
        if (!degraded || (s.pending != null && now >= s.deadline)) {
            cancel(s);
            s.goodAt = valid && accuracy <= 50f ? now : -1;
            s.goodAccuracy = accuracy;
            return false;
        }
        s.pending = event; // retain only the latest coarse event, never replay a backlog
        if (s.deadline == 0) {
            s.deadline = now + HOLD_MS; // later bad fixes cannot extend the grace window
            renderers.put(renderer, s);
            if (handler == null) handler = new Handler(Looper.getMainLooper());
            State pending = s;
            s.timer = () -> release(pending, false);
            handler.postDelayed(s.timer, HOLD_MS);
        }
        return true;
    }

    /** Freeze the last rendered coordinates/radius, not merely the input fix. */
    public static synchronized boolean frozen(Object renderer) {
        State s = renderers.get(renderer);
        return on && !Shapes.playLocation() && s != null && s.pending != null &&
                SystemClock.elapsedRealtime() < s.deadline;
    }

    private static void release(State s, boolean disabling) {
        Object listener = s.listener.get(), controller = s.controller.get(), renderer = s.renderer.get();
        try {
            if ((!disabling && (!on || Shapes.playLocation())) || listener == null ||
                    controller == null || renderer == null || !active(controller, renderer)) {
                synchronized (LocationSmoothing.class) { cancel(s); s.goodAt = -1; }
                return;
            }
            Executor executor = (Executor) field(listener, "c");
            long generation;
            synchronized (LocationSmoothing.class) { generation = s.generation; }
            // Keep Maps' own listener executor and synchronized dispatch, not an arbitrary timer thread.
            executor.execute(() -> replay(s, listener, disabling, generation));
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            synchronized (LocationSmoothing.class) { cancel(s); s.goodAt = -1; }
        }
    }

    private static void replay(State s, Object listener, boolean disabling, long generation) {
        Object event;
        synchronized (LocationSmoothing.class) {
            if (s.generation != generation || s.pending == null ||
                    (!disabling && SystemClock.elapsedRealtime() < s.deadline)) return;
            event = s.pending;
            cancel(s);
            s.goodAt = -1; // do not hold a second coarse fix until a new precise fix arrives
            s.replay = event;
        }
        try {
            Object controller = s.controller.get(), renderer = s.renderer.get();
            if (controller != null && renderer != null && active(controller, renderer)) {
                // d(aybv) is the public synchronized native event wrapper.
                for (java.lang.reflect.Method m : listener.getClass().getMethods()) {
                    if (m.getName().equals("d") && m.getParameterCount() == 1 &&
                            m.getParameterTypes()[0].isInstance(event)) {
                        m.invoke(listener, event);
                        return;
                    }
                }
            }
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            // No fake fresh fix and no durable location/trace output; the next real event stays stock.
        } finally {
            synchronized (LocationSmoothing.class) { if (s.replay == event) s.replay = null; }
        }
    }

    private static synchronized void forget(Object listener) {
        State s = listeners.remove(listener);
        if (s != null) { cancel(s); s.goodAt = -1; }
    }

    private static void cancel(State s) {
        s.generation++;
        if (s.timer != null && handler != null) handler.removeCallbacks(s.timer);
        Object renderer = s.renderer.get();
        if (renderer != null && renderers.get(renderer) == s) renderers.remove(renderer);
        s.pending = null;
        s.deadline = 0;
        s.timer = null;
    }

    private static final class State {
        final WeakReference<Object> listener, controller, renderer;
        long goodAt = -1, deadline, generation;
        float goodAccuracy;
        Object pending, replay;
        Runnable timer;
        State(Object l, Object c, Object r) {
            listener = new WeakReference<>(l);
            controller = new WeakReference<>(c);
            renderer = new WeakReference<>(r);
        }
    }
}
