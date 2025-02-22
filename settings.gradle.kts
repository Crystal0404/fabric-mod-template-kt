pluginManagement {
    repositories {
        if (System.getenv("CI") != "true") {
            // If you're not from China, please remove this, it will slow down your downloads
            maven {
                name = "Tencent"
                url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
            }
        }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
