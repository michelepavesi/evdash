package autorad.android.sensor;

public interface SensorDataListener {

	public DataType[] getDataTypes();
	
	public void onData(DataType type, float... data);
	
}
