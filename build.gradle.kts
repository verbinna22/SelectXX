plugins {
    application
    kotlin("jvm") version "2.1.21"
}

group = "ru.yandex.mylogininya"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("ru.yandex.mylogininya.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}