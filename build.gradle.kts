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

    // Jackson (cho việc xử lý JSON)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")

    // BCrypt Password Encoder
    implementation("org.springframework.security:spring-security-crypto")

    // Lombok (giảm thiểu mã nguồn)
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    // Testing Dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.0")

}



tasks.withType<Test> {
	useJUnitPlatform()
}
