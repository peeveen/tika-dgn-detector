plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.tika:tika-core:1.24.1")
    implementation("org.apache.poi:poi:4.1.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
}

val codingCredentialsFilePath: String? = System.getenv("CODING_CREDENTIALS_FILE_PATH")
val codingCredentialsFile =
    if (codingCredentialsFilePath != null)
        File(codingCredentialsFilePath)
    else
        null
val codingCredentials =
    if (codingCredentialsFile?.exists() == true)
        codingCredentialsFile
            .readLines()
            .filter { it.isNotBlank() }
            .map { it.trim().split("=") }
            .filter { it.size > 1 }
            .map { Pair(it[0].trim(), it.subList(1, it.size).joinToString(separator = "") { str -> str.trim() }) }
            .toMap()
    else
        mapOf()

gradle.taskGraph.whenReady {
    if (allTasks.any { it is Sign }) {
        allprojects {
            extra["signing.keyId"] = codingCredentials["signingKeyID"]
            extra["signing.secretKeyRingFile"] = codingCredentials["signingKeyRingFile"]
            extra["signing.password"] = codingCredentials["signingPassword"]
        }
    }
}

publishing {
    repositories {
        maven {
            credentials {
                username = codingCredentials["nexusUser"]
                password = codingCredentials["nexusPass"]
            }

            url = if (project.version.toString().endsWith("-SNAPSHOT")) {
                uri("https://oss.sonatype.org/content/repositories/snapshots")
            } else {
                uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            }
        }
    }
    publications {
        create<MavenPublication>("tikaLibrary") {
            pom {
                name.set("tika-dgn-detector")
                description.set("A Tika Detector (not Parser!) for MicroStation DGN files (v7 & v8).")
                url.set("https://github.com/peeveen/tika-dgn-detector")
                developers {
                    developer {
                        id.set("peeveen")
                        name.set("Steven Frew")
                        email.set("steven.fullhouse@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/peeveen/tika-dgn-detector")
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
            groupId = "com.github.peeveen"
            artifactId = "tika-dgn-detector"
            version = "0.3"
            from(components["java"])

            val sourcesJar by tasks.creating(Jar::class) {
                dependsOn(JavaPlugin.CLASSES_TASK_NAME)
                archiveClassifier.set("sources")
                from(sourceSets["main"].allSource)
            }

            val javadocJar by tasks.creating(Jar::class) {
                val javadoc by tasks
                archiveClassifier.set("javadoc")
                from(javadoc)
            }

            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
}

signing {
    sign(publishing.publications["tikaLibrary"])
}