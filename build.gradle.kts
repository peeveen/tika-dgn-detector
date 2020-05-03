plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("myLibrary") {
            pom {
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
            groupId = "com.github.peeveen"
            artifactId = "tika-dgn-detector"
            version = "1.0"
            from(components["java"])
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.tika:tika-core:1.24.1")
    implementation("org.apache.poi:poi:4.1.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
}