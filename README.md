# 🏥 Hospital Management System — Microservices Architecture

A full-stack Hospital Management System built with **Spring Boot microservices**, connected via **Eureka Service Discovery** and an **API Gateway**. The system handles user authentication (JWT), doctor staff management, and patient medical records.

---

## 📐 Architecture Overview

```
                         ┌──────────────────┐
                         │   Eureka Server   │
                         │    (Port 8761)    │
                         └────────┬─────────┘
                                  │ Service Registry
          ┌───────────────────────┼───────────────────────┐
          │                       │                       │
          ▼                       ▼                       ▼
 ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
 │   AuthService    │   │  DoctorService   │   │ PatientService   │
 │   (Port 8081)    │   │   (Port 8082)    │   │   (Port 8083)    │
 │   JWT + MySQL    │   │     MySQL        │   │     MySQL        │
 └────────┬─────────┘   └────────┬─────────┘   └────────┬─────────┘
          │                       │                       │
          └───────────────────────┼───────────────────────┘
                                  │ lb:// (Load Balanced)
                         ┌────────┴─────────┐
                         │   API Gateway     │
                         │    (Port 8080)    │
                         └────────┬─────────┘
                                  │
                         ┌────────┴─────────┐
                         │  Client / Postman │
                         └──────────────────┘
```

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| **Spring Boot 3.5.16** | Core framework for all microservices |
| **Spring Cloud 2025.0.0** | Microservice infrastructure (Eureka, Gateway) |
| **Spring Cloud Gateway** | API Gateway with WebFlux for routing |
| **Netflix Eureka** | Service discovery and registration |
| **Spring Security + JWT** | Authentication and authorization |
| **Spring Data JPA** | ORM for database operations |
| **MySQL** | Relational database |
| **Lombok** | Reduce boilerplate code |
| **SpringDoc OpenAPI** | Swagger UI for API documentation |
| **Maven** | Build and dependency management |

---

## 📦 Microservices

### 1. Eureka Server (`/eureka`) — Port 8761

The **service registry**. All other microservices register themselves here on startup. The API Gateway discovers services through Eureka instead of using hardcoded URLs.

- **Dashboard**: `http://localhost:8761`
- **Role**: Central registry — does NOT handle any business logic
- **Key Annotation**: `@EnableEurekaServer`

---

### 2. API Gateway (`/ApiGateway`) — Port 8080

The **single entry point** for all client requests. Routes traffic to the correct microservice using Eureka-based load balancing (`lb://`).

| Route Pattern | Target Service | Eureka Name |
|---------------|---------------|-------------|
| `/auth/**` | AuthService | `AUTH-SERVICE` |
| `/doctors/**` | DoctorService | `DOCTOR-SERVICE` |
| `/patients/**` | PatientService | `PATIENT-SERVICE` |

> **Why use the Gateway?** Clients only need to know one URL (`localhost:8080`). The Gateway handles routing, and if services scale to multiple instances, it load-balances automatically.

---

### 3. AuthService (`/AuthService`) — Port 8081

Handles **user registration, login, and JWT token generation**. Secured with Spring Security.

#### Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|:------------:|-------------|
| `POST` | `/auth/register` | ❌ | Register a new user (DOCTOR or PATIENT role) |
| `POST` | `/auth/login` | ❌ | Login and receive a JWT token |
| `GET` | `/auth/profile` | ✅ Bearer Token | View the logged-in user's profile |

#### How Authentication Works

1. User calls `/auth/register` with username, email, password, and role
2. Password is hashed with **BCrypt** and stored in MySQL
3. User calls `/auth/login` with email + password
4. Server validates credentials and returns a **JWT token** (valid for 24 hours)
5. For protected endpoints, pass the token as: `Authorization: Bearer <token>`

#### JWT Token Contains
- User ID
- Username
- Role (DOCTOR / PATIENT)
- Email (as subject)
- Expiration timestamp

