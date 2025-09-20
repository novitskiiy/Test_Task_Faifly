# Healthcare Visit Tracking System

A Spring Boot application for tracking doctor visits by patients.

## Features

- **Create Visits**: POST endpoint to create new doctor visits with timezone support and conflict detection
- **List Patients**: GET endpoint to retrieve paginated list of patients with their last visits to each doctor
- **Optimized Queries**: Database queries optimized for handling hundreds of thousands of records
- **Timezone Support**: Proper timezone handling for doctors in different time zones
- **Validation**: Comprehensive request validation and error handling
- **Testing**: Complete test coverage with unit, integration, and repository tests

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **MySQL 8**
- **Maven**
- **JUnit 5**
- **TestContainers** (for integration tests)

## Dump file
`Test_Task_Faifly/visit_tracking_dump.sql`

## API Endpoints

### 1. Create Visit

**POST** `/api/visits`

Creates a new doctor visit with conflict detection.

**Request Body:**
```json
{
    "start": "2024-01-15T10:00:00",
    "end": "2024-01-15T11:00:00",
    "patientId": 1,
    "doctorId": 2
}
```

**Response:** Created visit object with all details.

<img width="1759" height="1170" alt="image_2025-09-20_14-16-00" src="https://github.com/user-attachments/assets/660a42dc-9a1f-4aff-8860-61766cc1a0ff" />


### 2. Get Patients List

**GET** `/api/visits/patients`

Retrieves paginated list of patients with their last visits.

<img width="1744" height="1161" alt="image_2025-09-20_14-17-25" src="https://github.com/user-attachments/assets/07a86127-b7d4-4717-bf23-f126fdbb9352" />


**Query Parameters:**
- `page` (optional): Page number (default: 1)
- `size` (optional): Page size (default: 20)
- `search` (optional): Search by patient name
- `doctorIds` (optional): Filter by doctor IDs (comma-separated)

**Example:** `/api/visits/patients?page=1&size=10&search=John&doctorIds=1,2`

**Response:**
```json
{
    "data": [
        {
            "firstName": "John",
            "lastName": "Doe",
            "lastVisits": [
                {
                    "start": "2024-01-15T10:00:00",
                    "end": "2024-01-15T11:00:00",
                    "doctor": {
                        "firstName": "Dr. Smith",
                        "lastName": "Johnson",
                        "totalPatients": 150
                    }
                }
            ]
        }
    ],
    "count": 1
}
```

## Database Schema

### Tables

1. **doctors**
   - `id` (BIGINT, PRIMARY KEY)
   - `first_name` (VARCHAR(100))
   - `last_name` (VARCHAR(100))
   - `timezone` (VARCHAR(50))

2. **patients**
   - `id` (BIGINT, PRIMARY KEY)
   - `first_name` (VARCHAR(100))
   - `last_name` (VARCHAR(100))

3. **visits**
   - `id` (BIGINT, PRIMARY KEY)
   - `start_date_time` (DATETIME)
   - `end_date_time` (DATETIME)
   - `patient_id` (BIGINT, FOREIGN KEY)
   - `doctor_id` (BIGINT, FOREIGN KEY)

### Indexes

- Composite index on `visits(doctor_id, start_date_time, end_date_time)` for conflict detection
- Index on `patients(first_name, last_name)` for search optimization
- Index on `visits(doctor_id, patient_id)` for patient count optimization

## Performance Optimizations

1. **Database Queries:**
   - Uses JOIN FETCH to avoid N+1 queries
   - Optimized conflict detection with single query
   - Batch patient count queries for multiple doctors
   - Proper indexing for fast lookups

2. **Java Code:**
   - Lazy loading for associations
   - Efficient stream processing
   - Minimal database round trips
   - Proper timezone conversion caching


## Testing

### Run All Tests

```bash
mvn test
```

### Test Coverage

The project includes:

1. **Unit Tests:**
   - `VisitControllerTest` - Controller layer tests
   - `VisitServiceTest` - Service layer tests
   - `PatientServiceTest` - Service layer tests

2. **Repository Tests:**
   - `VisitRepositoryTest` - Data access layer tests

3. **Integration Tests:**
   - `VisitTrackingIntegrationTest` - End-to-end API tests

### Test Database

Tests use H2 in-memory database for fast execution.

## API Testing with Postman

### Prerequisites

1. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

2. **Add test data to MySQL:**
   Execute `create_test_data.sql` in MySQL Workbench to add sample doctors and patients.

### Postman Collection Setup

Create a new Postman collection called "Healthcare Visit Tracking API" with the following requests:

## 1. Create a Visit (POST)

**Request Details:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/visits`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "start": "2025-01-15T10:00:00",
    "end": "2025-01-15T11:00:00",
    "patientId": 1,
    "doctorId": 1
  }
  ```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "startDateTime": "2025-01-15T10:00:00",
  "endDateTime": "2025-01-15T11:00:00",
  "patient": {
    "id": 1,
    "firstName": "Alice",
    "lastName": "Smith"
  },
  "doctor": {
    "id": 1,
    "firstName": "John",
    "lastName": "Smith",
    "timezone": "America/New_York"
  }
}
```

## 2. Get All Patients (GET)

**Request Details:**
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/visits/patients`

**Expected Response (200 OK):**
```json
{
  "data": [
    {
      "firstName": "Alice",
      "lastName": "Smith",
      "lastVisits": [
        {
          "start": "2025-01-15T10:00:00",
          "end": "2025-01-15T11:00:00",
          "doctor": {
            "firstName": "John",
            "lastName": "Smith",
            "totalPatients": 1
          }
        }
      ]
    }
  ],
  "count": 1
}
```

## 3. Get Patients with Pagination (GET)

**Request Details:**
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/visits/patients?page=1&size=5`

## 4. Search Patients by Name (GET)

**Request Details:**
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/visits/patients?search=Alice`

## 5. Filter by Doctor IDs (GET)

**Request Details:**
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/visits/patients?doctorIds=1,2,3`

## 6. Combined Filters (GET)

**Request Details:**
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/visits/patients?page=1&size=10&search=John&doctorIds=1,2`

## 7. Test Time Conflict (POST)

**Request Details:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/visits`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "start": "2025-01-15T10:30:00",
    "end": "2025-01-15T11:30:00",
    "patientId": 1,
    "doctorId": 1
  }
  ```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Doctor has conflicting visit at this time"
}
```

## 8. Test Validation Errors (POST)

**Request Details:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/visits`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "start": "",
    "end": "2025-01-15T11:00:00",
    "patientId": null,
    "doctorId": 1
  }
  ```

**Expected Response (400 Bad Request):**
```json
{
  "details": {
    "start": "Start time is required",
    "patientId": "Patient ID is required"
  },
  "error": "Validation failed"
}
```

## 9. Test Non-existent Patient (POST)

**Request Details:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/visits`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "start": "2025-01-15T10:00:00",
    "end": "2025-01-15T11:00:00",
    "patientId": 999,
    "doctorId": 1
  }
  ```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Patient not found with ID: 999"
}
```

## 10. Test Non-existent Doctor (POST)

**Request Details:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/visits`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "start": "2025-01-15T10:00:00",
    "end": "2025-01-15T11:00:00",
    "patientId": 1,
    "doctorId": 999
  }
  ```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Doctor not found with ID: 999"
}
```



