# Gary Quick Start Guide

Get started with Gary in 5 minutes and find your first best deal!

---

## What is Gary?

Gary is your smart shopping assistant that helps you find the best deals by comparing prices across Amazon and Mercado Livre. Just like SpongeBob's loyal snail, Gary is here to help you save money!

---

## Prerequisites

- Access to amazon-shopping skill (already configured)
- Internet connection
- Basic command line knowledge (optional)

---

## 5-Minute Quick Start

### Step 1: Define What You Want (1 min)

Write down:
1. **Product name**: What are you looking for?
2. **Budget**: Maximum price?
3. **Must-have features**: Deal-breakers?

**Example**:
```
Product: Wireless ergonomic mouse
Budget: Up to $100
Must-have: Bluetooth, ergonomic design, good battery
```

---

### Step 2: Search Amazon (2 min)

Use the **amazon-shopping skill** - it's already set up and ready to use!

The skill will:
- Ask about your requirements
- Search Amazon automatically
- Extract verified product data
- Show ranked recommendations

**You get**:
- Product names with verified prices
- Ratings and review counts
- Direct Amazon links
- Shipping information

---

### Step 3: Search Mercado Livre (1 min)

**Manual search** (automated version coming soon):

1. Go to: https://www.mercadolivre.com.br
2. Search using similar keywords
3. Note the top 2-3 products:
   - Price (R$)
   - Shipping cost
   - Seller reputation
   - Link

---

### Step 4: Quick Compare (1 min)

Create a simple comparison:

```markdown
## Quick Comparison: [Product Name]

### Amazon
- Price: $XX.XX
- Shipping: Free/Prime
- Total: $XX.XX
- Rating: X.X/5 (X,XXX reviews)
- Link: [URL]

### Mercado Livre
- Price: R$ XXX,XX
- Shipping: Grátis/R$ XX
- Total: R$ XXX,XX (≈$XX at rate X.XX)
- Rating: X.X/5 (XXX reviews)
- Link: [URL]

### Winner: [Platform]
Saves: R$ XX or $XX
Reason: [Lower price / Free shipping / Faster delivery]
```

**Pro tip**: Current exchange rate is around USD 1.00 = BRL 5.00 (check current rate!)

---

## Example: Quick Product Search

### Scenario
"I need a wireless mouse for programming work, budget up to $80"

### Amazon (via amazon-shopping skill)
```
1. Logitech MX Master 3S - $99.99 ✓
   - ASIN: B09HM94VDS
   - Rating: 4.7/5 (8,234 reviews)
   - Shipping: Free Prime
   - Total: $99.99
```

### Mercado Livre (manual)
```
1. Logitech MX Master 3S - R$ 450.00
   - Shipping: Free
   - Rating: 4.8/5 (234 reviews)
   - Total: R$ 450.00 (≈$90 at 5.00 rate)
```

### Quick Decision
**Winner: Mercado Livre**
- Saves: $9.99
- Same product, verified seller
- Local warranty easier to use

---

## Next Steps

### For Detailed Comparison
Use the full template:
```bash
cp docs/COMPARISON_TEMPLATE.md comparisons/2026-03-12_my-product.md
```

Fill in all sections for comprehensive analysis.

### Save Your Comparison
```bash
# Save in comparisons folder
echo "[Your quick comparison]" > comparisons/2026-03-12_product-name.md
```

### Learn More
- Read `docs/SEARCH_GUIDE.md` for detailed instructions
- Check `docs/COMPARISON_TEMPLATE.md` for full template
- See `docs/PROJECT_STRUCTURE.md` for project organization

---

## Tips for Success

