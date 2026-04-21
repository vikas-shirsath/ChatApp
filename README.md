# 🔒 SecureChat — End-to-End Encrypted Chat Application

A production-ready, real-time chat backend built with **Spring Boot**, **Supabase (PostgreSQL)**, and **STOMP WebSockets**. All messages are end-to-end encrypted — the server only relays ciphertext and **never** decrypts messages.

---

## ✨ Features

- **End-to-End Encryption (E2EE)** — RSA + AES hybrid encryption. The server never sees plaintext.
- **Real-Time Messaging** — STOMP over WebSockets with SockJS fallback.
- **Direct Messages** — One-to-one encrypted conversations.
- **Group Messaging** — Create groups, manage members, and send encrypted group messages.
- **JWT Authentication** — Stateless, token-based auth with BCrypt password hashing.
- **Online Presence** — Track user online/offline status via WebSocket events.
- **Message Status** — Sent → Delivered → Read tracking.
- **Supabase PostgreSQL** — Cloud-hosted database with connection pooling.

---

## 🏗️ Architecture

```
┌──────────────┐     STOMP/WS      ┌──────────────────────┐      JDBC       ┌──────────────┐
│              │◄──────────────────►│                      │◄───────────────►│              │
│   Frontend   │     REST API      │   Spring Boot 3.4    │                 │   Supabase   │
│   (Client)   │◄──────────────────►│   (Java 17)          │                 │  PostgreSQL  │
│              │                   │                      │                 │              │
└──────────────┘                   └──────────────────────┘                 └──────────────┘
                                     │  Security (JWT)
                                     │  WebSocket Broker
                                     │  JPA / Hibernate
```

### Project Structure

```
src/main/java/com/chatapp/
├── ChatAppApplication.java          # Entry point
├── config/
│   ├── SecurityConfig.java          # Spring Security (CORS, CSRF, JWT filter)
│   ├── JwtAuthenticationFilter.java # JWT token validation filter
│   ├── WebSocketConfig.java         # STOMP + SockJS configuration
│   └── WebSocketAuthInterceptor.java
├── util/
│   └── JwtUtil.java                 # JWT generation & validation
├── model/
│   ├── User.java                    # User entity (UUID, publicKey for E2EE)
│   ├── Message.java                 # Encrypted message entity
│   ├── Group.java                   # Chat group entity
│   ├── GroupMember.java             # Group membership
│   ├── GroupEncryptedKey.java       # Per-user encrypted group keys
│   └── enums/
│       ├── MessageStatus.java       # SENT, DELIVERED, READ
│       └── GroupRole.java           # ADMIN, MEMBER
├── dto/                             # Request/Response DTOs
│   ├── auth/                        # RegisterRequest, LoginRequest, AuthResponse
│   ├── message/                     # SendMessageRequest, MessageResponse, etc.
│   ├── group/                       # CreateGroupRequest, GroupResponse, etc.
│   └── user/                        # UserResponse, UserPublicKeyRequest
├── repository/                      # Spring Data JPA repositories
├── service/                         # Business logic layer
│   ├── AuthService.java
│   ├── UserService.java
│   ├── MessageService.java
│   ├── GroupService.java
│   ├── PresenceService.java
│   └── CustomUserDetailsService.java
├── controller/                      # REST + WebSocket controllers
│   ├── AuthController.java
│   ├── UserController.java
│   ├── MessageController.java
│   ├── GroupController.java
│   └── WebSocketMessageController.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── ResourceNotFoundException.java
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|-------------|---------|
| Java (JDK)  | 17+     |
| Git         | Any     |
| Supabase Account | [supabase.com](https://supabase.com) |

> **Note:** Maven is **not** required — the project includes a Maven wrapper (`mvnw.cmd`).

### 1. Clone the Repository

```bash
git clone https://github.com/vikas-shirsath/ChatApp.git
cd ChatApp
```

### 2. Configure Supabase

1. Create a project at [supabase.com](https://supabase.com)
2. Go to **Settings → Database → Connection string → Session Pooler**
3. Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://<POOLER_HOST>:5432/postgres
spring.datasource.username=postgres.<PROJECT_REF>
spring.datasource.password=<YOUR_PASSWORD>
```

