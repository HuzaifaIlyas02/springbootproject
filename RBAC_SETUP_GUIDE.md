# RBAC Configuration Guide - Keycloak Setup

## Overview
This guide will help you configure Role-Based Access Control (RBAC) in Keycloak for your microservices application.

## Access Control Rules
- ✅ **ADMIN role** - Can create/add products (POST `/api/product`)
- ✅ **Any authenticated user** - Can view products (GET `/api/product`)
- ✅ **Any authenticated user** - Can place orders (POST `/api/order`)
- ✅ **Any authenticated user** - Can check inventory (GET `/api/inventory`)

---

## Step 1: Access Keycloak Admin Console

1. Open browser and go to: **http://localhost:8080**
2. Click on **Administration Console**
3. Login with admin credentials:
   - Username: `admin`
   - Password: `admin`

---

## Step 2: Configure Realm Roles

1. Select your realm: **`spring-boot-microservices-realm`**
2. In the left sidebar, click **Realm roles**
3. Click **Create role** button

### Create ADMIN Role
- **Role name**: `ADMIN`
- **Description**: Administrator role with full access
- Click **Save**

### Create USER Role (Optional)
- **Role name**: `USER`
- **Description**: Regular user role
- Click **Save**

---

## Step 3: Create Admin User

### Option A: Create New Admin User

1. In left sidebar, click **Users**
2. Click **Add user** button
3. Fill in details:
   - **Username**: `admin`
   - **Email**: `admin@example.com`
   - **First name**: `Admin`
   - **Last name**: `User`
   - **Email verified**: ON
   - **Enabled**: ON
4. Click **Create**

### Set Password
1. Go to **Credentials** tab
2. Click **Set password**
3. Enter password: `admin` (or your choice)
4. **Temporary**: OFF
5. Click **Save**
6. Confirm by clicking **Save password**

### Assign ADMIN Role
1. Go to **Role mapping** tab
2. Click **Assign role** button
3. Select **Filter by realm roles**
4. Check **ADMIN** role
5. Click **Assign**

---

## Step 4: Update Existing Test User

1. In left sidebar, click **Users**
2. Search for: `testuser`
3. Click on the user

### Assign USER Role (Optional)
1. Go to **Role mapping** tab
2. Click **Assign role**
3. Select **USER** role
4. Click **Assign**

**Note**: `testuser` will only have USER role and cannot create products.

---

## Step 5: Configure Client Roles (Optional)

If you want client-specific roles:

1. Go to **Clients** in left sidebar
2. Select your client: **`spring-cloud-client`**
3. Go to **Roles** tab
4. Create roles as needed

---

## Step 6: Test the Configuration

### Test 1: Login as Admin
```bash
curl -X POST http://localhost:8080/realms/spring-boot-microservices-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spring-cloud-client" \
  -d "client_secret=UWrCvJDU8CybRB3D7cI6OEMIJR7G0CWw" \
  -d "grant_type=password" \
  -d "username=admin" \
  -d "password=admin"
```

### Test 2: Create Product as Admin
```bash
curl -X POST http://localhost:8181/api/product \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "Admin created product",
    "price": 99.99
  }'
```
**Expected**: ✅ Success (200 OK)

### Test 3: Create Product as Regular User
```bash
curl -X POST http://localhost:8181/api/product \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "User created product",
    "price": 99.99
  }'
```
**Expected**: ❌ Forbidden (403)

### Test 4: View Products (Any User)
```bash
curl -X GET http://localhost:8181/api/product \
  -H "Authorization: Bearer <USER_TOKEN>"
```
**Expected**: ✅ Success (200 OK)

### Test 5: Place Order (Any User)
```bash
curl -X POST http://localhost:8181/api/order \
  -H "Authorization: Bearer <USER_TOKEN>" \
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
**Expected**: ✅ Success (200 OK)

---

## Verification Checklist

- [ ] ADMIN role created in Keycloak
- [ ] Admin user created with ADMIN role assigned
- [ ] testuser has USER role (or no ADMIN role)
- [ ] Admin can create products
- [ ] Regular users CANNOT create products
- [ ] All authenticated users can view products
- [ ] All authenticated users can place orders

---

## React App Updates Needed

The React app needs to be updated to:
1. **Hide "Add Product" button** for non-admin users
2. **Show admin panel** only for users with ADMIN role
3. **Display appropriate error messages** when authorization fails

Would you like me to update the React frontend to handle these roles?

---

## Token Structure

After login, the JWT token will contain:
```json
{
  "realm_access": {
    "roles": ["ADMIN", "USER"]
  },
  "preferred_username": "admin",
  "email": "admin@example.com"
}
```

The API Gateway extracts these roles and converts them to Spring Security authorities:
- `ADMIN` → `ROLE_ADMIN`
- `USER` → `ROLE_USER`

---

## Troubleshooting

### Issue: 403 Forbidden for all users
**Solution**: Make sure roles are assigned correctly in Keycloak

### Issue: Admin can't create products
**Solution**: 
1. Verify ADMIN role is assigned
2. Check token contains realm_access.roles
3. Restart API Gateway

### Issue: Role not found in token
**Solution**: 
1. In Keycloak, go to **Client scopes**
2. Select **roles**
3. Go to **Mappers** tab
4. Ensure **realm roles** mapper is enabled

---

## Summary

✅ **API Gateway** - Updated with role-based authorization
✅ **Keycloak** - Needs ADMIN and USER roles configured
✅ **Backend Services** - No changes needed (API Gateway handles authorization)
✅ **React Frontend** - Needs updates to show/hide features based on roles

The security is now enforced at the API Gateway level, ensuring that only users with the ADMIN role can create products!
