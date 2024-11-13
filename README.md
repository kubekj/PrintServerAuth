# Print Server with Authentication and Access Control

A secure print server implementation using Java RMI with authentication and session management. This project is part of the Authentication and Access Control Lab, implementing a client-server architecture for a mock print server system.

## Features

### Authentication System
- Password-based authentication using secure hashing
- Session management with UUIDs
- Automatic session expiration after 30 minutes
- H2 database for credential storage
- Secure password storage with salting

### Print Server Operations
- Print job submission
- Queue management
- Printer status monitoring
- Configuration management
- Server start/stop/restart capabilities

### Available Operations
- `print(filename, printer)`: Submit a print job
- `queue(printer)`: List print queue for a printer
- `topQueue(printer, job)`: Move job to top of queue
- `start()`: Start print server
- `stop()`: Stop print server
- `restart()`: Reset print server
- `status(printer)`: Get printer status
- `readConfig(parameter)`: Read configuration
- `setConfig(parameter, value)`: Update configuration

## Technical Details

### Authentication Implementation
- Secure password storage using H2 database
- Session-based authentication with UUID tokens
- Salt generation and password hashing
- Automatic session cleanup for expired sessions

### Technology Stack
- Java RMI for client-server communication
- H2 Database for data persistence
- Lombok for reducing boilerplate
- SLF4J with Logback for logging

## Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Dependencies
```xml
<dependencies>
    <!-- H2 Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.224</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Logging -->
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.4.11</version>
    </dependency>
</dependencies>
```

### Running the Application

1. Start the server:
```bash
mvn compile exec:java -Dexec.mainClass="server.PrintServer"
```

2. Run the client:
```bash
mvn compile exec:java -Dexec.mainClass="client.PrintClient"
```

## Project Structure

```
src/main/java/
├── auth/
│   ├── AuthManager.java         # Authentication and session management
│   ├── PasswordStorage.java     # Secure password storage
│   ├── Session.java             # Session handling
│   └── AuthenticationException.java
├── server/
│   └── PrintServer.java         # RMI server implementation
├── client/
│   └── PrintClient.java         # Client implementation
└── service/
    ├── PrintService.java        # Service implementation
    └── IPrintService.java       # Service interface
```

## Security Considerations

- Passwords are stored using secure hashing with unique salts
- Sessions expire automatically after inactivity
- All operations require valid session authentication
- Database connections are secured with proper credentials
- Input validation on all operations

## Possible Future Improvements

- Implement access control lists (ACL)
- Add role-based access control (RBAC)
- Implement secure communication channel
- Add password policy enforcement
- Implement audit logging
- Add user management interface

## License
MIT
