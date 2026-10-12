# ADR 0001: Polars as Engine, Not a Fork

## Context
Python data scientists are frustrated by Pandas performance bottlenecks (single-threaded CPU execution, massive memory overhead 5-10x file size, lack of query optimization). Polars is the fastest open-source columnar engine written in Rust.

## Decision
1. Salim depends directly on the official `polars` crate from crates.io.
2. We strictly forbid forking, vendoring, or renaming Polars code. This ensures zero drift from upstream performance updates and bug fixes.
3. Salim wraps Polars types into an ultra-ergonomic, Pythonic API (no lifetime gymnastics for newcomers).
4. Every DataFrame and Series offers explicit escape hatches: `.into_polars()` and `.from_polars()` so power users never hit a ceiling.

## Consequences
- Upgrades to newer Polars versions remain clean and isolated.
- Users achieve 10-100x performance gains over Pandas with zero binary size penalty compared to pure Rust.
