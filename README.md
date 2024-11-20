# Print Server with Authentication and Access Control
A secure print server implementation using Java RMI with authentication and access control.

## Security Assumptions
As per lab requirements, secure communication between client and server is assumed to be handled by external means. Channel confidentiality and integrity are considered provided by the underlying infrastructure. The implementation focuses on authentication and access control mechanisms rather than transport security.

## Features

### Authentication System
The authentication system provides secure user verification through:
- Password-based authentication with SHA-256 hashing
- JWT-based session management with 30-minute expiration
- H2 in-memory database for credential storage
- Unique salt generation per user

### Print Server Operations
The print server provides comprehensive job management including:
```java
print(filename, printer)     // Submit a print job
queue(printer)               // List print queue for a printer
topQueue(printer, job)       // Move job to top of queue
start()                      // Start print server
stop()                       // Stop print server
restart()                    // Reset print server
status(printer)              // Get printer status
readConfig(parameter)        // Read configuration
setConfig(parameter, value)  // Update configuration
```

## Technical Implementation

The system is built using Java RMI for client-server communication, with H2 providing in-memory data storage. Authentication is managed through JWT tokens, with passwords secured using SHA-256 hashing and unique salts.

Key technical components:
- Java RMI for client-server communication
- H2 Database for in-memory storage
- JWT for session management
- SHA-256 for password hashing
- Lombok for code reduction
- SLF4J with Logback for logging

### Project Structure
```
src/main/java/
├── auth/
│   ├── AuthManager.java        # Authentication orchestration
│   ├── PasswordStorage.java    # Secure password management
│   ├── TokenManager.java       # JWT token handling
│   └── exceptions/
│       └── AuthenticationException.java
│       └── ExpiredTokenException.java
├── server/
│   └── PrintServer.java        # RMI server implementation
├── client/
│   └── PrintClient.java        # Client implementation
└── service/
    ├── PrintService.java       # Service implementation
    └── IPrintService.java      # Service interface
```

## Setup and Configuration

### Prerequisites
The system requires Java 17 or higher and Maven 3.6 or higher.

### Dependencies
```xml
<dependencies>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.224</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.4.11</version>
    </dependency>
</dependencies>
```

### Running the Application
Start the server:
```bash
mvn compile exec:java -Dexec.mainClass="server.PrintServer"
```

Run the client:
```bash
mvn compile exec:java -Dexec.mainClass="client.PrintClient"
```

## Security Implementation

The system implements several key security features:
- Secure password storage using SHA-256 hashing
- Unique salt generation per user
- JWT tokens signed with HS256 algorithm
- 30-minute token expiration
- Mandatory token validation for all operations
- Comprehensive input validation

### Test Users
The system comes pre-configured with test users representing different roles:

| Username | Role       | Password    |
|----------|------------|-------------|
| alice    | admin      | password123 |
| bob      | technician | password123 |
| cecilia  | power user | password123 |
| david    | normal user| password123 |

## Implementation Notes
Current implementation characteristics:
- In-memory H2 database for data storage
- JWT tokens with HS256 signing
- SHA-256 password hashing
- RMI registry on port 5099

## Improvements/Extensions
The second part of the assignment can be found under branches with corresponding names (Part2 and Part3) and it extends the current solution with:
- Access Control Lists (ACL)
- Role-Based Access Control (RBAC)
- Policy-based permission management
- Role hierarchy implementation
- Comprehensive audit logging

## License
MIT License
