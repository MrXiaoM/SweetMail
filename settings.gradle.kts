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

include(":v1_7_R4")

include(":paper")
include(":paper:craft-engine")
include(":email")

project(":email").name = "SweetMailNotice"
