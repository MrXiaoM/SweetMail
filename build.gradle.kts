plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "top.mrxiaom"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.fastmirror.net/repositories/minecraft")
    maven("https://mvn.lumine.io/repository/maven/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    // Minecraft
    val mcVersion = "1.19.4"
    compileOnly("org.spigotmc:spigot-api:$mcVersion-R0.1-SNAPSHOT")

    // API
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.6.1")
    compileOnly("org.xerial:sqlite-jdbc:3.41.2.2")

    // Dependency Plugins
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("org.black_ixx:playerpoints:3.2.5")

    // MythicMobs 4 and 5
    compileOnly("io.lumine:Mythic-Dist:4.13.0")
    compileOnly("io.lumine:Mythic:5.4.1")
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")

    // Shadow Dependency
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.jetbrains:annotations:19.0.0")
}

val targetJavaVersion = 8
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        val p = "top.mrxiaom.sweetmail.utils"
        relocate("org.intellij.lang.annotations", "$p.annotations.intellij")
        relocate("org.jetbrains.annotations", "$p.annotations.jetbrains")
        relocate("com.zaxxer.hikari", "$p.hikari")
    }
    build {
        dependsOn(shadowJar)
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf(
                "version" to version,
            ))
            include("plugin.yml")
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
