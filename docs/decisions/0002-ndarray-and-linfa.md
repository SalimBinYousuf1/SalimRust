# ADR 0002: Numerical Computing via ndarray and Machine Learning via Linfa

## Context
Python data science heavily leverages NumPy for N-dimensional arrays and Scikit-Learn for classical machine learning (regressions, trees, clustering). Salim requires an equally powerful, zero-cost abstraction stack in Rust.

## Decision
1. **salim-array** wraps the industry-standard `ndarray` crate.
   - Provides numpy-equivalent constructors: `zeros`, `ones`, `arange`, `linspace`, `eye`, `random`.
   - Vectorized broadcasting, slicing, and linear algebra routines (LU, SVD, QR, Cholesky, Eigenvalues).
   - Seamless zero-copy conversion between `ndarray::ArrayView`, `salim_frame::Series`, and Arrow chunks.
2. **salim-ml** builds upon `linfa`, Rust's premier Scikit-learn equivalent.
   - Implements a uniform `fit`, `predict`, `score` API.
   - Enforces deterministic random seeds for reproducible science.
   - Outputs rich evaluation structs (Confusion Matrix, Precision/Recall, ROC-AUC, R-squared).

## Consequences
- Eliminates Python's Global Interpreter Lock (GIL) during array math.
- Enables Rayon parallelism by default on multi-core architectures.
