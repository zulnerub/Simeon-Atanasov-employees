# Employee Collaboration Analyzer

Spring Boot + React application for identifying the pair of employees who worked together on common projects for the longest total period of time.

## Requirements covered

- Loads input data from a CSV file.
- Supports `EmpID, ProjectID, DateFrom, DateTo` CSV structure.
- Treats `DateTo = NULL` as today's date.
- Finds the employee pair with the longest total collaboration period across all common projects.
- Provides a UI where the user selects a CSV file from the file system.
- Displays the winning pair's common projects in a table with:
  - Employee ID #1
  - Employee ID #2
  - Project ID
  - Days worked
- Supports multiple date formats.
- Provides Docker Compose run option.
- Includes backend and frontend tests, and sample CSV files.

## Architecture

```text
React UI
  -> uploads CSV as multipart/form-data
Spring Boot REST API
  -> parses and validates CSV
Collaboration service
  -> merges duplicate/overlapping intervals
  -> calculates overlapping days by employee pair and project
JSON response
  -> React result summary and data grid
```

The core business logic is in the backend. The frontend only handles file upload, loading/error states, and result display.

## Technology stack

Backend:

- Java 21
- Spring Boot 3.4.6
- Maven
- Apache Commons CSV
- JUnit 5 / Spring Boot Test

Frontend:

- React
- TypeScript
- Vite
- Vitest / React Testing Library
- Nginx for Docker runtime

Infrastructure:

- Docker
- Docker Compose
- GitHub Actions CI

## Assumptions

1. Date ranges are inclusive.

   Example: `2024-01-01` to `2024-01-01` counts as `1` day.

2. Employee pairs are normalized.

   Example: pair `218, 143` is represented as `143, 218`.

3. `DateTo = NULL`, `null`, or blank means today's date according to the backend server clock.

4. If the same employee has duplicate or overlapping date intervals on the same project, those intervals are merged before pair overlap is calculated. This prevents double-counting.

5. If there are no employees who worked together on common projects, the API returns a successful response with an explanatory message and an empty project list.

6. For ambiguous numeric dates, parsing priority is documented by the supported date format order below. European `dd/MM/yyyy` is tried before US `MM/dd/yyyy`.

## Supported date formats

The backend supports:

```text
yyyy-MM-dd
yyyy/MM/dd
yyyy.MM.dd
dd-MM-yyyy
dd/MM/yyyy
dd.MM.yyyy
MM/dd/yyyy
MMM d, yyyy
MMMM d, yyyy
d MMM yyyy
d MMMM yyyy
yyyy-MM-ddTHH:mm:ss
```

Examples:

```text
2024-01-20
2024/01/20
2024.01.20
20-01-2024
20/01/2024
20.01.2024
01/20/2024
Jan 20, 2024
January 20, 2024
20 Jan 2024
20 January 2024
2024-01-20T10:15:30
```

## Run with Docker Compose

From the repository root:

```bash
docker compose up --build
```

Open:

```text
http://localhost:3000
```

The backend is exposed at:

```text
http://localhost:8080
```

The frontend container proxies `/api` calls to the backend container.

## Run locally without Docker

Start the backend:

```bash
cd backend
mvn spring-boot:run
```

Start the frontend in another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

Vite proxies `/api` calls to `http://localhost:8080`.

## Build as a single runnable Spring Boot JAR

This is optional. It builds the React production bundle, copies it into Spring Boot static resources, and packages a single JAR.

From the repository root:

```bash
./scripts/build-single-jar.sh
java -jar backend/target/employees-0.0.1-SNAPSHOT.jar
```

Open:

```text
http://localhost:8080
```

## Run tests

Backend tests:

```bash
cd backend
mvn clean test
```

Frontend tests:

```bash
cd frontend
npm install
npm test
```

## API

### Analyze CSV

```http
POST /api/collaborations/analyze
Content-Type: multipart/form-data
```

Multipart field:

```text
file
```

Successful response:

```json
{
  "employeeId1": 143,
  "employeeId2": 218,
  "totalDaysWorked": 11,
  "message": "Longest collaboration found.",
  "projects": [
    {
      "employeeId1": 143,
      "employeeId2": 218,
      "projectId": 10,
      "daysWorked": 6
    },
    {
      "employeeId1": 143,
      "employeeId2": 218,
      "projectId": 20,
      "daysWorked": 5
    }
  ]
}
```

Validation error response:

```json
{
  "message": "CSV validation failed.",
  "errors": [
    "Line 2: EmpID must be numeric.",
    "Line 3: DateTo cannot be before DateFrom."
  ]
}
```

Example curl command:

```bash
curl -F "file=@sample-data/valid-employees.csv" http://localhost:8080/api/collaborations/analyze
```

## Sample data

Sample files are available in `sample-data/`:

```text
valid-employees.csv
multiple-date-formats.csv
invalid-employees.csv
```

## Backend design notes

The collaboration algorithm works in these steps:

1. Group work records by project.
2. Inside each project, group records by employee.
3. Merge overlapping or adjacent intervals for the same employee on the same project.
4. Compare employee interval lists inside the same project.
5. Calculate inclusive overlap days.
6. Aggregate overlap days by employee pair and project.
7. Return the pair with the highest total number of days.

This avoids double-counting duplicate or overlapping rows for the same employee.
