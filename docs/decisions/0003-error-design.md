# ADR 0003: Human-First Error Diagnostics and Python Habit Interception

## Context
One of the largest hurdles for Python developers migrating to Rust is dealing with cryptic borrow checker errors or rigid compile-time types. A core thesis of Salim is "Python's ease. Rust's speed."

## Decision
1. Implement a specialized `salim-errors` crate that intercepts runtime and validation failures with actionable, plain-English suggestions.
2. Incorporate string similarity (Levenshtein / Jaro-Winkler via `strsim`) for fuzzy column name matching: e.g., typing `df.col("revnue")` will suggest `"Did you mean 'revenue'?"` along with the full list of available columns.
3. Detect common Python habits and provide friendly pedagogical suggestions:
   - Attempting in-place mutation (`inplace=True` pattern) is explained with Rust's immutable handle model.
   - Negative index access is guided toward `.tail()` or length-based indexing.
   - Type mismatches between columns and literals provide exact cast recommendations (`.cast(DataType::Float64)`).

## Consequences
- Radically reduces the learning curve for incoming data scientists.
- Error messages empower users rather than frustrating them.
