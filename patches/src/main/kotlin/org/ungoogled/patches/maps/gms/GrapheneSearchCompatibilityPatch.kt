package org.ungoogled.patches.maps.gms

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import org.ungoogled.patches.maps.location.headingShape
import org.ungoogled.patches.maps.ui.sharedExtensionPatch
import org.ungoogled.patches.shared.Constants.COMPATIBILITY_MAPS

private object NativeMinModeProbeFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(string("config_minmode_enabled")),
)

/** An optional SystemUI feature probe must not terminate search on this known OS failure. */
@Suppress("unused")
val grapheneSearchCompatibilityPatch = bytecodePatch(
    name = "Fix GrapheneOS search crash",
    description = "Keeps search working when GrapheneOS's Google Play asset hook fails during " +
        "the optional native power-saving capability check. Unrelated exceptions are not hidden.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_MAPS)
    dependsOn(sharedExtensionPatch)
    execute {
        val method = NativeMinModeProbeFingerprint.method
        val impl = method.implementation ?: throw PatchException("Native min-mode probe has no code")
        if (headingShape(method) != "fb13c53f1959ec7890f590095c1c0e367db92f65232d7d1e6a67d38ea601a7a6") {
            throw PatchException("Unexpected or already modified native min-mode probe")
        }
        // The code hash does not include handlers. Gate them independently before editing.
        val handlers = impl.tryBlocks.map {
            "${it.startCodeAddress}:${it.codeUnitCount}:" +
                "${it.exceptionHandler.exceptionType}:${it.exceptionHandler.handlerCodeAddress}"
        }.sorted()
        if (handlers != listOf(
                "3:25:Landroid/content/pm/PackageManager\$NameNotFoundException;:44",
                "3:25:Landroid/content/res/Resources\$NotFoundException;:29",
            ).sorted()) {
            throw PatchException("Unexpected native min-mode probe exception table")
        }
        // Keep every original instruction, target and handler address. Append a separate
        // NPE-only handler outside all original try regions; v0 is an exact-gated local.
        val handlerIndex = impl.instructions.size
        method.addInstructions(handlerIndex, """
            move-exception v0
            invoke-static { v0 }, Lorg/ungoogled/ui/MinModeCompatibility;->onProbeNull(Ljava/lang/NullPointerException;)Z
            move-result v0
            return v0
        """)
        impl.addCatch(
            "Ljava/lang/NullPointerException;",
            impl.newLabelForAddress(3),
            impl.newLabelForAddress(28),
            impl.newLabelForIndex(handlerIndex),
        )
    }
}
