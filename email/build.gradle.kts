plugins {
    java
}

group = "top.mrxiaom"
version = "1.0.0"

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")

    // Dependency Plugins
    compileOnly(rootProject)
    compileOnly("com.github.MrXiaoM:Emailer:v2.1.8")

    implementation("org.jetbrains:annotations:24.0.0")
}

tasks {
    jar {
        archiveBaseName.set("SweetMailNotice")
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("LICENSE")
        from(sourceSets.main.get().resources.srcDirs) {
            expand("version" to version)
            include("plugin.yml")
        }
    }
}
