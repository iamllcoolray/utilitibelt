import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.1.21"
  id("org.jetbrains.intellij.platform") version "2.15.0"
  id("org.jetbrains.changelog") version "2.2.0"
}

group = "com.hibiscusgames"
version = "1.5.0"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    intellijIdea("2026.1")
    bundledPlugin("com.intellij.java")
    testFramework(TestFrameworkType.Platform)
  }
  implementation("org.freemarker:freemarker:2.3.32")
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "241"
      untilBuild = "261.*"
    }

    val cl = project.changelog
    changeNotes = providers.provider {
      with(cl) {
        renderItem(
          (getOrNull(project.version.toString()) ?: getUnreleased())
            .withHeader(false)
            .withEmptySections(false),
          Changelog.OutputType.HTML,
        )
      }
    }
  }
}

changelog {
  groups.set(listOf("NEW", "UPDATE", "FIXED"))
  versionPrefix = ""
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "23"
    targetCompatibility = "23"
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_23)
    }
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(providers.gradleProperty("publish.token"))
  }
}
