plugins {
    kotlin("jvm") version "1.5.31"
    `kotlin-dsl`
    `maven-publish`
    id("artfable.artifact") version "0.0.3"
    id("com.jfrog.artifactory") version "4.24.23"
}

group = "com.artfable.gradle"
version = "0.2.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("io.bit3:jsass:5.10.4")
}

tasks {
    test {
        useJUnitPlatform()
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourceJar"])
            artifact(tasks["javadocJar"])

            pom {
                description.set("The plugin that allowed compile .scss to .css")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://raw.githubusercontent.com/artfable/gradle-sass-plugin/master/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("artfable")
                        name.set("Artem Veselov")
                        email.set("art-fable@mail.ru")
                    }
                }
            }
        }
    }
}

artifactory {
    setContextUrl("https://artfable.jfrog.io/artifactory/")
    publish {
        repository {
            setRepoKey("default-maven-local")
            setUsername(artifactoryCredentials.user)
            setPassword(artifactoryCredentials.key)
        }
        defaults {
            publications ("mavenJava")

            setPublishArtifacts(true)
            setPublishPom(true)
            setPublishIvy(false)
        }
    }
}
