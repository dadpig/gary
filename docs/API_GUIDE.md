# Gary API Guide

Complete guide for using the Gary Assistant REST API.

---

## Base URL

```
http://localhost:8080/api/v1
```

---

## Authentication

Currently, the API is open. Authentication will be added in future versions.

---

## Endpoints

### 1. Health Check

Check if the API is running.

**Endpoint:** `GET /health`

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2026-03-12T19:30:00Z",
  "service": "Gary Assistant API"
}
```

### 2. Scraper Status

Check the status of platform scrapers.

**Endpoint:** `GET /health/scrapers`

**Response:**
```json
{
  "AMAZON": false,
  "MERCADO_LIVRE": false
}
```

---

## Search API

### Search Products

Search for products across multiple platforms.

**Endpoint:** `POST /search`

**Request Body:**
```json
{
  "query": "wireless mouse",
  "maxPrice": 100.00,
  "platforms": ["AMAZON", "MERCADO_LIVRE"],
  "sortBy": "LOWEST_PRICE"
}
```

**Parameters:**
- `query` (required): Search query string
- `maxPrice` (optional): Maximum price filter
- `platforms` (optional): List of platforms to search. Default: all platforms
- `sortBy` (optional): Sort criteria
  - `LOWEST_PRICE` - Sort by total cost (default)
  - `HIGHEST_RATING` - Sort by rating
  - `MOST_REVIEWS` - Sort by review count

**Response:**
```json
{
  "searchId": "550e8400-e29b-41d4-a716-446655440000",
  "query": "wireless mouse",
  "products": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Logitech MX Master 3S",
      "description": "Advanced wireless mouse...",
      "platform": "AMAZON",
      "platformProductId": "B09HM94VDS",
      "url": "https://www.amazon.com/dp/B09HM94VDS",
      "price": {
        "amount": 99.99,
        "currency": "USD",
        "shippingCost": 0.00,
        "total": 99.99
      },
      "rating": {
        "score": 4.7,
        "reviewCount": 8234,
        "reliable": true
      },
      "imageUrl": "https://...",
      "available": true,
      "lastUpdated": "2026-03-12T19:30:00Z"
    }
  ],
  "totalResults": 10,
  "searchedAt": "2026-03-12T19:30:00Z",
  "durationMs": 2345
}
```

**Status Codes:**
- `200 OK` - Success
- `400 Bad Request` - Invalid parameters
- `503 Service Unavailable` - Scraper error

---

### Get Product Details

Get detailed information about a specific product.

**Endpoint:** `GET /search/{productId}`

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Logitech MX Master 3S",
  "description": "Advanced wireless mouse...",
  "platform": "AMAZON",
  "platformProductId": "B09HM94VDS",
  "url": "https://www.amazon.com/dp/B09HM94VDS",
  "price": {
    "amount": 99.99,
    "currency": "USD",
    "shippingCost": 0.00,
    "total": 99.99
  },
  "rating": {
    "score": 4.7,
    "reviewCount": 8234,
    "reliable": true
  },
  "imageUrl": "https://...",
  "available": true,
  "lastUpdated": "2026-03-12T19:30:00Z"
}
```

**Status Codes:**
- `200 OK` - Success
- `404 Not Found` - Product not found

---

## Comparison API

### Compare Products

Compare multiple products side-by-side.

**Endpoint:** `POST /compare`

**Request Body:**
```json
{
  "productIds": [
    "123e4567-e89b-12d3-a456-426614174000",
    "223e4567-e89b-12d3-a456-426614174001"
  ]
}
```

**Response:**
```json
{
  "products": [
    { /* Product 1 details */ },
    { /* Product 2 details */ }
  ],
  "bestPrice": {
    "productId": "223e4567-e89b-12d3-a456-426614174001",
    "productName": "Logitech M720",
    "totalCost": 39.99,
    "reason": "Lowest total cost (price + shipping)"
  },
  "bestValue": {
    "productId": "123e4567-e89b-12d3-a456-426614174000",
    "productName": "Logitech MX Master 3S",
    "totalCost": 99.99,
    "reason": "Best combination of price, rating, and reviews"
  },
  "metrics": [
    {
      "name": "Lowest Price",
      "winner": "Mercado Livre",
      "value": "R$ 450.00",
      "description": "Cheapest option available"
    },
    {
      "name": "Highest Rating",
      "winner": "Mercado Livre",
      "value": "4.8/5",
      "description": "Best customer satisfaction"
    },
    {
      "name": "Most Reviews",
      "winner": "Amazon",
      "value": "8234 reviews",
      "description": "Most validated by customers"
    }
  ]
}
```

**Status Codes:**
- `200 OK` - Success
- `400 Bad Request` - Invalid product IDs
- `404 Not Found` - Product not found

---

## Error Responses

All errors follow this format:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request parameters",
  "details": [
    "Search query is required",
    "Max price must be positive"
  ],
  "path": "/api/v1/search",
  "timestamp": "2026-03-12T19:30:00Z"
}
```

---

## Examples

### Example 1: Search for Wireless Keyboard

```bash
curl -X POST http://localhost:8080/api/v1/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mechanical keyboard wireless",
    "maxPrice": 150,
    "sortBy": "HIGHEST_RATING"
  }'
```

### Example 2: Compare Two Products

```bash
curl -X POST http://localhost:8080/api/v1/compare \
  -H "Content-Type: application/json" \
  -d '{
    "productIds": [
      "123e4567-e89b-12d3-a456-426614174000",
      "223e4567-e89b-12d3-a456-426614174001"
    ]
  }'
```

### Example 3: Get Product Details

```bash
curl http://localhost:8080/api/v1/search/123e4567-e89b-12d3-a456-426614174000
```

---

## Rate Limiting

Currently no rate limiting. Will be added in future versions.

**Planned limits:**
- 100 requests per minute per IP
- 1000 requests per hour per IP

---

## Caching

Responses are cached for:
- Product details: 15 minutes
- Search results: 5 minutes

---

## OpenAPI Documentation

Interactive API documentation is available at:

**Swagger UI:** http://localhost:8080/swagger-ui.html
**OpenAPI Spec:** http://localhost:8080/api-docs

---

## Testing with Postman

Import the OpenAPI spec into Postman:
1. Open Postman
2. Import → Link
3. Enter: `http://localhost:8080/api-docs`
4. Done!

---

## SDK / Client Libraries

Coming soon:
- JavaScript/TypeScript SDK
- Python SDK
- Java Client

---

> "Meow!" - Gary
> *Translation: "Happy API testing!"*
