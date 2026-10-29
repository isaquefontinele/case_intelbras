plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint) apply false
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    source.setFrom(
        files(
            subprojects.flatMap { listOf("${it.projectDir}/src/main/kotlin", "${it.projectDir}/src/test/kotlin") },
        ),
    )
    parallel = true
}

// detekt 1.23.x embeds Kotlin 2.0; keep its classpath isolated from the project's Kotlin version.
configurations.matching { it.name == "detekt" }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") useVersion("2.0.21")
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
