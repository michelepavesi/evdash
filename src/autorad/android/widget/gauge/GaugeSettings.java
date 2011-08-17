package autorad.android.widget.gauge;

import java.util.UUID;

import org.json.JSONObject;


import android.util.Log;
import autorad.android.C;


public class GaugeSettings {

	private String id;
	private GaugeType gaugeType;
	private GaugeSize size;
	private int posX;
	private int posY;
	private boolean enabled;
	private int viewIdx;
	
	public GaugeSettings(String id, String settings) throws Exception {
		try {
			this.id = id;
			JSONObject obj = new JSONObject(settings);
		
			if (id.length() < 30) {
				gaugeType = GaugeType.valueOf(id);
			} else {
				if (obj.has("gaugeType")) {
					gaugeType = GaugeType.valueOf(obj.getString("gaugeType"));
				} else {
					throw new Exception("Invalid Json - gaugegType not found");
				}
			}

			size = GaugeSize.valueOf(obj.getString("size"));
			posX = obj.getInt("posX");
			posY = obj.getInt("posY");
			enabled = obj.getBoolean("enabled");
			
			if (obj.has("viewIdx")) {
				viewIdx = obj.getInt("viewIdx");
			} else {
				viewIdx = 1;
			}
			
			if (C.D) Log.d(C.TAG, "Gauge settings loaded ok");
			
		} catch (Exception ex) {
			Log.e(C.TAG, "JSON parsin exception " + ex.getMessage());
			throw ex;
		}
		
	}
	
	public GaugeSettings(GaugeType gaugeType, int viewIdx) {
		this.id = UUID.randomUUID().toString();
		this.gaugeType = gaugeType;
		this.size = gaugeType.getGaugeDetails().getDefaultSize();
		this.enabled = true;
		this.posX = 0;
		this.posY = 0;
		this.viewIdx = viewIdx;
	}
	/*
	public GaugeSettings(GaugeType gaugeType, GaugeSize size) {
		this.id = UUID.randomUUID().toString();
		this.gaugeType = gaugeType;
		this.size = size;
		this.enabled = true;
		this.posX = 0;
		this.posY = 0;
	}
	 */
	public String getId() {
		return id;
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
	
	public void setSize(GaugeSize size) {
		this.size = size;
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
		
		if (gaugeType.getGaugeDetails().getMaxSize() == size) { // cant' go larger than default
			return false;
		} else {
			size = size.getLarger();
			return true;
		}
	}
	
	
	public GaugeType getGaugeType() {
		return gaugeType;
	}
	
	public int getViewIdx() {
		return viewIdx;
	}
	
	public void setViewIdx(int viewIdx) {
		this.viewIdx = viewIdx;
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
		return "{\"gaugeType\": \"" + gaugeType.name() + "\", \"size\": \"" + size.name() + "\", \"enabled\": " + enabled + ", \"viewIdx\": " + viewIdx + ",\"posX\": " + posX + ", \"posY\": " + posY + "}";
	}
	

	
	
}
