# Survey Management System - Spring Boot (Backend)
This project is a **Survey Management Application** built using **Spring Boot** with a focus on employee management, survey tracking, onboarding email notifications, reminders, and admin dashboard analytics. It implements **JWT-based authentication** and provides secure access control for **ADMIN** users only.

## Features
✅ **Admin Login**: Secure login using JWT authentication  
✅ **Dashboard Analytics**: Filter surveys by Survey and Department to view participation  
✅ **Filled / Not-Filled Tracking**: Identify employees who completed or skipped surveys  

### Employee Management:
- Add employee manually  
- Batch upload via Excel/CSV  
- Get employee details  
- Edit/Update employee  

### Survey Management:
- Create a new survey  
- View survey form  
- Edit existing survey form  
- Delete survey  
- View survey responses  

✅ **Onboarding Email**: Send welcome emails to new employees  
✅ **Reminder Emails**: Send reminders to employees who haven't submitted the survey  
✅ **Real-Time Stats**: Dashboard insights  
✅ **Admin Profile View**  
✅ **Sidebar Toggle**  
✅ **Header with Company Logo**  
✅ **Works on Real MySQL Database**

## Entities
- **Admin** (adminId, name, email, password)  
- **Employee** (empId, name, email, department, status)  
- **Department** (deptId, deptName)  
- **Survey** (surveyId, title, description, startDate, endDate)  
- **EmployeeSurveyStatus** (empId, surveyId, filledStatus, filledDate)

## Database Structure
- **admins** – Stores admin credentials  
- **employees** – Stores employee master data  
- **departments** – Stores department list  
- **surveys** – Stores survey definitions  
- **employee_survey_status** – Tracks filled/not-filled survey status  

This backend is connected to a **real MySQL database** with full CRUD functionality using JPA/Hibernate.

## Endpoints
Here is the list of API endpoints available for testing:

### 1. Admin Authentication
- **POST /api/auth/login**: Admin Login  
  - Request Body: email, password  
  - Response: JWT token + admin details  

### 2. Dashboard Operations
- **GET /api/dashboard/summary**  
  Returns total employees, filled count, pending count  

- **GET /api/dashboard/filter?surveyId=&departmentId=**  
  Returns filtered list of filled and not-filled employees  

### 3. Employee Operations
- **GET /api/employees** — Get all employees  
  Response: Complete employee dataset  

- **GET /api/employees/{id}** — Get single employee details  

- **POST /api/employees** — Add new employee  
  Request Body: name, email, departmentId  

- **PUT /api/employees/{id}** — Update/Edit employee  
  Request Body: updated name, email, department, status  

- **POST /api/employees/upload** — Batch upload  
  Validates file and inserts employee records in bulk  

### 4. Onboarding Email Operations
- **POST /api/email/onboard/{employeeId}**  
  Send onboarding/welcome email  

### 5. Reminder Email Operations
- **POST /api/reminders/send**  
  Send reminders to employees who have not filled the survey  

### 6. Survey Operations
- **GET /api/surveys** — Get all surveys  

- **POST /api/surveys** — Create a new survey  
  Request Body: title, description, startDate, endDate  

- **GET /api/surveys/{surveyId}** — View a single survey form  

- **PUT /api/surveys/{surveyId}** — Edit/Update survey  

- **DELETE /api/surveys/{surveyId}** — Delete survey  

- **GET /api/surveys/{surveyId}/employees**  
  View filled and not-filled employees for that survey  

- **GET /api/surveys/{surveyId}/responses** — View survey responses  

## Technologies Used
- **Core Java**  
- **Spring Boot**  
- **Spring Security (JWT Authentication)**  
- **JPA/Hibernate**  
- **MySQL (Real Database)**  
- **Thymeleaf (Email Templates)**  
- **JavaMailSender**  
- **ModelMapper**  
- **Postman (API Testing)**  
- **Swagger**  
- **Maven (Dependency Management)**
