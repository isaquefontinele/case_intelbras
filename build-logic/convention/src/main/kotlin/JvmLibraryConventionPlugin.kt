import com.isaquefontinele.devicemanager.JAVA_VERSION
import com.isaquefontinele.devicemanager.configureKotlinJvmTarget
import com.isaquefontinele.devicemanager.libs
import com.isaquefontinele.devicemanager.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
        }
        configureKotlinJvmTarget()

        dependencies {
            add("testImplementation", libs.library("junit"))
        }
    }
}
