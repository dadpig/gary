# Product Comparisons Archive

This directory stores historical product price comparisons between Amazon and Mercado Livre.

## Purpose

- Track price comparisons over time
- Reference past research when buying similar products
- Learn from previous shopping decisions
- Identify pricing patterns and trends

## How to Use

### Creating a New Comparison

1. **Copy the template**:
   ```bash
   cp ../docs/COMPARISON_TEMPLATE.md ./YYYY-MM-DD_product-name.md
   ```

2. **Fill in the details**:
   - Search results from Amazon (using amazon-shopping skill)
   - Search results from Mercado Livre (manual for now)
   - Complete the comparison matrix
   - Make your recommendation

3. **Save and commit**:
   ```bash
   git add ./YYYY-MM-DD_product-name.md
   git commit -m "Add price comparison for [product]"
   ```

## Naming Convention

**Format**: `YYYY-MM-DD_product-name.md`

**Examples**:
- `2026-03-12_logitech-mx-master-3s.md`
- `2026-03-10_kindle-paperwhite-11gen.md`
- `2026-03-08_sony-wh1000xm5.md`

**Rules**:
- Date comes first (for easy sorting)
- Use hyphens for spaces
- Keep it concise but descriptive
- Include model number if relevant
- Use lowercase

## Directory Structure

```
comparisons/
├── README.md                              # This file
├── 2026-03-12_logitech-mx-master-3s.md   # Recent comparison
├── 2026-03-10_kindle-paperwhite.md
├── 2026-03-08_sony-headphones.md
└── archive/                               # Older comparisons
    └── 2025-12-15_iphone-14.md
```

## Comparison Lifecycle

### Active Comparisons (Recent)
Keep in root of `/comparisons/` for easy access.

### Archived Comparisons (6+ months old)
Move to `/archive/` subfolder to keep main directory clean.

```bash
# Archive old comparisons
mv 2025-*.md archive/
```

## Quick Reference

### View Latest Comparisons
```bash
# Show 5 most recent
ls -lt | head -6
```

### Search for Product
```bash
# Find all comparisons for "logitech"
grep -l "logitech" *.md
```

### Count Total Comparisons
```bash
find . -name "*.md" ! -name "README.md" | wc -l
```

## Tips

### Before Comparing
- Check if you've compared similar products before
- Review past decisions and outcomes
- Learn from previous mistakes

### During Comparison
- Be thorough with data collection
- Include all relevant factors
- Take screenshots if helpful

### After Purchase
- Update the file with actual outcome:
  ```markdown
  ## Purchase Outcome
  **Purchased From**: [Platform]
  **Final Price**: [Amount]
  **Delivery Experience**: [Rating/10]
  **Product Quality**: [Rating/10]
  **Would Buy Again**: [Yes/No]
  **Notes**: [Any surprises or lessons learned]
  ```

## Example Workflow

### Step 1: Start New Comparison
```bash
cd comparisons
cp ../docs/COMPARISON_TEMPLATE.md 2026-03-12_wireless-mouse.md
```

### Step 2: Gather Data
- Use amazon-shopping skill for Amazon
- Manually search Mercado Livre
- Fill in the template

### Step 3: Analyze
- Complete comparison matrix
- Calculate total costs
- Make recommendation

### Step 4: Save
```bash
git add 2026-03-12_wireless-mouse.md
git commit -m "Compare wireless mouse options"
```

### Step 5: Decide & Purchase
Follow your recommendation and make the purchase.

### Step 6: Update Post-Purchase (Optional)
Add "Purchase Outcome" section after receiving product.

## Analytics

### Price Trends
Track how prices change over time for similar products:
```bash
# Example: See all mechanical keyboard comparisons
grep -r "mechanical keyboard" . | cut -d: -f1
```

### Best Platforms
Review past comparisons to see which platform generally offers better deals:
```bash
# Count recommendations by platform
grep "MY CHOICE:" *.md | grep -o "Amazon\|Mercado Livre" | sort | uniq -c
```

### Savings Achieved
Calculate total savings across all comparisons:
```bash
grep "Savings with" *.md
```

## Maintenance

### Monthly Review
- Archive comparisons older than 6 months
- Review patterns and learnings
- Update search strategies based on findings

### Cleanup
```bash
# Move old files to archive
mkdir -p archive
find . -name "2025-*.md" -exec mv {} archive/ \;
```

## Templates Available

1. **COMPARISON_TEMPLATE.md** - Full detailed comparison
2. **Quick Comparison** (coming soon) - Simplified version
3. **Bulk Compare** (coming soon) - Multiple items at once

---

> "Meow!" - Gary
> *Translation: "Keep track of your smart shopping decisions!"*