### Amazon Tips
- Use the amazon-shopping skill (it's automatic!)
- Look for Prime-eligible items
- Check for coupons on product page
- Consider Subscribe & Save discounts

### Mercado Livre Tips
- Filter by "Frete grátis" (free shipping)
- Check seller reputation (green thermometer = good)
- Look for "ML Full" items (better service)
- Use "Comprando agora" deals

### Comparison Tips
- **Always compare total cost** (product + shipping)
- **Convert currencies** for fair comparison
- **Consider delivery time** (worth a small premium?)
- **Check return policies** (international vs local)
- **Review count matters** (more reviews = more reliable)

---

## Common Scenarios

### Scenario 1: Lowest Price Matters Most
1. Search both platforms
2. Calculate total cost (price + shipping)
3. Convert to same currency
4. Choose lowest total cost

**Choose**: Platform with lowest total cost in your currency

### Scenario 2: Fast Delivery Needed
1. Check delivery estimates
2. Consider Prime shipping (1-2 days)
3. ML Full often faster in Brazil

**Choose**: Fastest delivery, even if slightly more expensive

### Scenario 3: Local Warranty Important
1. Electronics with warranty needs
2. Local (ML) vs International (Amazon) warranty
3. Consider ease of returns/repairs

**Choose**: Mercado Livre for easier local support

---

## Keyboard Shortcuts (Command Line)

```bash
# Quick search Amazon (using skill)
# Just ask: "Search for [product] on Amazon"

# View saved comparisons
ls comparisons/

# Create new comparison from template
cp docs/COMPARISON_TEMPLATE.md comparisons/$(date +%Y-%m-%d)_product.md

# View latest comparison
cat comparisons/$(ls -t comparisons/ | head -1)
```

---

## Troubleshooting

### Amazon skill not working?
- Check if agent-browser is installed
- Make sure you're connected to internet
- Try closing and reopening the session

### Can't find product on Mercado Livre?
- Try different keywords (Portuguese terms)
- Broaden your search
- Check spelling

### Prices seem off?
- Verify on actual product page
- Check if price includes all variants
- Look for hidden costs (shipping, import duties)

---

## Real World Example

### User Request
"Find me the best price for Kindle Paperwhite"

### Gary's Process (3 minutes)

**Step 1: Amazon (30 seconds)**
- Use amazon-shopping skill
- Found: Kindle Paperwhite (16GB) - $149.99
- Free Prime shipping
- 4.6/5 rating (45,678 reviews)

**Step 2: Mercado Livre (30 seconds)**
- Manual search: "Kindle Paperwhite"
- Found: Kindle Paperwhite (16GB) - R$ 699,00
- Free shipping
- 4.8/5 rating (892 reviews)

**Step 3: Compare (30 seconds)**
- Amazon: $149.99 = R$ 749.95 (at rate 5.00)
- ML: R$ 699.00 = $139.80
- **Savings with ML: $10.19**

**Step 4: Decision (30 seconds)**
- **Winner: Mercado Livre**
- **Reason**: Saves R$ 50.95, local warranty, easier returns
- **Action**: Purchase from ML

**Step 5: Save comparison (30 seconds)**
- Documented in `comparisons/2026-03-12_kindle-paperwhite.md`
- Can reference later for similar purchases

---

## Your First Search Checklist

- [ ] Define product requirements clearly
- [ ] Set realistic budget
- [ ] Search Amazon using amazon-shopping skill
- [ ] Search Mercado Livre manually
- [ ] Record top 2-3 options from each platform
- [ ] Calculate total costs (including shipping)
- [ ] Convert to common currency
- [ ] Compare total costs
- [ ] Consider quality factors (rating, reviews, warranty)
- [ ] Make decision
- [ ] Save comparison for future reference
- [ ] Purchase from winning platform

---

## Success Metrics

After using Gary, you should:
- ✅ Save money on purchases (typically 5-20%)
- ✅ Make informed decisions quickly (under 5 minutes)
- ✅ Feel confident about your choice
- ✅ Build a library of comparisons for future reference

---

## Get Help

**Need more details?**
- Read `docs/SEARCH_GUIDE.md` - Comprehensive guide
- Check `docs/COMPARISON_TEMPLATE.md` - Full template
- Review `docs/PROJECT_STRUCTURE.md` - Project overview

**Found a bug or have feedback?**
- Open an issue on GitHub
- Document what went wrong
- Share your comparison file

---

## What's Next?

Once you're comfortable with the basics:

1. **Try complex comparisons** - Multiple variants, different models
2. **Track price history** - Save comparisons over time
3. **Build your comparison library** - Reference past decisions
4. **Share insights** - Help improve Gary for everyone

---

> "Meow!" - Gary
> *Translation: "You're ready to start saving money!"*
