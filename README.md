# Personal Finance Manager

A comprehensive personal finance management system built with Spring Boot 3.x that enables users to track income, expenses, and savings goals through a robust web-based application.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

## 📋 Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Design Decisions](#design-decisions)
- [Project Architecture](#project-architecture)
- [Testing](#testing)
- [Deployment](#deployment)

---

## ✨ Features

### User Management & Authentication
- **User Registration** with email, password, full name, and phone number
- **Session-based Authentication** with secure cookies
- **Data Isolation** - Users only access their own data
- **Secure Password Storage** using BCrypt hashing

### Transaction Management
- **Create Transactions** with amount, date, category, and description
- **View All Transactions** sorted by newest first
- **Filter Transactions** by date range, category, and type
- **Update Transactions** (all fields except date)
- **Delete Transactions** with automatic exclusion from reports

### Category Management
- **7 Default Categories** (Salary, Food, Rent, Transportation, Entertainment, Healthcare, Utilities)
- **Custom Categories** - Create income/expense categories
- **Category Protection** - Cannot delete categories in use
- **Default Category Preservation** - System categories cannot be modified

### Savings Goals
- **Create Goals** with name, target amount, and target date
- **Progress Tracking** - Automatic calculation: (Total Income - Total Expenses) since start date
- **Percentage Completion** - Visual progress indicators
- **Remaining Amount** - Real-time calculation
- **Update/Delete Goals** - Full CRUD operations

### Reports & Analytics
- **Monthly Reports** - Income and expenses by category with net savings
- **Yearly Reports** - Aggregated annual financial overview
- **Real-time Calculations** - Dynamic report generation

---

## 🛠 Technology Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.14 |
| **Security** | Spring Security 6.x |
| **Database** | H2 (Embedded) |
| **ORM** | JPA/Hibernate |
| **Build Tool** | Maven |
| **Testing** | JUnit 5, Mockito |

---

## 📦 Prerequisites

- **Java 21** or higher ([Download](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html))
- **Maven 3.6+** (included via Maven Wrapper)
- **Git** ([Download](https://git-scm.com/downloads))

---

## 🚀 Installation & Setup

### 1. Clone the Repository

\`\`\`bash
git clone https://github.com/phalgen/personal-finance-manager.git
cd personal-finance-manager
\`\`\`

### 2. Build the Project

**Windows:**
\`\`\`bash
mvnw.cmd clean install
\`\`\`

**macOS/Linux:**
\`\`\`bash
./mvnw clean install
\`\`\`

### 3. Configure Database (Optional)

The application uses an H2 file-based database by default. Configuration is in \`src/main/resources/application.properties\`:

\`\`\`properties
spring.datasource.url=jdbc:h2:file:./data/finance_db
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
\`\`\`

---

## ▶️ Running the Application

### Start the Server

**Windows:**
\`\`\`bash
mvnw.cmd spring-boot:run
\`\`\`

**macOS/Linux:**
\`\`\`bash
./mvnw spring-boot:run
\`\`\`

The application will start on **\`http://localhost:8080\`**

### Access the Application

- **Web Interface:** [http://localhost:8080](http://localhost:8080)
- **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: \`jdbc:h2:file:./data/finance_db\`
  - Username: \`sa\`
  - Password: *(leave empty)*

---

## 📚 API Documentation

See full API documentation in the [API.md](API.md) file or visit `/swagger-ui.html` when running locally.

### Quick Reference

**Authentication:**
- \`POST /api/auth/register\` - Register new user
- \`POST /api/auth/login\` - Login (creates session)
- \`POST /api/auth/logout\` - Logout

**Transactions:**
- \`GET /api/transactions\` - Get all transactions
- \`POST /api/transactions\` - Create transaction
- \`PUT /api/transactions/{id}\` - Update transaction
- \`DELETE /api/transactions/{id}\` - Delete transaction

**Categories:**
- \`GET /api/categories\` - Get all categories
- \`POST /api/categories\` - Create custom category
- \`DELETE /api/categories/{name}\` - Delete custom category

**Goals:**
- \`GET /api/goals\` - Get all goals
- \`POST /api/goals\` - Create goal
- \`PUT /api/goals/{id}\` - Update goal
- \`DELETE /api/goals/{id}\` - Delete goal

**Reports:**
- \`GET /api/reports/monthly/{year}/{month}\` - Monthly report
- \`GET /api/reports/yearly/{year}\` - Yearly report

---

## 🎯 Design Decisions

### 1. Session-Based Authentication
**Decision:** Use HTTP sessions with secure cookies instead of JWT tokens.

**Rationale:**
- Simpler implementation for web applications
- Better security - sessions stored server-side
- Automatic session management by Spring Security
- Easier session invalidation on logout

### 2. H2 Embedded Database
**Decision:** Use H2 file-based database for development and deployment.

**Rationale:**
- Zero configuration required
- No external database server needed
- Perfect for assignment/demo projects
- Easy to deploy on free hosting platforms
- Persistent storage via file system

### 3. Layered Architecture
**Decision:** Implement Controller → Service → Repository pattern.

**Rationale:**
- Clear separation of concerns
- Easy to test each layer independently
- Business logic isolated in service layer
- Controllers handle only HTTP concerns

### 4. DTOs (Data Transfer Objects)
**Decision:** Separate request/response DTOs from entity models.

**Rationale:**
- Prevents exposing internal entity structure
- Allows different validation rules for create/update
- Enables API versioning without changing entities
- Better security - no accidental data exposure

---

## 🏗 Project Architecture

### Package Structure

\`\`\`
com.finance.personalfinancemanager/
├── controller/          # REST API endpoints
├── service/             # Business logic layer
├── repository/          # Data access layer
├── entity/              # JPA entities
├── dto/                 # Data Transfer Objects
├── security/            # Security configuration
└── exception/           # Custom exceptions & handlers
\`\`\`

### Request Flow

\`\`\`
HTTP Request → SessionAuthFilter → Controller → Service → Repository → Database
\`\`\`

---

## 🧪 Testing

### Run Tests

\`\`\`bash
mvnw test
\`\`\`

**Target Coverage:** 80%+

---

## 🚀 Deployment

### Deploy to Render

1. Create account at [render.com](https://render.com)
2. Connect GitHub repository
3. Configure build:
   - Build Command: \`mvn clean install\`
   - Start Command: \`java -jar target/personal-finance-manager-0.0.1-SNAPSHOT.jar\`
4. Deploy!

---

## 📝 Error Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |

---

## 👨‍💻 Author

**Your Name**
- GitHub: [@YOUR_USERNAME](https://github.com/YOUR_USERNAME)

---

## 📄 License

MIT License

---

**⭐ Star this repository if you found it helpful!**
