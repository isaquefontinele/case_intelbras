plugins {
    alias(libs.plugins.devicemanager.android.library)
    alias(libs.plugins.devicemanager.hilt)
    alias(libs.plugins.room)
}

android {
    namespace = "com.isaquefontinele.devicemanager.core.database"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    api(project(":core:model"))
    api(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.androidx.test.ext.junit)
}
