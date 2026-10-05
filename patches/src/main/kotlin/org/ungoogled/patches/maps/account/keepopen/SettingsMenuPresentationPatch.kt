package org.ungoogled.patches.maps.account.keepopen

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import org.ungoogled.patches.maps.ui.sharedExtensionPatch

private const val MENU_UI = "Lorg/ungoogled/ui/SettingsMenu;"

/** Reuse the native menu, hit target and icon; replace only account presentation. */
internal val settingsMenuPresentationPatch = bytecodePatch(
    description = "Shows a settings gear without modifying menu navigation or animations.",
) {
    dependsOn(sharedExtensionPatch)
    execute {
        val disc = mutableClassDefBy("Lcom/google/android/libraries/onegoogle/accountmenu/SelectedAccountDisc;")
        if (disc.superclass != "Landroid/widget/FrameLayout;" ||
            disc.methods.any { it.name == "dispatchDraw" || it.name == "setContentDescription" }) {
            throw PatchException("Selected account disc changed shape")
        }
        disc.methods.add(ImmutableMethod(
            disc.type, "dispatchDraw", listOf(ImmutableMethodParameter("Landroid/graphics/Canvas;", null, null)),
            "V", AccessFlags.PROTECTED.value, null, null, MutableMethodImplementation(2),
        ).toMutable().apply {
            addInstructions(0, """
                invoke-static { p0, p1 }, $MENU_UI->drawGear(Landroid/view/View;Landroid/graphics/Canvas;)V
                return-void
            """)
        })
        disc.methods.add(ImmutableMethod(
            disc.type, "setContentDescription", listOf(ImmutableMethodParameter("Ljava/lang/CharSequence;", null, null)),
            "V", AccessFlags.PUBLIC.value, null, null, MutableMethodImplementation(2),
        ).toMutable().apply {
            addInstructions(0, """
                invoke-static { p0 }, $MENU_UI->label(Landroid/view/View;)Ljava/lang/CharSequence;
                move-result-object p1
                invoke-super { p0, p1 }, Landroid/widget/FrameLayout;->setContentDescription(Ljava/lang/CharSequence;)V
                return-void
            """)
        })

    }
}
