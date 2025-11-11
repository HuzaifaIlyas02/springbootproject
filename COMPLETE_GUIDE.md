# Full-Stack E-Commerce Application - Complete Guide

## 📋 Project Overview

A complete microservices-based e-commerce platform with:
- **Backend**: 5 Spring Boot microservices with OAuth2 security
- **Frontend**: React 18 with TypeScript, Material-UI, and Tailwind CSS
- **Authentication**: Keycloak (OAuth2/OIDC)
- **Databases**: PostgreSQL (Order & Inventory), MongoDB (Product)

---

## 🏗️ Architecture

### Backend Microservices
1. **Discovery Server** (Port 8761) - Eureka service registry
2. **API Gateway** (Port 8181) - OAuth2-secured gateway
3. **Product Service** (Port 8083) - Product catalog management
4. **Order Service** (Port 8081) - Order processing
5. **Inventory Service** (Port 8082) - Stock management

### Frontend
- **React App** (Port 3000) - E-commerce user interface

### Infrastructure
- **Keycloak** (Port 8080) - Identity and access management
- **PostgreSQL** (Ports 5431, 5432) - Relational databases
- **MongoDB** (Port 27017) - NoSQL database

---

## 🚀 Quick Start

### 1. Start Backend Services

```bash
# Terminal 1 - Discovery Server
cd /Users/mac140/Desktop/microservices-new/discovery-server
mvnd spring-boot:run

# Terminal 2 - API Gateway  
cd /Users/mac140/Desktop/microservices-new/api-gateway
mvnd spring-boot:run

# Terminal 3 - Product Service
cd /Users/mac140/Desktop/microservices-new/product-service
mvnd spring-boot:run

# Terminal 4 - Order Service
cd /Users/mac140/Desktop/microservices-new/order-service
mvnd spring-boot:run

# Terminal 5 - Inventory Service
cd /Users/mac140/Desktop/microservices-new/inventory-service
mvnd spring-boot:run
```

### 2. Start Keycloak

Keycloak should already be running on port 8080. If not, start it.

### 3. Start React Frontend

```bash
cd /Users/mac140/Desktop/microservices-new/ecommerce
npm start
```

Access the app at: **http://localhost:3000**

---

## 🔑 Credentials & Configuration

### Keycloak Configuration
- **URL**: http://localhost:8080
- **Realm**: `spring-boot-microservices-realm`
- **Client ID**: `spring-cloud-client`
- **Client Secret**: `UWrCvJDU8CybRB3D7cI6OEMIJR7G0CWw`

### Test User
- **Username**: `testuser`
- **Password**: `password`

### Database Credentials
```properties
# Order Service (PostgreSQL - Port 5431)
spring.datasource.username=postgres
spring.datasource.password=password

# Inventory Service (PostgreSQL - Port 5432)
spring.datasource.username=postgres
spring.datasource.password=password

# Product Service (MongoDB - Port 27017)
spring.data.mongodb.uri=mongodb://localhost:27017/product-service
```

---

## 📦 Sample Data

### Inventory
The inventory database is pre-populated with:
```sql
- iphone_15_pro: 100 units
- macbook_pro_m3: 50 units
- airpods_pro: 200 units
```

---

## 🛠️ Technology Stack

### Backend
- **Java 21** (Amazon Corretto)
- **Spring Boot 3.3.5**
- **Spring Cloud 2023.0.3** ⚠️ (Downgraded from 2023.0.6 for compatibility)
- **Spring Security OAuth2 Resource Server**
- **Maven** (using mvnd for faster builds)
- **PostgreSQL** & **MongoDB**

### Frontend
- **React 18** with TypeScript
- **Material-UI v5** (@mui/material)
- **Tailwind CSS v4** with @tailwindcss/postcss
- **Axios** for HTTP requests
- **React Router v6** for navigation
- **React Context API** for state management

### Infrastructure
- **Keycloak 18.0.0** for OAuth2/OIDC
- **Eureka** for service discovery

---

## 📂 Frontend Structure

```
ecommerce/
├── src/
│   ├── components/
│   │   └── Navbar.tsx          # App bar with cart and logout
│   ├── pages/
│   │   ├── LoginPage.tsx       # OAuth2 login with Keycloak
│   │   ├── ProductsPage.tsx    # Product catalog with grid layout
│   │   └── CheckoutPage.tsx    # Shopping cart and checkout
│   ├── services/
│   │   ├── api.ts              # Axios instance with auth interceptor
│   │   ├── authService.ts      # OAuth2 token management
│   │   ├── productService.ts   # Product API calls
│   │   └── orderService.ts     # Order API calls
│   ├── context/
│   │   ├── AuthContext.tsx     # Authentication state
│   │   └── CartContext.tsx     # Shopping cart state
│   ├── types/
│   │   └── index.ts            # TypeScript interfaces
│   ├── App.tsx                 # Main app with routing
│   └── index.css               # Tailwind CSS imports
├── tailwind.config.js          # Tailwind configuration
├── postcss.config.js           # PostCSS with Tailwind plugin
└── package.json                # Dependencies
```

---

## 🔐 Authentication Flow

