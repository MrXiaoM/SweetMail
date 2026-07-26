import java.util.Locale

plugins {
    java
}

repositories {
    if (Locale.getDefault().country == "CN") {
        maven("https://mirrors.huaweicloud.com/repository/maven/")
    }
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
}

group = "top.mrxiaom"
version = "1.0.0"

@Suppress("VulnerableLibrariesLocal")
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")

    // Permission API
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("net.luckperms:api:5.4")

    // Dependency Plugins
    compileOnly(rootProject)
    compileOnly("com.github.MrXiaoM:Emailer:v2.1.8")

    implementation("org.jetbrains:annotations:24.0.0")
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("LICENSE")
        from(sourceSets.main.get().resources.srcDirs) {
            expand("version" to version)
            include("plugin.yml")
        }
    }
}
