package org.ungoogled.ui;

import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35, manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
public class LocationSmoothingTest {
    public static final class Fix {
        public Duration l;
        private final float accuracy;
        public Fix(float a) { accuracy = a; l = Duration.ofMillis(SystemClock.elapsedRealtime()); }
        public float a() { return accuracy; }
    }
    public static final class Event {
        final Fix fix;
        public Event(float a) { fix = new Fix(a); }
        public Fix c() { return fix; }
    }
    public static final class Wrapper { public final Object a; Wrapper(Object o) { a = o; } }
    public static final class Painter { public Object s; }
    public static final class Controller {
        public boolean q = true;
        public Object m = new Object(), x;
        public Painter s = new Painter();
    }
    public static final class Listener {
        private int a = 1;
        private Object d;
        public java.util.concurrent.Executor c = Runnable::run;
        public final List<Event> delivered = new ArrayList<>();
        final Controller controller;
        Listener() throws Exception {
            controller = new Controller();
            controller.x = Class.forName("amdc").getConstructor().newInstance();
            d = new Wrapper(controller);
        }
        public synchronized void d(Event e) { if (!LocationSmoothing.defer(this, e)) delivered.add(e); }
    }
    private Listener l;
    private static void flag(String name, Object value) throws Exception {
        Field f = LocationSmoothing.class.getDeclaredField(name); f.setAccessible(true); f.set(null, value);
    }
    @Before public void setup() throws Exception {
        LocationSmoothing.setEnabled(RuntimeEnvironment.getApplication(), false);
        flag("on", true);
        Field source = Shapes.class.getDeclaredField("PLAY_LOCATION"); source.setAccessible(true); source.set(null, 0);
        l = new Listener();
    }
    private static void waitMs(long n) { shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(n)); }
    private void good() { l.d(new Event(8)); }

    @Test public void switchIsOffAndAbsentPatchCannotEnableIt() {
        Context c = RuntimeEnvironment.getApplication();
        assertFalse(LocationSmoothing.enabled(c));
        c.getSharedPreferences(Shapes.PREFS, 0).edit().putBoolean(LocationSmoothing.KEY, true).commit();
        assertFalse(LocationSmoothing.enabled(c));
    }
    @Test public void animatedBrowseRendererAlsoHoldsAndRecovers() throws Exception {
        l.controller.x = Class.forName("amcy").getConstructor().newInstance();
        good(); Event poor = new Event(700); l.d(poor);
        assertEquals(1, l.delivered.size()); assertTrue(LocationSmoothing.frozen(l.controller.x));
        Event recovered = new Event(8); l.d(recovered); waitMs(4000);
        assertEquals(2, l.delivered.size()); assertSame(recovered, l.delivered.get(1));
    }
    @Test public void navigationPainterIsNotFiltered() throws Exception {
        l.controller.s.s = Class.forName("amga").getConstructor().newInstance();
        good(); l.d(new Event(700)); waitMs(4000);
        assertEquals(2, l.delivered.size()); assertFalse(LocationSmoothing.frozen(l.controller.x));
    }
    @Test public void unknownRendererPassesThrough() {
        l.controller.x = new Object(); good(); l.d(new Event(700));
        assertEquals(2, l.delivered.size());
    }
    @Test public void coarseFirstFixIsNeverHidden() {
        Event coarse = new Event(600); l.d(coarse);
        assertEquals(List.of(coarse), l.delivered); assertFalse(LocationSmoothing.frozen(l.controller.x));
    }
    @Test public void shortLossHoldsPositionAndRecoveryCancelsReplay() {
        good(); Event poor = new Event(700); l.d(poor);
        assertEquals(1, l.delivered.size()); assertTrue(LocationSmoothing.frozen(l.controller.x));
        waitMs(1000); Event recovered = new Event(9); l.d(recovered);
        assertEquals(recovered, l.delivered.get(1)); assertFalse(LocationSmoothing.frozen(l.controller.x));
        waitMs(4000); assertEquals(2, l.delivered.size());
    }
    @Test public void timeoutReleasesEvenWithoutAnotherLocationCallback() {
        good(); Event poor = new Event(700); l.d(poor);
        waitMs(2999); assertEquals(1, l.delivered.size());
        waitMs(1); assertEquals(List.of(l.delivered.get(0), poor), l.delivered);
        assertFalse(LocationSmoothing.frozen(l.controller.x));
    }
    @Test public void repeatedCoarseFixesDoNotExtendDeadlineAndOnlyLatestIsReleased() {
        good(); l.d(new Event(600)); waitMs(2000);
        Event latest = new Event(900); l.d(latest); waitMs(1000);
        assertEquals(2, l.delivered.size()); assertSame(latest, l.delivered.get(1));
        Event next = new Event(1000); l.d(next); assertSame(next, l.delivered.get(2));
    }
    @Test public void ordinaryPreciseMovementIsNotHeld() {
        good(); for (int i = 0; i < 5; i++) { waitMs(500); l.d(new Event(12)); }
        assertEquals(6, l.delivered.size()); assertFalse(LocationSmoothing.frozen(l.controller.x));
    }
    @Test public void moderateAccuracyChangeIsNotHeld() {
        good(); l.d(new Event(40)); assertEquals(2, l.delivered.size());
    }
    @Test public void staleOrInvalidFixIsNotPromotedToFresh() {
        good(); Event stale = new Event(600); stale.fix.l = Duration.ofMillis(-10000); l.d(stale);
        assertEquals(2, l.delivered.size()); assertFalse(LocationSmoothing.frozen(l.controller.x));
        l.d(new Event(Float.NaN)); l.d(new Event(0)); assertEquals(4, l.delivered.size());
    }
    @Test public void oldBaselineCannotStartHolding() {
        good(); waitMs(5001); l.d(new Event(600)); assertEquals(2, l.delivered.size());
    }
    @Test public void stoppedControllerCannotReceiveDeferredReplay() {
        good(); l.d(new Event(600)); l.controller.q = false; waitMs(4000);
        assertEquals(1, l.delivered.size()); assertFalse(LocationSmoothing.frozen(l.controller.x));
    }
    @Test public void replacementRendererCancelsOldHold() throws Exception {
        good(); Object old = l.controller.x; l.d(new Event(600));
        l.controller.x = Class.forName("amdc").getConstructor().newInstance();
        l.d(new Event(600)); waitMs(4000);
        assertEquals(2, l.delivered.size()); assertFalse(LocationSmoothing.frozen(old));
    }
    @Test public void disablingReleasesLatestCoarseFixImmediately() {
        good(); Event poor = new Event(600); l.d(poor);
        LocationSmoothing.setEnabled(RuntimeEnvironment.getApplication(), false);
        assertEquals(2, l.delivered.size()); assertSame(poor, l.delivered.get(1));
        waitMs(4000); assertEquals(2, l.delivered.size());
    }
    @Test public void unrelatedEventBranchPassesThrough() {
        good(); l.a = 2; l.d(new Event(600)); assertEquals(2, l.delivered.size());
    }
    @Test public void listenerStatesAreIndependent() throws Exception {
        good(); l.d(new Event(600)); Listener other = new Listener();
        other.d(new Event(600)); assertEquals(1, other.delivered.size());
        assertTrue(LocationSmoothing.frozen(l.controller.x)); assertFalse(LocationSmoothing.frozen(other.controller.x));
    }
    @Test public void queuedTimeoutCannotOverwriteRecovery() {
        List<Runnable> queue = new ArrayList<>(); l.c = queue::add;
        good(); l.d(new Event(600)); waitMs(3000); assertEquals(1, queue.size());
        Event recovered = new Event(8); l.d(recovered); queue.get(0).run();
        assertEquals(2, l.delivered.size()); assertSame(recovered, l.delivered.get(1));
    }
    @Test public void sourceChangeCancelsPendingReplay() throws Exception {
        good(); l.d(new Event(600));
        Field source = Shapes.class.getDeclaredField("PLAY_LOCATION"); source.setAccessible(true); source.set(null, 1);
        waitMs(4000); assertEquals(1, l.delivered.size()); assertFalse(LocationSmoothing.frozen(l.controller.x));
    }
    @Test public void googleSourceIsUntouched() throws Exception {
        good(); Field source = Shapes.class.getDeclaredField("PLAY_LOCATION"); source.setAccessible(true); source.set(null, 1);
        l.d(new Event(600)); assertEquals(2, l.delivered.size());
    }
}
