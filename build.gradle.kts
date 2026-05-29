import java.util.Locale

plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.3.0"
    id("com.github.gmazzo.buildconfig") version "5.6.7"
}
buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.23")
}

var isRelease = gradle.startParameter.taskNames.run {
    contains("release") || contains("publishToMavenLocal")
}
val targetJavaVersion = 8
java {
    disableAutoTargetJvm()
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
    withJavadocJar()
}

group = "top.mrxiaom"
version = "1.1.9"

allprojects {
    apply(plugin="java")
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

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}

val base = top.mrxiaom.gradle.LibraryHelper(project)

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
    compileOnly("net.momirealms:craft-engine-core:26.5.3")
    compileOnly("net.momirealms:craft-engine-bukkit:26.5.3")

    compileOnly(files("gradle/wrapper/stub-rt.jar")) // sun.misc.Unsafe
    compileOnly(base.depend.annotations)

    base.library("org.slf4j:slf4j-api:2.0.16")
    base.library(base.depend.HikariCP)
    base.library("net.kyori:adventure-api:4.25.0")
    base.library("net.kyori:adventure-text-serializer-gson:4.25.0")
    base.library("net.kyori:adventure-text-minimessage:4.25.0")

    // Shadow Dependency
    implementation(base.depend.nbtapi)
    implementation("com.github.technicallycoded:FoliaLib:0.4.4") { isTransitive = false }
    implementation(base.depend.EvalEx)
    implementation(base.resolver.lite)
    implementation(project(":v1_7_R4"))
    implementation(project(":paper"))
}

buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.sweetmail")

    base.doResolveLibraries()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("java.time.Instant", "BUILD_TIME", "java.time.Instant.ofEpochSecond(${System.currentTimeMillis() / 1000L}L)")

    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
}

tasks {
    shadowJar {
        configurations.add(project.configurations["runtimeClasspath"])
        mapOf(
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
            "com.tcoded.folialib" to "folialib",
            "com.ezylang.evalex" to "evalex",
            "top.mrxiaom.pluginbase.resolver" to "resolver",
        ).forEach { (original, target) ->
            relocate(original, "top.mrxiaom.sweetmail.utils.$target")
        }
    }
    this.register("release")
    val jarName = "${project.name}-$version-plugin.jar"
    val copyTask = register<Copy>("copyBuildArtifact") {
        dependsOn(shadowJar)
        from(shadowJar.get().outputs)
        rename { jarName }
        into(rootProject.file("out"))
    }
    build {
        dependsOn(copyTask)
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("LICENSE")
        from(sourceSets.main.get().resources.srcDirs) {
            expand(
                "version" to if (isRelease) version else ("$version-unstable"),
                "libraries" to base.addedLibrariesYAML.joinToString("\n"),
            )
            include("plugin.yml")
        }
    }
    javadoc {
        (options as? StandardJavadocDocletOptions)?.apply {
            links("https://hub.spigotmc.org/javadocs/spigot/")

            locale("zh_CN")
            charset("UTF-8")
            encoding("UTF-8")
            docEncoding("UTF-8")
            addBooleanOption("keywords", true)
            addBooleanOption("Xdoclint:none", true)

            val currentJavaVersion = JavaVersion.current()
            if (currentJavaVersion > JavaVersion.VERSION_1_9) {
                addBooleanOption("html5", true)
            }
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("mavenRelease") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            artifact(tasks["shadowJar"]).classifier = null
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}
