import java.util.*

plugins {
    id("maven-publish")
    id("com.github.hierynomus.license") version "0.16.1"
    id("fabric-loom") version "1.10-SNAPSHOT"

    // https://github.com/ReplayMod/preprocessor
    // https://github.com/Fallen-Breath/preprocessor
    id("com.replaymod.preprocess") version "9d21b334a7"

    // https://github.com/Fallen-Breath/yamlang
    id("me.fallenbreath.yamlang") version "1.4.1"
}

val author = "Crystal0404"
val mixinConfigPath = "template_mod.mixins.json"
val langDir = "assets/template_mod/lang"

val mcVersion = project.property("mcVersion") as Int

repositories {
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "FallenMaven"
        url = uri("https://maven.fallenbreath.me/releases")
    }
}

// https://github.com/FabricMC/fabric-loader/issues/783
configurations {
    modRuntimeOnly {
        exclude(group = "net.fabricmc", module = "fabric-loader")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

//    // [FEATURE] MIXIN_AUDITOR
//    modRuntimeOnly("me.fallenbreath:mixin-auditor:0.1.0")
}

val javaCompatibility = when {
    mcVersion >= 12005 -> {
        JavaVersion.VERSION_21
    }
    mcVersion >= 11800 -> {
        JavaVersion.VERSION_17
    }
    mcVersion >= 11700 -> {
        JavaVersion.VERSION_16
    }
    else -> {
        JavaVersion.VERSION_1_8
    }
}

val mixinCompatibilityLevel = javaCompatibility

loom {
    val commonVmArgs = listOf("-Dmixin.debug.export=true")
    runConfigs.configureEach {
        // to make sure it generates all "Minecraft Client (:subproject_name)" applications
        ideConfigGenerated(true)

        runDir("../../run")
        vmArgs(commonVmArgs)
    }

//    // [FEATURE] MIXIN_AUDITOR
//    runs {
//        val auditVmArgs = listOf(*commonVmArgs.toTypedArray(), "-DmixinAuditor.audit=true")
//        create("serverMixinAudit") {
//            server()
//            vmArgs(auditVmArgs)
//            ideConfigGenerated(false)
//        }
//    }
}

var modVersionSuffix = ""
val artifactVersion = project.property("mod_version") as String
var artifactVersionSuffix = ""
// detect github action environment variables
// https://docs.github.com/en/actions/learn-github-actions/environment-variables#default-environment-variables
if (System.getenv("BUILD_RELEASE") != "true") {
    val buildNumber = System.getenv("BUILD_ID")
    modVersionSuffix += if (buildNumber != null) ("+build.$buildNumber") else "-SNAPSHOT"
    artifactVersionSuffix = "-SNAPSHOT"  // A non-release artifact is always a SNAPSHOT artifact
}
val fullModVersion = "${project.property("mod_version")}" + modVersionSuffix
var fullProjectVersion: String
var fullArtifactVersion: String


// Example version values:
//   project.mod_version     1.0.3                      (the base mod version)
//   modVersionSuffix        +build.88                  (use github action build number if possible)
//   artifactVersionSuffix   -SNAPSHOT
//   fullModVersion          1.0.3+build.88             (the actual mod version to use in the mod)
//   fullProjectVersion      v1.0.3-mc1.15.2+build.88   (in build output jar name)
//   fullArtifactVersion     1.0.3-mc1.15.2-SNAPSHOT    (maven artifact version)

group = "${project.property("maven_group")}"
if (System.getenv("JITPACK") == "true") {
    // move mc version into archivesBaseName, so jitpack will be able to organize archives from multiple subprojects correctly
    base.archivesName = "${project.property("archives_base_name")}-mc${project.property("minecraft_version")}"
    fullProjectVersion = "v${project.property("mod_version")}" + modVersionSuffix
    fullArtifactVersion = artifactVersion + artifactVersionSuffix
} else {
    base.archivesName = "${project.property("archives_base_name")}"
    fullProjectVersion = "v${project.property("mod_version")}-mc${project.property("minecraft_version")}" + modVersionSuffix
    fullArtifactVersion = artifactVersion + "-mc${project.property("minecraft_version")}" + artifactVersionSuffix
}
version = fullProjectVersion

// See https://youtrack.jetbrains.com/issue/IDEA-296490
// if IDEA complains about "Cannot resolve resource filtering of MatchingCopyAction" and you want to know why
tasks.processResources {
    val modId = project.property("mod_id")
    val modName = project.property("mod_name")
    val mcDependency = project.property("minecraft_dependency")
    val version = fullModVersion

    inputs.property("id", modId)
    inputs.property("name", modName)
    inputs.property("version", version)
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        val valueMap = mapOf(
            "id" to modId,
            "name" to modName,
            "minecraft_dependency" to mcDependency,
            "version" to version
        )
        expand(valueMap)
    }

    filesMatching(mixinConfigPath) {
        filter { it.replace("{{COMPATIBILITY_LEVEL}}", "JAVA_${mixinCompatibilityLevel.ordinal + 1}") }
    }
}

// https://github.com/Fallen-Breath/yamlang
yamlang {
    targetSourceSets.set(listOf(sourceSets["main"]))
    inputDir.set(langDir)
}

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    if (javaCompatibility <= JavaVersion.VERSION_1_8) {
        // suppressed "source/target value 8 is obsolete and will be removed in a future release"
        options.compilerArgs.add("-Xlint:-options")
    }
}

java {
    sourceCompatibility = javaCompatibility
    targetCompatibility = javaCompatibility

    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${project.property("archives_base_name")}" }
    }
}

// https://github.com/hierynomus/license-gradle-plugin
license {
    // use "gradle licenseFormat" to apply license headers
    header = rootProject.file("HEADER.txt")
    include("**/*.java")
    skipExistingHeaders = true

    headerDefinitions {
        // ref: https://github.com/mathieucarbou/license-maven-plugin/blob/4c42374bb737378f5022a3a36849d5e23ac326ea/license-maven-plugin/src/main/java/com/mycila/maven/plugin/license/header/HeaderType.java#L48
        // modification: add a newline at the end
        create("SLASHSTAR_STYLE_NEWLINE") {
            firstLine = "/*"
            beforeEachLine = " * "
            endLine = " */" + System.lineSeparator()
            afterEachLine = ""
            skipLinePattern = null
            firstLineDetectionPattern = "(\\s|\\t)*/\\*.*\$"
            lastLineDetectionPattern = ".*\\*/(\\s|\\t)*\$"
            allowBlankLines = false
            isMultiline = true
            padLines = false
        }
    }
    mapping("java", "SLASHSTAR_STYLE_NEWLINE")

    ext {
        set("name", project.property("mod_name") as String)
        set("author", author)
        set("year", Calendar.getInstance().get(Calendar.YEAR).toString())
    }
}
tasks.named("classes").configure {
    dependsOn(tasks.named("licenseFormatMain"))
}
tasks.named("testClasses").configure {
    dependsOn(tasks.named("licenseFormatTest"))
}

publishing {

}