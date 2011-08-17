package autorad.android.widget.gauge;

import autorad.android.sensor.DataType;

public class GaugeDetails {

	private DataType[] dataTypes;
	private GaugeStyle style;
	private String units;
	private int startAngle;
	private int endAngle;
	private int restAngle;
	
	private int minDataValue;
	private int maxDataValue;
		
	private boolean rotateClockwise;
	
	private GaugeSize defaultSize;
	private GaugeSize maxSize;
	
	public GaugeDetails(GaugeStyle style, String units, int startAngle, int endAngle,
			int restAngle, GaugeSize defaultSize, GaugeSize maxSize, int minDataValue, 
			int maxDataValue, boolean rotateClockwise, DataType... dataTypes) {
		this.style = style;
		this.units = units;
		this.startAngle = startAngle;
		this.endAngle = endAngle;
		this.restAngle = restAngle;
		this.minDataValue = minDataValue;
		this.maxDataValue = maxDataValue;
		this.rotateClockwise = rotateClockwise;
		this.defaultSize = defaultSize;
		this.maxSize = maxSize;
		this.dataTypes = dataTypes;
	}
	
	public GaugeDetails(GaugeStyle style,GaugeSize defaultSize, DataType... dataTypes) {
		this.style = style;
		this.defaultSize = defaultSize;
		this.dataTypes = dataTypes;
	}
	
	public GaugeStyle getGaugeStyle() {
		return style;
	}
	
	public DataType[] getDataTypes() {
		return dataTypes;
	}
	public String getUnits() {
		return units;
	}
	public int getStartAngle() {
		return startAngle;
	}
	public int getEndAngle() {
		return endAngle;
	}
	public int getRestAngle() {
		return restAngle;
	}
	public int getMinDataValue() {
		return minDataValue;
	}
	public int getMaxDataValue() {
		return maxDataValue;
	}
	public GaugeSize getDefaultSize() {
		return defaultSize;
	}
	public GaugeSize getMaxSize() {
		return maxSize;
	}
	public boolean getRotateClockwise() {
		return rotateClockwise;
	}
	
}
