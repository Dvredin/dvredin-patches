package com.android.internal.gmscompat.fileservice;
/** QA-only exact null-prefix reproducer; NEVER packaged in the owner MPP/APK. */
public final class GmsCoreFileServerClientHooks {
    private static String prefix;
    public static boolean isInGmsCoreDeDataDirAndShouldCacheFds(String path) {
        return path.startsWith(prefix);
    }
}
