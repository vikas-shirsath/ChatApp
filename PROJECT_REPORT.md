# Pimpri Chinchwad Education Trust's
# PIMPRI CHINCHWAD COLLEGE OF ENGINEERING
### (An Autonomous Institute affiliated to Savitribai Phule Pune University)
## DEPARTMENT OF INFORMATION TECHNOLOGY

---

## MINI PROJECT REPORT
### on
## "SecureChat – End-to-End Encrypted Chat Application"

**Submitted by**

**Academic Year: 2025–26**

---

## TABLE OF CONTENTS

1. Introduction
2. Frontend Development
3. Backend Development
4. Authentication & Security
5. Frontend–Backend Integration
6. Deployment
7. Testing and Validation
8. Results and Discussion
9. Conclusion
10. References

---

# 1. Introduction

## 1.1 Background

SecureChat is a full-stack web application designed to provide secure, real-time, end-to-end encrypted messaging between users. In traditional messaging systems, messages are stored as plaintext on the server, making them vulnerable to data breaches, unauthorized surveillance, and privacy violations. Even popular platforms have faced criticism for storing user messages in readable formats on their servers.

As digital communication becomes the primary mode of interaction, the need for privacy-preserving communication tools has become critical. Organizations handling sensitive data, healthcare providers, legal firms, and privacy-conscious individuals require messaging platforms that guarantee confidentiality. SecureChat addresses these challenges by implementing RSA-2048 key exchange with AES-256-GCM message encryption, ensuring that messages can only be read by the intended sender and receiver. Not even the server can decrypt the messages.

By integrating modern web technologies including React, Spring Boot, and WebSocket communication, SecureChat provides a seamless, real-time messaging experience with enterprise-grade security.

## 1.2 Problem Statement

The existing messaging systems suffer from several limitations:

- Messages are stored as plaintext on servers, making them vulnerable to data breaches
- Lack of true end-to-end encryption where only communicating parties can read messages
- No client-side key management for ensuring zero-knowledge architecture
- Absence of real-time message delivery with encryption
- No notification system for messages received while viewing other conversations
- Limited group communication capabilities with encryption support
- Increased risk of data leakage and unauthorized access to private conversations

These issues highlight the need for a robust, encrypted messaging platform that ensures privacy, real-time communication, and secure group collaboration.

## 1.3 Objectives

The primary objectives of the SecureChat system are:

- To implement true end-to-end encryption using RSA-2048 and AES-256-GCM algorithms
- To provide real-time messaging using WebSocket (STOMP over SockJS) protocol
- To implement secure user authentication using JWT tokens
- To enable encrypted group chat functionality
- To provide a contact-based messaging system with user search capability
- To deliver real-time toast notifications for incoming messages
- To maintain a zero-knowledge architecture where the server cannot read message content
- To deploy the application on cloud platforms for global accessibility

---

# 2. Frontend Development

## 2.1 UI/UX Design

The frontend of SecureChat is developed using React 18 (JavaScript), a modern library for building user interfaces, along with vanilla CSS following a premium dark-theme design system. The design focuses on visual excellence, usability, and a startup-level aesthetic.

The application provides the following user-facing pages:

- Landing Page
- Login Page
- Registration Page
- Chat Dashboard

The UI uses:

