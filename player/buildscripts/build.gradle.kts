plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "4.0.2"
}

sourceSets.main.get().java.srcDir("src")

application {
    mainClassName = "sc.playerYEAR.Starter"
}

repositories {
    jcenter()
    maven("http://dist.wso2.org/maven2")
    maven("https://jitpack.io")
}

dependencies {
    if(gradle.startParameter.isOffline) {
        implementation(fileTree("lib"))
    } else {
        implementation("com.github.CAU-Kiel-Tech-Inf.socha", "GAME", "VERSION")
    }
}

tasks.shadowJar {
    archiveBaseName.set("GAME_client")
    archiveClassifier.set("")
    destinationDirectory.set(rootDir)
}
