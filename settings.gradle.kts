rootProject.name = "SweetMail"

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("top.mrxiaom:shadow:7.1.3")
    }
}

include(":paper")
include(":paper:craft-engine")
include(":paper:modern")
include(":email")

project(":email").name = "SweetMailNotice"
