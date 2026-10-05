group = "org.ungoogled"

// Publish the same no-Black selection accepted in the local candidate.
// Keep the upstream source for provenance; do not offer it in this release catalog.
kotlin.sourceSets.named("main") {
    kotlin.exclude("org/ungoogled/patches/maps/ui/black/BlackThemePatch.kt")
}

patches {
    about {
        name = "Dvredin Patches"
        description = "Personal fork of bearinmind Maps patches with a system Cronet fallback."
        source = "https://github.com/Dvredin/dvredin-patches"
        author = "Dvredin; Maps patches by bearinmindcat"
        contact = "https://github.com/Dvredin"
        website = "https://github.com/Dvredin/dvredin-patches"
        license = "GPLv3"
    }
}

// Runtime support for metadata generation only; never bundled into the app.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Generate the catalog from actual compiled patches"
        dependsOn(build)
        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }
    publish {
        dependsOn("generatePatchesList")
    }
}
