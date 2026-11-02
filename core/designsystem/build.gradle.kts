plugins {
    alias(libs.plugins.devicemanager.android.compose)
}

android {
    namespace = "com.isaquefontinele.devicemanager.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
}
