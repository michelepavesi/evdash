package autorad.android.widget.gauge;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import autorad.android.C;
import autorad.android.R;
import autorad.android.DashDisplay;
import autorad.android.sensor.SensorDataListener;
import autorad.android.sensor.DataType;



public class TextGauge extends AbstractGauge implements SensorDataListener {

	float lastAccuracy = 0;
	float lastData = -1f;
	LinearLayout baseLayout;
	private HashMap<DataType, TextView> textDataViews = new HashMap<DataType, TextView>();
	private ArrayList<TextView> allTextViews = new ArrayList<TextView>();
	
	public TextGauge(DashDisplay ctx, GaugeSettings settings) {
		super(ctx);
		this.ctx = ctx;
		gaugeSettings = settings;
		gaugeType = gaugeSettings.getGaugeType();
		details = gaugeType.getGaugeDetails();
		dataTypes = details.getDataTypes();
		
		this.setTag(gaugeSettings);
		applySettings();
		
		setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.background1));
		
		baseLayout = new LinearLayout(ctx);
		baseLayout.setOrientation(LinearLayout.VERTICAL);
		addView(baseLayout);
	    
	    this.setClickable(true);
	    this.setLongClickable(true);
	    this.setFocusable(true);
	    this.setFocusableInTouchMode(true);
	    this.setOnTouchListener(new OnTouchListener() {
	    	public boolean onTouch(View arg0, MotionEvent event) {
	    		Log.d(C.TAG, "Got Touch on text gauge");
				TextGauge.this.ctx.getParentOnTouchListener().onTouch(arg0, event);
				onGaugeMotionEvent(event);

				return false;
			}
		});

	    for(DataType dataType : dataTypes) {
	    	addDataSet(dataType);
	    }
	    
	}
	
	public void passivate() {}
	
	public void unpassivate() {}
	
	private void addDataSet(DataType dataType) {
		
		LinearLayout layout = new LinearLayout(ctx);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		
		TextView textLabel = new TextView(ctx);
		textLabel.setTextColor(0xFFFFFFFF);
	    textLabel.setText(ctx.getString(dataType.getLabelId()) + ":");
	    allTextViews.add(textLabel);
	    layout.addView(textLabel);
		
		TextView textData = new TextView(ctx);
		textData.setTypeface(Typeface.createFromAsset(ctx.getAssets(), "fonts/digital-7.ttf"));
	    textData.setTextColor(0xFFFFFFFF);
	    textData.setPadding(6, 0, 0, 0);
	    if (dataType == DataType.LOCATION) {
	    	textData.setLines(2);
	    	textData.setLineSpacing(0, 1.2f);
	    	textLabel.setLines(2);
	    }
		layout.addView(textData);
	    textDataViews.put(dataType, textData);
	    allTextViews.add(textData);
	    baseLayout.addView(layout);
		
	}
	
	
	public void destroy() {
		
		ctx = null;
		gaugeType = null;
		dataTypes = null;
		gaugeSettings= null;
		details = null;
		
		
	}
	
	public String getToastString() {
		return null;
	}
	
	
	public void applySettings() {
		float fontSize;
		switch (gaugeSettings.getSize()) {
		case TINY:
			fontSize = 8f;
			break;
		case VERY_SMALL:
			fontSize = 11f;
			break;
		case SMALL:
			fontSize = 16f;
			break;
		case MEDIUM:
			fontSize = 24f;
			break;
		case LARGE:
			fontSize = 32f;
			break;
		default:
			fontSize = 18f;
		}

		for(TextView textView : allTextViews) {
			textView.setTextSize(fontSize);
		}

	}

	
	public void calibrate() {
		
	}
	

	
	public void onData(DataType type, float... data) {
		//if (C.D) Log.d("EVDASH", "Text Gauge Received Data:" + data[0]);

		TextView textView = textDataViews.get(type);
		
		switch(type) {
		
		case AZIMUTH:
			textView.setText(String.format(DataType.AZIMUTH.getUnits(),data[0]));
			break;
		case BEARING:
			if (data[0] > 0) {
				textView.setText(String.format(DataType.BEARING.getUnits(),data[0]));
			}
			break;
		case SPEED_KPH:
			textView.setText(Float.toString(data[0]));
			break;
		case ALTITUDE:
			textView.setText(Float.toString(data[0]) + DataType.ALTITUDE.getUnits());
			break;
		case SATELLITE_ACCURACY:
			lastAccuracy = data[0];
			if (data[0] < 0) {
				if (data[0] == -2) {
					textView.setTextColor(Color.RED);
					textView.setText(R.string.GPS_STOPPED);
				} else {
					textView.setTextColor(Color.YELLOW);
					textView.setText(R.string.GPS_NO_FIX);
				}
			} else {
				textView.setTextColor(Color.WHITE);
				textView.setText(Float.toString(data[0]) + DataType.SATELLITE_ACCURACY.getUnits());
			}
			break;
		case SATELLITE_COUNT:
			//Log.d("EVDASH", "Text Gauge Received Data:" + data[0]);
			int color;
			switch((int)data[0]) {
			case 0:case 1:case 2:
				color = Color.RED;
				break;
			case 3: 
				color = Color.YELLOW;
				break;
			case 4:case 5:case 6:
				if (lastAccuracy > 0) {
					color = Color.GREEN;	
				} else {
					color = Color.YELLOW;
				}
				break;
			default:
				color = Color.GREEN; 
			}
			textView.setTextColor(color);
			textView.setText(Float.toString(data[0]));
			break;
		case LOCATION:
			textView.setText(formatLocation(Math.abs(data[0]),data[0] < 0 ? "S " : "N ") + 
							 "\n" +
							 formatLocation(Math.abs(data[1]), data[1] < 0 ? "W" : "E"));
			break;
		}
		
	}
	
	private String formatLocation(float data, String suffix) {
	
		String locStr = Location.convert((double)data, Location.FORMAT_SECONDS);
		//if (C.D) Log.d("EVDASH", "Format Data:" + locStr);
		String[] tokens = locStr.split(":|\\.");
		if (tokens.length == 4) {
			return String.format(DataType.LOCATION.getUnits(), tokens[0], tokens[1], tokens[2], suffix);
		} else {
			return "";
		}
	}

	
	protected void onGaugeMotionEvent(MotionEvent event) {
		ctx.onGaugeMotionEvent(this, event);
	}

	
}
