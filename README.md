# ShopSphere-Scalable-E-Commerce-Backend-Platform
ShopSphere is a scalable e-commerce backend platform built with Spring Boot, Spring Security, JWT, MySQL, and Hibernate. Supports secure authentication, role-based access, product/catalog management, cart, orders, refresh tokens, Swagger docs, and layered architecture.

## How It Works
- Users authenticate using JWT-based login/register APIs.
- Admin manages products and categories through protected endpoints.
- Users add products to cart and place orders securely.
- Order placement validates stock, updates inventory, and clears cart transactionally.
- Spring Security enforces role-based access; JPA/Hibernate persists data in MySQL.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Security | Spring Security 6 + JWT (JJWT) |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| API Docs | Springdoc OpenAPI (Swagger UI) |
| Build | Maven |
| Extras | Lombok, JPA Auditing |

---

## Project Structure

```
src/main/java/com/ecommerce/
├── config/
│   ├── DataInitializer.java      # Seeds sample data on startup
│   ├── JpaConfig.java            # Enables JPA auditing
│   ├── SecurityConfig.java       # Spring Security + JWT filter chain
│   └── SwaggerConfig.java        # OpenAPI JWT bearer auth setup
├── controller/
│   ├── AuthController.java       # POST /api/auth/register, login, refresh
│   ├── CartController.java       # GET/POST/PUT/DELETE /api/cart
│   ├── CategoryController.java   # CRUD /api/categories
│   ├── OrderController.java      # POST/GET /api/orders
│   ├── ProductController.java    # CRUD /api/products + search
│   └── UserController.java       # GET /api/users/me
├── dto/
│   ├── request/                  # AuthRequest, ProductRequest, OrderRequest
│   └── response/                 # ApiResponse (all response DTOs)
├── entity/
│   ├── Cart.java / CartItem.java
│   ├── Category.java
│   ├── Order.java / OrderItem.java
│   ├── Product.java
│   └── User.java
├── exception/
│   ├── ApiException.java         # Custom runtime exception
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice
├── repository/                   # Spring Data JPA repositories
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtUtils.java             # Token generation & validation
└── EcommerceApplication.java
```

---

## Setup & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 1. Clone and configure database

```bash
# Create MySQL database
mysql -u root -p
CREATE DATABASE ecommerce_db;
exit;
```

### 2. Update `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Build and run

```bash
mvn clean install
mvn spring-boot:run
```

### 4. Access Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

## Default Credentials (seeded on first run)

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@ecommerce.com | Admin@1234 |
| User | john@example.com | User@1234 |

---

## API Endpoints

### Authentication
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/auth/register | Public | Register new user |
| POST | /api/auth/login | Public | Login & get JWT |
| POST | /api/auth/refresh | Public | Refresh access token |

### Products
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | /api/products | Public | Get all products (paginated) |
| GET | /api/products/{id} | Public | Get product by ID |
| GET | /api/products/search | Public | Search with filters |
| POST | /api/products | Admin | Create product |
| PUT | /api/products/{id} | Admin | Update product |
| DELETE | /api/products/{id} | Admin | Soft delete product |

### Cart
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | /api/cart | User | View cart |
| POST | /api/cart/items | User | Add item to cart |
| PUT | /api/cart/items/{id} | User | Update item quantity |
| DELETE | /api/cart/items/{id} | User | Remove item |
| DELETE | /api/cart/clear | User | Clear cart |

### Orders
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/orders | User | Place order from cart |
| GET | /api/orders | User | Get my orders |
| GET | /api/orders/{id} | User | Get order details |
| POST | /api/orders/{id}/cancel | User | Cancel pending order |
| PUT | /api/orders/{id}/status | Admin | Update order status |

### Categories
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | /api/categories | Public | List all categories |
| POST | /api/categories | Admin | Create category |
| PUT | /api/categories/{id} | Admin | Update category |
| DELETE | /api/categories/{id} | Admin | Delete category |

---

## JWT Authentication Flow

```
1. POST /api/auth/login  →  { accessToken, refreshToken }
2. Add header to requests:  Authorization: Bearer <accessToken>
3. Access token expires in 24h, refresh token in 7 days
4. POST /api/auth/refresh with refreshToken  →  new accessToken
```
