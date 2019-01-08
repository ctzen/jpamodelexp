package com.ctzen.jpamodelexp.gradle

object Config {

    /**
     * Versions
     */
    object Vers {
        const val gradle = "5.1"
        const val hibernate = "5.4.0.Final"
    }

    /**
     * Dependencies
     */
    object Deps {
        // jpamodelexp dependencies
        const val libGuava = "com.google.guava:guava:27.0.1-jre"
        const val libHibernateCore = "org.hibernate:hibernate-core:${Vers.hibernate}"
        const val libH2 = "com.h2database:h2:1.4.197"

        // test dependencies
        const val libAssertj = "org.assertj:assertj-core:3.11.1"
        const val libTestNg = "org.testng:testng:6.14.3"
        const val libReportNg = "org.uncommons:reportng:1.1.4"
        const val libGuice = "com.google.inject:guice:4.2.2"   // reportng dependencies

        // annotation processors
        const val apJpaModelGen = "org.hibernate:hibernate-jpamodelgen:${Vers.hibernate}"
    }

    val javaCompilerArgs = arrayOf(
            "-Xlint:unchecked",
            "-Xlint:deprecation"
    )

}
