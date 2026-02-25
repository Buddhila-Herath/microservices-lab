# Microservices Lab

A polyglot microservices system with four independently deployable services, an API Gateway with JWT authentication, and Docker Compose orchestration.

## System Architecture

```
Client (Postman / Browser)
        |
        | HTTP Requests
        v
+---------------------------+
|   API Gateway :8080       |
|  /items  /orders /payments|
+----+--------+--------+----+
     |        |        |
     v        v        v
  Item     Order    Payment
  :8081    :8082    :8083
 (Node)  (Spring) (FastAPI)
```

All services run on the same Docker network (`microservices-net`). The API Gateway is the single entry point — services do not call each other directly.

## Services

| Service | Tech Stack | Port | Description |
|---------|-----------|------|-------------|
| **Item Service** | Node.js / Express | 8081 | Manages items (CRUD) |
| **Order Service** | Java 17 / Spring Boot 3.2 | 8082 | Manages orders (CRUD) |
| **Payment Service** | Python 3.9 / FastAPI | 8083 | Processes payments |
| **API Gateway** | Java 17 / Spring Cloud Gateway | 8080 | Routes requests, JWT auth |

## API Endpoints

All endpoints are accessible through the API Gateway on port **8080**.

### Item Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/items` | Returns list of all items |
| POST | `/items` | Create a new item |
| GET | `/items/{id}` | Get item by ID |

### Order Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/orders` | Returns all orders |
| POST | `/orders` | Place a new order |
| GET | `/orders/{id}` | Get order by ID |

### Payment Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/payments` | Returns all payments |
| POST | `/payments/process` | Process a payment |
| GET | `/payments/{id}` | Get payment status |

### Health Endpoints

| Service | Endpoint |
|---------|----------|
| Item Service | `http://localhost:8081/health` |
| Order Service | `http://localhost:8082/actuator/health` |
| Payment Service | `http://localhost:8083/health` |
| API Gateway | `http://localhost:8080/actuator/health` |

## Prerequisites

- **Docker** and **Docker Compose**
- **Java 17** (for local builds of order-service and api-gateway)
- **Node.js 18+** (for local item-service development)
- **Python 3.9+** (for local payment-service development)

## Build & Run

### Using Docker Compose (Recommended)

```bash
# Build all services
docker-compose build

# Start all containers
docker-compose up

# Start in background (detached mode)
docker-compose up -d

# View running containers
docker ps

# View logs for a specific service
docker-compose logs item-service

# Stop and remove all containers
docker-compose down
```

### Building Java Services Locally

The Spring Boot services must be built before Docker can package them:

```bash
# Build order-service
cd order-service
./mvnw clean package -DskipTests

# Build api-gateway
cd ../api-gateway
./mvnw clean package -DskipTests
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

## JWT Authentication

The API Gateway secures all routes with JWT (HS256 symmetric key). Requests without a valid token receive **401 Unauthorized**.

- **Algorithm:** HS256
- **Secret:** `my-very-strong-secret-key-for-hmac-256-at-least-32-chars`
- The `/actuator/**` endpoints are open (no auth required)

### Generating a Test Token

Go to [jwt.io](https://jwt.io) and set:
- Algorithm: **HS256**
- Payload: `{"sub": "user", "roles": ["user"]}`
- Secret: `my-very-strong-secret-key-for-hmac-256-at-least-32-chars`

### Example Requests

```bash
# Without token (returns 401)
curl http://localhost:8080/items

# With token
curl -H "Authorization: Bearer <your-token>" http://localhost:8080/items

# Create an item
curl -X POST http://localhost:8080/items \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop"}'

# Place an order
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{"item": "Laptop", "quantity": 2, "customerId": "C001"}'

# Process a payment
curl -X POST http://localhost:8080/payments/process \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{"orderId": 1, "amount": 999.99, "method": "CREDIT_CARD"}'
```

## Project Structure

```
microservices-lab/
├── docker-compose.yml
├── .gitignore
├── README.md
│
├── item-service/               # Node.js + Express
│   ├── index.js
│   ├── package.json
│   ├── Dockerfile
│   └── .gitignore
│
├── order-service/              # Java 17 + Spring Boot
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   ├── Dockerfile
│   ├── .mvn/wrapper/
│   └── src/main/
│       ├── java/com/example/orderservice/
│       │   ├── OrderServiceApplication.java
│       │   ├── Order.java
│       │   └── OrderController.java
│       └── resources/
│           └── application.properties
│
├── payment-service/            # Python 3.9 + FastAPI
│   ├── main.py
│   ├── requirements.txt
│   └── Dockerfile
│
└── api-gateway/                # Java 17 + Spring Cloud Gateway
    ├── pom.xml
    ├── mvnw / mvnw.cmd
    ├── Dockerfile
    ├── .mvn/wrapper/
    └── src/main/
        ├── java/com/example/apigateway/
        │   ├── ApiGatewayApplication.java
        │   └── SecurityConfig.java
        └── resources/
            ├── application.yml
            └── application-docker.yml
```

## Docker Configuration

### Dockerfiles

| Service | Base Image |
|---------|-----------|
| Item Service | `node:18-alpine` |
| Order Service | `eclipse-temurin:17-jdk-alpine` |
| Payment Service | `python:3.9-slim` |
| API Gateway | `eclipse-temurin:17-jdk-alpine` |

### Docker Compose

- All services share the `microservices-net` bridge network
- The gateway `depends_on` all three backend services
- Health checks are configured for all containers
- The gateway activates the `docker` Spring profile to use Docker service names for routing

## Gateway Routing

| Path | Routed To |
|------|-----------|
| `/items/**` | `http://item-service:8081` |
| `/orders/**` | `http://order-service:8082` |
| `/payments/**` | `http://payment-service:8083` |

Inside Docker, the gateway uses service names as hostnames. For local development, `application.yml` uses `localhost`.
