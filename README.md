# 🎓 University Management System

A full-stack University Management System web application built with **Java Spring Boot**, **Spring Security (RBAC)**, **Spring Data JPA**, **Thymeleaf**, **PostgreSQL**, and **Docker**.

---

## 📌 Table of Contents
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Role-Based Access Control (RBAC) Matrix](#-role-based-access-control-rbac-matrix)
- [Default User Credentials](#-default-user-credentials)
- [REST API Endpoints](#-rest-api-endpoints)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Run Locally via Maven](#option-1-run-locally-via-maven)
  - [Run in IntelliJ IDEA](#option-2-run-in-intellij-idea)
  - [Run with Docker Compose](#option-3-run-with-docker-compose)
- [Testing](#-testing)
  - [Unit Tests](#unit-tests)
  - [Integration Tests](#integration-tests)
  - [Test Execution Commands](#test-execution-commands)
- [GitHub Actions CI/CD](#-github-actions-cicd)
- [Database Configuration](#-database-configuration)

---

## ✨ Features

- **Full CRUD REST API**: Standard RESTful endpoints for Admin, Teacher, and Student resources returning clean JSON with proper HTTP status codes (`200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `403 Forbidden`, `404 Not Found`).
- **Role-Based Access Control (RBAC)**: Secure multi-role permission architecture (`ADMIN`, `TEACHER`, `STUDENT`) implemented with Spring Security 6.
- **Password Hashing**: Industry-standard **BCrypt** encryption for all user passwords stored in the database.
- **Interactive UI**: Responsive frontend built using **Thymeleaf**, **Bootstrap 5**, and Bootstrap Icons.
- **Self-Service Profile Management**: Dedicated `/profile` page where logged-in Teachers and Students can update their own name, email, and password.
- **Dockerization**: Complete containerization setup with multi-stage `Dockerfile` and `compose.yaml` supporting Apple Silicon (`ARM64`) and Intel (`AMD64`).
- **Automated Testing**: 6 unit tests (Mockito + JUnit 5) and 3 integration tests (MockMvc + Spring Security) covering happy paths and failure scenarios.
- **CI/CD Pipeline**: Automated GitHub Actions workflow for building, testing, packaging, and Docker verification on every push and pull request.

---

## 🛠 Tech Stack

- **Backend**: Java 17, Spring Boot 4.1.2
- **Security**: Spring Security (Form Login, Session Management, RBAC, BCrypt)
- **Persistence**: Spring Data JPA, Hibernate ORM
- **Database**: PostgreSQL (Production/Render), H2 In-Memory (Automated Tests)
- **Frontend / View**: Thymeleaf Template Engine, HTML5, CSS3, Bootstrap 5.3
- **Containerization**: Docker, Docker Compose
- **Testing**: JUnit 5, Mockito, Spring Test (MockMvc), Hamcrest
- **CI/CD**: GitHub Actions

---

## 🔐 Role-Based Access Control (RBAC) Matrix

| Resource / Action | Admin | Teacher | Student |
| :--- | :---: | :---: | :---: |
| **View Dashboard (`/dashboard`)** | ✅ | ✅ | ✅ |
| **Manage Admins (`/admin`, `/api/admins/**`)** | ✅ | ❌ | ❌ |
| **View Teacher Directory (`/teachers`)** | ✅ | ✅ | ❌ |
| **Add / Remove Teachers** | ✅ | ❌ | ❌ |
| **View Student Directory (`/students`)** | ✅ | ✅ | ✅ |
| **Add / Remove Students** | ✅ | ❌ | ❌ |
| **Update Own Information (`/profile`)** | ✅ | ✅ | ✅ |
| **Update Other Users' Records** | ✅ | ❌ | ❌ |

---

## 🔑 Default User Credentials

On initial application startup, `DataInitializer` seeds default accounts automatically if the database is empty:

| Role | Email | Password |
| :--- | :--- | :--- |
| **ADMIN** | `admin@uni.com` | `admin123` |
| **TEACHER** | `teacher@uni.com` | `teacher123` |
| **STUDENT** | `student@uni.com` | `student123` |

---

## 📡 REST API Endpoints

All REST APIs reside under `/api/**` with CSRF disabled for programmatic access. Passwords are never returned in responses.

### 1. Admins (`/api/admins`) — Admin Only
| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admins` | Retrieve all administrators | `ADMIN` |
| `GET` | `/api/admins/{id}` | Retrieve admin by ID | `ADMIN` |
| `POST` | `/api/admins` | Create a new admin | `ADMIN` |
| `PUT` | `/api/admins/{id}` | Update admin details | `ADMIN` |
| `DELETE` | `/api/admins/{id}` | Remove admin account | `ADMIN` |

### 2. Teachers (`/api/teachers`)
| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/teachers` | List all teachers | `ADMIN`, `TEACHER` |
| `GET` | `/api/teachers/{id}` | Get teacher by ID | `ADMIN`, `TEACHER` |
| `POST` | `/api/teachers` | Create a new teacher | `ADMIN` |
| `PUT` | `/api/teachers/{id}` | Update teacher details | `ADMIN` (Any) / `TEACHER` (Own ID only) |
| `DELETE` | `/api/teachers/{id}` | Remove teacher account | `ADMIN` |

### 3. Students (`/api/students`)
| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/students` | List all enrolled students | `ADMIN`, `TEACHER`, `STUDENT` |
| `GET` | `/api/students/{id}` | Get student by ID | `ADMIN`, `TEACHER`, `STUDENT` |
| `POST` | `/api/students` | Register a new student | `ADMIN` |
| `PUT` | `/api/students/{id}` | Update student details | `ADMIN` (Any) / `STUDENT` (Own ID only) |
| `DELETE` | `/api/students/{id}` | Remove student record | `ADMIN` |

---

## 📁 Project Structure

```text
UniversityManagementSystem/
├── .github/
│   └── workflows/
│       └── ci.yml                            # GitHub Actions CI workflow
├── src/
│   ├── main/
│   │   ├── java/org/example/universitymanagementsystem/
│   │   │   ├── config/
│   │   │   │   └── DataInitializer.java      # Database seeder on startup
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java      # REST API: /api/admins
│   │   │   │   ├── StudentController.java    # REST API: /api/students
│   │   │   │   ├── TeacherController.java    # REST API: /api/teachers
│   │   │   │   └── WebController.java        # Web UI controller (Thymeleaf)
│   │   │   ├── dto/
│   │   │   │   └── UserDTO.java              # Request validation DTO
│   │   │   ├── model/
│   │   │   │   ├── Role.java                 # Enum: ADMIN, TEACHER, STUDENT
│   │   │   │   └── User.java                 # JPA Entity (users table)
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java       # Spring Data JPA repository
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   └── SecurityConfig.java       # Spring Security & RBAC rules
│   │   │   └── service/
│   │   │       └── UserService.java          # Business logic layer
│   │   └── resources/
│   │       ├── application.properties        # Main config & PostgreSQL credentials
│   │       └── templates/
│   │           ├── admin/list.html           # Admin directory view
│   │           ├── teacher/
│   │           │   ├── list.html             # Teacher directory view
│   │           │   └── form.html             # Add / Edit teacher form
│   │           ├── student/
│   │           │   ├── list.html             # Student directory view
│   │           │   └── form.html             # Add / Edit student form
│   │           ├── fragments/navbar.html     # Reusable navigation bar
│   │           ├── dashboard.html            # Role-aware dashboard
│   │           ├── login.html                # Custom login page
│   │           └── profile.html              # Self-update profile page
│   └── test/
│       ├── java/org/example/universitymanagementsystem/
│       │   ├── service/
│       │   │   └── UserServiceTest.java      # 6 Unit Tests (Mockito)
│       │   └── integration/
│       │       ├── StudentIntegrationTest.java
│       │       ├── TeacherIntegrationTest.java
│       │       └── WebSecurityIntegrationTest.java
│       └── resources/
│           ├── application.properties        # H2 in-memory test DB config
│           └── mockito-extensions/           # Mockito subclass config
├── .dockerignore
├── compose.yaml                              # Docker Compose runner
├── Dockerfile                                # Multi-stage Docker container build
├── pom.xml                                   # Maven dependencies
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 17** or higher
- **Docker Desktop** (optional, for running via containers)
- **Maven** (bundled via `./mvnw`)

---

### Option 1: Run Locally via Maven

1. Clone or download this repository:
   ```bash
   git clone <your-repo-url>
   cd UniversityManagementSystem
   ```
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Open your browser at:
   ```
   http://localhost:8080
   ```

---

### Option 2: Run in IntelliJ IDEA

1. Open IntelliJ IDEA and choose **Open**, then select the `UniversityManagementSystem` folder.
2. Allow Maven to sync dependencies.
3. Locate `src/main/java/org/example/universitymanagementsystem/UniversityManagementSystemApplication.java`.
4. Click the **green play button ▶️** next to the `main` method.
5. Open your browser at `http://localhost:8080`.

---

### Option 3: Run with Docker Compose

1. Make sure Docker Desktop is running on your machine.
2. In the project root, execute:
   ```bash
   docker compose up --build
   ```
3. Docker compiles the application inside a multi-stage container and launches it.
4. Access the web application at `http://localhost:8080`.
5. To stop the container:
   ```bash
   docker compose down
   ```

---

## 🧪 Testing

The test suite contains a combination of **Unit Tests** and **Integration Tests** showcasing both **passing** (happy-path) and **failing** (security, validation, and error-handling) cases.

### Unit Tests (`UserServiceTest.java`)
Tests business logic in isolation with **Mockito** and **JUnit 5**:
* `test1_CreateUser_Passes`: Confirms BCrypt password hashing and persistence.
* `test2_GetAllByRole_Passes`: Confirms filtering users by role.
* `test3_GetById_Passes`: Confirms user retrieval by ID.
* `test4_GetByEmail_Passes`: Confirms user retrieval by email.
* `test5_CreateUser_PasswordNotHashed_Fails`: Demonstrates failure if plain-text password match is expected.
* `test6_UpdateNonExistentUser_ExpectSuccess_Fails`: Demonstrates failure when updating a non-existent ID.

### Integration Tests (MockMvc + Spring Security + H2 DB)
Tests the complete web layer, HTTP status codes, security boundaries, and database transactions:
* **Student Integration** (`StudentIntegrationTest.java`):
  * Admin creates student (`201 Created`).
  * Teacher reads student directory (`200 OK`).
  * Blank name fails validation (`400 Bad Request`).
* **Teacher Integration** (`TeacherIntegrationTest.java`):
  * Admin creates teacher (`201 Created`).
  * Teacher views teachers list (`200 OK`).
  * Student accessing Teacher API is blocked (`403 Forbidden`).
* **Web Security Integration** (`WebSecurityIntegrationTest.java`):
  * Admin views `/admin` page (`200 OK`).
  * Teacher views `/students` page (`200 OK`).
  * Student accessing `/admin` page is blocked (`403 Forbidden`).

---

### Test Execution Commands

```bash
# Run the entire test suite
./mvnw test

# Run only the Unit Tests
./mvnw test -Dtest=UserServiceTest

# Run only the Integration Tests
./mvnw test -Dtest=*IntegrationTest

# Run a single specific test method
./mvnw test -Dtest=StudentIntegrationTest#test1_AdminCreateStudent_Passes
```

---

## 🤖 GitHub Actions CI/CD

This repository includes continuous integration configured in [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

On every `push` and `pull_request` to `main` or `master`:
1. Sets up **Java 17 (Temurin)** with dependency caching.
2. Runs the automated test suite (`./mvnw test`).
3. Packages the project into a deployable JAR (`./mvnw package -DskipTests`).
4. Verifies the **Docker Compose build** (`docker compose build`).

---

## 🗄 Database Configuration

- **Development / Production:** Uses remote PostgreSQL on Render (configured in `src/main/resources/application.properties`).
- **Automated Tests:** Uses an in-memory **H2 Database** (configured in `src/test/resources/application.properties`). Tests run completely offline, fast, and never interfere with live database records.