#### Roles
| Role | Description |
|------|-------------|
| `PATIENT` | Default role if none specified during registration |
| `DOCTOR` | Must be explicitly set during registration |
| `ADMIN` | Exists in enum but cannot be self-registered |
| `PHARMACIST` | Exists in enum but cannot be self-registered |

---

### 4. DoctorService (`/DoctorServices`) — Port 8082

Manages the **hospital's doctor directory** — onboarding staff, tracking availability, and updating profiles.

#### Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|:------------:|-------------|
| `POST` | `/doctors` | ❌ | Add a new doctor to the hospital staff |
| `GET` | `/doctors` | ❌ | List all doctors in the hospital |
| `GET` | `/doctors/{id}` | ❌ | Look up a specific doctor's profile |
| `PUT` | `/doctors/{id}` | ❌ | Update a doctor's details (fee, availability, etc.) |
| `DELETE` | `/doctors/{id}` | ❌ | Remove a doctor from the hospital |

#### Doctor Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Auto-generated unique ID |
| `name` | String | Doctor's full name |
| `specialization` | String | e.g., Cardiology, Neurology, Orthopedics |
| `experience` | Integer | Years of practice |
| `consultationFee` | Double | Fee per consultation |
| `phone` | String | Contact number |
| `email` | String | Email address |
| `available` | Boolean | Whether the doctor is currently available |

---

### 5. PatientService (`/PatientServices`) — Port 8083

Manages **patient medical records** — admissions, record lookups, updates, and discharge.

#### Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|:------------:|-------------|
| `POST` | `/patients` | ❌ | Admit a new patient (create medical record) |
| `GET` | `/patients` | ❌ | List all patient records |
| `GET` | `/patients/{id}` | ❌ | Look up a specific patient's record |
| `PUT` | `/patients/{id}` | ❌ | Update a patient's details |
| `DELETE` | `/patients/{id}` | ❌ | Discharge / remove a patient record |

#### Patient Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Auto-generated unique ID |
| `name` | String | Patient's full name |
| `gender` | String | Male / Female / Other |
| `age` | Integer | Patient's age |
| `phone` | String | Contact number |
| `email` | String | Email address |
| `address` | String | Residential address |
| `bloodGroup` | String | e.g., A+, B-, O+, AB+ |
| `dateOfBirth` | LocalDate | Format: `YYYY-MM-DD` |

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven** (or use the included `mvnw` wrapper)
- **MySQL** running on `localhost:3306`

### 1. Setup Database

```sql
CREATE DATABASE hospital_management_system;
```

### 2. Set Environment Variables

```bash
# Linux / Mac
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password

# Windows (Command Prompt)
set DB_USERNAME=root
set DB_PASSWORD=your_mysql_password

# Windows (PowerShell)
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
```

### 3. Start Services (in order)

> ⚠️ **Start in this exact order.** Eureka must be running before other services can register.

```bash
# Terminal 1 — Eureka Server (start first, wait until ready)
cd eureka
./mvnw spring-boot:run

# Terminal 2 — Auth Service
cd AuthService
./mvnw spring-boot:run

# Terminal 3 — Doctor Service
cd DoctorServices
./mvnw spring-boot:run

# Terminal 4 — Patient Service
cd PatientServices
./mvnw spring-boot:run

# Terminal 5 — API Gateway (start last)
cd ApiGateway
./mvnw spring-boot:run
```

### 4. Verify

- **Eureka Dashboard**: http://localhost:8761 — all 4 services should show as `UP`
- **API Gateway**: http://localhost:8080 — single entry point for all APIs

---

## 📖 Swagger UI (API Documentation)

Each service has interactive API docs:

| Service | Swagger URL |
|---------|-------------|
| AuthService | http://localhost:8081/swagger-ui.html |
| DoctorService | http://localhost:8082/swagger-ui.html |
| PatientService | http://localhost:8083/swagger-ui.html |

