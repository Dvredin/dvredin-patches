package org.ungoogled.ui;

/** Narrow containment for the optional SystemUI min-mode resource probe only. */
public final class MinModeCompatibility {
    private MinModeCompatibility() {}

    /** Return unavailable for the observed GrapheneOS asset-hook NPE; rethrow all others. */
    public static boolean onProbeNull(NullPointerException failure) {
        StackTraceElement[] stack = failure.getStackTrace();
        for (int i = 0; i + 1 < stack.length; i++) {
            StackTraceElement hook = stack[i];
            StackTraceElement caller = stack[i + 1];
            if (hook.getClassName().equals(
                    "com.android.internal.gmscompat.fileservice.GmsCoreFileServerClientHooks")
                    && hook.getMethodName().equals("isInGmsCoreDeDataDirAndShouldCacheFds")
                    && caller.getClassName().equals(
                    "com.android.internal.gmscompat.dynamite.GmsDynamiteClientHooks")
                    && caller.getMethodName().equals("loadAssetsFromPath")) {
                return false;
            }
        }
        throw failure;
    }
}
