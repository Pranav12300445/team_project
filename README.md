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
| **HTML5 & CSS3** | Client structure & styling with responsive CSS variables (Glassmorphism design) |
| **Vanilla JS (ES6+)** | Single Page Application logic, in-memory caching, routing, and fetch integrations |

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

### 6. AppointmentService (`/` at root) — Port 8084

Manages **appointment bookings** between patients and doctors, tracks appointment statuses, and exposes endpoints to let doctors accept/complete appointments.

#### Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|:------------:|-------------|
| `POST` | `/appointments` | ❌ | Book a new appointment (expects doctorId, patientId, date, time, reason) |
| `GET` | `/appointments` | ❌ | List all appointments |
| `GET` | `/appointments/{id}` | ❌ | Look up details for a specific appointment |
| `PUT` | `/appointments/{id}` | ❌ | Update appointment details |
| `PUT` | `/appointments/{id}/status` | ❌ | Update appointment status (e.g., set status to `CONFIRMED` / `COMPLETED` / `CANCELLED`) |
| `DELETE` | `/appointments/{id}` | ❌ | Delete / cancel an appointment |

#### Appointment Entity Fields
| Field | Type | Description |
|-------|------|-------------|
| `appointmentId` | Long | Auto-generated unique ID |
| `doctorId` | Long | ID of the doctor |
| `patientId` | Long | ID of the patient |
| `appointmentDate` | LocalDate | Date of the appointment |
| `appointmentTime` | LocalTime | Time of the appointment |
| `reason` | String | Reason for appointment |
| `status` | AppointmentStatus | Current status: `PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED` |

---

## 🖥️ Frontend Architecture (`/frontend`)

The client application is a responsive, highly performant **Single Page Application (SPA)** written using native web technologies for speed, simplicity, and portability.

```
                  ┌──────────────────────────────────────────────┐
                  │                 index.html                   │
                  │  (Single Page Web Interface / Portal Client)  │
                  └──────────────────────┬───────────────────────┘
                                         │
                        HTTP Requests /  │  localStorage (JWT)
                        REST API Calls   │  hms_token
                                         ▼
                  ┌──────────────────────────────────────────────┐
                  │            API Gateway (Port 8080)           │
                  └──────────────────────────────────────────────┘
```

### Key Technical Pillars:
- **Zero-Dependency SPA**: Built entirely with standard HTML5, modern semantic CSS3, and ES6+ JavaScript. No bundlers or compilers (like Webpack/Vite/Babel) are needed to launch, keeping the frontend fast and lightweight.
- **Glassmorphism Design & Styling**: Leverages CSS Variables (`:root`) to manage design tokens (colors, blur intensities, border radii). Features background blur filters, interactive micro-animations (smooth hover transforms, scales), custom scrollbars, and modern typography (Inter and JetBrains Mono) for a premium, responsive dashboard experience.
- **Client-Side Routing**: Navigates between views dynamically via a central state-driven function `navigate(page)` (e.g., Dashboard, My Profile, Book Appointment, My Appointments, and Patient Directory) without triggerring full page reloads.
- **Local In-Memory Cache**: Maintains transient caches (`doctorsCache`, `patientsCache`, `appointmentsCache`, `currentUser`, `myProfile`) to store service payloads locally, minimizing network latency.
- **Rich Interaction Utilities**: Implements dynamic notification overlays via a custom Toast system (`toast(message, type)`), custom-rendered loading spinner states, and dynamic confirmation dialog overlays (`#modal-root`).
- **Responsive Framework**: Adopts flexbox and grid layouts paired with specific media queries targeting devices below `768px` to swap layouts seamlessly from static sidebar components to fluid mobile viewports.

### Security & Session Management:
- **JWT Decoding**: Upon login, the client decodes the signature-less payload of the JWT token to extract the logged-in user's profile metadata (`id`, `username`, `role`, `email`, and `expiration`).
- **Session Persistence**: Saves the token securely in standard `localStorage` (`hms_token`) and automatically validates expiration times to prompt session logouts on timeout.
- **State-Dependent Route Guarding**: Dynamically alters the navigation menus based on whether the logged-in user has the `PATIENT` or `DOCTOR` role.
- **Token Injection Filter**: An API helper wrapper automatically configures request headers to pass the current token as `Authorization: Bearer <token>` for all calls to microservices.

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

# Terminal 5 — Appointment Service (Root)
./mvnw spring-boot:run

# Terminal 6 — API Gateway (start last)
cd ApiGateway
./mvnw spring-boot:run

# Terminal 7 — Frontend Application
# Double-click frontend/index.html to open in your browser, or serve it:
npx serve frontend
```

### 4. Verify

- **Eureka Dashboard**: http://localhost:8761 — all 4 services should show as `UP`
- **API Gateway**: http://localhost:8080 — single entry point for all APIs
- **Frontend Portal**: Open `frontend/index.html` in your browser (or at `http://localhost:3000` if using a local server like `serve`)

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

## 🔄 End-to-End System Workflow

The Hospital Management System operates through a synchronized workflow between the Frontend UI Portal, the API Gateway, and the microservices database layer. Below is the step-by-step lifecycle of the system.

### 🗺️ Unified Workflow Diagram

```
[Patient / Doctor]
      │
      ├─ 1. Register Account (index.html) ──▶ AuthService (Port 8081)
      │                                            │
      │                                            ├─ Save Auth Account (Auth DB)
      │                                            └─ REST Post (Eureka) ──▶ [Doctor/Patient Service]
      │                                                                           └─ Initialize Profile
      │
      ├─ 2. Log In & Fetch Token (index.html) ──▶ AuthService (Port 8081)
      │                                                 └─ Returns JWT Token
      │
      ├─ 3. Complete Profile Details (index.html) ──▶ [Doctor/Patient Service] (PUT update)
      │
      ├─ 4. Book Appointment (Patient) ──▶ AppointmentService (Port 8084) (Creates PENDING)
      │
      └─ 5. Review & Confirm (Doctor) ──▶ AppointmentService (Port 8084) (Updates to CONFIRMED/COMPLETED)
```

