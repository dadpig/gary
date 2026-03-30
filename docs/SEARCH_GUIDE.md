# Gary Search Guide - Finding the Best Deals

## Quick Start: Product Search & Comparison

This guide helps you find the best deals by comparing prices across Amazon and Mercado Livre.

---

## Step 1: Define Your Search

Before starting, clearly define:

```markdown
### Product Requirements
- **Product**: [What are you looking for?]
- **Budget**: [Maximum price you're willing to pay]
- **Usage**: [Personal, gift, professional?]
- **Must-Have Features**: [List deal-breakers]
- **Nice-to-Have Features**: [Preferences]
```

---

## Step 2: Search Amazon

### Using the amazon-shopping Skill

The amazon-shopping skill is already configured in Gary. Use it to search Amazon.com:

```bash
# The skill will guide you through:
# 1. Requirements gathering (budget, usage, preferences)
# 2. Automated Amazon search
# 3. Product extraction and verification
# 4. Ranked recommendations
```

**Key Features:**
- Automatic ASIN extraction
- Product page verification
- Price validation
- Rating and review analysis

**Expected Output:**
```markdown
## Amazon Shortlist - [Category]

### 1. [Product Name] - $XX.XX ✓ VERIFIED
**ASIN**: B0XXXXXXXXX (verified on product page)
**Rating**: 4.5/5 (12,345 reviews)
**Amazon**: https://www.amazon.com/dp/B0XXXXXXXXX
**Shipping**: [Free/Prime/$X.XX]
**Why this**: [Reason for recommendation]
**Key specs**: [Important features]
```

---

## Step 3: Search Mercado Livre

### Manual Search (Until ML Scraper is Built)

1. **Visit Mercado Livre BR**: https://www.mercadolivre.com.br
2. **Search for product**: Use similar keywords as Amazon search
3. **Filter results**:
   - Free shipping (Frete grátis)
   - Seller reputation (Reputação do vendedor)
   - Product condition (Novo/Usado)
   - Price range

### Manual Data Collection

For each promising product, record:

```markdown
### Product: [Name]
- **Price**: R$ XXX,XX
- **Link**: [Full URL]
- **Seller**: [Seller name]
- **Reputation**: [Thermometer color - green/yellow/orange/red]
- **Rating**: X.X/5 (XXX reviews)
- **Shipping**: R$ XX,XX or "Grátis"
- **Delivery Time**: [X-X days]
- **Condition**: Novo/Usado
```

---

## Step 4: Compare Prices

### Use the Comparison Template

Copy the comparison template to compare your findings:

**Formula for Total Cost:**
```
Total Cost = Product Price + Shipping Cost
```

**Currency Conversion (when needed):**
```
# Check current exchange rate
USD to BRL = [Current rate]
BRL to USD = [Current rate]
```

### Comparison Matrix

| Platform | Product | Price | Shipping | Total | Rating | Reviews | Delivery |
|----------|---------|-------|----------|-------|--------|---------|----------|
| Amazon   | [Name]  | $XX   | $XX/Free | $XX   | X.X/5  | XX,XXX  | X-X days |
| ML       | [Name]  | R$XX  | R$XX/Free| R$XX  | X.X/5  | XXX     | X-X days |

### Calculate in Common Currency

```markdown
**Amazon Total (USD)**: $XX.XX
**Amazon Total (BRL)**: R$ XXX,XX (at R$/$ = X.XX)

**Mercado Livre Total (BRL)**: R$ XXX,XX
**Mercado Livre Total (USD)**: $XX.XX (at R$/$ = X.XX)

### Winner: [Platform]
**Savings**: R$ XX,XX or $XX.XX
**Reason**: [Lower total cost / Better shipping / Faster delivery / Higher rating]
```

---

## Step 5: Decision Factors

### Price-Only Decision
- Lowest total cost (price + shipping)
- Consider currency conversion for fair comparison

