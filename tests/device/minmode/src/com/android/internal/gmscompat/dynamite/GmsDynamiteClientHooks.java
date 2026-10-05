package com.android.internal.gmscompat.dynamite;
import android.content.Context;
/** QA-only call chain, not a replacement implementation of GrapheneOS. */
public final class GmsDynamiteClientHooks {
    public static Context loadAssetsFromPath() {
        com.android.internal.gmscompat.fileservice.GmsCoreFileServerClientHooks
            .isInGmsCoreDeDataDirAndShouldCacheFds("/system_ext/priv-app/SystemUI/SystemUI.apk");
        throw new AssertionError("Expected null-prefix failure");
    }
}
