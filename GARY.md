# 🐌 Gary - Your Smart Shopping Assistant

## About Gary

Gary is SpongeBob's beloved pet snail, now transformed into an intelligent shopping assistant. Just like how Gary always knows what SpongeBob needs (even if it's just a good "meow"), Gary the AI Assistant knows how to find you the best deals across the internet!

## Core Principles

### 🎯 Mission
To help users find their desired products at the lowest price possible by searching across multiple popular e-commerce platforms.

### 🐌 Personality Traits
- **Loyal**: Always working in the user's best interest to find the best deals
- **Patient**: Will search thoroughly and persistently, like Gary waiting for his dinner
- **Smart**: Analyzes prices, shipping costs, and product ratings intelligently
- **Reliable**: Provides accurate, up-to-date information from trusted e-commerce sources

### 💡 Core Values
1. **Transparency**: Show clear price comparisons and sources
2. **Efficiency**: Fast searches without compromising accuracy
3. **User-Centric**: Simple, intuitive interface for all shopping needs
4. **Trust**: Only suggest legitimate products from verified sellers

## Technical Stack

- **Language**: Java 25
- **Framework**: Spring Boot (Latest Version)
- **Purpose**: Web scraping and price comparison service

## Supported E-commerce Platforms

### Primary Targets
- 🇧🇷 **Mercado Livre** (mercadolivre.com.br) - Latin America's leading marketplace
- 🌎 **Amazon** (amazon.com) - Global e-commerce giant

### Future Expansion
- Additional regional and international marketplaces
- Specialty stores for specific product categories

## Key Features

### 1. Product Search
- Multi-platform search capability
- Real-time price fetching
- Product matching across different platforms

### 2. Price Comparison
- Side-by-side price analysis
- Shipping cost inclusion
- Total cost calculation
- Historical price tracking (future)

### 3. Smart Filtering
- Filter by seller reputation
- Minimum rating threshold
- Shipping options
- Product condition (new/used)

### 4. User Notifications
- Price drop alerts
- Deal notifications
- Stock availability updates

### 5. Search Intelligence
- Natural language product queries
- Category suggestions
- Similar product recommendations
- Brand and model matching

## Architecture Overview

```
┌─────────────────┐
│   User Request  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Gary API       │
│  (Spring Boot)  │
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌────────┐ ┌────────┐
│ ML API │ │ Amazon │
│ Scraper│ │ Scraper│
└────────┘ └────────┘
    │         │
    └────┬────┘
         ▼
┌─────────────────┐
│ Price Analyzer  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Response       │
│  (Best Deals)   │
└─────────────────┘
```

## API Endpoints (Proposed)

### Search Product
```
POST /api/v1/search
{
  "query": "smartphone samsung galaxy",
  "maxPrice": 2000.00,
  "platforms": ["mercadolivre", "amazon"],
  "sortBy": "lowest_price"
}
```

### Get Product Comparison
```
GET /api/v1/compare?productId={id}
```

### Track Price
```
POST /api/v1/track
{
  "productUrl": "https://...",
  "targetPrice": 1500.00,
  "notifyEmail": "user@example.com"
}
```

## Development Principles

### Clean Code
- Follow SOLID principles
- Comprehensive unit and integration tests
- Clear documentation and comments
- Modular, maintainable architecture

### Performance
- Efficient web scraping with rate limiting
- Caching strategies for frequently searched products
- Asynchronous processing for multiple platform searches
- Database optimization for quick queries

### Security
- Respect robots.txt and platform terms of service
- Secure API endpoints with authentication
- Data privacy and GDPR compliance
- Rate limiting to prevent abuse

### Scalability
- Microservices-ready architecture
- Horizontal scaling capability
- Queue-based processing for heavy loads
- Cloud-native design

## Ethical Considerations

1. **Respect Platform Policies**: Comply with each e-commerce platform's terms of service
2. **Fair Usage**: Implement rate limiting and respectful scraping practices
3. **Data Privacy**: Never store or share user personal data unnecessarily
4. **Transparency**: Clearly indicate data sources and last update times
5. **No Manipulation**: Present unbiased price comparisons without favoring any platform

## Getting Started

### Prerequisites
- Java 25 JDK
- Maven or Gradle
- PostgreSQL or MySQL (for data persistence)
- Redis (for caching)

### Installation
```bash
# Clone the repository
git clone https://github.com/your-org/gary-assistant.git

# Navigate to project directory
cd gary-assistant

# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

## Roadmap

- [ ] Phase 1: Core search functionality for Mercado Livre
- [ ] Phase 2: Amazon integration
- [ ] Phase 3: Price history tracking
- [ ] Phase 4: User accounts and wish lists
- [ ] Phase 5: Mobile app integration
- [ ] Phase 6: AI-powered deal recommendations
- [ ] Phase 7: Additional e-commerce platforms

## Contributing

Gary welcomes contributions! Just like SpongeBob takes care of Gary, we hope you'll help take care of this project.

---

> "Meow!" - Gary
>
> *Translation: "Let's find you the best deals!"*
