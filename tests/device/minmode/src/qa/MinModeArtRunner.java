package qa;

import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import java.lang.reflect.*;
import org.json.JSONObject;

/** QA-only: invokes the real generated Maps method on ART with controlled Context faults. */
public final class MinModeArtRunner extends Instrumentation {
    private boolean patched;
    private int checks;
    private Constructor<?> constructor;
    private Method probe;
    private final NullPointerException otherNull = new NullPointerException("unrelated QA fault");
    private final SecurityException security = new SecurityException("unrelated QA permission");
    private void check(boolean b, String label) { checks++; if (!b) throw new AssertionError(label); }
    private boolean invoke(int mode) throws Throwable {
        Context base = getTargetContext();
        Context input = new ContextWrapper(base) {
            @Override public Context createPackageContext(String name, int flags)
                    throws PackageManager.NameNotFoundException {
                check(name.equals("com.android.systemui") && flags == 0, "original resource request");
                if (mode == 0) return com.android.internal.gmscompat.dynamite.GmsDynamiteClientHooks.loadAssetsFromPath();
                if (mode == 4) throw new PackageManager.NameNotFoundException("QA");
                if (mode == 5) throw new Resources.NotFoundException("QA");
                if (mode == 6) throw otherNull;
                if (mode == 7) throw security;
                Resources r = base.getResources();
                Resources fixture = new Resources(r.getAssets(), r.getDisplayMetrics(), r.getConfiguration()) {
                    @Override public int getIdentifier(String n, String type, String pkg) {
                        check(n.equals("config_minmode_enabled") && type.equals("bool")
                                && pkg.equals("com.android.systemui"), "original boolean resource");
                        return mode == 3 ? 0 : 1;
                    }
                    @Override public boolean getBoolean(int id) { return mode == 1; }
                };
                return new ContextWrapper(base) {
                    @Override public Resources getResources() { return fixture; }
                };
            }
        };
        Object instance = constructor.newInstance(input, null);
        try { return (Boolean) probe.invoke(instance); }
        catch (InvocationTargetException e) { throw e.getCause(); }
    }
    @Override public void onCreate(Bundle b) { super.onCreate(b); patched = "true".equals(b.getString("patched")); start(); }
    @Override public void onStart() {
        JSONObject report = new JSONObject(); Bundle out = new Bundle(); boolean pass = false;
        try {
            android.content.SharedPreferences upgrade = getTargetContext().getSharedPreferences("minmode_art_upgrade", 0);
            if (patched) {
                check(upgrade.getBoolean("baseline_marker", false), "in-place upgrade retained data");
                upgrade.edit().clear().commit();
            } else check(upgrade.edit().putBoolean("baseline_marker", true).commit(), "seed upgrade marker");
            ClassLoader loader = getTargetContext().getClassLoader();
            Class<?> target = loader.loadClass("abmz");
            constructor = target.getDeclaredConstructor(Context.class, loader.loadClass("ayxm"));
            constructor.setAccessible(true); probe = target.getDeclaredMethod("a"); probe.setAccessible(true);
            if (patched) check(!invoke(0), "fixed actual DEX returns false for known fault");
            else {
                try { invoke(0); throw new AssertionError("baseline unexpectedly contained fault"); }
                catch (NullPointerException expected) {
                    boolean frame = false;
                    for (StackTraceElement f : expected.getStackTrace())
                        if (f.getClassName().equals("com.android.internal.gmscompat.fileservice.GmsCoreFileServerClientHooks")) frame = true;
                    check(frame, "baseline reproduces exact framework-hook NPE escape");
                }
            }
            check(invoke(1), "normal supported remains true");
            check(!invoke(2), "normal disabled remains false");
            check(!invoke(3), "missing identifier remains false");
            check(!invoke(4), "stock missing package handler preserved");
            check(!invoke(5), "stock missing resource handler preserved");
            try { invoke(6); throw new AssertionError("unrelated NPE hidden"); }
            catch (NullPointerException e) { check(e == otherNull, "unrelated NPE identity retained"); }
            try { invoke(7); throw new AssertionError("SecurityException hidden"); }
            catch (SecurityException e) { check(e == security, "security failure retained"); }
            report.put("controlled_fault", true).put("owner_natural_reproduction", false)
                .put("actual_generated_maps_method", "abmz.a").put("fixed", patched)
                .put("cases", 8).put("assertions", checks); pass = true;
        } catch (Throwable e) {
            try { report.put("error", e.toString()).put("assertions", checks); } catch (Exception ignored) {}
        }
        try { report.put("pass", pass); out.putString("stream", report.toString()); } catch (Exception e) { pass = false; }
        finish(pass ? 0 : -1, out);
    }
}
