import java.util.Locale

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

allprojects {
    group = "top.mrxiaom"
    version = "1.0.0"

    apply(plugin="java")
    repositories {
        mavenCentral()
        if (Locale.getDefault().country == "CN") {
            maven("https://maven.fastmirror.net/repositories/minecraft/")
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://mvn.lumine.io/repository/maven/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://jitpack.io")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://oss.sonatype.org/content/groups/public/")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}

@Suppress("VulnerableLibrariesLocal")
dependencies {
    // Minecraft
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")

    // API
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")

    // Dependency Plugins
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.black_ixx:playerpoints:3.2.7")
    compileOnly("com.github.MascusJeoraly:LanguageUtils:1.9")

    compileOnly("com.mojang:authlib:2.1.28")

    // MythicMobs 4 and 5
    compileOnly("io.lumine:Mythic-Dist:4.13.0")
    compileOnly("io.lumine:Mythic:5.6.2") // 不要再升级 Mythic5 了，再升级就要 java 21 才能编译了
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")

    compileOnly(files("gradle/wrapper/stub-rt.jar")) // sun.misc.Unsafe

    // Shadow Dependency
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.slf4j:slf4j-nop:2.0.16")
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("de.tr7zw:item-nbt-api:2.14.2-SNAPSHOT")
    implementation("com.github.technicallycoded:FoliaLib:0.4.3")
    implementation(project(":paper"))
}

tasks {
    shadowJar {
        mapOf(
            "org.intellij.lang.annotations" to "annotations.intellij",
            "org.jetbrains.annotations" to "annotations.jetbrains",
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
            "com.zaxxer.hikari" to "hikari",
            "org.slf4j" to "slf4j",
            "net.kyori" to "kyori",
            "com.tcoded.folialib" to "folialib",
        ).forEach { (original, target) ->
            relocate(original, "top.mrxiaom.sweetmail.utils.$target")
        }
        ignoreRelocations("top/mrxiaom/sweetmail/utils/inventory/PaperInventoryFactory.class")
    }
    create("release")
    withType<Jar> {
        if (isRelease) {
            archiveClassifier.set("")
        } else {
            archiveClassifier.set("unstable")
        }
    }
    jar {
        archiveBaseName.set("SweetMail-api")
    }
    build {
        dependsOn(shadowJar)
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("LICENSE")
        from(sourceSets.main.get().resources.srcDirs) {
            expand("version" to if (isRelease) version else ("$version-unstable"))
            include("plugin.yml")
        }
    }
    javadoc {
        (options as? StandardJavadocDocletOptions)?.apply {
            links("https://docs.oracle.com/javase/8/docs/api")
            links("https://hub.spigotmc.org/javadocs/spigot/")
            links("https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/")

            locale("zh_CN")
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