> **Important:** Use the **Session Pooler** URL (not direct connection) for IPv4 compatibility.

### 3. Run the Application

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

The server starts at **http://localhost:8080**

### 4. Verify

```bash
curl http://localhost:8080/auth/health
# Response: "ChatApp is running!"
```

---

## 📡 API Endpoints

See [API_REFERENCE.txt](API_REFERENCE.txt) for complete documentation with request/response examples.

### Quick Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/register` | Register new user |
| `POST` | `/auth/login` | Login, get JWT |
| `GET` | `/auth/health` | Health check |
| `GET` | `/users` | List all users |
| `GET` | `/users/{id}` | Get user profile |
| `PUT` | `/users/{id}/public-key` | Upload E2EE public key |
| `GET` | `/users/{id}/public-key` | Get user's public key |
| `GET` | `/users/{id}/online` | Check online status |
| `POST` | `/messages/send` | Send encrypted DM |
| `GET` | `/messages/{userId}?currentUserId=X` | Chat history |
| `PATCH` | `/messages/{id}/status` | Update read status |
| `GET` | `/messages/undelivered/{userId}` | Undelivered messages |
| `POST` | `/groups` | Create group |
| `GET` | `/groups/user/{userId}` | User's groups |
| `POST` | `/groups/{id}/members` | Add member |
| `DELETE` | `/groups/{id}/members/{userId}` | Remove member |
| `POST` | `/groups/{id}/messages` | Send group message |
| `GET` | `/groups/{id}/messages` | Group message history |
| `POST` | `/groups/{id}/keys` | Store encrypted group keys |
| `GET` | `/groups/{id}/keys` | Get group keys |

### WebSocket Endpoints

| Direction | Destination | Purpose |
|-----------|-------------|---------|
| Client → Server | `/app/chat.send` | Send direct message |
| Client → Server | `/app/chat.group` | Send group message |
| Client → Server | `/app/chat.status` | Update message status |
| Server → Client | `/user/{id}/queue/messages` | Receive messages |
| Server → Client | `/user/{id}/queue/status` | Receive status updates |

**WebSocket URL:** `ws://localhost:8080/ws` (SockJS: `http://localhost:8080/ws`)

---

## 🔐 E2EE Message Flow

```
1. Alice wants to send "Hello Bob"

2. Client-side (Alice):
   ├── Generate random AES-256 key
   ├── Encrypt "Hello Bob" with AES → encryptedPayload
   ├── Fetch Bob's RSA public key: GET /users/{bobId}/public-key
   ├── Encrypt AES key with Bob's public key → encryptedKey
   └── Send { encryptedPayload, encryptedKey } to server

3. Server (relay only):
   ├── Store encrypted blobs in database
   └── Forward to Bob via WebSocket (NO decryption)

4. Client-side (Bob):
   ├── Receive { encryptedPayload, encryptedKey }
   ├── Decrypt encryptedKey with his RSA private key → AES key
   └── Decrypt encryptedPayload with AES key → "Hello Bob"
```

> **The server NEVER sees plaintext.** It only stores and forwards ciphertext.

---

## 🗄️ Database Schema

The schema is auto-created by Hibernate (`ddl-auto=update`). For manual setup, run [supabase_schema.sql](supabase_schema.sql) in the Supabase SQL Editor.

| Table | Purpose |
|-------|---------|
| `users` | User accounts with E2EE public keys |
| `messages` | Encrypted DM and group messages |
| `chat_groups` | Group chat rooms |
| `group_members` | Group membership (ADMIN/MEMBER roles) |
| `group_encrypted_keys` | Per-user encrypted symmetric group keys |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 3.4.4 |
| Language | Java 17 |
| Database | PostgreSQL (Supabase) |
| ORM | Hibernate 6 / Spring Data JPA |
| Auth | JWT (JJWT 0.12.6) + BCrypt |
| Real-time | STOMP over WebSocket + SockJS |
| Build | Maven (with wrapper) |

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
