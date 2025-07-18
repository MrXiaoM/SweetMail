import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `maven-publish`
    id("top.mrxiaom.shadow")
}

var isRelease = gradle.startParameter.taskNames.run {
    contains("release") || contains("publishToMavenLocal")
}
val targetJavaVersion = 8
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
    withJavadocJar()
}

group = "top.mrxiaom"
version = "1.0.4"

allprojects {
    apply(plugin="java")
    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://mvn.lumine.io/repository/maven/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.helpch.at/releases/")
        maven("https://jitpack.io")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://libraries.minecraft.net/")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}

val shadowLink = configurations.create("shadowLink")
val modern = configurations.create("modern")
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

    compileOnly("com.mojang:authlib:2.1.28")

    // MythicMobs 4 and 5
    compileOnly("io.lumine:Mythic-Dist:4.13.0")
    compileOnly("io.lumine:Mythic:5.6.2")
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")

    compileOnly(files("gradle/wrapper/stub-rt.jar")) // sun.misc.Unsafe

    // Shadow Dependency
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.slf4j:slf4j-nop:2.0.16")
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("net.kyori:adventure-api:4.22.0")
    implementation("net.kyori:adventure-platform-bukkit:4.4.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.22.0")
    implementation("net.kyori:adventure-text-minimessage:4.22.0")
    implementation("de.tr7zw:item-nbt-api:2.15.2-SNAPSHOT")
    implementation("com.github.technicallycoded:FoliaLib:0.4.4")
    implementation(project(":paper"))
    "shadowLink"(project(":paper:craft-engine"))
    "modern"(project(":paper:modern"))
}

tasks {
    val relocations = mapOf(
        "org.intellij.lang.annotations" to "annotations.intellij",
        "org.jetbrains.annotations" to "annotations.jetbrains",
        "de.tr7zw.changeme.nbtapi" to "nbtapi",
        "com.zaxxer.hikari" to "hikari",
        "org.slf4j" to "slf4j",
        "com.tcoded.folialib" to "folialib",
    )
    val shadowLegacy = configureShadow("shadowLegacy") {
        if (isRelease) {
            archiveClassifier.set("legacy")
        } else {
            archiveClassifier.set("legacy-unstable")
        }
        mapOf(
            "net.kyori" to "kyori",
            "com.google.gson" to "gson",
        ).plus(relocations).forEach { (original, target) ->
            relocate(original, "top.mrxiaom.sweetmail.utils.$target")
        }
        exclude("plugin.modern.yml")
        rename {
            if (it == "plugin.legacy.yml") "plugin.yml" else it
        }
        ignoreRelocations("top/mrxiaom/sweetmail/utils/inventory/PaperInventoryFactory.class")
        ignoreRelocations("top/mrxiaom/sweetmail/utils/items/CraftEngineProviderImpl.class")
    }
    val shadowModern = configureShadow("shadowModern") {
        if (isRelease) {
            archiveClassifier.set("modern")
        } else {
            archiveClassifier.set("modern-unstable")
        }
        configurations.add(modern)
        dependencies {
            exclude { it.moduleGroup == "net.kyori" }
            exclude(dependency("com.google.code.gson:gson"))
        }
        exclude("plugin.legacy.yml")
        rename {
            if (it == "plugin.modern.yml") "plugin.yml" else it
        }
        relocations.forEach { (original, target) ->
            relocate(original, "top.mrxiaom.sweetmail.utils.$target")
        }
        ignoreRelocations("top/mrxiaom/sweetmail/utils/items/CraftEngineProviderImpl.class")
    }
    create("release")
    build {
        dependsOn(shadowLegacy)
        dependsOn(shadowModern)
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("LICENSE")
        from(sourceSets.main.get().resources.srcDirs) {
            expand("version" to if (isRelease) version else ("$version-unstable"))
            include("plugin.yml")
            include("plugin.modern.yml")
        }
    }
    javadoc {
        (options as? StandardJavadocDocletOptions)?.apply {
            links("https://hub.spigotmc.org/javadocs/spigot/")
            links("https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/")

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
            from(components.getByName("java"))
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
}
fun TaskContainer.configureShadow(
    name: String,
    block: ShadowJar.() -> Unit
): ShadowJar = create<ShadowJar>(name) {
    group = "shadow"
    manifest.inheritFrom(project.tasks.jar.get().manifest)
    from(project.sourceSets.main.get().output)
    configurations.add(
        project.configurations.findByName("runtimeClasspath") ?: project.configurations.findByName("runtime")
    )
    configurations.add(shadowLink)
    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
    dependencies {
        exclude(dependency(project.dependencies.gradleApi()))
    }
    destinationDirectory.set(project.file("out"))

    block()
}
