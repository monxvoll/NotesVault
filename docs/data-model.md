# Data Model

This diagram illustrates the core entities stored in Cloud Firestore.

```mermaid
erDiagram
    USER {
        string uid PK "Firebase UID"
        string email
        string userName
        boolean isActive "Soft delete flag"
        boolean isConfirmed "Email verification flag"
        timestamp createdAt
    }

    NOTE {
        string id PK "Document ID"
        string title
        string content
        string date "Creation or modification date"
        boolean isActive "Soft delete flag"
        string userId FK "Reference to the User (implicit)"
    }

    USER ||--o{ NOTE : "owns"
```

## Entities

### User
Represents a user account in the system. The `uid` matches the identifier provided by Firebase Authentication.
- **Soft Delete**: Instead of permanently deleting the user, the `isActive` flag is set to false.

### Note
Represents a text note created by a user.
- **Soft Delete**: Deleting a note sets `isActive` to false, preserving the data for potential recovery.
