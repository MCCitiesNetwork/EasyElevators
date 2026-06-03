plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

group = "com.minecraftcitiesnetwork"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    implementation("org.spongepowered:configurate-yaml:4.2.0")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching(listOf("plugin.yml")) {
        expand("version" to project.version.toString())
    }
}

tasks.jar {
    archiveBaseName.set("EasyElevators")
}

tasks.shadowJar {
    archiveBaseName.set("EasyElevators")
    archiveClassifier.set("")
    relocate("org.spongepowered", "com.minecraftcitiesnetwork.easyelevators.libs.spongepowered")
    relocate("org.yaml", "com.minecraftcitiesnetwork.easyelevators.libs.yaml")
    relocate("io.leangen.geantyref", "com.minecraftcitiesnetwork.easyelevators.libs.geantyref")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
