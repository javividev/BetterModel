import io.papermc.paperweight.tasks.JavaLauncherTaskBase
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.convention.paperweight)
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.release = 21
    }
    compileKotlin {
        compilerOptions.jvmTarget = JvmTarget.JVM_21
    }
    named("paperweightUserdevSetup") {
        (this as JavaLauncherTaskBase).launcher.set(
            project.extensions.getByType(JavaToolchainService::class.java).launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        )
    }
}
