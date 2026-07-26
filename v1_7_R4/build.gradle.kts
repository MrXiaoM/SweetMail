import java.util.Locale

plugins {
    java
}

repositories {
    if (Locale.getDefault().country == "CN") {
        maven("https://mirrors.huaweicloud.com/repository/maven/")
    }
    mavenCentral()
    maven("https://mvn.lumine.io/repository/maven/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-1.7.10:dev@8.jar")
}
