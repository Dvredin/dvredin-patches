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

private const val HEADING = "Laipl;"
private const val ORIGINAL_HEADING = "d000355961ccd3d23d5c1c1969f07462f52068307d6d1fde95e0c78106ddd12e"

/**
 * Coordinates and orientation have separate GMS selectors. Spoofed availability
 * can select Google's orientation service even in Android location mode, and its
 * old request-failure callback has no native fallback. Narrow the selector so
 * Android mode reuses Maps' existing SensorManager compass. Preserve every stock
 * instruction for the Play source. The existing source-choice UI restarts Maps,
 * so clients are constructed again when that preference changes.
 */
internal val nativeHeadingPatch = bytecodePatch {
    dependsOn(sharedExtensionPatch)
    execute {
        val selector = mutableClassDefBy(HEADING).methods.singleOrNull {
            it.name == "o" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        } ?: throw PatchException("Heading source selector not found")
        if (selector.implementation == null || headingShape(selector) != ORIGINAL_HEADING) {
            throw PatchException("Unexpected or already modified heading source selector")
        }
        // The exact shape proves v0 is a local, p0=v8, and the stock tail is
        // untouched. This is a dynamic AND, not a cached replacement of o().
        selector.addInstructionsWithLabels(0, """
            invoke-static {}, $SHAPES->playLocation()Z
            move-result v0
            if-nez v0, :play_heading
            const/4 v0, 0x0
            return v0
        """, ExternalLabel("play_heading", selector.implementation!!.instructions.first()))
    }
}

/** Fail closed on a changed body, signature layout, or overlapping correction. */
private fun headingShape(method: Method): String {
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
