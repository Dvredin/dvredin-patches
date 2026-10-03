package org.ungoogled.patches.maps.location

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.*
import org.ungoogled.patches.maps.ui.SHAPES
import org.ungoogled.patches.maps.ui.sharedExtensionPatch
import java.security.MessageDigest
import java.util.Locale

private const val SETTINGS = "Lamco;"
private const val NATIVE_SETTINGS = "Lamcm;"
private const val CALLBACK = "Lamcn;"
private const val ORIGINAL_SETTINGS = "a6a921c72446548df2a09ce4da7b18f390d20b2b163e1f6b3ae757ba40264faa"

/**
 * The provider toggle redirects GMS location binds, but Maps' My Location button
 * still asks the GMS SettingsClient to complete an accuracy check. After resume
 * that request can stay pending, preventing the camera callback from running.
 * Reuse Maps' synchronous GPS-settings implementation for the Android source.
 * Its GPS-only branch preserves disabled-GPS dialogs without asking for GMS or
 * prompting to enable Google's network location. Keep the stock wrapper callback
 * and leave the entire original implementation reachable for the Play source.
 */
internal val nativeLocationSettingsPatch = bytecodePatch {
    dependsOn(sharedExtensionPatch)
    execute {
        val settings = mutableClassDefBy(SETTINGS).methods.singleOrNull {
            it.name == "f" && it.returnType == "V" &&
                it.parameterTypes == listOf("Z", "I", "Z", "Lamdj;")
        } ?: throw PatchException("Location settings completion method not found")
        if (settings.implementation == null || settingsShape(settings) != ORIGINAL_SETTINGS) {
            throw PatchException("Unexpected or already modified location settings completion method")
        }
        val constructorParameters = listOf("Landroid/app/Activity;", "Laije;", "Lbcph;", "Lbcjh;", "Lbcit;")
        if (classDefBy(NATIVE_SETTINGS).methods.none {
            it.name == "<init>" && it.parameterTypes == constructorParameters
        } || classDefBy(CALLBACK).methods.none {
            it.name == "<init>" && it.parameterTypes == listOf("Ljava/lang/Object;", "Ljava/lang/Object;", "I")
        }) throw PatchException("Native location settings constructor/callback changed")
        val fields = classDefBy(SETTINGS).fields.map { it.name to it.type }.toSet()
        if (!fields.containsAll(listOf("d" to "Lnxb;", "b" to "Laije;", "h" to "Lbcph;", "i" to "Lbcjh;", "j" to "Lbcit;"))) {
            throw PatchException("Native location settings dependencies changed")
        }

        // The exact gate proves seven locals and p0..p4=v7..v11. No parameter is
        // overwritten, and neither the old tail nor its branch targets change.
        settings.addInstructionsWithLabels(0, """
            invoke-static {}, $SHAPES->playLocation()Z
            move-result v0
            if-nez v0, :play_settings
            new-instance v0, $NATIVE_SETTINGS
            iget-object v1, p0, $SETTINGS->d:Lnxb;
            iget-object v2, p0, $SETTINGS->b:Laije;
            iget-object v3, p0, $SETTINGS->h:Lbcph;
            iget-object v4, p0, $SETTINGS->i:Lbcjh;
            iget-object v5, p0, $SETTINGS->j:Lbcit;
            invoke-direct/range {v0 .. v5}, $NATIVE_SETTINGS-><init>(Landroid/app/Activity;Laije;Lbcph;Lbcjh;Lbcit;)V
            new-instance v6, $CALLBACK
            const/4 v1, 0x0
            invoke-direct {v6, p0, p4, v1}, $CALLBACK-><init>(Ljava/lang/Object;Ljava/lang/Object;I)V
            const/4 v2, 0x1
            invoke-interface {v0, p1, v1, v2, v6}, Lamcl;->c(ZZZLamck;)V
            return-void
        """, ExternalLabel("play_settings", settings.implementation!!.instructions.first()))
    }
}

/** Fail closed on a different version, register layout, or overlapping patch. */
private fun settingsShape(method: Method): String {
    val implementation = method.implementation!!
    val shape = buildString {
        append(method.accessFlags).append(':').append(implementation.registerCount).append('\n')
        implementation.instructions.forEach { instruction ->
            append(instruction.opcode.toString())
            if (instruction is OneRegisterInstruction) append(" A=").append(instruction.registerA)
            if (instruction is TwoRegisterInstruction) append(" B=").append(instruction.registerB)
            if (instruction is ThreeRegisterInstruction) append(" C=").append(instruction.registerC)
            if (instruction is FiveRegisterInstruction) {
                append(" n=").append(instruction.registerCount)
                append(" C=").append(instruction.registerC)
                append(" D=").append(instruction.registerD)
                append(" E=").append(instruction.registerE)
                append(" F=").append(instruction.registerF)
                append(" G=").append(instruction.registerG)
            }
            if (instruction is RegisterRangeInstruction) {
                append(" n=").append(instruction.registerCount)
                append(" start=").append(instruction.startRegister)
            }
            if (instruction is ReferenceInstruction) append(" ref=").append(instruction.reference)
            if (instruction is DualReferenceInstruction) append(" ref2=").append(instruction.reference2)
            if (instruction is OffsetInstruction) append(" offset=").append(instruction.codeOffset)
            if (instruction is WideLiteralInstruction) append(" literal=").append(instruction.wideLiteral)
            append('\n')
        }
    }
    return MessageDigest.getInstance("SHA-256").digest(shape.toByteArray(Charsets.UTF_8))
        .joinToString("") { String.format(Locale.ROOT, "%02x", it.toInt() and 0xff) }
}
