package org.ungoogled.patches.maps.location

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import org.ungoogled.patches.maps.ui.applicationStartHookPatch
import org.ungoogled.patches.maps.ui.activityContextHookPatch
import org.ungoogled.patches.maps.ui.markPatched
import org.ungoogled.patches.shared.Constants.COMPATIBILITY_MAPS

private const val SMOOTH = "Lorg/ungoogled/ui/LocationSmoothing;"

/** UI-only grace window. Raw providers, location pipeline and navigation stay stock. */
@Suppress("unused")
val smoothLocationUpdatesPatch = bytecodePatch(
    name = "Smooth location updates",
    description = "Adds an off-by-default Customization switch for Android API location. " +
        "Holds the last precise blue-dot position and accuracy circle for up to 3 seconds " +
        "when accuracy suddenly drops, then resumes Maps' own animation. " +
        "Does not filter navigation or recorded locations.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_MAPS)
    dependsOn(locationProviderTogglePatch, applicationStartHookPatch, activityContextHookPatch)
    execute {
        val listener = mutableClassDefBy("Lamdz;").methods.singleOrNull {
            it.name == "a" && it.returnType == "V" && it.parameterTypes == listOf("Laybv;")
        } ?: throw PatchException("My Location UI event listener not found")
        val tickerHashes = mapOf(
            "Lamdc;" to "23b83868d513f27f2d296d7d7b4a74454739e9700ec58628b9a75a4d6d834fb7",
            "Lamcy;" to "cd7d772006550561a55273651d94dcc637f8a2a2b576978e2c3799a291c24bf3",
        )
        val tickers = tickerHashes.map { (type, expected) ->
            val ticker = mutableClassDefBy(type).methods.singleOrNull {
                it.name == "k" && it.returnType == "Z" && it.parameterTypes == listOf("J")
            } ?: throw PatchException("My Location marker animation ticker not found: $type")
            if (ticker.implementation == null || headingShape(ticker) != expected) {
                throw PatchException("Unexpected or already modified My Location ticker: $type")
            }
            ticker
        }
        if (listener.implementation == null ||
            headingShape(listener) != "37b37b2c833e45527fdda26f3760866f535ad7745fca63635cab700017023a93") {
            throw PatchException("Unexpected or already modified My Location UI method")
        }
        // Original local v0 is free at entry; parameter/register layouts are exact-gated.
        listener.addInstructionsWithLabels(0, """
            invoke-static/range {p0 .. p1}, $SMOOTH->defer(Ljava/lang/Object;Ljava/lang/Object;)Z
            move-result v0
            if-eqz v0, :stock_location_event
            return-void
        """, ExternalLabel("stock_location_event", listener.implementation!!.instructions.first()))
        // Pause only this browse renderer's extrapolation while its event is pending.
        // Insert BEFORE its monitor-enter; never bypass an acquired native monitor.
        for (ticker in tickers) {
            ticker.addInstructionsWithLabels(0, """
                invoke-static/range {p0 .. p0}, $SMOOTH->frozen(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :stock_marker_tick
                const/4 v0, 0x0
                return v0
            """, ExternalLabel("stock_marker_tick", ticker.implementation!!.instructions.first()))
        }
        markPatched("locationSmoothingPatched")
    }
}
