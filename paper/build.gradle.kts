plugins {
    java
}
version = "1.0-SNAPSHOT"

@Suppress("VulnerableLibrariesLocal")
dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.4")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
}
