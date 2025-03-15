import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    id("io.quarkus")
    id("com.diffplug.spotless") version "7.0.2"
    id("net.ltgt.errorprone") version "4.1.0"
    id("com.github.spotbugs") version "6.1.7"
}

repositories {
    mavenCentral()
    mavenLocal()
}

group = "com.beachape"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

val mapstructVersion = "1.6.3"
val lombokVersion = "1.18.36"
val lombokMapstructBindingVersion = "0.2.0"

val errorProneVersion = "2.36.0"
val errorProneSupportVersion = "0.20.0"

dependencies {
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-opentelemetry")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-security") // for BCFIPS
    implementation("io.quarkus:quarkus-smallrye-jwt-build")
    // https://mvnrepository.com/artifact/org.bouncycastle/bc-fips
    implementation("org.bouncycastle:bc-fips:2.1.0")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    // https://mvnrepository.com/artifact/io.quarkus/quarkus-junit5-mockito
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("org.assertj:assertj-core:3.11.1")

    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    implementation("org.projectlombok:lombok:$lombokVersion")

    // Static Analysis
    errorprone("com.google.errorprone:error_prone_core:$errorProneVersion")
    errorprone("com.uber.nullaway:nullaway:0.10.24")
    // Error Prone Support's additional bug checkers.
    errorprone("tech.picnic.error-prone-support:error-prone-contrib:$errorProneSupportVersion")
    // Error Prone Support's Refaster rules.
    errorprone("tech.picnic.error-prone-support:refaster-runner:$errorProneSupportVersion")

    annotationProcessor(
        "org.mapstruct:mapstruct-processor:$mapstructVersion",
    )
    annotationProcessor(
        "org.projectlombok:lombok:$lombokVersion",
    )
    annotationProcessor(
        "org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion",
    )
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
        formatAnnotations()
        // Exclude generated sources
        targetExclude(
            "**/generated/**",
            "**/build/**",
        )
    }

    // Format Gradle Kotlin DSL files
    kotlinGradle {
        ktlint()
    }
}

spotbugs {
    ignoreFailures.set(false) // Fail build on SpotBugs errors
    showProgress.set(true)
    includeFilter.set(project.file("spotbugs-filter.xml"))
}

// Configure build to fail if spotless checks fail
gradle.startParameter.isContinueOnFailure = false

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
        listOf(
            "-parameters",
            "-Xlint:all",
            // Things we don't control anyway
            "-Xlint:-serial",
            "-Xlint:-classfile",
            // Ignore processing warnings
            "-Xlint:-processing",
            // Throw on warnings
            "-Werror",
        ),
    )

    val generatedSourceOutputDirectory = options.generatedSourceOutputDirectory

    options.errorprone {
        allErrorsAsWarnings.set(false)
        disableWarningsInGeneratedCode.set(true)
        excludedPaths.set(generatedSourceOutputDirectory.locationOnly.map { """.*/\Q${relativePath(it)}\E/*""" })
        option("NullAway:AnnotatedPackages", "com.beachape")
        option("NullAway:ExcludedClassAnnotations", "javax.annotation.processing.Generated")

        // Additional Error Prone checks for safer code
        error("NullAway")
    }
}

// Add a verification task to check all code quality issues
tasks.register("verifyCodeQuality") {
    group = "verification"
    description = "Runs all code quality checks"
    dependsOn("spotlessCheck", "spotbugsMain")
}

// Make the build task depend on code quality checks
tasks.named("build") {
    dependsOn("verifyCodeQuality")
}
