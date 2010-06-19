package autorad.android.sensor;


public interface SensorDataSource {

	public void start();
	
	public void stop();
	
	public void registerSensorDataListener(SensorDataListener listener);
	
	public void unregisterSensorDataListener(SensorDataListener listener);
	
}
