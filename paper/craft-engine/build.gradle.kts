plugins {
    java
}

val targetJavaVersion = 21

repositories {
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("net.momirealms:craft-engine-core:0.0.52")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.52")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}
