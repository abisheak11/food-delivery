# Food Delivery App Microservices (Event-Driven with Apache Kafka)

A modern Spring Boot microservices backend for a Food Delivery platform, powered by Java 17, Apache Maven, and Apache Kafka.

## Services Architecture

| Service | Port | Description | Swagger UI |
|---|---|---|---|
| **`api-gateway`** | **`8090`** | **Common Gateway & Unified Swagger Hub** | **`http://localhost:8090/swagger-ui.html`** |
| **`auth-service`** | `8081` | Authentication, registration, JWT tokens | `http://localhost:8081/swagger-ui/index.html` |
| **`order-service`** | `8082` | Order placement, status tracking, Kafka producer/consumer | `http://localhost:8082/swagger-ui/index.html` |
| **`restaurant-service`** | `8083` | Restaurant & Menu management, Kafka order accept/reject | `http://localhost:8083/swagger-ui/index.html` |

---

## Event-Driven Architecture (Kafka)

```
[Customer] -> Places Order (Status: PLACED)
     |
  (order-service)
     |-- Publishes "order-created" event --> [Kafka: order-created]
                                                   |
                                              (restaurant-service) receives incoming order (Status: PENDING)
                                                   |
                                            [Restaurant Owner Reviews]
                                                   |
                                            POST /api/restaurants/orders/{orderId}/decision (ACCEPT / REJECT)
                                                   |
                                            Publishes "restaurant-order-decision" event --> [Kafka: restaurant-order-decision]
                                                                                                  |
                                              (order-service) receives decision <------------------
                                                   |
                                    Updates Order Status: CONFIRMED (if accepted) or CANCELLED (if rejected)
```

---

## Unified Swagger UI (Common Port `8090`)

Access all microservice documentation on **Port 8090**:
👉 Open **`http://localhost:8090/swagger-ui.html`** in your browser.
- Select from dropdown:
  - `1. Auth Service (8081)`
  - `2. Order Service (8082)`
  - `3. Restaurant Service (8083)`

---

## Running the Services

### Option 1: One-Click Concurrent Launcher (PowerShell / Batch)
Run all 4 microservices simultaneously with a single command from the project root:

**PowerShell:**
```powershell
.\start-all.ps1
```

**Command Prompt / Double-click:**
```cmd
start-all.bat
```

To stop all services at once:
```powershell
.\stop-all.ps1
```

---

### Option 2: Docker Compose (Including Zookeeper & Kafka)
```bash
docker compose up --build
```
To stop:
```bash
docker compose down
```

---

## API Endpoints (Via Common Port `8090`)

### 1. Auth Service (`http://localhost:8090/api/auth` or `:8081`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user (`ROLE_CUSTOMER`, `ROLE_RESTAURANT`, etc.) | No |
| `POST` | `/api/auth/login` | Login and receive JWT token | No |
| `GET` | `/api/auth/validate?token={jwt}` | Validate a JWT token | No |
| `GET` | `/api/auth/me` | Get profile of logged-in user | Yes (Bearer Token) |
| `GET` | `/api/auth/health` | Health status | No |

---

### 2. Restaurant Service (`http://localhost:8090/api/restaurants` or `:8083`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/restaurants` | Create restaurant profile | Yes (`ROLE_RESTAURANT` / `ROLE_ADMIN`) |
| `GET` | `/api/restaurants` | List all open restaurants | No |
| `GET` | `/api/restaurants/{id}` | Get restaurant details | No |
| `POST` | `/api/restaurants/{id}/menu` | Add menu item to restaurant | Yes (`ROLE_RESTAURANT` / `ROLE_ADMIN`) |
| `GET` | `/api/restaurants/{id}/menu` | Get restaurant menu items | No |
| `GET` | `/api/restaurants/{id}/orders` | View incoming orders for restaurant | Yes (`ROLE_RESTAURANT` / `ROLE_ADMIN`) |
| `POST` | `/api/restaurants/orders/{orderId}/decision` | **Accept or Reject order** (Emits Kafka decision) | Yes (`ROLE_RESTAURANT` / `ROLE_ADMIN`) |
| `GET` | `/api/restaurants/health` | Health status | No |

#### Example: Accept an Order
```bash
curl -X POST http://localhost:8090/api/restaurants/orders/1/decision \
  -H "Authorization: Bearer <RESTAURANT_OWNER_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "decision": "ACCEPTED",
    "reason": "Order accepted, preparing food now"
  }'
```

#### Example: Reject an Order
```bash
curl -X POST http://localhost:8090/api/restaurants/orders/1/decision \
  -H "Authorization: Bearer <RESTAURANT_OWNER_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "decision": "REJECTED",
    "reason": "Item out of stock"
  }'
```

---

### 3. Order Service (`http://localhost:8090/api/orders` or `:8082`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/orders` | **Place order** (Emits Kafka `order-created` event) | Yes (Bearer Token) |
| `GET` | `/api/orders/{id}` | Get order by ID | Yes (Bearer Token) |
| `GET` | `/api/orders/number/{orderNumber}` | Get order by order number | Yes (Bearer Token) |
| `GET` | `/api/orders/my-orders` | Get logged-in user's orders | Yes (Bearer Token) |
| `PATCH` | `/api/orders/{id}/status` | Update order status manually | Yes (`ROLE_RESTAURANT` / `ROLE_DELIVERY` / `ROLE_ADMIN`) |
| `GET` | `/api/orders/health` | Health status | No |
