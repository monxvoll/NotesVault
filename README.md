![Status](https://img.shields.io/badge/status-in%20process-yellow) 

---

 # NotesVault 
NotesVault is a cloud-based application designed to manage notes efficiently and securely. It allows users to create, read, update, and delete notes with ease. The project was born from a personal interest in having a dedicated note management tool, with a strong focus on security, organization, and scalability.

## Current Technologies <img src="https://www.vectorlogo.zone/logos/java/java-icon.svg" alt="Java Logo" width="38"/> 
![Java](https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white) ![Firestore](https://img.shields.io/badge/Firestore-FFCA28?logo=firebase&logoColor=black) ![Postman](https://img.shields.io/badge/Postman-FF6C37?logo=postman&logoColor=white) ![Maven](https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white) 

## Current Features 🌱 
- Create, read, update, and delete notes (CRUD).
- Account confirmation through token validation via email.
- Account deletion through token validation via email.
- Soft delete: notes and accounts are marked as inactive instead of permanent removal.
- Password recovery through token validation.
- Cloud storage with Firestore.
- MVC architecture implemented with Spring Boot.

## 📌 Current Progress and Planned Improvements

- [x] Basic CRUD functionality for notes  
- [x] Authentication system with registration, login, and email confirmation  
- [x] Password recovery flow with token validation via email  
- [x] Soft delete: notes and accounts are marked as inactive instead of permanently removed
- [ ] Encrypt notes content before storage
- [ ] General Testing with github actions
- [ ] Token-based authentication for all note-related operations (CRUD)  
- [ ] Enhanced security measures (improved token handling, etc...)  
- [ ] Advanced search and tagging system for notes  
- [ ] Auto-save and real-time synchronization of notes across devices 
- [ ] Image support: attach and manage images within notes
- [ ] RESTful API fully documented and standardized  
- [ ] Frontend design prototype in Figma  
- [ ] Desktop application version (embedded web app)  
- [ ] Additional improvements coming soon... 

##  API Endpoints 📡

### 🔑 Authentication
- **POST**   `/auth/register`             Register a new user account  
- **GET**    `/auth/confirm`              Confirm account via email with verification token  
- **POST**   `/auth/resend-confirmation`  Resend account confirmation email with new token  
- **POST**   `/auth/login`                Authenticate user and receive access token  

### 🔐 Recovery
- **POST**   `/recovery/request`        Send recovery email with secure token  
- **POST**   `/recovery/verify-token`   Validate the recovery token  
- **POST**   `/recovery/reset-password` Set a new password using a valid token  
- **GET**    `/recovery/reset`          Handle recovery link validation from email  

### 📝 Notes (CRUD)
- **POST**   `/note/create`              Create a new note  
- **GET**    `/note/read`                Retrieve all notes for the authenticated user  
- **PATCH**  `/note/update/{noteId}`     Update an existing note by ID  
- **DELETE** `/note/delete`              Soft delete a note (mark as inactive)  

## 🧪 Testing
 All endpoints have been tested using  Postman.

---
