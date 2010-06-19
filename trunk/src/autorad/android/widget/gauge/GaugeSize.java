package autorad.android.widget.gauge;

public enum GaugeSize {
	TINY { public GaugeSize getLarger() {return VERY_SMALL; } public GaugeSize getSmaller() {return TINY; } },
	VERY_SMALL { public GaugeSize getLarger() {return SMALL; } public GaugeSize getSmaller() {return TINY; } },
	SMALL { public GaugeSize getLarger() {return MEDIUM; } public GaugeSize getSmaller() {return VERY_SMALL; } },
	MEDIUM { public GaugeSize getLarger() {return LARGE; } public GaugeSize getSmaller() {return SMALL; } },
	LARGE { public GaugeSize getLarger() {return LARGE; } public GaugeSize getSmaller() {return MEDIUM; } },
	DEFAULT { public GaugeSize getLarger() {return DEFAULT; } public GaugeSize getSmaller() {return DEFAULT; } };
	
	public abstract GaugeSize getLarger();
	public abstract GaugeSize getSmaller();
}
