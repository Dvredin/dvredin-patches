package org.ungoogled.patches.maps.account

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.Opcode
import org.ungoogled.patches.shared.Constants.COMPATIBILITY_MAPS

@Suppress("unused")
val hideSignInButtonPatch = bytecodePatch(
    name = "Hide sign-in button",
    description = "Removes the account sheet's sign-in pill and keeps it hidden when the header rebinds.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_MAPS)

    execute {
        val wrapper = SignInButtonWrapperFingerprint.classDef
        if (wrapper.type != "Lbrjx;" || wrapper.superclass != "Landroid/widget/FrameLayout;" ||
            wrapper.methods.any { it.name == "setVisibility" }) {
            throw PatchException("Sign-in wrapper changed shape")
        }
        // The header can set VISIBLE after construction. Enforce GONE at the view
        // boundary so asynchronous account binding cannot bring the pill back.
        wrapper.methods.add(
            ImmutableMethod(
                wrapper.type, "setVisibility", listOf(ImmutableMethodParameter("I", null, null)), "V",
                AccessFlags.PUBLIC.value, null, null, MutableMethodImplementation(2),
            ).toMutable().apply {
                addInstructions(0, """
                    const/16 p1, 0x8
                    invoke-super { p0, p1 }, Landroid/widget/FrameLayout;->setVisibility(I)V
                    return-void
                """)
            },
        )
        // Every constructor ends by hiding the view. p1 is the Context in all of
        // them and is dead by the time the constructor returns, so it can hold
        // the GONE constant without adding a register.
        SignInButtonWrapperFingerprint.classDef.methods
            .filter { it.name == "<init>" }
            .forEach { constructor ->
                val returns = constructor.implementation!!.instructions
                    .withIndex()
                    .filter { it.value.opcode == Opcode.RETURN_VOID }
                    .map { it.index }
                // Insert back to front so earlier indices stay valid.
                returns.reversed().forEach { index ->
                    constructor.addInstructions(
                        index,
                        """
                            const/16 p1, 0x8
                            invoke-virtual { p0, p1 }, Landroid/view/View;->setVisibility(I)V
                        """,
                    )
                }
            }
    }
}
