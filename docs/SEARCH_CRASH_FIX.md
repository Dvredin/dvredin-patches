# GrapheneOS search compatibility

The **Fix GrapheneOS search crash** patch contains a specific framework error
encountered while Maps opens search and checks optional native power-saving support.
The original probe reads `com.android.systemui/config_minmode_enabled`; the observed
GrapheneOS Google Play asset hook raises a NullPointerException during that load.

The exact-gated patch preserves the original probe instructions, normal true/false
results, and existing missing-package/resource handlers. An appended NPE handler
returns unavailable only for the observed adjacent framework hook/caller frames.
Unrelated null-pointer and security failures are not hidden. No global resource
loader, native renderer, application security setting or permission is changed.

Verification includes eight focused unit tests and a separate ART instrument that
invokes the actual generated Maps method. Controlled null-prefix injection escapes
from the old probe and is contained by the fixed one; valid results and unrelated
exceptions retain their behavior. Both modified-body and handler-only input
mutations are rejected by the real patcher. Ordinary search opening, submission,
cold launch, Recents reopen and in-place data retention passed on a dedicated Pixel.
The maintainer then reported the fixed candidate working on the primary GrapheneOS
phone and explicitly approved stable publication.

This is not a claim that every framework failure or every GrapheneOS version is
covered. Satellite startup imagery is a separate unresolved issue. No diagnostics
or test-only hook classes are included in the production source bundle.

Build the supported original APK with this patch enabled and retain the existing
package and Manager signing key when updating. Do not uninstall or clear Maps data.

See [ART regression source](../tests/device/minmode/README.md),
[publication status](RELEASE_STATUS.md) and [verification](VERIFICATION.md).
