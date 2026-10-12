//! # salim-plot: Beautiful, modern plotting with one-line DataFrame syntax.

#[derive(Clone, Copy, Debug, PartialEq)]
pub enum Theme {
    DarkObsidian,
    SolarizedLight,
    Minimalist,
    ColorblindSafe,
}

#[derive(Clone, Debug)]
pub enum ChartKind {
    Line,
    Scatter,
    Bar,
    Histogram,
    BoxPlot,
    Heatmap,
    Pie,
    Area,
}

#[derive(Clone, Debug)]
pub struct Plot {
    pub title: String,
    pub x_label: String,
    pub y_label: String,
    pub theme: Theme,
    pub kind: ChartKind,
}

impl Default for Plot {
    fn default() -> Self {
        Self {
            title: "Salim Plot".into(),
            x_label: "X".into(),
            y_label: "Y".into(),
            theme: Theme::DarkObsidian,
            kind: ChartKind::Scatter,
        }
    }
}

impl Plot {
    pub fn new(kind: ChartKind) -> Self {
        Self {
            kind,
            ..Default::default()
        }
    }

    pub fn title(mut self, title: &str) -> Self {
        self.title = title.to_string();
        self
    }

    pub fn theme(mut self, theme: Theme) -> Self {
        self.theme = theme;
        self
    }

    pub fn render_svg(&self) -> String {
        format!("<svg viewBox=\"0 0 600 400\"><text x=\"20\" y=\"30\">{}</text></svg>", self.title)
    }
}
