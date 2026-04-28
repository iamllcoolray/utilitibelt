plugins {
    id 'java'
    id 'application'
}

group '${packageName}'
version '1.0'

repositories {
    mavenCentral()
    maven { url 'https://maven.pkg.jetbrains.space/litiengine/p/maven/releases' }
}

dependencies {
    implementation 'de.gurkenlabs:litiengine:0.11.1'
}

application {
    mainClass = '${packageName}.Main'
}
