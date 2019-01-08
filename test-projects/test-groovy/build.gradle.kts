import com.ctzen.jpamodelexp.JpaModelExporter
import com.ctzen.jpamodelexp.jpa.AnnotatedClassesJpaMetamodelBuilder
import com.ctzen.jpamodelexp.pkgscan.AnnotatedJpaClassesPackageScanner
import jpamodelexp.test.gradle.Config
import java.net.URL
import java.net.URLClassLoader

plugins {
    groovy
}

val EXPORT_PATH = "build/generated"

sourceSets {
    test {
        withConvention(GroovySourceSet::class) {
            groovy {
                setSrcDirs(listOf("src/test/groovy", EXPORT_PATH))
            }
        }
    }
}

repositories {
    jcenter()
}

dependencies {
    implementation(Config.Deps.libGroovy)
    implementation(Config.Deps.libJpaApi)
    testImplementation(Config.superDeps.libTestNg)
    testRuntimeOnly(Config.superDeps.libReportNg)
    testRuntimeOnly(Config.superDeps.libGuice)
}

tasks.jar {
    enabled = false
}

tasks.test {
    systemProperty("org.uncommons.reportng.stylesheet", "${rootDir}/../src/test/resources/reportng-custom.css")
    useTestNG {
        listeners.add("org.uncommons.reportng.HTMLReporter")
    }
}

val jpaModelExport by tasks.registering {
    dependsOn(tasks.classes)
    outputs.dir(EXPORT_PATH)
    doLast {
        // create class loader
        val compileTask = tasks.compileGroovy.get()
        val urls = mutableListOf<URL>(compileTask.destinationDir.toURI().toURL())
        compileTask.classpath.forEach{ urls.add(it.toURI().toURL()) }
        val classLoader = URLClassLoader(urls.toTypedArray(), Thread.currentThread().contextClassLoader)
        // package scanner to locate JPA classes
        val scanner = AnnotatedJpaClassesPackageScanner(classLoader)
        scanner.addPackages("jpamodelexp.test")
        // set to scan all protocols to exercise jar:file:
        scanner.setUrlProtocols()
        val jpaClasses = scanner.scan()
        // JpaMetamodel builder
        val jmb = AnnotatedClassesJpaMetamodelBuilder(classLoader)
        jmb.addAnnotatedClasses(jpaClasses)
        val jpaMetamodel = jmb.build()
        // exports JPA static metamodels
        val exporter = JpaModelExporter(file(EXPORT_PATH), jpaMetamodel)
        exporter.export()
    }
}

tasks.compileTestGroovy {
    dependsOn(jpaModelExport)
}
