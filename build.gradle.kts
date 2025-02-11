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
    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Boot Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // SQL Server JDBC Driver
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.2.0.jre8")

    // BCrypt Password Encoder
    implementation("org.springframework.security:spring-security-crypto")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    testCompileOnly("org.projectlombok:lombok:1.18.24") // Thêm dòng này
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24") // Thêm dòng này

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.0")

    // Mail Service
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.sun.mail:jakarta.mail:2.0.1")

    // Validation (Thêm nếu cần)
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.3.1") // Thêm nếu cần
}

tasks.withType<Test> {
	useJUnitPlatform()
}
