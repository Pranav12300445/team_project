# 🏥 Hospital Management System — Microservices Architecture

A full-stack Hospital Management System built with **Spring Boot microservices**, connected via **Eureka Service Discovery** and an **API Gateway**. The system handles user authentication (JWT), doctor staff management, and patient medical records — with **automatic cross-service registration** so that signing up through AuthService creates the corresponding Doctor or Patient profile instantly.

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
 │   AuthService    │──▶│  DoctorService   │   │ PatientService   │
 │   (Port 8081)    │   │   (Port 8082)    │   │   (Port 8083)    │
 │   JWT + MySQL    │──▶│     MySQL        │   │     MySQL        │
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

> **Inter-service calls**: AuthService uses a **load-balanced RestClient** (via Eureka) to call DoctorService or PatientService during registration, automatically creating the domain profile.

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

Handles **user registration, login, and JWT token generation**. Secured with Spring Security. On registration, **automatically creates a Doctor or Patient profile** in the corresponding downstream service via inter-service REST calls.

#### Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|:------------:|-------------|
| `POST` | `/auth/register` | ❌ | Register a new user (DOCTOR or PATIENT role) — also creates Doctor/Patient profile |
| `POST` | `/auth/login` | ❌ | Login and receive a JWT token |
| `GET` | `/auth/profile` | ✅ Bearer Token | View the logged-in user's profile |

#### Registration Flow

```
POST /auth/register (role=DOCTOR)
  ├─ 1. Save User to `users` table (AuthService)
  ├─ 2. HTTP POST to DOCTOR-SERVICE /doctors/register (via Eureka lb://)
  │     └─ Creates a Doctor record with userId, name, email
  └─ 3. Return "DOCTOR Registered Successfully"

POST /auth/register (role=PATIENT)
  ├─ 1. Save User to `users` table (AuthService)
  ├─ 2. HTTP POST to PATIENT-SERVICE /patients/register (via Eureka lb://)
  │     └─ Creates a Patient record with userId, name, email
  └─ 3. Return "PATIENT Registered Successfully"
```

> **Note**: If the downstream service is unavailable, the user is still saved in the `users` table and a warning is logged. The Doctor/Patient profile can be created manually later.

#### How Authentication Works

1. User calls `/auth/register` with username, email, password, and role
2. Password is hashed with **BCrypt** and stored in MySQL
3. AuthService calls DoctorService or PatientService to create the domain profile
4. User calls `/auth/login` with email + password
5. Server validates credentials and returns a **JWT token** (valid for 24 hours)
6. For protected endpoints, pass the token as: `Authorization: Bearer <token>`

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
| `POST` | `/doctors/register` | ❌ (internal) | Called by AuthService during registration — creates profile with basic info |
| `POST` | `/doctors` | ❌ | Add a new doctor to the hospital staff (manual) |
| `GET` | `/doctors` | ❌ | List all doctors in the hospital |
| `GET` | `/doctors/{id}` | ❌ | Look up a specific doctor's profile |
| `PUT` | `/doctors/{id}` | ❌ | Update a doctor's details (specialization, fee, availability, etc.) |
| `DELETE` | `/doctors/{id}` | ❌ | Remove a doctor from the hospital |

> **Profile completion**: After registration, doctors have only `name` and `email`. Use `PUT /doctors/{id}` to update specialization, experience, consultation fee, phone, and availability.

#### Doctor Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Auto-generated unique ID |
| `userId` | Long | Links back to the `users` table (unique) |
| `name` | String | Doctor's full name |
| `specialization` | String | e.g., Cardiology, Neurology, Orthopedics |
| `experience` | Integer | Years of practice |
| `consultationFee` | Double | Fee per consultation |
| `phone` | String | Contact number |
| `email` | String | Email address (unique) |
| `available` | Boolean | Whether the doctor is currently available |

---

### 5. PatientService (`/PatientServices`) — Port 8083

Manages **patient medical records** — admissions, record lookups, updates, and discharge.

#### Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|:------------:|-------------|
| `POST` | `/patients/register` | ❌ (internal) | Called by AuthService during registration — creates record with basic info |
| `POST` | `/patients` | ❌ | Admit a new patient (create medical record manually) |
| `GET` | `/patients` | ❌ | List all patient records |
| `GET` | `/patients/{id}` | ❌ | Look up a specific patient's record |
| `PUT` | `/patients/{id}` | ❌ | Update a patient's details (gender, age, blood group, etc.) |
| `DELETE` | `/patients/{id}` | ❌ | Discharge / remove a patient record |

> **Profile completion**: After registration, patients have only `name` and `email`. Use `PUT /patients/{id}` to update gender, age, phone, address, blood group, and date of birth.

#### Patient Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Auto-generated unique ID |
| `userId` | Long | Links back to the `users` table (unique) |
| `name` | String | Patient's full name |
| `gender` | String | Male / Female / Other |
| `age` | Integer | Patient's age |
| `phone` | String | Contact number |
| `email` | String | Email address (unique) |
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

### Step 1: Register a Doctor

Registering creates **both** a `users` record (for auth) **and** a `doctors` record (for profile) automatically.

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

> ✅ A Doctor profile is now created in the `doctors` table with `name="Dr. Smith"`, `email="drsmith@hospital.com"`, and `available=true`.

### Step 2: Complete Doctor Profile

The auto-created doctor profile has only basic info. Update it with full details:

```bash
curl -X PUT http://localhost:8080/doctors/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. Smith",
    "specialization": "Cardiology",
    "experience": 12,
    "consultationFee": 500.00,
    "phone": "9876543210",
    "email": "drsmith@hospital.com",
    "available": true
  }'
```

### Step 3: Register a Patient

Same flow — creates both a `users` record and a `patients` record.

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "Rahul Sharma",
    "email": "rahul@email.com",
    "password": "password123",
    "role": "PATIENT"
  }'
```
**Response**: `PATIENT Registered Successfully`

> ✅ A Patient record is now created in the `patients` table with `name="Rahul Sharma"` and `email="rahul@email.com"`.

### Step 4: Complete Patient Profile

```bash
curl -X PUT http://localhost:8080/patients/1 \
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

### Step 5: Login

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

### Step 6: View Profile (Protected)

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
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   └── RestClientConfig.java       # Load-balanced RestClient for inter-service calls
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
│       │   ├── AuthService.java             # Calls Doctor/Patient services on registration
│       │   └── CustomUserDetailsService.java
│       └── util/JwtUtil.java
│
├── DoctorServices/                  # Doctor Management Service
│   └── src/main/java/com/example/demo/
│       ├── controller/DoctorController.java  # Includes POST /doctors/register
│       ├── dto/DoctorRegisterRequest.java    # DTO for registration
│       ├── entity/Doctor.java                # Has userId field
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
        ├── controller/PatientController.java  # Includes POST /patients/register
        ├── dto/PatientRegisterRequest.java    # DTO for registration
        ├── entity/Patient.java                # Has userId field
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

| Table | Created By | Key Fields | Description |
|-------|-----------|------------|-------------|
| `users` | AuthService | `id`, `email`, `role` | Stores registered users with hashed passwords |
| `doctors` | DoctorService | `id`, `userId` → `users.id`, `email` | Hospital staff directory (linked to users) |
| `patients` | PatientService | `id`, `userId` → `users.id`, `email` | Patient medical records (linked to users) |

> Tables are auto-created by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).  
> The `userId` column in `doctors` and `patients` tables links each domain record back to the corresponding entry in the `users` table.

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request
