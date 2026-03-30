# Gary Branding Guide

Visual identity and branding for Gary Assistant.

---

## ASCII Art (Included)

Gary's ASCII art is displayed at application startup and in the README!

### Startup Banner

When you run the application, you'll see Gary greeting you:

```
   ____                        _                _     _              _
  / ___| __ _ _ __ _   _     / \   ___ ___(_)___| |_ __ _ _ __ | |_
 | |  _ / _` | '__| | | |   / _ \ / __/ __| / __| __/ _` | '_ \| __|
 | |_| | (_| | |  | |_| |  / ___ \\__ \__ \ \__ \ || (_| | | | | |_
  \____|\__,_|_|   \__, | /_/   \_\___/___/_|___/\__\__,_|_| |_|\__|
                   |___/

        _____
      .'     '.
     /  o   o  \
    |     ^     |
    |   \___/   |
     \  _____  /
      '._____..'     "Meow!" - Let me find you the best deals!
       _|   |_
      (@)   (@)

╔═══════════════════════════════════════════════════════════════════╗
║  🐌 Gary - Your Smart Shopping Assistant                         ║
║  💰 Comparing prices across Amazon & Mercado Livre               ║
║  🚀 Powered by Spring Boot & Java 25                             ║
╚═══════════════════════════════════════════════════════════════════╝
```

The banner is located at: `src/main/resources/banner.txt`

---

## Creating a Custom Logo/Image

I cannot generate PNG images directly, but here are several ways to get a Gary logo:

### Option 1: AI Image Generation

Use AI tools to create a custom Gary image:

**DALL-E / Midjourney / Stable Diffusion:**
```
Prompt: "Cute cartoon snail named Gary, friendly expression,
wearing a small detective hat, holding a magnifying glass looking
at price tags, vibrant colors, mascot style, transparent background"
```

**Alternative Prompt:**
```
"Adorable blue snail mascot character with big friendly eyes,
shopping theme, cute kawaii style, simple design suitable for
app icon, transparent PNG background"
```

### Option 2: Commission an Artist

Platforms to find artists:
- **Fiverr**: $5-50 for simple mascot designs
- **Upwork**: Professional designers
- **99designs**: Design contest format
- **Dribbble**: Hire top designers

**What to request:**
- Snail character inspired by SpongeBob's Gary
- Friendly, approachable design
- Shopping/deal-finding theme elements (magnifying glass, shopping cart, price tags)
- Multiple formats: PNG, SVG, favicon
- Various sizes: 512x512, 256x256, 128x128, 64x64, 32x32

### Option 3: Use Emoji/Unicode

Simple representation using emojis:
- 🐌 (snail emoji)
- 🛒 (shopping cart)
- 💰 (money bag)
- 🏷️ (price tag)

Combined: 🐌🛒💰

### Option 4: Free Design Tools

Create your own using:
- **Canva**: Free online design tool
- **Figma**: Professional design tool (free tier)
- **Inkscape**: Free vector graphics editor
- **GIMP**: Free image editor

---

## Adding the Logo to README

Once you have an image, add it to the project:

### 1. Save the Image

```bash
mkdir -p docs/images
# Save your image as: docs/images/gary-logo.png
```

### 2. Update README

Add at the top of `README.md`:

```markdown
<div align="center">
  <img src="docs/images/gary-logo.png" alt="Gary Logo" width="200"/>
  <h1>Gary - Smart Shopping Assistant 🐌</h1>
</div>
```

Or for a banner style:

```markdown
![Gary Banner](docs/images/gary-banner.png)

# Gary - Smart Shopping Assistant 🐌
```

---

## Recommended Image Specifications

### Logo
- **Format**: PNG with transparent background
- **Size**: 512x512 pixels (square)
- **File size**: < 100KB
- **Colors**: Bright, friendly colors (blues, greens)
- **Style**: Cartoon/mascot, approachable

### Banner
- **Format**: PNG
- **Size**: 1200x300 pixels (horizontal banner)
- **File size**: < 200KB
- **Content**: Gary + text/tagline
- **Style**: Professional but friendly

### Favicon
- **Format**: ICO or PNG
- **Size**: 32x32, 16x16 pixels
- **File size**: < 10KB
- **Simple**: Recognizable at small size

---

## Brand Colors (Suggested)

### Primary Colors
```css
/* Gary Blue - Main brand color */
--gary-blue: #4A90E2;

/* Snail Shell - Accent color */
--shell-brown: #8B6F47;

/* Deal Green - Success/savings */
--deal-green: #4CAF50;

/* Alert Red - Important info */
--alert-red: #F44336;
```

### Secondary Colors
```css
/* Light Background */
--bg-light: #F5F7FA;

/* Dark Text */
--text-dark: #2C3E50;

/* Accent Yellow */
--accent-yellow: #FFC107;
```

---

## Typography

### Recommended Fonts

**Primary (Headings):**
- Poppins (Google Fonts)
- Montserrat
- Nunito

**Secondary (Body):**
- Inter
- Roboto
- Open Sans

**Code:**
- JetBrains Mono
- Fira Code
- Source Code Pro

---

## Usage Examples

### In Documentation

```markdown
![Gary](docs/images/gary-logo.png)

Gary helps you save money by finding the best deals!
```

### In Web Interface (Future)

```html
<img src="/static/images/gary-logo.png"
     alt="Gary Assistant"
     class="logo" />
```

### In API Responses (Future)

```json
{
  "assistant": {
    "name": "Gary",
    "avatar": "https://api.gary-assistant.com/images/gary-avatar.png",
    "tagline": "Finding you the best deals!"
  }
}
```

---

## File Organization

```
docs/
├── images/
│   ├── gary-logo.png          # Main logo (512x512)
│   ├── gary-logo-sm.png       # Small logo (128x128)
│   ├── gary-banner.png        # Horizontal banner
│   ├── gary-icon.png          # Square icon
│   └── gary-favicon.ico       # Favicon
└── BRANDING.md               # This file
```

---

## ASCII Art Variations

### Simple Version (Simple Banner)
```
    @@@@@@@
  @@       @@
 @  o   o   @
@      ^     @
@    \___/   @
 @          @
  @@  ___  @@
    @@   @@
   ___|___|___
  (___) (___)
```

Located at: `src/main/resources/banner-simple.txt`

To use the simple banner instead:
```yaml
# application.yml
spring:
  banner:
    location: classpath:banner-simple.txt
```

### Minimal Version
```
 🐌 GARY
 Smart Shopping
```

---

## Design Principles

### Character Traits
- **Friendly**: Approachable, not intimidating
- **Smart**: Intelligent, analytical
- **Helpful**: Service-oriented, supportive
- **Loyal**: Reliable, trustworthy (like Gary from SpongeBob)

### Visual Style
- **Clean**: Not cluttered
- **Modern**: Contemporary design
- **Professional**: Business-appropriate
- **Fun**: Playful elements without being childish

---

## Future Branding Assets

### To Create
- [ ] Official logo PNG
- [ ] Horizontal banner for README
- [ ] Favicon for web interface
- [ ] Social media preview image (1200x630)
- [ ] App icon (various sizes)
- [ ] Loading animation
- [ ] Error state mascot
- [ ] Success state mascot
- [ ] Email template header

---

## Legal Considerations

### Trademark
- Ensure your Gary design doesn't infringe on SpongeBob's Gary
- Make it distinct and original
- Consider trademark registration if going commercial

### Licensing
- Specify image license in repository
- Recommended: Creative Commons Attribution (CC BY)
- Or: All rights reserved if commercial

---

## Quick Start: Using Emojis (No Design Required)

If you want to start immediately without creating custom graphics:

### README Header
```markdown
# 🐌 Gary - Smart Shopping Assistant

🛍️ Find the best deals | 💰 Save money | 🚀 Fast & Easy
```

### Application Logging
```java
logger.info("🐌 Gary is searching for deals...");
logger.info("💰 Found 5 products!");
logger.info("✅ Comparison complete!");
```

---

## Examples from Similar Projects

### Inspiration
- **GitHub Octocat**: Simple, memorable, versatile
- **DigitalOcean Sammy**: Friendly, professional
- **Slack Bot Icons**: Clean, modern, expressive
- **Discord Wumpus**: Cute but not childish

---

## Contributing Your Design

If you create a Gary logo/image:

1. Save in `docs/images/`
2. Update this guide with the new image
3. Update README.md to include the image
4. Commit with message: "Add Gary logo/branding"

---

> "Meow!" - Gary 🐌
>
> *Translation: "Looking good! Now let's save some money!"*

---

**Note**: ASCII art is included and working! For PNG images, follow the options above to create your custom Gary design. The ASCII version looks great in the terminal and is ready to use right now!
