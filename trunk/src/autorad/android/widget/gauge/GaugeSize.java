package autorad.android.widget.gauge;

public enum GaugeSize {
	TINY { public GaugeSize getLarger() {return TINY1; } public GaugeSize getSmaller() {return TINY; } },
	VERY_SMALL { public GaugeSize getLarger() {return VERY_SMALL1; } public GaugeSize getSmaller() {return TINY1; } },
	SMALL { public GaugeSize getLarger() {return SMALL1; } public GaugeSize getSmaller() {return VERY_SMALL1; } },
	MEDIUM { public GaugeSize getLarger() {return MEDIUM1; } public GaugeSize getSmaller() {return SMALL1; } },
	LARGE { public GaugeSize getLarger() {return LARGE1; } public GaugeSize getSmaller() {return MEDIUM1; } },
	DEFAULT { public GaugeSize getLarger() {return DEFAULT; } public GaugeSize getSmaller() {return DEFAULT; } },
	TINY1 { public GaugeSize getLarger() {return VERY_SMALL; } public GaugeSize getSmaller() {return TINY; } },
	VERY_SMALL1 { public GaugeSize getLarger() {return SMALL; } public GaugeSize getSmaller() {return VERY_SMALL; } },
	SMALL1 { public GaugeSize getLarger() {return MEDIUM; } public GaugeSize getSmaller() {return SMALL; } },
	MEDIUM1 { public GaugeSize getLarger() {return LARGE; } public GaugeSize getSmaller() {return MEDIUM; } },
	LARGE1 { public GaugeSize getLarger() {return LARGE2; } public GaugeSize getSmaller() {return LARGE; } },
	LARGE2 { public GaugeSize getLarger() {return VERYLARGE; } public GaugeSize getSmaller() {return LARGE1; } },
	VERYLARGE { public GaugeSize getLarger() {return VERYLARGE1; } public GaugeSize getSmaller() {return LARGE2; } },
	VERYLARGE1 { public GaugeSize getLarger() {return VERYLARGE1; } public GaugeSize getSmaller() {return VERYLARGE; } }
	
	;
	
	public abstract GaugeSize getLarger();
	public abstract GaugeSize getSmaller();
}
