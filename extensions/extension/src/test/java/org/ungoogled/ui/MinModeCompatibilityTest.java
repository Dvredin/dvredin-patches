package org.ungoogled.ui;

import org.junit.Test;
import static org.junit.Assert.*;

public class MinModeCompatibilityTest {
    private static StackTraceElement frame(String c, String m) {
        return new StackTraceElement(c, m, "AnyVersion.java", 999);
    }
    private static final StackTraceElement HOOK = frame(
        "com.android.internal.gmscompat.fileservice.GmsCoreFileServerClientHooks",
        "isInGmsCoreDeDataDirAndShouldCacheFds");
    private static final StackTraceElement LOADER = frame(
        "com.android.internal.gmscompat.dynamite.GmsDynamiteClientHooks", "loadAssetsFromPath");
    private static NullPointerException error(StackTraceElement... frames) {
        NullPointerException e = new NullPointerException("message is not a matcher");
        e.setStackTrace(frames);
        return e;
    }
    private static void rethrows(NullPointerException e) {
        try { MinModeCompatibility.onProbeNull(e); fail("unrelated NPE swallowed"); }
        catch (NullPointerException actual) { assertSame(e, actual); }
    }
    @Test public void ownerChainReturnsUnavailable() {
        assertFalse(MinModeCompatibility.onProbeNull(error(
            frame("java.lang.String", "startsWith"), frame("java.lang.String", "startsWith"),
            HOOK, LOADER, frame("android.content.res.ApkAssets", "loadFromPath"))));
    }
    @Test public void sourceLineAndMessageIndependent() {
        assertFalse(MinModeCompatibility.onProbeNull(error(HOOK, LOADER)));
    }
    @Test public void unrelatedNullRethrownUnchanged() { rethrows(new NullPointerException()); }
    @Test public void incompleteChainRethrown() { rethrows(error(HOOK)); rethrows(error(LOADER)); }
    @Test public void wrongOrderRethrown() { rethrows(error(LOADER, HOOK)); }
    @Test public void unrelatedCallerRethrown() {
        rethrows(error(HOOK, frame("application.Example", "loadAssetsFromPath")));
    }
    @Test public void unrelatedHookMethodRethrown() {
        rethrows(error(frame(HOOK.getClassName(), "otherMethod"), LOADER));
    }
    @Test public void separatedFramesRethrown() {
        rethrows(error(HOOK, frame("application.Example", "other"), LOADER));
    }
}
