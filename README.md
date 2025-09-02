
![Status](https://img.shields.io/badge/status-in%20process-yellow)

---


# NotesVault

NotesVault is a cloud-based application for managing notes, allowing users to easily and securely create, read, update, and delete their notes.

NotesVault was born from a personal interest in having my own application to manage notes, with a focus on **security**, **organization**, and **scalability**.

##  Current Technologies <img src="https://www.vectorlogo.zone/logos/java/java-icon.svg" alt="Java Logo" width="38"/>

![Java](https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white)
![Firestore](https://img.shields.io/badge/Firestore-FFCA28?logo=firebase&logoColor=black)
![Postman](https://img.shields.io/badge/Postman-FF6C37?logo=postman&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)



## Current Features 🌱

- Create, read, update, and delete notes (CRUD).
- Account confirmation through token validation via email.
- Account deletion through token validation via email.
- Soft delete: notes and accounts are marked as inactive instead of permanent removal.
- Password recovery through token validation.
- Cloud storage with Firestore.
- MVC architecture implemented with Spring Boot.

## API Endpoints 📡

🔐 Authentication

- POST /auth/register — Registers a new user.

- POST /auth/login — Logs in with valid credentials.

- GET /auth/confirm — Confirms an account via email.

- POST /auth/resend-confirmation — Resends the account confirmation email.

👤 Account Management

- DELETE /account/deleteAccount — Requests account deletion.

- GET /account/delete-confirmation — Confirms account deletion via email.

- POST /resend-delete-confirmation — Resends the account deletion confirmation email.

🔑 Password Recovery

- POST /recovery/request — Requests a password reset.

- POST /recovery/verify-token — Verifies if a recovery token is valid.

- POST /recovery/reset-password — Resends the password recovery email.

- GET /recovery/reset — Resets the user’s password.

📝 Notes

- POST /note/create — Creates a new note.

- GET /note/read — Retrieves the user’s notes.

- PATCH /note/update — Updates an existing note.

- DELETE /note/delete — Deletes a note.


## 🧪 Testing
All API endpoints have been tested using Postman.

---

*README in process…*
