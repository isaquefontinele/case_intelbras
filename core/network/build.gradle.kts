import java.util.Properties

plugins {
    alias(libs.plugins.devicemanager.android.library)
    alias(libs.plugins.devicemanager.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val content = providers.fileContents(rootProject.layout.projectDirectory.file("local.properties")).asText.orNull
    if (content != null) load(content.reader())
}

fun secret(propertyKey: String, envKey: String, default: String = ""): String = localProperties.getProperty(propertyKey)
    ?: providers.environmentVariable(envKey).orNull
    ?: default

android {
    namespace = "com.isaquefontinele.devicemanager.core.network"
    buildFeatures.buildConfig = true
    defaultConfig {
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${secret("api.baseUrl", "API_BASE_URL", "http://squadapps.ddns-intelbras.com.br:37025/")}\"",
        )
        buildConfigField("String", "API_TOKEN", "\"${secret("api.token", "API_TOKEN")}\"")
    }
}

dependencies {
    api(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)

    testImplementation(libs.okhttp.mockwebserver)
}
