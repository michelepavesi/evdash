package autorad.android.sensor;

import autorad.android.R;

public enum DataType {
	VOLTAGE(R.string.LABEL_VOLT,"V"),
    CURRENT(R.string.LABEL_CURRENT,"amps"),
    TEMPERATURE_CELCIUS(R.string.LABEL_TEMP, "°C"),
    RPM(R.string.LABEL_RPM, "rpm"),
    SPEED_KPH(R.string.LABEL_SPEED, "km/h"),
    DISTANCE_METRES(R.string.LABEL_DISTANCE, "m"),
    BEARING(R.string.LABEL_BEARING, "%.2f°"),
    AZIMUTH(R.string.LABEL_AZIMUTH, "%.2f°"),
    LOCATION(R.string.LABEL_LOCATION, "%s°%s'%s\"%s"),
    ALTITUDE(R.string.LABEL_ALTITUDE, "m"),
    SATELLITE_COUNT(R.string.LABEL_SATELLITES, null),
    SATELLITE_ACCURACY(R.string.LABEL_ACCURACY, "m"),
    LATERALG(R.string.LABEL_LATG, "m/s²"),
    ACCELERATIONG(R.string.LABEL_ACCG, "m/s²"),
    SPEED_MPH(R.string.LABEL_SPEED, "mph"),
	SPEED_MS(R.string.LABEL_SPEED, "m/s"),
	SPEED_KNOTS(R.string.LABEL_SPEED, "kts");
	
	int label;
	String unit;
	
	private DataType(int label, String unit) {
		this.label = label;
		this.unit = unit;
	}
	
	public int getLabelId() {
		return label;
	}
	
	public String getUnits() {
		return unit;
	}
    
}
