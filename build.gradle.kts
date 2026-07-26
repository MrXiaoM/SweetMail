import top.mrxiaom.gradle.LibraryHelper
import java.util.Locale

plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.3.0"
    id("com.github.gmazzo.buildconfig") version "5.6.7"
}

buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.29")
}
val base = LibraryHelper(project)

val targetJavaVersion = 8
val pluginBaseModules = base.modules.run { listOf(library, paper, gui, actions, l10n, misc) }
val shadowGroup = "top.mrxiaom.sweet.mail.libs"

println("Group:   $group")
println("Version: $version")

repositories {
    if (Locale.getDefault().country == "CN") {
        maven("https://mirrors.huaweicloud.com/repository/maven/")
    }
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://mvn.lumine.io/repository/maven/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://libraries.minecraft.net/")
    maven("https://r.irepo.space/maven/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://maven.devs.beer/")
}

@Suppress("VulnerableLibrariesLocal")
dependencies {
    // Minecraft
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:2.1.28")

    // API
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("dev.lone:api-itemsadder:4.0.10")

    // Dependency Plugins
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("com.github.MascusJeoraly:LanguageUtils:1.9")
    compileOnly("com.github.dmulloy2:ProtocolLib:5.3.0")
    compileOnly("pers.neige.neigeitems:NeigeItems:1.21.145")

    // MythicMobs 4 and 5
    compileOnly("io.lumine:Mythic-Dist:4.13.0")
    compileOnly("io.lumine:Mythic:5.6.2")
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")

    // CraftEngine
    compileOnly("net.momirealms:craft-engine-core:26.7.4")
    compileOnly("net.momirealms:craft-engine-bukkit:26.7.4")

    compileOnly(files("gradle/wrapper/stub-rt.jar")) // sun.misc.Unsafe
    compileOnly(base.depend.annotations)

    base.library("org.slf4j:slf4j-api:2.0.16")
    base.library(base.depend.HikariCP)
    base.library(LibraryHelper.adventure("4.25.0"))
    base.collectPluginHolders()

    // Shadow Dependency
    implementation(base.depend.nbtapi)
    implementation(base.depend.EvalEx)
    implementation("com.github.technicallycoded:FoliaLib:0.4.4") { isTransitive = false }
    for (artifact in pluginBaseModules) {
        implementation(artifact)
    }
    implementation(base.resolver.lite)
    implementation(project(":v1_7_R4"))
    implementation(project(":paper"))
}

configurations.forEach {
    // 去除 jspecify，免得总是跟 jetbrains annotations 打架
    it.exclude(group="org.jspecify", module="jspecify")
}

buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.sweet.mail")

    base.doResolveLibraries()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("java.time.Instant", "BUILD_TIME", "java.time.Instant.ofEpochSecond(${System.currentTimeMillis() / 1000L}L)")

    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
}

LibraryHelper.initJava(project, base, targetJavaVersion, true)
LibraryHelper.initPublishing(project)

tasks {
    shadowJar {
        configurations.add(project.configurations.runtimeClasspath.get())
        mapOf(
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
            "com.tcoded.folialib" to "folialib",
            "com.ezylang.evalex" to "evalex",
            "top.mrxiaom.pluginbase.resolver" to "resolver",
        ).forEach { (original, target) ->
            relocate(original, "top.mrxiaom.sweetmail.utils.$target")
        }
    }
}
