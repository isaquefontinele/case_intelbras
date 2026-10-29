import com.android.build.api.dsl.LibraryExtension
import com.isaquefontinele.devicemanager.JAVA_VERSION
import com.isaquefontinele.devicemanager.configureKotlinJvmTarget
import com.isaquefontinele.devicemanager.libs
import com.isaquefontinele.devicemanager.library
import com.isaquefontinele.devicemanager.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            compileSdk = libs.version("compileSdk").toInt()
            defaultConfig {
                minSdk = libs.version("minSdk").toInt()
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
            testOptions {
                unitTests.isReturnDefaultValues = true
                unitTests.isIncludeAndroidResources = true
            }
        }
        configureKotlinJvmTarget()

        dependencies {
            add("testImplementation", libs.library("junit"))
            add("testImplementation", libs.library("mockk"))
            add("testImplementation", libs.library("turbine"))
            add("testImplementation", libs.library("kotlinx-coroutines-test"))
        }
    }
}
