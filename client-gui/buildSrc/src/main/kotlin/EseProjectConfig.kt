import EseTarget.*
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.plugins

enum class EseTarget {
    CUI,
    GUI,
    ALL
}

fun DependencyHandler.implementation(notation: Any) =
    add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, notation)


@EseBuildDsl
inline fun Project.ese(block:EseProjectConfig.() -> Unit) {
    EseProjectConfig.block()
    with(EseProjectConfig) {
        configure()
    }
}

val Project.eseTarget get() = project.properties["targetPlatform"].toString().toEnum<EseTarget>() ?: ALL
val Project.eseMainClass get() = EseProjectConfig.mainClass

@DslMarker
annotation class EseBuildDsl
@EseBuildDsl
 object EseProjectConfig {
    @PublishedApi
    internal var gui = EsePlatform()

    @PublishedApi
    internal var cui = EsePlatform()

    @PublishedApi
    internal var all = EsePlatform()

    lateinit var mainClass: String

     class EsePlatform internal constructor() {
        @PublishedApi
        internal var deps: DependencyHandler.() -> Unit = {}

        @PublishedApi
        internal var func: (EseTarget) -> Unit = {}

        var mainClass: String? = null

        fun dependencies(deps: DependencyHandler.() -> Unit) {
            this.deps = deps
        }

        fun selected(block: (EseTarget) -> Unit) {
            func = block
        }

        internal fun select(dependencyHandler: DependencyHandler, target: EseTarget) {
            deps(dependencyHandler)
            func(target)
            EseProjectConfig.mainClass = mainClass ?: throw IllegalStateException("mainClassを設定してください")
        }
    }

    inline fun gui(block: EsePlatform.() -> Unit) {
        gui.block()
    }

    inline fun cui(block: EsePlatform.() -> Unit) {
        cui.block()
    }

    inline fun all(block: EsePlatform.() -> Unit) {
        all.block()
    }

    @PublishedApi
    internal fun Project.configure() {
        val target = eseTarget
        println("targetPlatform is $target")

        dependencies {

            when (target) {
                CUI -> cui.select(this, target)
                GUI -> gui.select(this, target)
                ALL -> {
                    cui.select(this, target)
                    gui.select(this, target)
                    all.select(this, target)
                }
            }
        }

    }

}




