plugins {
    alias(libs.plugins.devicemanager.android.feature)
}

android {
    namespace = "com.isaquefontinele.devicemanager.feature.dashboard"
}

dependencies {
    implementation(project(":core:insights"))
}
