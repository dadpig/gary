# Gary Project Structure

This document outlines the complete project structure for Gary, the smart shopping assistant.

---

## Directory Layout

```
gary/
├── README.md                          # Project overview
├── Gary.md                            # Project vision and architecture
├── pom.xml / build.gradle            # Build configuration (Java/Spring Boot)
├── .gitignore                        # Git ignore rules
│
├── src/                              # Source code (Java Spring Boot)
│   ├── main/
│   │   ├── java/
│   │   │   └── com/gary/assistant/
│   │   │       ├── GaryApplication.java
│   │   │       ├── config/            # Configuration classes
│   │   │       ├── controller/        # REST API controllers
│   │   │       │   ├── SearchController.java
│   │   │       │   ├── ComparisonController.java
│   │   │       │   └── TrackingController.java
│   │   │       ├── service/           # Business logic
│   │   │       │   ├── SearchService.java
│   │   │       │   ├── PriceComparisonService.java
│   │   │       │   ├── ScraperService.java
│   │   │       │   └── NotificationService.java
│   │   │       ├── scraper/           # Web scraping modules
│   │   │       │   ├── AmazonScraper.java
│   │   │       │   ├── MercadoLivreScraper.java
│   │   │       │   └── BaseScraper.java
│   │   │       ├── model/             # Data models
│   │   │       │   ├── Product.java
│   │   │       │   ├── Price.java
│   │   │       │   ├── SearchRequest.java
│   │   │       │   └── Comparison.java
│   │   │       ├── repository/        # Database access
│   │   │       │   ├── ProductRepository.java
│   │   │       │   └── PriceHistoryRepository.java
│   │   │       └── util/              # Utilities
│   │   │           ├── CurrencyConverter.java
│   │   │           ├── PriceExtractor.java
│   │   │           └── ProductMatcher.java
│   │   └── resources/
│   │       ├── application.yml        # App configuration
│   │       ├── application-dev.yml    # Dev configuration
│   │       ├── application-prod.yml   # Prod configuration
│   │       └── templates/             # Email templates
│   │           └── price-alert.html
│   └── test/
│       └── java/
│           └── com/gary/assistant/
│               ├── controller/         # Controller tests
│               ├── service/            # Service tests
│               └── scraper/            # Scraper tests
│
├── docs/                              # Documentation
│   ├── PROJECT_STRUCTURE.md           # This file
│   ├── SEARCH_GUIDE.md                # User guide for searching
│   ├── COMPARISON_TEMPLATE.md         # Template for comparisons
│   ├── API_DOCUMENTATION.md           # API endpoints reference
│   ├── DEVELOPMENT_GUIDE.md           # Developer setup
│   ├── SCRAPING_GUIDE.md              # Web scraping best practices
│   └── DEPLOYMENT.md                  # Deployment instructions
│
├── .agents/                           # Agent skills directory
│   └── skills/
│       ├── amazon-shopping/           # Amazon shopping skill
│       │   ├── SKILL.md
│       │   ├── scripts/
│       │   │   ├── extract_products.py
│       │   │   ├── rank_products.py
│       │   │   └── verify_products.sh
│       │   └── reference/
│       │       ├── asin-extraction.md
│       │       ├── output-formats.md
│       │       └── common-errors.md
│       └── mercadolivre-shopping/     # [Future] ML shopping skill
│           └── SKILL.md
│
├── scripts/                           # Utility scripts
│   ├── setup.sh                       # Project setup script
│   ├── run-dev.sh                     # Run in dev mode
│   ├── test-scrapers.sh               # Test scraper functionality
│   └── price-check.sh                 # Manual price checking
│
├── comparisons/                       # Saved price comparisons
│   ├── 2026-03-12_logitech-mx-master.md
│   ├── 2026-03-10_kindle-paperwhite.md
│   └── README.md                      # How to use comparisons folder
│
├── config/                            # Configuration files
│   ├── database.yml                   # Database configuration
│   ├── redis.yml                      # Cache configuration
│   └── scraper-config.yml             # Scraper settings
│
├── docker/                            # Docker configuration
│   ├── Dockerfile                     # Main application
│   ├── docker-compose.yml             # Full stack setup
│   └── nginx/                         # Reverse proxy config
│       └── nginx.conf
│
└── infrastructure/                    # Infrastructure as Code
    ├── terraform/                     # Cloud infrastructure
    │   ├── main.tf
    │   └── variables.tf
    └── kubernetes/                    # K8s deployments
        ├── deployment.yml
        └── service.yml
```

---

## Key Directories Explained

### `/src/main/java/` - Application Code

#### Controllers (`controller/`)
Handle HTTP requests and responses for the REST API.

**SearchController.java**
- `POST /api/v1/search` - Search products across platforms
- `GET /api/v1/search/{id}` - Get search results by ID

**ComparisonController.java**
- `GET /api/v1/compare` - Compare product prices
- `POST /api/v1/compare` - Create new comparison

**TrackingController.java**
- `POST /api/v1/track` - Start tracking a product
- `GET /api/v1/track/{id}` - Get tracking status
- `DELETE /api/v1/track/{id}` - Stop tracking

#### Services (`service/`)
Business logic and orchestration.

**ScraperService.java**
- Coordinates scraping across platforms
- Rate limiting and retry logic
- Error handling

**PriceComparisonService.java**
- Compares prices across platforms
- Currency conversion
- Shipping cost analysis
- Calculates best deals

**SearchService.java**
- Product search coordination
- Result aggregation
- Caching strategy

#### Scrapers (`scraper/`)
Platform-specific web scraping logic.

**BaseScraper.java** (Abstract)
- Common scraping functionality
- HTTP client setup
- Rate limiting
- Error handling

