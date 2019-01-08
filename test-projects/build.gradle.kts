import jpamodelexp.test.gradle.Config

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        // The JpaModelExporter and dependencies
        classpath(fileTree("../build/libs") { include("*.jar") })
        classpath(jpamodelexp.test.gradle.Config.superDeps.libGuava)
        classpath(jpamodelexp.test.gradle.Config.superDeps.libHibernateCore)
        classpath(jpamodelexp.test.gradle.Config.superDeps.libH2)
    }
}

tasks.wrapper {
    version = Config.superVers.gradle
    distributionType = Wrapper.DistributionType.ALL
}
