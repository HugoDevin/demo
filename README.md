# Spring Boot 使用者身份驗證 Demo

## 專案目錄結構

```text
.
├── pom.xml
├── README.md
└── src
    └── main
        ├── java
        │   └── com/example/auth
        │       ├── AuthDemoApplication.java
        │       ├── config
        │       │   └── SecurityConfig.java
        │       ├── controller
        │       │   └── AuthController.java
        │       ├── dto
        │       │   ├── LoginRequest.java
        │       │   └── RegisterRequest.java
        │       ├── exception
        │       │   ├── EmailAlreadyExistsException.java
        │       │   ├── GlobalExceptionHandler.java
        │       │   └── InvalidCredentialsException.java
        │       ├── model
        │       │   └── AppUser.java
        │       ├── service
        │       │   └── AuthService.java
        │       └── store
        │           └── InMemoryUserStore.java
        └── resources
            └── application.properties
```

## pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.2</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>auth-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>auth-demo</name>
    <description>Simple Spring Boot authentication demo without database</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## application.properties

```properties
spring.application.name=auth-demo
server.error.whitelabel.enabled=false
```

## 主程式類別

`src/main/java/com/example/auth/AuthDemoApplication.java`

```java
package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthDemoApplication.class, args);
    }
}
```

## 所有 Java 類別

### SecurityConfig

```java
package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Demo 專案為了簡化 API 測試而停用 CSRF；正式環境通常不建議停用，
                // 應依照實際客戶端型態配置 CSRF 保護機制。
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/register", "/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)
                .anonymous(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### AuthController

```java
package com.example.auth.controller;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

### RegisterRequest

```java
package com.example.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be at least 6 characters.")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

### LoginRequest

```java
package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email is required.")
    private String email;

    @NotBlank(message = "Password is required.")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

### AppUser

```java
package com.example.auth.model;

public class AppUser {

    private final String email;
    private final String encodedPassword;

    public AppUser(String email, String encodedPassword) {
        this.email = email;
        this.encodedPassword = encodedPassword;
    }

    public String getEmail() {
        return email;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }
}
```

### InMemoryUserStore

```java
package com.example.auth.store;

import com.example.auth.model.AppUser;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryUserStore {

    private final ConcurrentHashMap<String, AppUser> users = new ConcurrentHashMap<>();

    public Optional<AppUser> findByEmail(String email) {
        return Optional.ofNullable(users.get(email));
    }

    public boolean existsByEmail(String email) {
        return users.containsKey(email);
    }

    public boolean save(AppUser user) {
        return users.putIfAbsent(user.getEmail(), user) == null;
    }
}
```

### AuthService

```java
package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.exception.EmailAlreadyExistsException;
import com.example.auth.exception.InvalidCredentialsException;
import com.example.auth.model.AppUser;
import com.example.auth.store.InMemoryUserStore;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final InMemoryUserStore userStore;
    private final PasswordEncoder passwordEncoder;

    public AuthService(InMemoryUserStore userStore, PasswordEncoder passwordEncoder) {
        this.userStore = userStore;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        AppUser user = new AppUser(normalizedEmail, encodedPassword);

        if (!userStore.save(user)) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        return "User registered successfully.";
    }

    public String login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        AppUser user = userStore.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getEncodedPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        return "Login successful";
    }
}
```

### EmailAlreadyExistsException

```java
package com.example.auth.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
```

### InvalidCredentialsException

```java
package com.example.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
```

### GlobalExceptionHandler

```java
package com.example.auth.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String message = resolveValidationMessage(ex.getBindingResult().getFieldErrors());
        return plainTextResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return plainTextResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(InvalidCredentialsException ex) {
        return plainTextResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
        return plainTextResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private String resolveValidationMessage(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Invalid request.");
    }

    private ResponseEntity<String> plainTextResponse(HttpStatus status, String body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_PLAIN)
                .body(body);
    }
}
```

## 啟動方式

```bash
mvn spring-boot:run
```

或先打包再執行：

```bash
mvn clean package
java -jar target/auth-demo-0.0.1-SNAPSHOT.jar
```

## curl 測試範例

### 1. 註冊成功

```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"mypassword"}'
```

預期回應：

```text
User registered successfully.
```

### 2. 登入成功

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"mypassword"}'
```

預期回應：

```text
Login successful
```

### 3. 無效 email

```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"email":"bad-email","password":"mypassword"}'
```

預期回應：

```text
Invalid email format.
```

### 4. 密碼太短

```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test2@example.com","password":"123"}'
```

預期回應：

```text
Password must be at least 6 characters.
```

### 5. 登入失敗

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"wrongpass"}'
```

預期回應：

```text
Invalid email or password.
```

## 設計說明

1. **為什麼使用 ConcurrentHashMap**  
   因為這是一個不使用資料庫的 demo，帳號資料直接存在記憶體中。`ConcurrentHashMap` 可在多執行緒請求下提供比一般 `HashMap` 更安全的並發存取能力，並可搭配 `putIfAbsent` 原子化處理重複 email 註冊。

2. **為什麼使用 BCrypt**  
   密碼不能以明文保存。BCrypt 具備雜湊與自動加鹽特性，能降低密碼外洩時被快速反推的風險，也是 Spring Security 常見且成熟的密碼編碼方式。

3. **為什麼不使用資料庫**  
   題目要求專案保持精簡且可面試展示，因此使用記憶體儲存即可聚焦在 API 設計、輸入驗證、密碼加密與登入流程，而不增加資料庫建置與維運複雜度。

4. **為什麼不使用 Spring Security 預設 login**  
   題目要求自行在 Controller 與 Service 中實作 `/login` 驗證邏輯，並回傳純文字結果。因此關閉 Spring Security 預設登入頁、`formLogin()` 與 `httpBasic()`，讓 API 行為完全符合需求。
