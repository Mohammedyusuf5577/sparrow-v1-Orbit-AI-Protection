# ORBIT - AI Based Organizational Risk & Intelligence Platform (Admin Module)

This repository contains the source code and documentation for the **ORBIT Admin Module**, an enterprise-level platform for organizational risk assessment and monitoring.

## 📚 Project Documentation
The complete project specification, architecture, database schema, and implementation plan can be found in:

👉 **[ORBIT Admin Module Specification](docs/ORBIT_ADMIN_MODULE_SPEC.md)**

## 🚀 Key Features (Admin Only)
- **Employee Monitoring**: Salary, Attendance, Leave requests.
- **Project Management**: Hierarchy, Deadline tracking, delayed project detection.
- **AI Risk Engine**: Automated background analysis of project delays and employee patterns.
- **Role-Based Security**: Secure JWT authentication for Admins only.

## 🛠 Tech Stack
- **Backend**: Java 17, Spring Boot 3, PostgreSQL, Flyway, JWT.
- **Frontend**: React, TypeScript, Tailwind CSS.
- **DevOps**: Gradle, Docker (optional).

## ⚡ Quick Start
1. Install Java 17 and PostgreSQL, and create the database.
2. Provide secrets (they are **not** stored in the repo) in one of two ways:
   - environment variables listed in [`.env.example`](.env.example), or
   - a gitignored file `src/main/resources/application-local.properties` with the keys
     `spring.datasource.password`, `app.jwt.secret`, `app.encryption.secret`,
     `razorpay.key.id`, `razorpay.key.secret`, `razorpay.webhook.secret`, `razorpay.account.number`.
3. Run the API: `./gradlew bootRun` (http://localhost:8080, Swagger at `/swagger-ui.html`).
4. Run the admin portal from the `sparrow-portal-v2-ORBIT-AI-Protection` repo: `npm ci && npm run dev` (http://localhost:3000, proxies `/api` to the backend).

### Environments (Spring profiles)
| Profile | Port | Database | Activate |
|---|---|---|---|
| *(default)* dev | 8080 | `postgresql_v1_orbit_co_in` | nothing to do |
| `stage` | 8081 | `postgresql_stage_v1_orbit_co_in` (user `orbit_stage_user`) | `SPRING_PROFILES_ACTIVE=stage` or `./gradlew bootRun --args='--spring.profiles.active=stage'` |

Dev and stage can run side by side (different ports, different databases).
The admin portal's Vite proxy targets `http://localhost:8080` (dev); change it to `8081` to use stage.

The stage profile lives in `src/main/resources/application-stage.properties`. It contains no secrets:
provide `STAGE_DB_PASSWORD` (and optionally `STAGE_DB_HOST`, `STAGE_DB_PORT`, `STAGE_DB_NAME`,
`STAGE_DB_USERNAME`) as environment variables, or put `STAGE_DB_PASSWORD` in the gitignored
`application-local.properties`. The schema is created automatically by Hibernate on first start.

For architecture and schema details see the documentation above.