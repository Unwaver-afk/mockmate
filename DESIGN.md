# Design System Strategy: The Empathetic Authority

## 1. Overview & Creative North Star
This design system is built upon the North Star of **"The Digital Mentor."** We are moving away from the cold, analytical "dashboard" aesthetic common in AI tools. Instead, we are creating an environment that feels like a high-end, private coaching studio—authoritative yet deeply supportive.

To achieve this, we reject the "generic SaaS" look. We break the grid through **intentional asymmetry**: large-scale editorial typography paired with generous, "unproductive" whitespace that allows the user to breathe. We utilize overlapping layers and shifting tonal surfaces to create a sense of physical space, moving the interface from a flat screen to a tactile, layered experience.

---

## 2. Color & Surface Philosophy
The palette focuses on "Trustworthy Blues" and "Success Greens," but interpreted through a high-end lens using Material Design-inspired tokens.

### The "No-Line" Rule
**Designers are prohibited from using 1px solid borders for sectioning.**
Structural boundaries must be defined solely through background color shifts. For example, a `surface-container-low` side panel sitting against a `surface` main content area provides enough distinction without the "clutter" of a line.

### Surface Hierarchy & Nesting
Treat the UI as physical layers of frosted glass and fine paper.
- **Background (`#f7f9fb`):** The base canvas.
- **Surface-Container-Low (`#f2f4f6`):** For secondary utility zones.
- **Surface-Container-Lowest (`#ffffff`):** Reserved for primary interactive cards to create a "lifted" focal point.
- **The Glass & Gradient Rule:** For floating AI coaching bubbles or "Pro" features, use `surface_bright` with a 60% opacity and a `24px` backdrop-blur.

### Signature Textures
Main CTAs (Primary) should not be flat. Apply a subtle linear gradient from `primary` (`#004497`) to `primary_container` (`#005bc5`) at a 135-degree angle. This adds "soul" and a slight metallic sheen that feels premium and intentional.

---

## 3. Typography: Editorial Precision
We utilize a dual-typeface system to balance authority with accessibility.

*   **Display & Headlines (Manrope):** Use Manrope for all headers. Its geometric yet warm curves suggest modern professionalism.
    *   *Display-LG (3.5rem):* Use for massive, encouraging score reveals.
    *   *Headline-MD (1.75rem):* Use for section headers.
*   **Body & Labels (Inter):** Use Inter for all functional text. Its high x-height ensures readability during stressful mock interviews.
    *   *Body-LG (1rem):* Standard for conversational chat bubbles.
    *   *Label-MD (0.75rem):* All-caps with +0.05em letter spacing for metadata and small tags.

---

## 4. Elevation & Depth
Hierarchy is achieved through **Tonal Layering** rather than structural scaffolding.

*   **The Layering Principle:** To highlight a "Mock Interview" card, do not add a border. Place a `surface-container-lowest` card on top of a `surface-container-low` background. The subtle shift in hex code creates a sophisticated, natural lift.
*   **Ambient Shadows:** If an element must float (like a chat input bar), use an extra-diffused shadow: `box-shadow: 0 12px 32px -4px rgba(25, 28, 30, 0.06);`. The shadow color is a low-opacity version of `on-surface`, never pure black.
*   **The "Ghost Border" Fallback:** If a border is required for accessibility, use `outline_variant` at **15% opacity**. It should be felt, not seen.

---

## 5. Components & Signature Patterns

### Chat Interface (The Conversational Core)
*   **User Bubbles:** `primary_container` with `on_primary_container` text. Roundedness: `xl` (1.5rem), but the bottom-right corner should be `sm` (0.25rem) to indicate origin.
*   **AI Bubbles:** `surface_container_highest` with a subtle glassmorphism blur.
*   **No Dividers:** Chat history is separated by vertical spacing (`24px`), never horizontal lines.

### Score Visualizations & Progress
*   **The "Success Glow":** When a user achieves a high score, use `tertiary_fixed` (`#6ffbbe`) as a soft outer glow (bloom effect) around the score circular progress bar.
*   **Progress Trackers:** Use `secondary_container` for the track and `primary` for the fill. The bar should have a height of `8px` and `full` rounding.

### Buttons & Inputs
*   **Primary Button:** Gradient-filled (`primary` to `primary_container`), `xl` roundedness, and `title-sm` typography.
*   **Input Fields:** Use `surface_container_low`. On focus, transition the background to `surface_container_lowest` and add a 2px `surface_tint` "Ghost Border" at 20% opacity.
*   **Cards:** Forbid divider lines. Use `surface_container_highest` headers against `surface_container_lowest` bodies to separate "Job Description" from "Interview Feedback."

---

## 6. Do’s and Don’ts

### Do
*   **Do** use asymmetrical layouts. Place a large `display-sm` headline on the left with a 60% width, leaving the right side for a floating AI tip.
*   **Do** use "Success Green" (`tertiary`) sparingly. It should feel like a reward, not a default decoration.
*   **Do** prioritize "Breathing Room." If a screen feels crowded, increase the padding from `1rem` to `2rem` before shrinking text.

### Don’t
*   **Don’t** use pure black (`#000000`). Use `on_surface` (`#191c1e`) for all high-contrast text.
*   **Don’t** use 1px dividers. If you feel the need for a line, try using a `4px` gap of a different surface color instead.
*   **Don’t** use "Default" shadows. If the shadow looks like a "Drop Shadow," it is too heavy. It should look like "Ambient Light."