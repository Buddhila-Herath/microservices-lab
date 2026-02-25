# Microservices Lab – Testing Guide

Use this guide to verify that Docker, the API Gateway, and all services are working with Postman.

---

## 1. Prerequisites

- **Docker Desktop** running (Windows/Mac) or Docker + Docker Compose (Linux).
- **Postman** installed: [https://postman.com](https://postman.com).

Optional (only if you build Java services locally first):

- Java 17, Node.js 18+, Python 3.9+.

---

## 2. Build & Start Services

Open a terminal in the project root (`microservices-lab`).

### Option A: Build and run in one go

```powershell
docker-compose up --build
```

Wait until you see the API Gateway and all three services started (no errors).  
To run in the background instead:

```powershell
docker-compose up -d --build
```

### Option B: Build first, then run

If the Java services (order-service, api-gateway) were not built before:

```powershell
# From project root
cd order-service
.\mvnw.cmd clean package -DskipTests
cd ..\api-gateway
.\mvnw.cmd clean package -DskipTests
cd ..
```

Then:

```powershell
docker-compose build
docker-compose up
```

### Verify containers are running

```powershell
docker ps
```

You should see 4 containers: `item-service`, `order-service`, `payment-service`, `api-gateway`, all with status “Up”.

---

## 3. Get a JWT Token (Required for API Gateway)

All requests through the gateway (port **8080**) need a valid JWT.

1. Go to [https://jwt.io](https://jwt.io).
2. In **Decoded**:
   - **Algorithm:** HS256  
   - **Payload:**  
     `{"sub": "user", "roles": ["user"]}`  
   - **Verify Signature – Secret:**  
     `my-very-strong-secret-key-for-hmac-256-at-least-32-chars`
3. Copy the token from the **Encoded** box (the long string in the left panel).
4. In Postman, you will use:  
   **Header:** `Authorization`  
   **Value:** `Bearer <paste-your-token-here>`

---

## 4. Postman Setup

### Create collection and auth

1. Open Postman.
2. Create a new **Collection** named **"Microservices Lab"**.
3. On the collection:
   - **Authorization** tab → Type: **Bearer Token**.
   - Paste your JWT in the **Token** field.  
   All requests in the collection will then send this token by default.
4. Optional: Add a **Collection variable**  
   - Variable: `baseUrl`  
   - Value: `http://localhost:8080`  
   Use `{{baseUrl}}` in request URLs.

### Base URL

- **Through API Gateway (use this for normal testing):**  
  `http://localhost:8080`
- **Direct to services (optional):**  
  - Items: `http://localhost:8081`  
  - Orders: `http://localhost:8082`  
  - Payments: `http://localhost:8083`  

Use **8080** for all requests below unless you are testing services directly.

---

## 5. Requests to Add and How to Test

Create one request per row. Use **GET** or **POST** as shown; for POST, use the **Body** tab → **raw** → **JSON** and paste the given JSON.

### Health checks (no auth on actuator)

| Name              | Method | URL                                      | Body |
|-------------------|--------|------------------------------------------|------|
| Gateway Health    | GET    | `http://localhost:8080/actuator/health`  | -    |
| Items Health      | GET    | `http://localhost:8081/health`           | -    |
| Orders Health     | GET    | `http://localhost:8082/actuator/health`  | -    |
| Payments Health   | GET    | `http://localhost:8083/health`           | -    |

For gateway health you can leave **Authorization** empty; for the others it doesn’t matter.  
Expected: status **200** and a JSON body indicating “UP” or similar.

---

### Item service (via gateway: port 8080, with auth)

| Name         | Method | URL                              | Body |
|--------------|--------|----------------------------------|------|
| Get all items| GET    | `http://localhost:8080/items`    | -    |
| Get item by ID | GET  | `http://localhost:8080/items/1`  | -    |
| Create item  | POST   | `http://localhost:8080/items`    | See below |

**POST /items** body:

```json
{
  "name": "Headphones"
}
```

---

### Order service (via gateway: port 8080, with auth)

| Name          | Method | URL                               | Body |
|---------------|--------|-----------------------------------|------|
| Get all orders| GET    | `http://localhost:8080/orders`    | -    |
| Get order by ID | GET  | `http://localhost:8080/orders/1`  | -    |
| Create order  | POST   | `http://localhost:8080/orders`    | See below |

**POST /orders** body:

```json
{
  "item": "Laptop",
  "quantity": 2,
  "customerId": "C001"
}
```

---

### Payment service (via gateway: port 8080, with auth)

| Name            | Method | URL                                    | Body |
|-----------------|--------|----------------------------------------|------|
| Get all payments| GET    | `http://localhost:8080/payments`       | -    |
| Get payment by ID | GET  | `http://localhost:8080/payments/1`    | -    |
| Process payment | POST   | `http://localhost:8080/payments/process` | See below |

**POST /payments/process** body:

```json
{
  "orderId": 1,
  "amount": 1299.99,
  "method": "CARD"
}
```

Note: `amount` must be &gt; 0; `method` can be any string (e.g. `"CARD"`, `"CREDIT_CARD"`).

---

## 6. Recommended order to “check everything”

1. **Start stack**  
   `docker-compose up --build` (or `-d`), then `docker ps` → 4 containers Up.

2. **Health**  
   In Postman, run the four health requests. All should return 200.

3. **Auth**  
   Request without token:  
   `GET http://localhost:8080/items`  
   → Expect **401 Unauthorized**.  
   Add the Bearer token (collection or request level) and repeat  
   → Expect **200** (and possibly an empty array `[]`).

4. **Items**  
   - POST `/items` with `{"name": "Headphones"}` → 201, body with `id` and `name`.  
   - GET `/items` → 200, list including the new item.  
   - GET `/items/1` → 200, single item.

5. **Orders**  
   - POST `/orders` with `{"item": "Laptop", "quantity": 2, "customerId": "C001"}` → 201.  
   - GET `/orders` → 200, list including the new order.  
   - GET `/orders/1` → 200, single order.

6. **Payments**  
   - POST `/payments/process` with `{"orderId": 1, "amount": 1299.99, "method": "CARD"}` → 201.  
   - GET `/payments` → 200, list including the new payment.  
   - GET `/payments/1` → 200, single payment.

7. **Error cases (optional)**  
   - POST `/payments/process` with `"amount": -1` → 400.  
   - GET `/items/999` (non-existent ID) → 404.

---

## 7. Quick checklist

- [ ] Docker Desktop (or Docker + Compose) running  
- [ ] `docker-compose up --build` (or `up -d --build`) completed without errors  
- [ ] `docker ps` shows 4 containers (item, order, payment, api-gateway)  
- [ ] JWT created at jwt.io and copied into Postman (Bearer Token)  
- [ ] Collection “Microservices Lab” created with auth set  
- [ ] All 4 health endpoints return 200  
- [ ] Without token: GET `/items` → 401  
- [ ] With token: GET `/items` → 200  
- [ ] POST `/items` with `{"name": "Headphones"}` → 201  
- [ ] POST `/orders` with item, quantity, customerId → 201  
- [ ] POST `/payments/process` with orderId, amount, method → 201  
- [ ] GET `/items`, `/orders`, `/payments` and GET by ID work after creating data  

---

## 8. Troubleshooting

| Issue | What to do |
|-------|------------|
| 401 on all gateway requests | Check JWT secret and payload on jwt.io; ensure header is `Authorization: Bearer <token>`. |
| Connection refused / ECONNREFUSED | Ensure `docker-compose up` is running and `docker ps` shows all 4 containers. |
| 502 Bad Gateway | Backend service not ready. Wait 30–60 s and retry; check `docker-compose logs api-gateway` and the backend service logs. |
| Java build fails (order-service / api-gateway) | Run `.\mvnw.cmd clean package -DskipTests` in `order-service` and `api-gateway`, then `docker-compose build` again. |
| Port already in use | Stop other apps using 8080–8083 or change ports in `docker-compose.yml`. |

If something still fails, check logs:

```powershell
docker-compose logs api-gateway
docker-compose logs item-service
docker-compose logs order-service
docker-compose logs payment-service
```

Use this guide to check that everything from Docker to each POST/GET endpoint works end to end.
