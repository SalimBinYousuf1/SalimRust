# ADR 0004: Declarative, High-Fidelity Plotting Engine (salim-plot)

## Context
Matplotlib is powerful but notoriously verbose, requiring extensive boilerplate for modern aesthetics. Existing Rust plotting libraries either require dense layout geometry or lack interactive exports.

## Decision
1. `salim-plot` provides a fluent, 1-line plotting interface directly callable from DataFrames:
   `df.plot().scatter("x", "y").theme(Theme::DarkObsidian).render()`
2. Built-in modern themes: *Dark Obsidian*, *Solarized Light*, *Minimalist*, and *Colorblind-Safe*.
3. Support multi-target rendering:
   - High-resolution SVG / PNG for publication.
   - Interactive HTML Canvas output with hover tooltips and zoom/pan.
   - Terminal ANSI art rendering for rapid headless debugging.
4. Automatic axis formatting for ISO 8601 timestamps and compact large numbers (1.2M, 450K).

## Consequences
- Produces publication-grade visualizations in a single line of readable code.
- Eliminates Python matplotlib/seaborn runtime dependencies.