1. User visits `/login` page
2. Enters credentials (testuser/password)
3. React app sends POST to Keycloak token endpoint
4. Keycloak returns JWT access token (expires in 5 minutes)
5. Token stored in localStorage
6. All API requests include `Authorization: Bearer <token>` header
7. API Gateway validates token with Keycloak
8. Valid requests routed to microservices

---

## 🛒 Shopping Flow

1. **Login** → User authenticates with Keycloak
2. **Browse Products** → View product catalog (fetched from Product Service via API Gateway)
3. **Add to Cart** → Items stored in React Context
4. **View Cart** → Navigate to checkout page
5. **Update Quantities** → Modify cart items
6. **Place Order** → Order sent to Order Service via API Gateway
7. **Order Confirmation** → Cart cleared, success message shown

---

## 🔧 Important Configuration Changes

### Spring Cloud Version Fix
**Issue**: NoSuchMethodError in API Gateway due to Spring Cloud 2023.0.6 incompatibility

**Solution**: Downgraded to Spring Cloud 2023.0.3 in parent pom.xml
```xml
<spring-cloud.version>2023.0.3</spring-cloud.version>
```

### Tailwind CSS v4 Fix
**Issue**: PostCSS plugin not found error

**Solution**: Installed @tailwindcss/postcss and updated postcss.config.js
```javascript
module.exports = {
  plugins: {
    '@tailwindcss/postcss': {},
    autoprefixer: {},
  },
}
```

### Material-UI Grid Fix
**Issue**: TypeScript errors with Grid `item` prop in MUI v5

**Solution**: Replaced Grid with CSS Grid using Box component
```tsx
<Box sx={{ 
  display: 'grid', 
  gridTemplateColumns: {
    xs: '1fr',
    sm: 'repeat(2, 1fr)',
    md: 'repeat(3, 1fr)'
  },
  gap: 3
}}>
```

---

## 🧪 Testing the Application

### 1. Test Authentication
```bash
curl -X POST http://localhost:8080/realms/spring-boot-microservices-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spring-cloud-client" \
  -d "client_secret=UWrCvJDU8CybRB3D7cI6OEMIJR7G0CWw" \
  -d "grant_type=password" \
  -d "username=testuser" \
  -d "password=password"
```

### 2. Test Product Service (with token)
```bash
curl -X GET http://localhost:8181/api/product \
  -H "Authorization: Bearer <your-access-token>"
```

### 3. Test Order Placement (with token)
```bash
curl -X POST http://localhost:8181/api/order \
  -H "Authorization: Bearer <your-access-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "orderLineItemsList": [
      {
        "skuCode": "iphone_15_pro",
        "price": 999.99,
        "quantity": 1
      }
    ]
  }'
```

---

## 🚨 Common Issues & Solutions

### 1. Token Expiration
**Symptom**: 401 Unauthorized errors after 5 minutes

**Solution**: The app automatically redirects to login. Token expiry is 300 seconds (5 minutes).

### 2. CORS Errors
**Symptom**: Browser console shows CORS policy errors

**Solution**: API Gateway should have CORS configured for http://localhost:3000. Check Gateway configuration.

### 3. Microservice Not Found
**Symptom**: 404 errors when accessing products/orders

**Solution**: Ensure all services are registered with Discovery Server at http://localhost:8761

### 4. Build Errors
**Symptom**: Compilation fails

**Solution**: 
- Clean and rebuild: `mvnd clean package -DskipTests`
- Verify JDK 21 is being used
- Check Spring Cloud version is 2023.0.3

---

## 📊 Service Endpoints

### Discovery Server
- Dashboard: http://localhost:8761

### API Gateway
- Base URL: http://localhost:8181
- Products: `/api/product`
- Orders: `/api/order`

### Direct Service Access (for debugging)
- Product Service: http://localhost:8083/api/product
- Order Service: http://localhost:8081/api/order  
- Inventory Service: http://localhost:8082/api/inventory

---

## 🔄 Development Workflow

### Backend Changes
1. Make code changes
2. Rebuild: `mvnd clean package -DskipTests`
3. Restart service: `mvnd spring-boot:run`

### Frontend Changes
1. Make code changes
2. Hot reload automatically applies changes
3. Check browser console for errors

### Database Changes
1. Update schema in `schema.sql` or entity classes
2. Restart affected service
3. Data persists in PostgreSQL/MongoDB

---

## 📝 Next Steps / Future Enhancements

- [ ] Add user registration functionality
- [ ] Implement order history page
- [ ] Add product search and filtering
- [ ] Implement pagination for product list
- [ ] Add product images
- [ ] Create admin dashboard
- [ ] Add payment gateway integration
- [ ] Implement token refresh mechanism
- [ ] Add unit and integration tests
- [ ] Set up CI/CD pipeline
- [ ] Containerize with Docker
- [ ] Deploy to cloud (Azure/AWS)

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [React Documentation](https://react.dev/)
- [Material-UI](https://mui.com/)
- [Tailwind CSS](https://tailwindcss.com/)

---

## 📞 Support

For issues or questions:
1. Check this guide first
2. Review console/logs for error messages
3. Verify all services are running
4. Check Keycloak configuration
5. Ensure correct credentials and ports

---

**Last Updated**: November 2024
**Spring Cloud Version**: 2023.0.3
**React Version**: 18.3.1
**Node Version**: 24.11.0
