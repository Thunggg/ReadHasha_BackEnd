plugins {
	java
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Web Starter
	implementation("org.springframework.boot:spring-boot-starter-web")
	
	// Spring Data JPA
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	
	// SQL Server JDBC Driver
	implementation("com.microsoft.sqlserver:mssql-jdbc:12.2.0.jre8")

	// Testing Dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks.withType<Test> {
	useJUnitPlatform()
}