> **Note**: AuthService Swagger may show a security popup — just click **Cancel** to dismiss it. The public endpoints (`/auth/register`, `/auth/login`) work without authentication.

---

## 🔄 API Usage Workflow

### Step 1: Register a User

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "Dr. Smith",
    "email": "drsmith@hospital.com",
    "password": "password123",
    "role": "DOCTOR"
  }'
```
**Response**: `DOCTOR Registered Successfully`

### Step 2: Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "drsmith@hospital.com",
    "password": "password123"
  }'
```
**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Step 3: Add a Doctor to Staff

```bash
curl -X POST http://localhost:8080/doctors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. Sarah Johnson",
    "specialization": "Cardiology",
    "experience": 12,
    "consultationFee": 500.00,
    "phone": "9876543210",
    "email": "sarah@hospital.com",
    "available": true
  }'
```

### Step 4: Admit a Patient

```bash
curl -X POST http://localhost:8080/patients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rahul Sharma",
    "gender": "Male",
    "age": 32,
    "phone": "9123456789",
    "email": "rahul@email.com",
    "address": "Mumbai, India",
    "bloodGroup": "B+",
    "dateOfBirth": "1994-03-15"
  }'
```

### Step 5: View Profile (Protected)

```bash
curl -X GET http://localhost:8080/auth/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## 🗂️ Project Structure

```
Hospital_Management_System/
├── eureka/                          # Eureka Service Registry
│   └── src/main/java/.../EurekaApplication.java
│
├── ApiGateway/                      # API Gateway
│   └── src/main/java/.../ApiGatewayApplication.java
│
├── AuthService/                     # Authentication Service
│   └── src/main/java/com/hospital/auth/
│       ├── config/SecurityConfig.java
│       ├── controller/AuthController.java
│       ├── dto/
│       │   ├── AuthResponse.java
│       │   ├── LoginRequest.java
│       │   └── RegisterRequest.java
│       ├── entity/
│       │   ├── User.java
│       │   └── Role.java
│       ├── exception/GlobalExceptionHandler.java
│       ├── repository/UserRepository.java
│       ├── security/JwtAuthenticationFilter.java
│       ├── service/
│       │   ├── AuthService.java
│       │   └── CustomUserDetailsService.java
│       └── util/JwtUtil.java
│
├── DoctorServices/                  # Doctor Management Service
│   └── src/main/java/com/example/demo/
│       ├── controller/DoctorController.java
│       ├── entity/Doctor.java
│       ├── exception/
│       │   ├── ResourceNotFoundException.java
│       │   └── GlobalExceptionHandler.java
│       ├── repo/DoctorRepository.java
│       └── service/
│           ├── DoctorService.java
│           └── DoctorServiceImpl.java
│
└── PatientServices/                 # Patient Management Service
    └── src/main/java/com/example/demo/
        ├── controller/PatientController.java
        ├── entity/Patient.java
        ├── exception/
        │   ├── ResourceNotFoundException.java
        │   └── GlobalExceptionHandler.java
        ├── repo/PatientRepository.java
        └── service/
            ├── PatientService.java
            └── PatientServiceImpl.java
```

---

## ⚙️ Port Summary

| Service | Port | Eureka Name |
|---------|------|-------------|
| Eureka Server | 8761 | — |
| API Gateway | 8080 | API-GATEWAY |
| AuthService | 8081 | AUTH-SERVICE |
| DoctorService | 8082 | DOCTOR-SERVICE |
| PatientService | 8083 | PATIENT-SERVICE |
| MySQL | 3306 | — |

---

## 🗄️ Database

All services share a single MySQL database: `hospital_management_system`

| Table | Created By | Description |
|-------|-----------|-------------|
| `users` | AuthService | Stores registered users with hashed passwords |
| `doctors` | DoctorService | Hospital staff directory |
| `patients` | PatientService | Patient medical records |

> Tables are auto-created by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request
