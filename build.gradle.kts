import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("kapt") version "1.9.25"
	id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("plugin.jpa") version "1.9.25"
	// OpenAPI Generator
	id("org.openapi.generator") version "7.2.0"
}

group = "com.comicsdb"
version = "0.0.1-SNAPSHOT"

val openApiOutputDir = "$buildDir/generated/openapi"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot Starters
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// Kotlin specific
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// Development Tools
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	// Databases
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")

	// Mapping
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	kapt("org.mapstruct:mapstruct-processor:1.5.5.Final")

	// OpenAPI / Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Additional useful libraries
	implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

// OpenAPI Generation Task
tasks.register<GenerateTask>("generateOpenApiModelClasses") {
	generatorName.set("kotlin-spring")
	inputSpec.set("$projectDir/src/main/resources/openapi/api.yaml")
	outputDir.set(openApiOutputDir)

	apiPackage.set("com.comicsdb.api")
	modelPackage.set("com.comicsdb.model")

	configOptions.set(mapOf(
		"dateLibrary" to "java8",
		"interfaceOnly" to "false",
		"serializationLibrary" to "jackson",
		"useSpringBoot3" to "true",
		"useBeanValidation" to "true",
		"enumPropertyNaming" to "UPPERCASE",
		"delegatePattern" to "true",
		"delegateMethod" to "true",
		"useTags" to "true"
	))

	generateApiTests.set(false)
	generateModelTests.set(false)
}
springBoot {
	mainClass.set("com.comicsdb.ComicsDb.ComicsDbApplicationKt")
}

// All Open Plugin Configuration
allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

// Source Sets Configuration
sourceSets {
	main {
		kotlin.srcDir("$openApiOutputDir/src/main/kotlin")
	}
}

// Ensure OpenAPI generation happens before compilation
tasks.withType<KotlinCompile> {
	dependsOn("generateOpenApiModelClasses")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Optional: OpenAPI generation task to be run manually
tasks.register("generateOpenApi") {
	description = "Generates OpenAPI classes"
	group = "openapi"
	dependsOn("generateOpenApiModelClasses")
}