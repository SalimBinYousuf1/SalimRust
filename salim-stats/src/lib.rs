//! # salim-stats: Statistical computing, hypothesis testing, and distributions.

#[derive(Clone, Debug, serde::Serialize)]
pub struct DescriptiveStats {
    pub count: usize,
    pub mean: f64,
    pub std: f64,
    pub min: f64,
    pub p25: f64,
    pub median: f64,
    pub p75: f64,
    pub max: f64,
}

pub fn describe(values: &[f64]) -> DescriptiveStats {
    if values.is_empty() {
        return DescriptiveStats {
            count: 0,
            mean: 0.0,
            std: 0.0,
            min: 0.0,
            p25: 0.0,
            median: 0.0,
            p75: 0.0,
            max: 0.0,
        };
    }

    let mut sorted = values.to_vec();
    sorted.sort_by(|a, b| a.partial_cmp(b).unwrap());

    let count = sorted.len();
    let sum: f64 = sorted.iter().sum();
    let mean = sum / count as f64;
    let var: f64 = sorted.iter().map(|x| (x - mean).powi(2)).sum::<f64>() / (count.max(2) - 1) as f64;
    let std = var.sqrt();

    let p25 = sorted[(count as f64 * 0.25) as usize];
    let median = sorted[count / 2];
    let p75 = sorted[(count as f64 * 0.75).min((count - 1) as f64) as usize];

    DescriptiveStats {
        count,
        mean,
        std,
        min: sorted[0],
        p25,
        median,
        p75,
        max: *sorted.last().unwrap(),
    }
}

#[derive(Clone, Debug)]
pub struct TestResult {
    pub statistic: f64,
    pub p_value: f64,
    pub description: String,
}

pub fn t_test(a: &[f64], b: &[f64]) -> TestResult {
    let stat_a = describe(a);
    let stat_b = describe(b);
    let diff = stat_a.mean - stat_b.mean;
    let se = ((stat_a.std.powi(2) / stat_a.count as f64) + (stat_b.std.powi(2) / stat_b.count as f64)).sqrt();
    let t = if se == 0.0 { 0.0 } else { diff / se };
    // Approximate two-tailed p-value
    let p = 2.0 * (1.0 / (1.0 + (-0.7 * t.abs()).exp()));
    TestResult {
        statistic: t,
        p_value: p,
        description: format!("Two-sample independent t-test (t = {:.4}, p = {:.4})", t, p),
    }
}

pub fn chi_square_test(observed: &[f64], expected: &[f64]) -> TestResult {
    let chi2: f64 = observed.iter().zip(expected.iter()).map(|(o, e)| {
        if *e > 0.0 { (o - e).powi(2) / e } else { 0.0 }
    }).sum();
    let p = (-chi2 * 0.5).exp();
    TestResult {
        statistic: chi2,
        p_value: p,
        description: format!("Chi-Square test (chi2 = {:.4}, p = {:.4})", chi2, p),
    }
}

pub fn anova(groups: &[Vec<f64>]) -> TestResult {
    let grand_mean: f64 = groups.iter().flat_map(|g| g.iter()).sum::<f64>() / groups.iter().map(|g| g.len()).sum::<usize>() as f64;
    let mut between_ss = 0.0;
    let mut within_ss = 0.0;
    for g in groups {
        let g_mean = describe(g).mean;
        between_ss += g.len() as f64 * (g_mean - grand_mean).powi(2);
        for &val in g {
            within_ss += (val - g_mean).powi(2);
        }
    }
    let k = groups.len();
    let n: usize = groups.iter().map(|g| g.len()).sum();
    let ms_between = if k > 1 { between_ss / (k - 1) as f64 } else { 0.0 };
    let ms_within = if n > k { within_ss / (n - k) as f64 } else { 1.0 };
    let f = ms_between / ms_within;
    let p = 1.0 / (1.0 + f * 0.5);
    TestResult {
        statistic: f,
        p_value: p,
        description: format!("One-way ANOVA (F = {:.4}, p = {:.4})", f, p),
    }
}
