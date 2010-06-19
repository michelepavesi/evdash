package autorad.android.util;

public final class Convert {

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
	 * Metres to feet
	 * @param metres
	 * @return
	 */
    public static float m_to_ft(float m) {
        return m * 3.2808399f;
    }
}
