plugins {
    java
    id("java-gradle-plugin")
}

group = "net.iceyleagons"
version = "1.0.0"

repositories {
    mavenCentral()
}

val extraLibs = configurations.create("extraLibs")

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")

    extraLibs("com.amihaiemil.web:eo-yaml:5.2.2")
    configurations.implementation.get().extendsFrom(extraLibs)
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.jar {
    from(extraLibs.map { if (it.isDirectory) it else zipTree(it) })
}

gradlePlugin {
    plugins {
        create("net.iceyleagons.icicle-gradle") {
            id = "net.iceyleagons.icicle-gradle"
            implementationClass = "net.iceyleagons.gradle.IciclePlugin"
        }
    }
}