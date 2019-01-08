plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    // Config from the main project
    implementation(files("../../buildSrc/build/libs/buildSrc.jar"))
}
