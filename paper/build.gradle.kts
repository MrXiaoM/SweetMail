plugins {
    java
}

allprojects {
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

java {
    disableAutoTargetJvm()
}

@Suppress("VulnerableLibrariesLocal")
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-chat:1.21-R0.5-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.25.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.25.0")
}
