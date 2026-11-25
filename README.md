# Hotel Booking System Backend

A production-grade, thread-safe Low-Level Design (LLD) implementation of a Hotel Booking System built with **Java 17** and **Spring Boot 3.3.5** featuring in-memory thread-safe persistence, atomic reservation management, pluggable search filters, and idempotent payment handling.

---

## Deliverables Summary

1. **Runnable Spring Boot Project**:
   - Complete source code under `src/main/java/com/hbooking`
   - Complete Maven build configuration in `pom.xml`
   - Pre-configured Postman collection and environment in `postman/`
2. **README**:
   - Build and execution instructions with curl examples
   - Key design decisions and architecture patterns
   - Assumptions and trade-offs
   - Next steps / future roadmap
3. **Unit & Concurrency Tests**:
   - Comprehensive test suite (35 automated tests) under `src/test/java/com/hbooking` covering all core business logic and concurrency constraints.

---

## How to Build and Run

### Prerequisites
- **Java**: JDK 17 or higher
- **Build Tool**: Apache Maven 3.8+ (or use the system `mvn`)

### 1. Build and Run Tests
Execute the entire test suite including multi-threaded race condition tests:
```bash
mvn clean test
```
*Expected result: 35 tests run, 0 failures, 0 errors.*

### 2. Package the Application
Create an executable fat JAR:
```bash
mvn clean package -DskipTests
```

### 3. Run the Application
You can run directly via Spring Boot Maven plugin:
```bash
mvn spring-boot:run
```
Or run the packaged JAR:
```bash
java -jar target/hotelbooking-0.0.1-SNAPSHOT.jar
```
The server will start on port `8080` (HTTP endpoint: `http://localhost:8080`).

---

## Quick Verification (Sample API Flow via cURL)

### Step 1: Register an Owner
```bash
curl -X POST http://localhost:8080/owners \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Smith",
    "email": "alice.smith@example.com",
    "phone": "+1-555-0101"
  }'
```

### Step 2: Register a Property with Room Types
```bash
curl -X POST http://localhost:8080/owners/{ownerId}/properties \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Grand Pacific Resort",
    "city": "San Francisco",
    "locality": "Downtown",
    "address": "100 Market St",
    "starRating": 5,
    "amenities": ["WIFI", "SWIMMING_POOL", "AIR_CONDITIONING", "SPA", "GYM"],
    "roomTypes": [
      {
        "category": "DELUXE",
        "name": "Deluxe King Room",
        "basePrice": 250.00,
        "currency": "USD",
        "maxCapacity": 2,
        "totalInventory": 5,
        "amenities": ["WIFI", "AIR_CONDITIONING"]
      }
    ]
  }'
```

### Step 3: Search for Available Rooms
```bash
curl -X GET "http://localhost:8080/properties/search?city=San%20Francisco&checkIn=2026-12-01&checkOut=2026-12-05&adults=2&children=0&minStarRating=4"
```

### Step 4: Create a Booking (`PENDING` state)
```bash
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId": "{propertyId}",
    "roomTypeId": "{roomTypeId}",
    "guestId": "guest-42",
    "checkIn": "2026-12-01",
    "checkOut": "2026-12-05",
    "adults": 2,
    "children": 0
  }'
```

### Step 5: Process Payment with Idempotency Key
```bash
curl -X POST http://localhost:8080/bookings/{bookingId}/pay \
  -H "Content-Type: application/json" \
  -d '{
    "idempotencyKey": "IDEMP-TXN-001",
    "paymentMethod": "CARD",
    "cardNumber": "4111111111111234",
    "cardHolderName": "John Doe",
    "expiry": "12/28",
    "cvv": "123"
  }'
```

### Step 6: Cancel Booking & Compute Tiered Refund
```bash
curl -X POST http://localhost:8080/bookings/{bookingId}/cancel
```

## What I Would Do With More Time

1. **Distributed Locks (Redisson / Redis)**:
   - Replace in-memory lock striping with distributed Redis locks to allow horizontal scaling across multiple application instances.
2. **Database Persistence & Optimistic Locking**:
   - Introduce PostgreSQL persistence with Flyway schema migrations, JPA repository mappings, and `@Version` optimistic locking columns on room inventory records.
3. **Background Reservation TTL Sweeper**:
   - Implement a Spring `@Scheduled` / Redis key expiration worker to automatically expire unpaid `PENDING` bookings after a configurable SLA (e.g., 15 minutes) and restore room inventory.
4. **Dynamic & Seasonal Pricing**:
   - Extend `PricingStrategy` to support weekday/weekend multipliers, seasonal holiday rate sheets, and occupancy-based dynamic surge pricing.
5. **Event-Driven Outbox Pattern**:
   - Emit domain events (`BookingConfirmedEvent`, `BookingCancelledEvent`) to Kafka or RabbitMQ for asynchronous confirmation emails, invoice generation, and analytics.
6. **Authentication & Authorization**:
   - Secure the API with Spring Security and JWT, establishing distinct roles for `ROLE_OWNER` and `ROLE_GUEST`.
