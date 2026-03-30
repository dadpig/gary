# Gary - Smart Shopping Assistant 🐌

```
        @@@@@@@
      @@       @@
     @  o   o   @       "Meow!"
    @      ^     @
    @    \___/   @      Finding you the best deals!
     @          @
      @@  ___  @@
        @@   @@
       ___|___|___
      (___) (___)
```

Gary is your intelligent shopping companion that helps you find the best deals by comparing prices across Amazon and Mercado Livre.

> "Meow!" - Gary the Snail 🐌
>
> *Translation: "Let me help you save money!"*

## Features

- 🔍 **Multi-Platform Search**: Search products on Amazon and Mercado Livre simultaneously
- 💰 **Price Comparison**: Compare total costs including shipping
- ⭐ **Smart Recommendations**: Rankings based on price, rating, and review count
- 🚀 **Modern REST API**: Built with Spring Boot 3.4 and Java 25
- 📊 **Real-time Data**: Up-to-date pricing information
- 📈 **Value Analysis**: Find the best deal considering quality and price

## Tech Stack

- **Java 25** - Latest Java features (records, pattern matching, sealed classes)
- **Spring Boot 3.4.3** - Modern Spring framework
- **PostgreSQL / H2** - Production and development databases
- **Redis** - Caching layer
- **JSoup** - Web scraping
- **OpenAPI/Swagger** - API documentation
- **JUnit 5** - Testing framework

## Quick Start

### Prerequisites

- Java 25 (OpenJDK)
- Maven 3.8+
- PostgreSQL (for production)
- Redis (optional, for caching)

### Installation

```bash
# Clone the repository
cd gary

# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### API Documentation

Once running, visit:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/api-docs

## API Endpoints

### Search Products

```bash
POST /api/v1/search
Content-Type: application/json

{
  "query": "wireless mouse",
  "maxPrice": 100.00,
  "platforms": ["AMAZON", "MERCADO_LIVRE"],
  "sortBy": "LOWEST_PRICE"
}
```

### Get Product Details

```bash
GET /api/v1/search/{productId}
```

### Compare Products

```bash
POST /api/v1/compare
Content-Type: application/json

{
  "productIds": [
    "uuid-1",
    "uuid-2"
  ]
}
```

### Health Check

```bash
GET /api/v1/health
GET /api/v1/health/scrapers
```

## Configuration

### Development Mode

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production Mode

Set environment variables:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/gary
export DATABASE_USERNAME=gary
export DATABASE_PASSWORD=your_password
export REDIS_HOST=localhost
export REDIS_PORT=6379

./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Project Structure

```
gary/
├── src/
│   ├── main/
│   │   ├── java/com/gary/assistant/
│   │   │   ├── config/         # Configuration classes
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── dto/           # Data Transfer Objects (Records)
│   │   │   ├── exception/     # Exception handling
│   │   │   ├── model/         # JPA entities
│   │   │   ├── repository/    # Data access layer
│   │   │   ├── scraper/       # Web scraping implementations
│   │   │   ├── service/       # Business logic
│   │   │   └── util/          # Utility classes
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
├── docs/                       # Documentation
│   ├── QUICK_START.md
│   ├── SEARCH_GUIDE.md
│   └── COMPARISON_TEMPLATE.md
├── comparisons/               # Saved price comparisons
└── pom.xml
```

## Modern Java Features Used

### Records (Java 14+)
```java
public record SearchRequest(
    String query,
    BigDecimal maxPrice,
    Set<Platform> platforms
) {}
```

### Pattern Matching (Java 21+)
```java
return switch (request.sortBy()) {
    case LOWEST_PRICE -> sortByPrice(products);
    case HIGHEST_RATING -> sortByRating(products);
    case MOST_REVIEWS -> sortByReviews(products);
};
```

### Sealed Classes (Java 17+)
Ready for future use with type hierarchies.

## Best Practices Implemented

✅ **No Lombok**: Uses standard Java features (records, getters/setters)
✅ **Constructor Injection**: All dependencies injected via constructor
✅ **Immutable DTOs**: Records are immutable by default
✅ **Exception Handling**: Global exception handler with proper HTTP status codes
✅ **Validation**: Bean validation on DTOs
✅ **Logging**: SLF4J with structured logging
✅ **API Documentation**: OpenAPI/Swagger annotations
✅ **Separation of Concerns**: Clear layers (controller → service → repository)
✅ **Async Processing**: Thread pool for parallel scraping
✅ **Caching**: Redis/Simple cache for frequent queries

## Development

### Running Tests

```bash
./mvnw test
```

### Code Coverage

```bash
./mvnw verify
```

### Format Code

```bash
./mvnw spotless:apply
```

## Documentation

- [Quick Start Guide](docs/QUICK_START.md) - Get started in 5 minutes
- [Search Guide](docs/SEARCH_GUIDE.md) - Detailed search instructions
- [Comparison Template](docs/COMPARISON_TEMPLATE.md) - Price comparison template
- [Project Structure](docs/PROJECT_STRUCTURE.md) - Architecture overview

## Roadmap

- [x] Project setup with Spring Boot 3.4 + Java 25
- [x] REST API with OpenAPI documentation
- [x] Domain models and DTOs using records
- [x] Service layer with comparison logic
- [ ] Amazon scraper implementation
- [ ] Mercado Livre scraper implementation
- [ ] Price history tracking
- [ ] Email notifications
- [ ] User authentication
- [ ] Frontend (React/Next.js)

## Contributing

Contributions are welcome! Please read the contributing guidelines first.

## License

MIT License - See LICENSE file for details

## Support

For issues and questions:
- GitHub Issues: https://github.com/your-org/gary/issues
- Documentation: Check the `/docs` folder

---

> "Meow!" - Gary 🐌
>
> *Translation: "Happy shopping and saving!"*
