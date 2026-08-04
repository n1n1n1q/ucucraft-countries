plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = project.property("group") as String
version = project.property("version") as String

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.mikeprimm.com/")
}

val titlesPlugin: Configuration by configurations.creating

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.87-stable")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("us.dynmap:dynmap-api:3.4-beta-3@jar")
    compileOnly("com.ucucraft:ucucraft-titles:1.0.0")
    titlesPlugin("com.ucucraft:ucucraft-titles:1.0.0")
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    runServer {
        minecraftVersion("26.2")
        pluginJars.from(titlesPlugin)
    }
}
