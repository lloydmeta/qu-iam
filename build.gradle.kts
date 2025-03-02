import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    id("io.quarkus")
    id("com.diffplug.spotless") version "7.0.2"
    id("net.ltgt.errorprone") version "4.1.0"
    id("org.owasp.dependencycheck") version "12.1.0"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-logging-json")
    implementation("io.quarkus:quarkus-opentelemetry")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
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

    // Static Analysis
    errorprone("com.google.errorprone:error_prone_core:2.36.0")
    errorprone("com.uber.nullaway:nullaway:0.10.24")

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

spotless {
    java {
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
        formatAnnotations()
        // Exclude generated sources
        targetExclude(
            "**/generated/**",
            "**/build/**"
        )
    }
}


tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        "-parameters", 
        "-Xlint:all",
        // Things we don't control anyway
        "-Xlint:-serial",
        "-Xlint:-classfile",
        // Throw on warnings
        "-Werror"
    ))

    options.errorprone {
        allErrorsAsWarnings.set(false)
        option("NullAway:AnnotatedPackages", "com.beachape")
        option("disableWarningsInGeneratedCode", "true")
        error("NullAway")
    }
}

dependencyCheck {
    failBuildOnCVSS = 7.0f
    formats = listOf("HTML", "SARIF")

    // Skip non-Java dependencies
    analyzers {
        // Disable all non-Java analyzers
        assemblyEnabled = false
        nodeEnabled = false
        rubygemsEnabled = false
        pyDistributionEnabled = false
        pyPackageEnabled = false
        nuspecEnabled = false
        nugetconfEnabled = false
        cmakeEnabled = false
        autoconfEnabled = false
        composerEnabled = false
        nodeAudit {
            enabled = false
        }
        retirejs {
            enabled = false
        }
        
        // Keep Java analyzers enabled
        jarEnabled = true
        centralEnabled = true
        nexusEnabled = true
        
        // Optional: Enable Maven analyzers if you use Maven dependencies
        centralEnabled = true
    }
    
    // Skip test dependencies if you want to focus only on runtime dependencies
    skipConfigurations = listOf("testImplementation", "testRuntimeOnly", "testCompileOnly")
}
