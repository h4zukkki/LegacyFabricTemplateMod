import groovy.json.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.fabric.loom.legacy)
    `maven-publish`
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

loom {
    runConfigs {
        // If your mod not client-side only, remove this line.
        remove(getByName("server"))

        "client" {
            /*
            I also recommend you to create a separate run directory if you develop multi mods,
            and want to share texture packs or something.

            runDir("path-to-your-run-directory/${libs.versions.minecraft.get()}")
            */
            log4jConfigs.from(file("log4j2.xml"))
        }
    }
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

dependencies {
    minecraft(libs.minecraft)
    mappings(legacy.yarn(libs.versions.minecraft.get(), libs.versions.yarn.get()))

    modImplementation(libs.bundles.fabric)
}

tasks.processResources {

    val fabricJsonFile =
        sourceSets["main"].resources.sourceDirectories
            .mapNotNull { dir ->
                File(dir, "fabric.mod.json").takeIf { fabricJsonFile -> fabricJsonFile.exists() }
            }
            .single()
    inputs.file(fabricJsonFile)

    val fabricModJson = JsonSlurper().parse(fabricJsonFile) as Map<*, *>

    @Suppress("UNCHECKED_CAST")
    val mixinsCollecting =
        fabricModJson["mixins"] as List<String>

    val mixinsCollected =
        buildMap {
            mixinsCollecting
                .forEach { mixinName ->

                    val mixinJsonFile = sourceSets["main"].resources.sourceDirectories
                        .mapNotNull { dir ->
                            File(dir, mixinName).takeIf { mixinJsonFile -> mixinJsonFile.exists() }
                        }
                        .single()

                    val mixinJson = JsonSlurper().parse(mixinJsonFile) as Map<*, *>

                    val mixinPackage = mixinJson["package"] as String

                    val mixinDirectory = sourceSets["main"].java.sourceDirectories
                        .mapNotNull { dir ->
                            File(dir, mixinPackage.replace(".", File.separator))
                                .takeIf { mixinDirectory -> mixinDirectory.exists() && mixinDirectory.isDirectory }
                        }
                        .single()
                    inputs.dir(mixinDirectory)

                    val mixinElements = mixinDirectory
                        .walkTopDown()
                        .filter { src -> src.isFile && src.name.contains("Mixin") && src.extension == "java" }
                        .filter { src -> src.readText().contains("@Mixin") }
                        .map { probablyMixin ->
                            probablyMixin.relativeTo(mixinDirectory).path
                                .removeSuffix(".java")
                                .replace(File.separator, ".")
                        }
                        .toList()

                    put(mixinName, mixinElements)
                }
        }

    val modProperties = mapOf(
        "mod_version" to project.version,

        "minecraft_version" to libs.versions.minecraft.get(),
        "fabricLoader_version" to libs.versions.fabric.loader.get(),
        "legacyFabricApi_version" to libs.versions.fabric.api.get(),
        "fabricLanguageKotlin_version" to libs.versions.fabric.kotlin.get(),
    )

    inputs.properties(modProperties)
    filesMatching("fabric.mod.json") {
        expand(modProperties)
    }

    doLast {
        mixinsCollecting
            .forEach { mixinName ->

                val mixinJsonFile = File(destinationDir, mixinName)

                val mixinJson = JsonSlurper().parse(mixinJsonFile) as Map<*, *>

                val inserted = mixinJson + mapOf("mixins" to mixinsCollected[mixinName])
                mixinJsonFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(inserted)))
                println("Updated mixin: $mixinName")
            }
    }
}

java {
    withSourcesJar()

    JavaVersion.toVersion(libs.versions.java.get())
        .also(::setSourceCompatibility)
        .also(::setTargetCompatibility)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    options.release = libs.versions.java.get().toInt()
    /*
    If you are using modern version of JDK, compiling to target 1.8
    is obsolete and will be removed in a future release,
    this's only for suppressing the warnings.
     */
    options.compilerArgs.add("-Xlint:-options")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("1.${libs.versions.java.get()}")
    }
}

tasks.jar {
    inputs.property("archivesName", project.name)
    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

// configure the maven publication
publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
