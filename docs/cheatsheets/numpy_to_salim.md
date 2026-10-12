# NumPy to Salim Cheat Sheet (Common Operations)

| Operation | NumPy (Python) | Salim (Rust) | Notes |
|---|---|---|---|
| Import | `import numpy as np` | `use salim::prelude::*;` | Single prelude import |
| Zeros | `np.zeros(10)` | `salim::zeros(10)` | Contiguous memory allocation |
| Ones | `np.ones(10)` | `salim::ones(10)` | Vector of 1.0 floats |
| Linspace | `np.linspace(0, 10, 100)` | `salim::linspace(0.0, 10.0, 100)` | Uniformly spaced interval |
| Arange | `np.arange(0, 10, 0.5)` | `salim::arange(0.0, 10.0, 0.5)` | Stepped floating range |
| Identity Matrix | `np.eye(4)` | `salim::eye(4)` | Diagonal 1.0 identity |
| Random Normal | `np.random.normal(0, 1, 100)` | `salim::random_normal(0.0, 1.0, 100)` | Gaussian random sampler |
| Random Uniform | `np.random.uniform(0, 1, 100)` | `salim::random_uniform(0.0, 1.0, 100)` | Uniform distribution [0, 1) |
| Dot Product | `np.dot(a, b)` | `a.dot(&b)` | SIMD vectorized dot product |
| Matrix Multiply | `A @ B` or `np.matmul(A, B)` | `A.matmul(&B)` | BLAS accelerated GEMM |
| Transpose | `A.T` | `A.t()` | Zero-copy transposed view |
| Determinant | `np.linalg.det(A)` | `A.det()` | LU decomposition determinant |
| Matrix Inverse | `np.linalg.inv(A)` | `A.inv()?` | Invertible square matrix solver |
| Eigenvalues | `np.linalg.eig(A)` | `A.eig()?` | Eigen decomposition |
| SVD | `np.linalg.svd(A)` | `A.svd()?` | Singular value decomposition |
| Norm | `np.linalg.norm(v)` | `v.norm()` | L2 Euclidean or Frobenius norm |
| Sum | `arr.sum()` | `arr.sum()` | Parallel multi-threaded reduction |
| Mean / Std | `arr.mean()`, `arr.std()` | `arr.mean()`, `arr.std()` | Numerical stability with Welford |
| Min / Max | `arr.min()`, `arr.max()` | `arr.min()`, `arr.max()` | Extreme value finder |
| Argmax | `arr.argmax()` | `arr.argmax()` | Index of maximum element |
| Element-wise Math | `np.exp(arr)`, `np.log(arr)` | `arr.mapv(f64::exp)` | Vectorized element map |
