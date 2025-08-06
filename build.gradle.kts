plugins {
    id("fabric-loom") version "1.7.3"
    id("maven-publish")
}

base {
    archivesName = "spawnerfarm"
}

version = "1.0.0"
group = "com.spawnerfarm"

repositories {
    // These repositories are required for Meteor Client and Baritone
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // Minecraft and Fabric
    minecraft("com.mojang:minecraft:1.21.1")
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.0")
    
    // Fabric API
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.102.0+1.21.1")
    
    // Meteor Client
    modImplementation("meteordevelopment:meteor-client:1.21.1-SNAPSHOT")
    
    // Baritone (available as part of Meteor)
    modImplementation("meteordevelopment:baritone:1.21.1-SNAPSHOT")
}

loom {
    accessWidenerPath = file("src/main/resources/spawnerfarm.accesswidener")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName.get()}" }
        }
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