**AmazonScraper.java**
- Amazon.com specific scraping
- ASIN extraction
- Price verification
- Review parsing

**MercadoLivreScraper.java**
- Mercado Livre specific scraping
- Product ID extraction
- Seller reputation parsing
- Shipping calculation

#### Models (`model/`)
Data structures and DTOs.

**Product.java**
```java
class Product {
    String id;
    String name;
    String platform;
    Price price;
    Rating rating;
    String url;
    // ... more fields
}
```

**Price.java**
```java
class Price {
    BigDecimal amount;
    String currency;
    BigDecimal shipping;
    BigDecimal total;
    LocalDateTime lastUpdated;
}
```

---

### `/docs/` - Documentation

**SEARCH_GUIDE.md**
- Step-by-step guide for users
- How to search and compare
- Best practices

**COMPARISON_TEMPLATE.md**
- Template for documenting comparisons
- Standardized format
- Examples

**API_DOCUMENTATION.md**
- Complete API reference
- Request/response examples
- Authentication details

**DEVELOPMENT_GUIDE.md**
- Developer setup instructions
- Code conventions
- Testing guidelines

**SCRAPING_GUIDE.md**
- Web scraping best practices
- Legal considerations
- Rate limiting strategies

---

### `/.agents/skills/` - Agent Skills

Pre-built skills for Claude Code to use:

**amazon-shopping/**
- Automated Amazon search
- Product verification
- Price extraction

**mercadolivre-shopping/** (Future)
- Automated Mercado Livre search
- Similar structure to Amazon skill

---

### `/comparisons/` - Saved Comparisons

Store historical product comparisons using the template:

```
comparisons/
├── README.md
├── 2026-03-12_logitech-mx-master.md
├── 2026-03-10_kindle-paperwhite.md
└── 2026-03-08_sony-headphones.md
```

**Naming Convention**: `YYYY-MM-DD_product-name.md`

Each file uses COMPARISON_TEMPLATE.md format.

---

### `/scripts/` - Utility Scripts

**setup.sh**
```bash
# Install dependencies
# Setup database
# Configure environment
```

**test-scrapers.sh**
```bash
# Test Amazon scraper
# Test ML scraper
# Validate results
```

**price-check.sh**
```bash
#!/bin/bash
# Quick manual price check script
PRODUCT=$1
./mvnw spring-boot:run -Dspring-boot.run.arguments="--search=$PRODUCT"
```

---

### `/config/` - Configuration Files

**scraper-config.yml**
```yaml
scrapers:
  amazon:
    rate_limit: 10  # requests per minute
    timeout: 30     # seconds
    user_agent: "Gary/1.0"
  mercadolivre:
    rate_limit: 15
    timeout: 30
```

---

## Configuration Flow

```
User Request
     ↓
SearchController (REST API)
     ↓
SearchService (Business Logic)
     ↓
  ┌──┴──┐
  ↓     ↓
AmazonScraper  MercadoLivreScraper
  ↓     ↓
  └──┬──┘
     ↓
PriceComparisonService
     ↓
Response to User
```

---

## Data Flow

### Search Flow
1. User submits search query via API
2. SearchService delegates to platform scrapers
3. Scrapers fetch and parse results
4. Results stored in cache (Redis)
5. PriceComparisonService analyzes results
6. Formatted response returned to user

### Tracking Flow
1. User requests price tracking
2. Product info stored in database
3. Scheduled job checks prices daily
4. Price changes trigger notifications
5. User receives email/SMS alert

---

## Development Workflow

### Local Development
```bash
# Start dependencies
docker-compose up -d postgres redis

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build for production
./mvnw clean package
```

### Using the Search Guide
1. Open `docs/SEARCH_GUIDE.md`
2. Follow step-by-step instructions
3. Use amazon-shopping skill for Amazon
4. Manually search Mercado Livre (for now)
5. Fill out COMPARISON_TEMPLATE.md
6. Save to `/comparisons/`

---

## Future Enhancements

### Phase 1 (Current)
- [x] Project structure
- [x] Documentation
- [x] Amazon shopping skill integration
- [ ] Basic Spring Boot setup

### Phase 2
- [ ] Amazon scraper implementation
- [ ] Basic API endpoints
- [ ] Manual comparison workflow

### Phase 3
- [ ] Mercado Livre scraper
- [ ] Automated comparison
- [ ] Price history tracking

### Phase 4
- [ ] ML shopping skill
- [ ] Real-time comparison API
- [ ] Web frontend

### Phase 5
- [ ] Price alerts
- [ ] User accounts
- [ ] Mobile app

---

## Getting Started

1. **Read the documentation**:
   ```bash
   cat docs/SEARCH_GUIDE.md
   ```

2. **Use the amazon-shopping skill**:
   - Search for products on Amazon
   - Get verified, ranked results

3. **Manual Mercado Livre search**:
   - Follow SEARCH_GUIDE.md steps
   - Record findings manually

4. **Create comparison**:
   ```bash
   cp docs/COMPARISON_TEMPLATE.md comparisons/2026-03-12_my-product.md
   # Fill in the template with your findings
   ```

5. **Make informed decision**:
   - Review the comparison
   - Consider all factors
   - Choose best option

---

## File Naming Conventions

### Comparisons
- Format: `YYYY-MM-DD_product-name.md`
- Example: `2026-03-12_logitech-mx-master-3s.md`

### Documentation
- Use UPPERCASE for main docs
- Descriptive names
- Use hyphens for spaces

### Code
- Follow Java naming conventions
- CamelCase for classes
- camelCase for methods

---

> "Meow!" - Gary
> *Translation: "Well-organized structure for smart shopping!"*
