plugins {
    kotlin("jvm")
    id("io.github.baole.konture")
}

dependencies {
    // The unified Konture library
    testImplementation("io.github.baole:konture:0.6.6")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
