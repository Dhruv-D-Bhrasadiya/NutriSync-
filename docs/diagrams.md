# NutriSync Diagrams

GitHub renders these Mermaid diagrams in Markdown. Export image files only if the assignment report requires static figures. These describe the proposed design until implementation is verified.

## DevOps lifecycle
~~~mermaid
flowchart LR
  A[Plan issue and acceptance criteria] --> B[Feature branch and code]
  B --> C[Pull request and review]
  C --> D{Checks pass?}
  D -- No --> B
  D -- Yes --> E[Merge to develop]
  E --> F[Jenkins checkout]
  F --> G[Maven build and unit tests]
  G --> H[Selenium tests]
  H --> I{All gates green?}
  I -- No --> J[Publish reports and fix]
  J --> B
  I -- Yes --> K[Package JAR and image]
  K --> L[Publish versioned image]
  L --> M[Deploy]
  M --> N[Health and smoke checks]
  N --> O{Healthy?}
  O -- Yes --> P[Operate and observe]
  O -- No --> Q[Rollback to known-good]
  Q --> R[Learning and backlog]
  P --> R
  R --> A
~~~

## Use cases
~~~mermaid
flowchart LR
  Client([Client])
  Staff([Nutritionist])
  Admin([Administrator])
  subgraph NutriSync
    Register[Register and sign in]
    Browse[Browse slots]
    Request[Request consultation]
    Status[View booking status]
    Cancel[Cancel eligible booking]
    Slots[Manage availability]
    Decide[Confirm or reject request]
    Health[Check application health]
  end
  Client --> Register
  Client --> Browse
  Client --> Request
  Client --> Status
  Client --> Cancel
  Staff --> Slots
  Staff --> Decide
  Admin --> Slots
  Admin --> Decide
  Admin --> Health
~~~

## Logical architecture
~~~mermaid
flowchart TB
  Browser[Browser and Selenium] --> Nginx[Nginx reverse proxy]
  Nginx --> App[Spring Boot MVC application]
  App --> Sec[Spring Security]
  Sec --> Service[Business service layer]
  Service --> Repo[Spring Data JPA]
  Repo --> DB[(PostgreSQL)]
  Git[GitHub repository] --> Jenkins[Jenkins Pipeline]
  Jenkins --> Registry[Docker registry]
  Registry --> Host[Linux deployment host]
  Ansible[Ansible control node] --> Host
  Host --> Nginx
~~~

## Booking sequence
~~~mermaid
sequenceDiagram
  actor Client
  participant UI as Browser
  participant API as Controller
  participant Svc as Booking service
  participant DB as PostgreSQL
  Client->>UI: Select open future slot
  UI->>API: POST /api/v1/bookings
  API->>Svc: Validate user/request
  Svc->>DB: Begin transaction and claim slot
  alt Slot available
    DB-->>Svc: Claim succeeds
    Svc->>DB: Persist PENDING booking and commit
    Svc-->>API: Booking ID and status
    API-->>UI: Created response
    UI-->>Client: Show pending status
  else Slot occupied
    DB-->>Svc: Conflict
    Svc-->>API: Slot unavailable
    API-->>UI: Conflict response
    UI-->>Client: Select another slot
  end
~~~

## Booking state model
~~~mermaid
stateDiagram-v2
  [*] --> PENDING: Client requests slot
  PENDING --> CONFIRMED: Staff confirms
  PENDING --> REJECTED: Staff rejects
  PENDING --> CANCELLED: Owner cancels when eligible
  CONFIRMED --> CANCELLED: Cancellation by policy
  CONFIRMED --> COMPLETED: Consultation completed
  REJECTED --> [*]
  CANCELLED --> [*]
  COMPLETED --> [*]
~~~

## Data relationship
~~~mermaid
erDiagram
  USERS ||--o{ AVAILABILITY_SLOTS : owns
  USERS ||--o{ BOOKINGS : requests
  AVAILABILITY_SLOTS ||--o| BOOKINGS : occupies
  USERS {
    string id PK
    string name
    string email UK
    string password_hash
    string role
  }
  AVAILABILITY_SLOTS {
    string id PK
    string nutritionist_id FK
    datetime starts_at
    datetime ends_at
    string status
  }
  BOOKINGS {
    string id PK
    string client_id FK
    string slot_id FK
    string status
    datetime requested_at
    datetime updated_at
  }
~~~

The model assumes one active booking per slot. Enforce it in PostgreSQL; do not depend solely on a diagram or UI check.