### Quality-Adjusted Decision
Consider:
- **Trust**: Amazon (international) vs ML (local, better import duties handling)
- **Delivery Time**: Faster option might be worth small premium
- **Return Policy**: Amazon usually easier for international returns
- **Reviews**: More reviews = more reliable rating
- **Warranty**: Local warranty vs international
- **Import Taxes**: ML often includes taxes; Amazon may charge at delivery

### Recommendation Template

```markdown
## Final Recommendation

### Best Price: [Platform] - [Product Name]
**Total Cost**: [Amount in BRL and USD]
**Link**: [URL]
**Why**: [Explanation]

### Best Value: [Platform] - [Product Name]
**Total Cost**: [Amount in BRL and USD]
**Link**: [URL]
**Why**: [Considering quality, shipping, trust, etc.]

### Quick Comparison
| Factor | Amazon | Mercado Livre | Winner |
|--------|--------|---------------|--------|
| Price | $XX | R$XX (≈$XX) | [Platform] |
| Shipping | [Free/Cost] | [Free/Cost] | [Platform] |
| Total | $XX | $XX | [Platform] |
| Delivery | X days | X days | [Platform] |
| Trust | High | High (local) | [Tie/Platform] |
| Warranty | International | Local | ML |
| **VERDICT** | | | **[Platform]** |
```

---

## Tips for Better Comparisons

### Amazon Search Tips
- Use the amazon-shopping skill for automated, verified results
- Look for Prime-eligible items for free/fast shipping
- Check for coupons and promotions
- Consider Subscribe & Save discounts

### Mercado Livre Tips
- Look for "Full" items (Mercado Livre handles shipping)
- Check seller reputation carefully
- Filter by free shipping when possible
- Consider "Comprando agora" deals
- Use ML's price tracking for trending products

### General Tips
- **Compare exact models/specifications**
- **Check reviews for common issues**
- **Factor in return/exchange policies**
- **Consider payment methods** (credit card protection, installments)
- **Check warranty coverage** (international vs local)
- **Import duties**: ML often better for avoiding surprise charges

---

## Workflow Summary

```
1. Define Requirements
   ↓
2. Search Amazon (use amazon-shopping skill)
   ↓
3. Search Mercado Livre (manual for now)
   ↓
4. Extract Key Data (price, shipping, reviews)
   ↓
5. Fill Comparison Template
   ↓
6. Convert Currencies
   ↓
7. Calculate Total Costs
   ↓
8. Consider Quality Factors
   ↓
9. Make Recommendation
```

---

## Example Search

### Product: Logitech MX Master 3S Mouse

#### Requirements
- **Budget**: Up to $100 or R$500
- **Usage**: Professional work (programming)
- **Must-Have**: Ergonomic, Bluetooth, good battery
- **Nice-to-Have**: Customizable buttons, fast charging

#### Search Results

**Amazon:**
- Price: $99.99
- Shipping: Free (Prime)
- Total: $99.99
- Rating: 4.7/5 (8,234 reviews)
- Delivery: 2 days

**Mercado Livre:**
- Price: R$ 450.00
- Shipping: Free
- Total: R$ 450.00
- Rating: 4.8/5 (234 reviews)
- Delivery: 3-5 days

#### Comparison (USD 1 = BRL 5.00)

| Platform | Total USD | Total BRL | Winner |
|----------|-----------|-----------|--------|
| Amazon | $99.99 | R$ 499.95 | - |
| ML | $90.00 | R$ 450.00 | ML (saves R$49.95) |

#### Recommendation
**Winner: Mercado Livre**
- Saves R$ 49.95 ($9.99)
- Similar/better rating
- Slightly longer delivery acceptable
- Local warranty easier to use
- No import duty surprises

---

## Future Enhancements

Once the Mercado Livre scraper is implemented:
- Automated ML search and extraction
- Side-by-side automated comparison
- Historical price tracking
- Price drop alerts
- API integration for real-time comparison

---

> "Meow!" - Gary
>
> *Translation: "I found you the best deal!"*
