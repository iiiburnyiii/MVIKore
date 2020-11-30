package plugins

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*
import java.util.*
import utils.readProperties

class BintrayPublishPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        apply(plugin = "maven-publish")
        apply(plugin = "com.jfrog.bintray")

        configure<PublishingExtension> {
            repositories {
                maven { uri(MVIKore.BINTRAY_URL) }
            }

            publications {
                create<MavenPublication>("mavenProject") {
                    groupId = MVIKore.GROUP_ID
                    artifactId = target.name
                    version = MVIKore.LIBRARY_VERSION_NAME
                }
            }
        }

        afterEvaluate {
            project.the<PublishingExtension>().publications.withType<MavenPublication> {
                groupId = MVIKore.GROUP_ID
                version = MVIKore.LIBRARY_VERSION_NAME
                artifactId = if (name.contains("metadata")) {
                    project.name
                } else {
                    "${project.name}-${this.name}"
                }
            }
        }

        tasks.withType<BintrayUploadTask> {
            doFirst {
                project.the<PublishingExtension>().publications
                    .filter { it.name != "kotlinMultiplatform" }
                    .also { setPublications(*it.toTypedArray()) }
            }

            dependsOn(tasks.getByName("publishToMavenLocal"))
        }

        configure<BintrayExtension> {
            readProperties("bintray") {
                this@configure.user = getProperty("user")
                this@configure.key = getProperty("apiKey")
            }

            publish = false
            setPublications("mavenProject")

            pkg.apply {
                setLicenses("Apache-2.0")

                repo = MVIKore.BINTRAY_REPO
                name = this@run.name
                userOrg = MVIKore.BINTRAY_ORG

                vcsUrl = MVIKore.VCS_URL
                websiteUrl = MVIKore.SITE_URL
                issueTrackerUrl = MVIKore.ISSUE_URL

                version.apply {
                    name = MVIKore.LIBRARY_VERSION_NAME
                    vcsTag = MVIKore.LIBRARY_VERSION_NAME
                    released = Date().toString()
                }
            }
        }
    }

}