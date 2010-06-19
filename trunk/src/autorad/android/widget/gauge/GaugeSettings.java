package autorad.android.widget.gauge;

import org.json.JSONObject;


import android.util.Log;
import autorad.android.C;


public class GaugeSettings {

	private GaugeType gaugeType;
	private GaugeSize size;
	private int posX;
	private int posY;
	private boolean enabled;
	
	
	public GaugeSettings(GaugeType gaugeType, String settings) throws Exception {
		try {
			this.gaugeType = gaugeType;
			JSONObject obj = new JSONObject(settings);
		
			size = GaugeSize.valueOf(obj.getString("size"));
			posX = obj.getInt("posX");
			posY = obj.getInt("posY");
			enabled = obj.getBoolean("enabled");
			
			if (C.D) Log.d(C.TAG, "Gauge settings loaded ok");
			
		} catch (Exception ex) {
			Log.e(C.TAG, "JSON parsin exception " + ex.getMessage());
			throw ex;
		}
		
	}
	
	public GaugeSettings(GaugeType gaugeType) {
		this.gaugeType = gaugeType;
		this.size = gaugeType.getGaugeDetails().getDefaultSize();
		this.enabled = true;
		this.posX = 0;
		this.posY = 0;
	}
	
	public GaugeSettings(GaugeType gaugeType, GaugeSize size) {
		this.gaugeType = gaugeType;
		this.size = size;
		this.enabled = true;
		this.posX = 0;
		this.posY = 0;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosition(int X, int Y) {
		this.posX = X;
		this.posY = Y;
	}

	public GaugeSize getSize() {
		return size;
	}
	
	public boolean makeSmaller() {
		
		if (size == GaugeSize.TINY) {
			return false;
		} else {
			size = size.getSmaller();
			return true;
		}
	}
	
	public boolean makeLarger() {
		
		if (gaugeType.getGaugeDetails().getDefaultSize() == size) { // cant' go larger than default
			return false;
		} else {
			size = size.getLarger();
			return true;
		}
	}
	
	
	public GaugeType getGaugeType() {
		return gaugeType;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void enable() {
		enabled = true;
	}
	
	public void disable() {
		enabled = false;
	}
	
	public String getSettingsAsJSONString() {
		return "{\"size\": \"" + size.name() + "\", \"enabled\": " + enabled + ", \"posX\": " + posX + ", \"posY\": " + posY + "}";
	}
	

	
	
}
