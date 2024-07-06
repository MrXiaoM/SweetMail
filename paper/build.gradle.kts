plugins {
    java
}
version = "1.0-SNAPSHOT"
dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly(project(":utils"))
}
