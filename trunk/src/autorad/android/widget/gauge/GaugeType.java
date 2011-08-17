package autorad.android.widget.gauge;

import autorad.android.sensor.DataType;

public enum GaugeType {

	BATTERY_CURRENT_HIGH(new GaugeDetails(
										 GaugeStyle.DIAL,
										 "amps", 		    // data description
										 13, 				// start angle
										 312, 				// end angle
										 13, 				// rest angle
										 GaugeSize.LARGE, 	// defaut gauge size
										 GaugeSize.VERYLARGE1, 	// max gauge size
										 0, 				// min data range
										 1800, 				// max data range
										 true,				// clockwise rotation
										 DataType.CURRENT  // Data type									 
										 )),
	
	BATTERY_CURRENT_LOW(new GaugeDetails(
										 GaugeStyle.DIAL,
										 "amps", 			// data description
										 40, 				// start angle
										 312, 				// end angle
										 40, 				// rest angle
										 GaugeSize.MEDIUM,  // defaut gauge size
										 GaugeSize.LARGE, 	// max gauge size
										 0, 				// min data range
										 180, 				// max data range
										 true,				// clockwise rotation
										 DataType.CURRENT 	// Data type									 
										 )),			
	
	RPM(new GaugeDetails(			  
										 GaugeStyle.DIAL,
										 "rpm", 			// data description
										 38, 				// start angle
										 312, 				// end angle
										 38, 				// rest angle
										 GaugeSize.LARGE, 	// defaut gauge size
										 GaugeSize.VERYLARGE1, 	// max gauge size
										 0, 				// min data range
										 8000, 				// max data range
										 true,			// clockwise rotation
										 DataType.RPM 		// Data type
										 )),
	
	CONTROLLER_TEMPERATURE(new GaugeDetails(
										 GaugeStyle.DIAL,
										 "°C", 				// data description
										 15, 				// start angle
										 145, 				// end angle
										 90, 				// rest angle
										 GaugeSize.SMALL, 	// defaut gauge size
										 GaugeSize.LARGE, 	// max gauge size
										 0, 				// min data range
										 100, 				// max data range
										 true,			// clockwise rotation
										 DataType.TEMPERATURE_CELCIUS // Data type
										 )),
	
	PACK_VOLTAGE(new GaugeDetails( 
										 GaugeStyle.DIAL,
										 "V", 				// data description
										 350, 				// start angle
										 200, 				// end angle
										 270, 				// rest angle
										 GaugeSize.SMALL, 	// defaut gauge size
										 GaugeSize.LARGE, 	// max gauge size
										 120, 				// min data range
										 180, 				// max data range
										 false,			// anti-clockwise rotation
										 DataType.VOLTAGE	// Data type
										 )),
	
	SPEED_KPH(new GaugeDetails(  
										 GaugeStyle.DIAL,
										 "km/h", 			// data description
										 41, 				// start angle
										 312, 				// end angle
										 41, 				// rest angle
										 GaugeSize.LARGE, 	// defaut gauge size
										 GaugeSize.VERYLARGE1, 	// max gauge size
										 0, 				// min data range
										 180, 				// max data range
										 true,			// clockwise rotation
										 DataType.SPEED_KPH // Data type
										 )),
	SPEED_MPH(new GaugeDetails(  
										 GaugeStyle.DIAL,
										 "mph", 			// data description
										 41, 				// start angle
										 312, 				// end angle
										 41, 				// rest angle
										 GaugeSize.LARGE, 	// defaut gauge size
										 GaugeSize.VERYLARGE1, 	// max gauge size
										 0, 				// min data range
										 180, 				// max data range
										 true,			// clockwise rotation
										 DataType.SPEED_MPH // Data type
										 )),
	SPEED_OLDSKOOL_MPH(new GaugeDetails(  
										 GaugeStyle.DIAL,
										 "mph", 			// data description
										 44, 				// start angle
										 304, 				// end angle
										 44, 				// rest angle
										 GaugeSize.LARGE, 	// defaut gauge size
										 GaugeSize.VERYLARGE1, 	// max gauge size
										 0, 				// min data range
										 130, 				// max data range
										 true,			// clockwise rotation
										 DataType.SPEED_MPH // Data type
										 )),

										 
	GPS_INFO(new GaugeDetails(  
										 GaugeStyle.TEXT,
										 GaugeSize.MEDIUM, 	// defaut gauge size
										 
										 DataType.BEARING, DataType.ALTITUDE, DataType.LOCATION, 
										 DataType.SATELLITE_COUNT, DataType.SATELLITE_ACCURACY
										  // Data type
										 )),
	LATERAL_G(new GaugeDetails(  
										 GaugeStyle.DIAL,
										 "m/s²", 			// data description
										 125, 				// start angle
										 235, 				// end angle
										 180, 				// rest angle
										 GaugeSize.MEDIUM, 	// defaut gauge size
										 GaugeSize.LARGE, 	// max gauge size
										 -15, 				// min data range
										 15, 				// max data range
										 true,			// clockwise rotation
										 DataType.LATERALG // Data type
										 )),
	ACCELERATION_G(new GaugeDetails(  
										 GaugeStyle.DIAL,
										 "m/s²", 			// data description
										 35, 				// start angle
										 145, 				// end angle
										 90, 				// rest angle
										 GaugeSize.MEDIUM, 	// defaut gauge size
										 GaugeSize.LARGE, 	// max gauge size
										 -15, 				// min data range
										 15, 				// max data range
										 true,			// clockwise rotation
										 DataType.ACCELERATIONG // Data type
										 ));
	
	
	private GaugeDetails gaugeDetails;
	
	private GaugeType(GaugeDetails gaugeDetails) {
		this.gaugeDetails = gaugeDetails;
	}
	
	public GaugeDetails getGaugeDetails() {
		return gaugeDetails;
	}
	
	
	
}
