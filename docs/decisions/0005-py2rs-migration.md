# ADR 0005: Bidirectional Migration and Interoperability (salim-py2rs)

## Context
Organizations cannot rewrite millions of lines of Python data pipelines overnight. To ensure frictionless adoption, Salim must provide gradual migration paths.

## Decision
1. **salim-py2rs CLI Translator:**
   - A command-line script converter (`salim translate script.py`) that parses AST calls to Pandas, NumPy, and Matplotlib and produces idiomatic Salim Rust code.
   - Accurately flags non-translatable dynamic constructs and suggests equivalent Rust patterns.
2. **200+ Operation Cheat Sheets:**
   - Complete side-by-side references for Pandas, NumPy, and Matplotlib.
3. **Python C-Extension (`salim-py` via PyO3):**
   - Allows Python code to call Salim's multi-threaded Rust routines with zero-copy Arrow memory exchange.
   - Teams can accelerate slow bottleneck functions in Rust while keeping existing orchestration.

## Consequences
- Lowers organizational switching risk to near zero.
- Enables piecemeal, incremental migration from legacy Python scripts.
