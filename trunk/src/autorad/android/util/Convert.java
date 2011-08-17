package autorad.android.util;

import autorad.android.sensor.DataType;

public final class Convert {

	public final static int MPH = 0;
	public final static int KPH = 1;
	public final static int KNOT = 2;
	
	public static float mpsToUnit(float speed_mps, int unitType) {
		switch (unitType) {
		case KPH:
			return mps_to_kmph(speed_mps);
		case KNOT:
			return mps_to_knot(speed_mps);
		default:
			return mps_to_mph(speed_mps);
		}
	}
	
	public static String getLabel(int unitType) {
		switch (unitType) {
		case KPH:
			return DataType.SPEED_KPH.getUnits();
		case KNOT:
			return DataType.SPEED_KNOTS.getUnits();
		case MPH:
			return DataType.SPEED_MPH.getUnits();
		}
		return "";
	}
	

	
	/**
	 * Metres per second to miles per hour
	 * @param speed m/s
	 * @return
	 */
	public static float mps_to_mph(float speed_mps) {
        return speed_mps * 2.23693629f;
    }

	/**
	 * Metres per second to Kilometres per hour
	 * @param speed m/s
	 * @return
	 */
    public static float mps_to_kmph(float speed_mps) {
        return speed_mps * 3.6f;
    }

    /**
	 * Metres per second to Kilometres per hour
	 * @param speed m/s
	 * @return
	 */
    public static float mps_to_knot(float speed_mps) {
        return speed_mps * 1.94384449f;
    }
    
	/**
	 * Metres to feet
	 * @param metres
	 * @return
	 */
    public static float m_to_ft(float m) {
        return m * 3.2808399f;
    }
}
