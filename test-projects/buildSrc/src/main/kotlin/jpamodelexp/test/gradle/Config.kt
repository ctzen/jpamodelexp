package jpamodelexp.test.gradle

object Config {

    // Expose the main projects's config objects
    val superVers = com.ctzen.jpamodelexp.gradle.Config.Vers
    val superDeps = com.ctzen.jpamodelexp.gradle.Config.Deps

    /**
     * Dependencies
     */
    object Deps {
        const val libJpaApi = "org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.2.Final"
        const val libGroovy = "org.codehaus.groovy:groovy-all:2.5.5"
    }

}