---

### 1. User Onboarding & Automated Service Sync

When a new user signs up in the frontend portal, the client sends a registration request containing the user's role (`DOCTOR` or `PATIENT`).

1. The client submits `POST /auth/register` to the API Gateway.
2. The Gateway routes the request to the `AuthService`.
3. The `AuthService` hashes the password and saves the account in the `users` table.
4. Using a load-balanced `RestClient` over Eureka, the `AuthService` calls the respective service (`DoctorService` or `PatientService`) to create a stub profile linked by `userId`.
5. The frontend handles the response, displays a toast, and guides the user to the login screen.

**REST Equivalent**:
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

---

### 2. Login & JWT Session Retrieval

The user enters their email and password to log in.

1. The client submits `POST /auth/login` to the Gateway.
2. `AuthService` validates the credentials and responds with a JWT token (24-hour expiration).
3. The frontend captures the token, saves it to `localStorage` under `hms_token`, decodes it to set up user state, and mounts the application dashboard layout.

**REST Equivalent**:
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

---

### 3. Profile Completion

The profile initialized during signup contains only basic info (name/email). The user can complete their onboarding in the **My Profile** view.

1. The user fills in missing details (specialization, experience, and fee for doctors; or age, gender, DOB, address, and blood group for patients).
2. The client sends a `PUT` request containing the updated fields to `/doctors/{id}` or `/patients/{id}` through the Gateway.
3. The target database updates the record, and the frontend updates its internal `myProfile` cache.

**REST Equivalent (Doctor Update)**:
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

---

### 4. Appointment Booking (Patient Flow)

Patients can search for available doctors and book visits.

1. The client fetches all doctors via `GET /doctors` and displays only those with `available: true`.
2. The patient fills in the date, time, and reason, and books the appointment.
3. The client submits `POST /appointments` to the `AppointmentService`.
4. The database stores the booking in a `PENDING` state, which instantly shows up on both the patient's and doctor's dashboards.

**REST Equivalent**:
```bash
curl -X POST http://localhost:8080/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": 1,
    "patientId": 1,
    "appointmentDate": "2026-07-15",
    "appointmentTime": "10:30:00",
    "reason": "Routine checkup"
  }'
```

---

### 5. Appointment Review & Actions (Doctor Flow)

Doctors can manage bookings assigned to them from the **My Appointments** dashboard.

1. The doctor views all appointments where `doctorId` matches their profile ID.
2. The doctor can invoke the following status changes:
   - **Accept**: Changes status to `CONFIRMED` (`PUT /appointments/{id}/status?status=CONFIRMED`).
   - **Complete**: Changes status to `COMPLETED` (`PUT /appointments/{id}/status?status=COMPLETED`).
   - **Cancel**: Changes status to `CANCELLED` (`PUT /appointments/{id}/status?status=CANCELLED`).
3. The client updates the appointment table in real-time.

**REST Equivalent (Confirming Status)**:
```bash
curl -X PUT http://localhost:8080/appointments/1/status?status=CONFIRMED \
  -H "Authorization: Bearer <token>"
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
├── PatientServices/                 # Patient Management Service
│   └── src/main/java/com/example/demo/
│       ├── controller/PatientController.java  # Includes POST /patients/register
│       ├── dto/PatientRegisterRequest.java    # DTO for registration
│       ├── entity/Patient.java                # Has userId field
│       ├── exception/
│       │   ├── ResourceNotFoundException.java
│       │   └── GlobalExceptionHandler.java
│       ├── repo/PatientRepository.java
│       └── service/
│           ├── PatientService.java
│           └── PatientServiceImpl.java
│
├── AppointmentService/              # Appointment Booking Service
│   └── src/main/java/com/shaan/appointmentservice/
│       ├── client/
│       │   ├── DoctorClient.java
│       │   └── PatientClient.java
│       ├── controller/AppointmentController.java
│       ├── dto/
│       │   ├── AppointmentRequest.java
│       │   ├── AppointmentResponse.java
│       │   ├── DoctorDTO.java
│       │   └── PatientDTO.java
│       ├── entity/Appointment.java
│       ├── enums/AppointmentStatus.java
│       ├── mapper/AppointmentMapper.java
│       └── service/
│           ├── AppointmentService.java
│           └── AppointmentServiceImpl.java
│
└── frontend/                        # Frontend Application (Portal UI)
    └── index.html                   # Single Page Application (HTML/CSS/JS)
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
| AppointmentService | 8084 | APPOINTMENT-SERVICE |
| MySQL | 3306 | — |

---

## 🗄️ Database

All services share a single MySQL database: `hospital_management_system`

| Table | Created By | Key Fields | Description |
|-------|-----------|------------|-------------|
| `users` | AuthService | `id`, `email`, `role` | Stores registered users with hashed passwords |
| `doctors` | DoctorService | `id`, `userId` → `users.id`, `email` | Hospital staff directory (linked to users) |
| `patients` | PatientService | `id`, `userId` → `users.id`, `email` | Patient medical records (linked to users) |
| `appointments` | AppointmentService | `appointment_id`, `doctor_id`, `patient_id` | Tracks patient appointment bookings |

> Tables are auto-created by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).  
> The `userId` column in `doctors` and `patients` tables links each domain record back to the corresponding entry in the `users` table.

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request
