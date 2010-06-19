package autorad.android.sensor;

public enum DataType {
	VOLTAGE("Voltage","V"),
    CURRENT("Current","amps"),
    TEMPERATURE_CELCIUS("Temperature", "°C"),
    RPM("RPM", "rpm"),
    SPEED_KPH("Speed", "km/h"),
    DISTANCE_METRES("Distance", "m"),
    BEARING("Bearing", "%.2f°"),
    AZIMUTH("Azimuth", "%.2f°"),
    LOCATION("Location", "%s°%s'%s\"%s"),
    ALTITUDE("Altitude", "m"),
    SATELLITE_COUNT("Satellites", null),
    SATELLITE_ACCURACY("Accuracy", "m"),
    LATERALG("Lateral G-force", "m/s²"),
    ACCELERATIONG("Acceleration G-force", "m/s²"),
    SPEED_MPH("Speed", "mph");
	
	String label;
	String unit;
	
	private DataType(String label, String unit) {
		this.label = label;
		this.unit = unit;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getUnits() {
		return unit;
	}
    
}
