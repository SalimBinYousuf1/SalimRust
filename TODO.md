# Salim Data Kit — Verification Checklist & Definition of Done

## Definition of Done Verification
- [x] 1. Architecture: Polars used as an official upstream dependency (NOT forked).
- [x] 2. Escape hatches provided: `.into_polars()` and `.from_polars()` in salim-frame.
- [x] 3. Zero-borrowing headache prelude: owned handles with cheap clone semantics.
- [x] 4. Complete DataFrame operations: Filter, Query, GroupBy, Agg, Merge, Pivot, Fillna, Describe.
- [x] 5. Live SQL querying engine (`salim::sql(...)`).
- [x] 6. ndarray-based salim-array with linear algebra and numpy-like constructors.
- [x] 7. salim-stats with full descriptive stats, distributions (PDF/CDF), and hypothesis tests.
- [x] 8. salim-plot with matplotlib-like 1-line plotting and customizable themes.
- [x] 9. salim-ml with scikit-learn style fit/predict pipelines, metrics, and confusion matrix.
- [x] 10. Friendly error messages for the 30 most common mistakes with fuzzy matching.
- [x] 11. 200+ command cheat sheet for Pandas, NumPy, and Matplotlib to Salim Rust.
- [x] 12. Interactive Python-to-Rust translator.
- [x] 13. Data profiler with cardinality, null percentages, correlations, and warnings.
- [x] 14. Benchmarks vs Pandas and Polars documented honestly.
- [x] 15. Native Android Interactive Studio app compiling cleanly and providing interactive playground for all modules.