- Dark color palette with purple (#6c5ce7) and teal (#00d2a0) accent colors
- Glassmorphism effects with backdrop blur on cards and modals
- Animated gradient floating orbs for visual depth
- Inter font family from Google Fonts for modern typography
- Smooth micro-animations for message bubbles, modals, and transitions
- Responsive layout adapting to mobile, tablet, and desktop screens

This results in a visually stunning, premium, and user-friendly interface.

## 2.2 Components and Structure

The frontend follows a component-based architecture, improving code reusability, scalability, and maintainability. Each part of the UI is divided into reusable components.

**Key Components:**

**Sidebar Component**
The Sidebar provides navigation and contact management. It displays:
- Branded header with gradient logo
- Search bar for finding new users by username
- Create Group button
- Groups section showing user's groups
- Messages section showing contacts (users with existing conversations)
- Unread message count badges
- Online/offline status indicators with green dots

**ChatWindow Component**
The main messaging area that handles:
- Loading and decrypting chat history
- Real-time incoming message display via WebSocket
- End-to-end encryption before sending messages
- Auto-scroll to latest messages
- E2EE status badge in the top bar
- Sending state management

**MessageBubble Component**
Displays individual messages with:
- Gradient purple bubbles for sent messages
- Dark themed bubbles for received messages
- Timestamp display
- Read receipt indicators (✓ sent, ✓✓ delivered/read)
- Fade-in animation on appearance

**CreateGroup Component**
A modal dialog for creating group conversations:
- Group name input
- User search to add members
- Selected members shown as removable chips
- Form validation and error handling

**Toast Component**
Real-time notification system:
- Slide-in animation from the right
- Shows sender name and message preview
- Auto-dismisses after 4 seconds
- Click to open the conversation

**EmptyState Component**
Displayed when no conversation is selected, showing a welcome message and instructions.

## 2.3 Routing and Navigation

The application uses React Router (v6) for client-side routing, enabling smooth navigation without page reloads.

Route protection is implemented using two guard components:

- **ProtectedRoute**: Checks if the user is logged in; redirects to /login if not
- **PublicRoute**: Redirects authenticated users to /chat

Routes are defined for:
- Public pages: Landing (/), Login (/login), Register (/register)
- Protected pages: Chat Dashboard (/chat)

## 2.4 Responsiveness

The frontend is designed to be fully responsive using CSS media queries and flexible layouts. The layout adapts to different screen sizes using:

- Flexbox-based layouts
- CSS Grid for feature cards on the landing page
- Responsive font sizing with clamp()
- Adaptive component sizing

This ensures that the application works smoothly on mobile devices, tablets, and desktop systems.

## 2.5 State Management and API Handling

The frontend uses React hooks and Context API for state management:

- **useState** for managing component state
- **useEffect** for handling lifecycle events and side effects
- **useRef** for maintaining mutable references across renders
- **useCallback** for memoizing event handlers
- **AuthContext** (Context API) for global authentication state management

API communication is handled using Axios with a centralized instance configured with:
- Dynamic base URL via VITE_API_URL environment variable
- JWT token interceptor that attaches the Authorization header to every request
- Token persistence in localStorage

## 2.6 End-to-End Encryption (Client-Side)

The frontend implements true E2EE using the Web Crypto API (zero external dependencies):

**Key Generation (Registration):**
- RSA-OAEP 2048-bit keypair is generated using window.crypto.subtle.generateKey
- Public key is exported and uploaded to the server
- Private key is stored securely in localStorage

**Message Encryption (Sending):**
1. A random AES-256-GCM key is generated per message
2. The plaintext message is encrypted with AES-GCM
3. The AES key is encrypted with the receiver's RSA public key
4. Both ciphertexts (encryptedPayload + encryptedKey) are sent to the server

**Message Decryption (Receiving):**
1. The encrypted AES key is decrypted using the receiver's RSA private key
2. The message payload is decrypted using the recovered AES key
3. The plaintext is displayed in the chat window

Sent messages are cached in localStorage so they can be displayed after page reload.

---

# 3. Backend Development

## 3.1 Spring Boot Architecture

The backend of SecureChat is developed using Spring Boot 3.4.4, which provides a robust framework for building scalable and production-ready applications. The system follows a layered architecture, ensuring proper separation of concerns and modular design.

The architecture consists of the following layers:

- **Controller Layer**: Handles incoming HTTP requests and WebSocket messages using REST APIs and STOMP message mappings
- **Service Layer**: Contains the core business logic, including message processing, user management, group management, and presence tracking
- **Repository Layer**: Uses Spring Data JPA to perform database operations with custom JPQL queries
- **Model Layer**: Defines entity classes such as User, Message, Group, and GroupMember
- **DTO Layer**: Data Transfer Objects for request/response serialization
- **Config Layer**: Security, WebSocket, and CORS configuration

The application uses constructor-based dependency injection via Lombok's @RequiredArgsConstructor, which improves testability and ensures loose coupling between components.

## 3.2 REST API Design

The backend exposes a comprehensive set of RESTful APIs enabling communication between frontend and backend systems. These APIs follow standard HTTP methods and REST principles.

**Authentication APIs:**
- POST /auth/register – User registration with BCrypt password hashing
- POST /auth/login – User login with JWT token generation
- GET /auth/health – Health check endpoint

**User Management APIs:**
- GET /users – Get all registered users
- GET /users/search?username=xxx – Search users by username
- GET /users/{userId} – Get user by ID
- PUT /users/{userId}/public-key – Upload RSA public key
- GET /users/{userId}/public-key – Retrieve user's public key
- GET /users/{userId}/online – Check online status

**Message APIs:**
- POST /messages/send – Send an encrypted message
- GET /messages/{otherUserId}?currentUserId=xxx – Get chat history
- PATCH /messages/{messageId}/status – Update message status
- GET /messages/undelivered/{userId} – Get undelivered messages
- GET /messages/conversations/{userId} – Get conversation partners

**Group APIs:**
- POST /groups – Create a new group
- GET /groups/user/{userId} – Get user's groups
- POST /groups/{groupId}/members – Add members to group
- GET /groups/{groupId}/messages – Get group messages
- POST /groups/{groupId}/messages – Send group message

**WebSocket Destinations:**
- /ws – SockJS endpoint
- /app/chat.send – Send direct message via WebSocket
- /app/chat.group – Send group message via WebSocket
- /user/{userId}/queue/messages – Receive messages
- /user/{userId}/queue/status – Receive status updates

Each API returns structured JSON responses and uses appropriate HTTP status codes such as 200 (success), 400 (bad request), and 404 (not found).

## 3.3 Business Logic Implementation

**Message Processing:**
When a message is sent via the REST API, the service layer:
1. Validates that both sender and receiver exist in the database
2. Creates a Message entity with the encrypted payload and encrypted key
3. Saves the message with SENT status
4. Returns the message response to the sender

**Contact Discovery:**
The system derives contacts from message history using a UNION query that finds all distinct user IDs that a given user has exchanged messages with, eliminating the need for a separate contacts table.

**Presence Tracking:**
Online/offline status is tracked using the PresenceService, which monitors WebSocket session connections and disconnections.

## 3.4 Database Configuration

The backend uses PostgreSQL (hosted on Supabase) as the database management system. Hibernate ORM is used to map Java objects to relational tables.

**Core Tables:**

**Users Table**
- id (UUID, primary key)
- username (unique, not null)
- email (unique, not null)
- password (BCrypt hashed)
- public_key (TEXT, RSA public key)
- created_at (timestamp)

**Messages Table**
- id (UUID, primary key)
- sender_id (UUID, foreign key to users)
- receiver_id (UUID, nullable, foreign key to users)
- group_id (UUID, nullable, foreign key to groups)
- encrypted_payload (TEXT, AES-encrypted message)
- encrypted_key (TEXT, RSA-encrypted AES key)
- status (ENUM: SENT, DELIVERED, READ)
- timestamp (timestamp)

**Groups Table**
- id (UUID, primary key)
- name (VARCHAR)
- created_by (UUID, foreign key to users)
- created_at (timestamp)

**Group Members Table**
- id (UUID, primary key)
- group_id (UUID, foreign key to groups)
- user_id (UUID, foreign key to users)
- joined_at (timestamp)

Foreign key relationships ensure referential integrity between tables.

## 3.5 CRUD Operations Implementation

### 3.5.1 Create
- Users can register new accounts
- Users can send encrypted messages
- Organizers can create groups and add members

### 3.5.2 Read
- Retrieve chat history between two users
- Fetch conversation partners (contacts)
- Search users by username
- Get group messages and member lists
- Retrieve user's public keys

### 3.5.3 Update
- Update message status (SENT → DELIVERED → READ)
- Update user's RSA public key
- Add new members to existing groups

### 3.5.4 Delete
- Remove group members when necessary
- Maintain data consistency across related tables

## 3.6 Real-time Communication (WebSocket)

The system implements real-time messaging using STOMP protocol over SockJS:

- **Simple Broker**: In-memory message broker for /queue (user-specific) and /topic (broadcast)
- **Application Prefix**: /app for @MessageMapping methods
- **User Destination Prefix**: /user for user-specific message delivery
- **SockJS Fallback**: Ensures compatibility across browsers and network configurations
- **Reconnection**: Client automatically reconnects with 5-second delay on disconnection
- **Heartbeat**: 4-second heartbeat intervals for connection health monitoring

## 3.7 Data Validation and Error Handling

The backend uses Data Transfer Objects (DTOs) with Jakarta validation annotations to validate incoming data.

Validation ensures:
- Required fields are present
- Correct data formats are used
- Invalid inputs are rejected with meaningful error messages

Error handling is implemented using:
- Global exception handler (@ControllerAdvice)
- Custom ResourceNotFoundException
- HTTP status codes (400, 401, 404, 500)
- Structured JSON error responses

---

# 4. Authentication & Security

## 4.1 User Registration and Login

SecureChat implements a secure authentication system using JWT (JSON Web Token).

**User Registration:**
During registration, users provide:
- Username (unique, minimum 3 characters)
- Email (unique, valid format)
- Password (minimum 6 characters)

Before storing the data:
- The system checks for duplicate username and email entries
- The password is encrypted using BCrypt hashing
- An RSA-2048 keypair is generated on the client
- The public key is uploaded to the server
- The private key is stored in the browser's localStorage

**User Login:**
- Users log in using username and password
- The system verifies credentials using BCrypt password matching
- Upon successful authentication, a JWT token is generated containing user ID and role
- The token is sent to the frontend and stored in localStorage
- The token is included in every subsequent API request

This ensures a stateless authentication mechanism, improving scalability and performance.

## 4.2 End-to-End Encryption

SecureChat implements true end-to-end encryption using the Web Crypto API:

**RSA-OAEP (2048-bit)** for asymmetric key exchange:
- Each user generates an RSA keypair during registration
- Public keys are stored on the server
- Private keys never leave the client device

**AES-256-GCM** for symmetric message encryption:
- A unique AES key is generated for each message
- Messages are encrypted with AES-GCM (authenticated encryption)
- The AES key is encrypted with the receiver's RSA public key
- Both ciphertexts are sent to the server

**Zero-Knowledge Architecture:**
- The server stores only encrypted data
- The server cannot decrypt messages (it doesn't have private keys)
- Even in case of a database breach, messages remain unreadable

## 4.3 Security Measures

SecureChat incorporates multiple security mechanisms:

**JWT-Based Authentication**
- Stateless authentication using JSON Web Tokens
- Tokens include expiration time (24 hours default)
- Token verification on every API request

**Password Encryption (BCrypt)**
- Passwords are hashed using BCrypt before storage
- Plaintext passwords are never stored or transmitted
- BCrypt provides protection against brute-force attacks

**CORS Configuration**
- Cross-Origin Resource Sharing configured for frontend-backend communication
- AllowedOriginPatterns set for deployment flexibility
- Credentials support enabled for WebSocket connections

**Data Protection**
- Messages stored as encrypted ciphertext in the database
- Private keys never transmitted to the server
- Sensitive fields excluded from API responses

---

# 5. Frontend–Backend Integration

## 5.1 API Integration

The frontend and backend are integrated using REST APIs and WebSocket connections. The frontend uses Axios for HTTP requests and STOMP/SockJS for real-time communication.

**Key Features of API Integration:**
- Centralized Axios instance with configurable base URL (VITE_API_URL)
- JWT tokens attached to request headers using an interceptor
- Authorization header format: `Authorization: Bearer <JWT_TOKEN>`
- WebSocket connection established on chat page load
- Real-time message delivery through STOMP subscriptions

## 5.2 Data Flow Mechanism

**Sending a Message:**
1. User types a message and clicks send
2. Frontend fetches receiver's RSA public key from server
3. Message is encrypted using AES-256-GCM with a random key
4. AES key is encrypted with receiver's RSA public key
5. Encrypted payload + encrypted key sent via REST API
6. Backend saves encrypted message and relays via WebSocket
7. Receiver's client decrypts and displays the message

**Receiving a Message (Real-time):**
1. WebSocket receives incoming message event
2. If the sender's chat is currently open → message appears instantly
3. If the sender's chat is NOT open → toast notification shown + unread badge incremented
4. Clicking the notification opens the conversation

## 5.3 Error Handling

**Backend Error Handling:**
- Exception handling in service and controller layers
- HTTP status codes: 400 (Bad Request), 401 (Unauthorized), 404 (Not Found), 500 (Internal Server Error)
- Meaningful error messages in JSON format

**Frontend Error Handling:**
- Axios interceptors capture API errors
- Error messages displayed to users via UI notifications
- Graceful decryption failure handling (shows fallback text)
- Loading states and disabled buttons during async operations

---

# 6. Deployment

## 6.1 Deployment Platform

The SecureChat application is deployed using modern cloud platforms:

- **Frontend**: Deployed on **Vercel**, providing fast hosting for React applications with automatic builds and continuous deployment from GitHub
- **Backend**: Deployed on **Render**, a cloud platform supporting Docker-based Spring Boot applications with automatic deployment from GitHub
- **Database**: **Supabase** (PostgreSQL) as a cloud-hosted database via session pooler connection

## 6.2 Deployment Steps

**Step 1: Database Setup (Supabase)**
- PostgreSQL database provisioned on Supabase
- Tables auto-created by Hibernate ORM (ddl-auto=update)
- Session pooler connection used for IPv4 compatibility

**Step 2: Push Code to GitHub**
- All project files committed to Git repository
- Repository used for deployment on both platforms

**Step 3: Backend Deployment (Render)**
- Connect Render account with GitHub
- Import the backend project repository
- Runtime set to Docker (multi-stage Dockerfile)
- Environment variables configured: DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD, JWT_SECRET
- Deploy the Spring Boot application
- Obtain the backend API URL

**Step 4: Frontend Deployment (Vercel)**
- Import frontend directory from GitHub into Vercel
- Root directory set to `frontend`
- Framework preset: Vite
- Environment variables configured: VITE_API_URL, VITE_WS_URL
- Deploy the React application
- Obtain the live frontend URL

**Step 5: Integration**
- Backend CORS already configured for all origins
- WebSocket endpoint accessible at backend URL + /ws
- Test complete application flow end-to-end

## 6.3 Live Application URLs

- Frontend URL: (Vercel deployment link)
- Backend URL: (Render deployment link)

---

# 7. Testing and Validation

## 7.1 API Testing

API testing was performed using Postman, testing all backend endpoints independently.

**Tested Endpoints:**
| Endpoint | Method | Result |
|----------|--------|--------|
| /auth/register | POST | ✅ Pass |
| /auth/login | POST | ✅ Pass |
| /auth/health | GET | ✅ Pass |
| /users/search | GET | ✅ Pass |
| /messages/send | POST | ✅ Pass |
| /messages/{id} | GET | ✅ Pass |
| /messages/conversations/{id} | GET | ✅ Pass |
| /groups | POST | ✅ Pass |
| /groups/user/{id} | GET | ✅ Pass |

## 7.2 Functional Test Cases

| Test Case | Description | Expected Result | Status |
|-----------|-------------|-----------------|--------|
| TC-01 | User registration with valid data | Account created, keys generated | ✅ Pass |
| TC-02 | Duplicate username registration | Error message returned | ✅ Pass |
| TC-03 | Login with valid credentials | JWT token returned | ✅ Pass |
| TC-04 | Login with invalid password | Authentication error | ✅ Pass |
| TC-05 | Send encrypted message | Message stored as ciphertext | ✅ Pass |
| TC-06 | Receive and decrypt message | Plaintext displayed correctly | ✅ Pass |
| TC-07 | Message history after reload | Messages persist and display | ✅ Pass |
| TC-08 | Search users by username | Matching users returned | ✅ Pass |
| TC-09 | Create group with members | Group created successfully | ✅ Pass |
| TC-10 | Real-time message via WebSocket | Message appears instantly | ✅ Pass |
| TC-11 | Notification for non-active chat | Toast notification shown | ✅ Pass |

## 7.3 Security Testing

| Test Case | Description | Result |
|-----------|-------------|--------|
| Database inspection | Messages stored as encrypted base64 | ✅ Verified |
| Token expiration | Expired JWT rejected | ✅ Verified |
| CORS validation | Cross-origin requests handled | ✅ Verified |
| Private key protection | Never transmitted to server | ✅ Verified |

## 7.4 Testing Outcome

All modules of the system were successfully tested. The application behaves as expected under various scenarios including edge cases and invalid inputs. The testing process confirms that the encryption is accurate, the real-time communication is reliable, and the system is functionally correct.

---

# 8. Results and Discussion

## 8.1 System Functionality Results

The system was evaluated based on its core functionalities:

- **User Authentication**: Secure registration and login with JWT tokens
- **End-to-End Encryption**: Messages encrypted client-side, stored as ciphertext on server
- **Real-time Messaging**: WebSocket-powered instant message delivery
- **Contact Management**: Shows only users with existing conversations
- **User Search**: Find new users by username to start conversations
- **Group Chat**: Create groups, add members, send group messages
- **Notifications**: Toast notifications for messages from non-active chats
- **Unread Badges**: Visual indicators for unread message counts

All functionalities were tested and performed successfully under different scenarios.

## 8.2 User Interface Screenshots

The application features a premium dark-themed design:

1. **Landing Page**: Hero section with animated gradient text, E2EE badge, feature cards, and CTA buttons
2. **Login/Register Pages**: Glassmorphism cards with animated background orbs and gradient buttons
3. **Chat Dashboard**: Sidebar with contacts/groups, chat window with gradient message bubbles
4. **Create Group Modal**: Slide-up animation with member search and selection
5. **Toast Notifications**: Slide-in notifications for real-time message alerts

## 8.3 Discussion

SecureChat effectively addresses the limitations of traditional messaging systems by providing a zero-knowledge, end-to-end encrypted platform. The RSA+AES hybrid encryption ensures that messages are secure both in transit and at rest. The WebSocket implementation provides a seamless real-time experience comparable to commercial messaging applications.

The system improves:
- **Privacy** through true end-to-end encryption
- **User Experience** through real-time messaging and notifications
- **Security** through JWT authentication and BCrypt password hashing
- **Scalability** through stateless architecture and cloud deployment

## 8.4 Limitations and Future Enhancements

- Implementation of read receipts synced across devices
- File and media sharing with encrypted attachments
- Group message encryption using shared group keys
- Push notifications for mobile browsers
- Message search functionality within encrypted conversations
- Voice and video calling with encryption

---

# 9. Conclusion

SecureChat is a comprehensive and secure solution designed to provide private, real-time messaging with end-to-end encryption. The system successfully implements a zero-knowledge architecture where the server cannot read user messages, addressing critical privacy concerns in modern communication.

The project demonstrates strong full-stack development capabilities, integrating a React-based frontend with a Spring Boot backend, PostgreSQL database, and WebSocket real-time communication. Key features such as RSA-2048/AES-256-GCM encryption, JWT authentication, contact-based messaging, group chats, and real-time notifications ensure that the system is both secure and feature-rich.

The implementation of client-side encryption using the Web Crypto API eliminates the need for external cryptographic libraries, keeping the bundle size small while maintaining enterprise-grade security.

The system is scalable and adaptable, making it suitable not only for academic purposes but also for real-world deployment. It ensures smooth interaction between users while maintaining data integrity, privacy, and security.

**Future Scope:**
- Integration of push notification systems for mobile devices
- Use of HttpOnly cookies for improved token security
- Integration with cloud storage for encrypted file sharing
- Implementation of the Signal Protocol for advanced key ratcheting
- Advanced analytics for message delivery performance monitoring

Overall, SecureChat demonstrates a complete and practical implementation of modern web technologies with a strong focus on privacy and security, making it a robust solution for secure communication.

---

# 10. References

The following resources were used during the development of the SecureChat system:

1. React Official Documentation – https://react.dev/
2. Spring Boot Documentation – https://spring.io/projects/spring-boot
3. PostgreSQL Documentation – https://www.postgresql.org/docs/
4. JWT (JSON Web Token) Documentation – https://jwt.io/introduction
5. Web Crypto API (MDN) – https://developer.mozilla.org/en-US/docs/Web/API/Web_Crypto_API
6. Axios Documentation – https://axios-http.com/docs/intro
7. STOMP Protocol Specification – https://stomp.github.io/
8. SockJS Documentation – https://github.com/sockjs/sockjs-client
9. Supabase Documentation – https://supabase.com/docs
10. Render Deployment Documentation – https://render.com/docs
11. Vercel Documentation – https://vercel.com/docs

These references provided guidance for frontend development, backend implementation, database management, encryption algorithms, and deployment processes.
