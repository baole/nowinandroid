plugins {
    kotlin("jvm")
    id("io.github.baole.koarchtest")
}

dependencies {
    // The unified KoArchTest library
    testImplementation("io.github.baole.koarchtest:library:0.6.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
