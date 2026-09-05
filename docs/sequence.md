# Sequence Diagrams

These diagrams illustrate the secure authentication flow implemented using **Spring Security** and **Firebase Auth**. They highlight how requests are intercepted to validate JWT tokens before reaching the protected endpoints.

## 1. Authenticated Request Flow

This diagram shows how a request with a valid token is processed:

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client
    participant Filter as TokenFilter
    participant Firebase as Firebase Auth
    participant Controller as Controller
    participant DB as Firestore

    Client->>Filter: HTTP Request (Bearer Token)
    
    Filter->>Firebase: Verify Token Signature & Expiration
    Firebase-->>Filter: Token Valid (Returns UID)
    
    Note over Filter: User Authenticated
    
    Filter->>Controller: Forward Request
    Controller->>DB: Process Data (CRUD)
    DB-->>Controller: Success
    Controller-->>Client: 200 OK / Data Response
```

## 2. Error / Unauthorized Request Flow

When a request lacks a valid token or validation fails:

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client
    participant Filter as TokenFilter
    participant Security as Spring Security

    Client->>Filter: HTTP Request (No Token / Invalid Token)
    
    Note over Filter: Validation Fails
    
    Filter-->>Security: Proceed as "Anonymous User"
    
    Security->>Security: Check Route Rules
    
    Note over Security: Protected Route + Anonymous User
    
    Security--xClient: 403 Forbidden (or 401 Unauthorized)
```
