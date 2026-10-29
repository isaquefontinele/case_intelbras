plugins {
    alias(libs.plugins.devicemanager.android.library)
    alias(libs.plugins.devicemanager.hilt)
}

android {
    namespace = "com.isaquefontinele.devicemanager.core.data"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.robolectric)
}
