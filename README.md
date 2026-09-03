# Food Delivery App Microservices (Event-Driven with Apache Kafka)

A modern Spring Boot microservices backend for a Food Delivery platform, powered by Java 17, Apache Maven, and Apache Kafka.

## Services Architecture

| Service | Port | Description | Swagger UI |
|---|---|---|---|
| **`api-gateway`** | **`8090`** | **Common Gateway & Unified Swagger Hub** | **`http://localhost:8090/swagger-ui.html`** |
| **`auth-service`** | `8081` | Authentication, registration, JWT tokens | `http://localhost:8081/swagger-ui/index.html` |
| **`order-service`** | `8082` | Order placement, status tracking, Kafka producer/consumer | `http://localhost:8082/swagger-ui/index.html` |
| **`restaurant-service`** | `8083` | Restaurant & Menu management, Kafka order accept/reject | `http://localhost:8083/swagger-ui/index.html` |
| **`delivery-service`** | `8084` | Delivery partner onboarding, GPS tracking, task assignment | `http://localhost:8084/swagger-ui/index.html` |
| **`payment-service`** | `8085` | Payment processing, gateway integration, refunds & webhooks | `http://localhost:8085/swagger-ui/index.html` |

---

## Event-Driven Architecture (Kafka)

```
[Customer] -> Places Order (Initial Status: PENDING_PAYMENT)
     |
  (order-service)
     |-- Publishes "order-created" event --> [Kafka: order-created]
                                                   |
                                          (payment-service) pre-registers payment ledger
                                                   |
                                      [Customer Processes Payment]
                                      POST /api/payments/process
                                                   |
                                      Publishes "payment-processed" (SUCCESS / FAILED)
                                                   |
                                            (order-service)
                         +-------------------------+-------------------------+
                         |                                                   |
                  Payment SUCCESS (or COD)                            Payment FAILED
                         |                                                   |
             Order Status -> PAID                                Order Status -> PAYMENT_FAILED
                         |                                            (Order is NOT sent to kitchen)
             Publishes "order-paid" event
                         |
      +------------------+------------------+
      |                                     |
(restaurant-service)                  (delivery-service)
Creates Restaurant Order              Creates Delivery Task
(Status: PENDING kitchen approval)    (Status: PENDING courier assignment)
      |
[Restaurant Reviews Order]
PUT /api/restaurants/orders/{id}/decision
      |
Publishes "restaurant-order-decision" (ACCEPTED / REJECTED)
      |
(order-service)
Order Status -> CONFIRMED / CANCELLED
```


---

## Unified Swagger UI (Common Port `8090`)

Access all microservice documentation on **Port 8090**:
👉 Open **`http://localhost:8090/swagger-ui.html`** in your browser.
- Select from dropdown:
  - `1. Auth Service (8081)`
  - `2. Order Service (8082)`
  - `3. Restaurant Service (8083)`
  - `4. Delivery Service (8084)`
  - `5. Payment Service (8085)`

---

## Running the Services

### Option 1: One-Click Concurrent Launcher (PowerShell / Batch)
Run all 6 microservices simultaneously with a single command from the project root:

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
| `POST` | `/api/auth/register` | Register a new user (`ROLE_CUSTOMER`, `ROLE_RESTAURANT`, `ROLE_DELIVERY`, `ROLE_ADMIN`) | No |
| `POST` | `/api/auth/login` | Login and receive JWT token | No |
| `GET` | `/api/auth/validate?token={jwt}` | Validate a JWT token | No |
| `GET` | `/api/auth/me` | Get profile of logged-in user | Yes (Bearer Token) |
| `GET` | `/api/auth/health` | Health status | No |

---

### 2. Payment Service (`http://localhost:8090/api/payments` or `:8085`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/payments/process` | **Process payment** (Card, UPI, COD, Wallet) | Yes (Bearer Token) |
| `GET` | `/api/payments/{id}` | Get payment by Payment ID | Yes (Bearer Token) |
| `GET` | `/api/payments/order/{orderId}` | Get payment details by Order ID | Yes (Bearer Token) |
| `GET` | `/api/payments/user/{userId}` | Get payment history for a user | Yes (Bearer Token) |
| `POST` | `/api/payments/refund` | **Process a refund** for completed payment | Yes (Bearer Token) |
| `GET` | `/api/payments/{paymentId}/refunds` | Get all refunds for a payment | Yes (Bearer Token) |
| `POST` | `/api/payments/webhook` | External payment gateway webhook listener | No (Signature Verified) |

#### Example: Process Payment
```bash
curl -X POST http://localhost:8090/api/payments/process \
  -H "Authorization: Bearer <CUSTOMER_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "orderNumber": "ORD-1",
    "amount": 49.99,
    "currency": "USD",
    "paymentMethod": "CREDIT_CARD",
    "cardNumber": "4111222233330000",
    "cardCvv": "123",
    "cardExpiry": "12/28"
  }'
```

#### Example: Process Refund
```bash
curl -X POST http://localhost:8090/api/payments/refund \
  -H "Authorization: Bearer <ADMIN_OR_CUSTOMER_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": 1,
    "amount": 49.99,
    "reason": "Order cancelled before preparation"
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
| `PATCH` | `/api/orders/{id}/status` | Update order status manually | Yes (Bearer Token) |
| `GET` | `/api/orders/health` | Health status | No |

---

### 4. Restaurant Service (`http://localhost:8090/api/restaurants` or `:8083`)

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

---

### 5. Delivery Service (`http://localhost:8090/api/deliveries` or `:8084`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/deliveries/partners/profile` | Register/Update delivery partner profile | Yes (`ROLE_DELIVERY`) |
| `PUT` | `/api/deliveries/partners/location` | Update live GPS coordinates | Yes (`ROLE_DELIVERY`) |
| `POST` | `/api/deliveries/tasks` | Create delivery task | Yes (`ROLE_ADMIN`) |
| `GET` | `/api/deliveries/available` | View available delivery tasks | Yes (`ROLE_DELIVERY`) |
| `POST` | `/api/deliveries/{id}/accept` | Accept delivery task | Yes (`ROLE_DELIVERY`) |
| `PUT` | `/api/deliveries/{id}/status` | Update delivery status (`PICKED_UP`, `DELIVERED`) | Yes (`ROLE_DELIVERY`) |
| `GET` | `/api/deliveries/order/{orderId}` | Track delivery status for order | Yes (Bearer Token) |
| `GET` | `/api/deliveries/health` | Health status | No |
