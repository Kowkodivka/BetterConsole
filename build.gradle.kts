import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    mavenCentral()
    maven(url = "https://www.jitpack.io")
}

dependencies {
    val mindustryVersion = "v136"
    val jline = "3.21.0"

    compileOnly("com.github.Anuken.Arc:arc-core:$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:server:$mindustryVersion")

    implementation("org.jline:jline-reader:$jline")
    implementation("org.jline:jline-terminal-jna:$jline")
}

tasks.register<Copy>("copy") {
    from("/home/lucin/IdeaProjects/${project.name}/build/libs/${project.name}.jar")
    into("/home/lucin/Shit/server/config/mods")
}

tasks.jar {
    archiveFileName.set("${project.name}.jar")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }){
        exclude("**/META-INF/*.SF")
        exclude("**/META-INF/*.DSA")
        exclude("**/META-INF/*.RSA")
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    from(resources.toString()) {
        include("plugin.json")
    }

    finalizedBy(tasks.getByName("copy"))
}