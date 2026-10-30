plugins {
    alias(libs.plugins.devicemanager.android.library)
}

android {
    namespace = "com.isaquefontinele.devicemanager.core.testing"
}

// Test doubles shared by the feature modules' unit tests.
dependencies {
    api(project(":core:data"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
