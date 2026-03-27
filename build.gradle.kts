plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.3.0"
    id("com.github.gmazzo.buildconfig") version "5.6.7"
}
buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.13")
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
version = "1.1.7"

allprojects {
    apply(plugin="java")
    repositories {
        mavenLocal()
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
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}

val shadowLink = configurations.create("shadowLink")
val base = top.mrxiaom.gradle.LibraryHelper(project)

@Suppress("VulnerableLibrariesLocal")
dependencies {
    // Minecraft
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")

    // API
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")

    // Dependency Plugins
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MascusJeoraly:LanguageUtils:1.9")
    compileOnly("com.github.dmulloy2:ProtocolLib:5.3.0")
    compileOnly("pers.neige.neigeitems:NeigeItems:1.21.96")

    compileOnly("com.mojang:authlib:2.1.28")

    // MythicMobs 4 and 5
    compileOnly("io.lumine:Mythic-Dist:4.13.0")
    compileOnly("io.lumine:Mythic:5.6.2")
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")

    // CraftEngine
    compileOnly("net.momirealms:craft-engine-core:0.0.67")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.67")

    compileOnly(files("gradle/wrapper/stub-rt.jar")) // sun.misc.Unsafe
    compileOnly("org.jetbrains:annotations:24.0.0")

    base.library("org.slf4j:slf4j-api:2.0.16")
    base.library("com.zaxxer:HikariCP:4.0.3")
    base.library("net.kyori:adventure-api:4.22.0")
    base.library("net.kyori:adventure-platform-bukkit:4.4.0")
    base.library("net.kyori:adventure-text-serializer-gson:4.22.0")
    base.library("net.kyori:adventure-text-serializer-plain:4.22.0")
    base.library("net.kyori:adventure-text-minimessage:4.22.0")

    // Shadow Dependency
    implementation("de.tr7zw:item-nbt-api:2.15.5")
    implementation("com.github.technicallycoded:FoliaLib:0.4.4") { isTransitive = false }
    implementation("top.mrxiaom:EvalEx-j8:3.4.0")
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
        configurations.add(shadowLink)
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
    val copyTask = this.register<Copy>("copyBuildArtifact") {
        dependsOn(shadowJar)
        from(shadowJar.get().outputs)
        rename { "${project.name}-$version-plugin.jar" }
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
                "libraries" to base.addedLibraries.joinToString("\"\n  - \""),
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
