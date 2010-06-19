package autorad.android;

import gps.GeoLocator;

import java.util.HashMap;

import controller.Soliton1;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import autorad.android.transport.DataStatus;
import autorad.android.transport.DataStatusChangeListener;
import autorad.android.widget.AbsoluteLayout;
import autorad.android.widget.gauge.AbstractGauge;
import autorad.android.widget.gauge.Gauge;
import autorad.android.widget.gauge.GaugeSettings;
import autorad.android.widget.gauge.GaugeType;
import autorad.android.widget.gauge.TextGauge;


public class DashDisplay extends Activity implements DataStatusChangeListener {

	//private NotificationManager mNotificationManager;
    //private int YOURAPP_NOTIFICATION_ID; 
    
	Soliton1 controller;
	//DeviceSensor deviceSensor;
	GeoLocator gps;
	
	ImageView statusLED;
	TextView textLabel;
	
	Handler handler = new Handler();
	DataStatus lastDataStatus = DataStatus.UNKNOWN;
	boolean gaugesLocked = true;
	boolean loggingEnabled = false;
	boolean calibrate = false;
	AbsoluteLayout layout;
	
	
	
	HashMap<GaugeType, AbstractGauge> gauges = new HashMap<GaugeType, AbstractGauge>();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	if (C.D) Log.d("EVDASH","onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        layout = new AbsoluteLayout(this);
        layout.setKeepScreenOn(true);
        setContentView(layout);
        	
        loadGauges();
        
        TextView autoradLabel = new TextView(this);
        AbsoluteLayout.LayoutParams autoradTextLayout = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 15 , getWindow().getWindowManager().getDefaultDisplay().getHeight() - 70);
        autoradLabel.setLayoutParams(autoradTextLayout);
        autoradLabel.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Faith.ttf"));
        autoradLabel.setText("AutoRAD Industries");
        autoradLabel.setTextSize(18);
        layout.addView(autoradLabel);
        
    }

     
    public void setText(String txt) {
    	textLabel.setText(txt);
    }
    
    @Override
    protected void onStop() {
    	if (C.D) Log.d(C.TAG,"onStop ");
    	//deviceSensor.stop();
    	if (gps != null) {
    		gps.stop();
    		gps = null;
    	}
    	if (controller != null) {
    		controller.stop();
    		controller = null;
    	//	this.mNotificationManager.cancel(YOURAPP_NOTIFICATION_ID); 
    	}
    	for(AbstractGauge g : gauges.values()) {
    		g.cleanup();
    	}
    	if (statusLED != null) {
	    	statusLED.setImageDrawable(null);
	    	statusLED = null;
    	}
    	handler = null;
    	layout = null;
    	super.onStop();
    	
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	if (C.D) Log.d(C.TAG,"onStart");
    	if (controller != null) {
    	controller.registerDataStatusChangeListener(this);
    	controller.start();
    	}
    	
    	if (gps != null) {
    		gps.start();
    	}
    	
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if (C.D) Log.d(C.TAG,"onPause");
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (C.D) Log.d(C.TAG,"onResume");
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if (C.D) Log.d(C.TAG,"onDestroy");
    }
    
    
    private void initGPS() {
    	gps = new GeoLocator(this);
    }
    
    
    private void initController() {
    	controller = new Soliton1();
    	
    	if (statusLED == null) {
	        statusLED = new ImageView(this);
	        AbsoluteLayout.LayoutParams ledLayoutParams = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0 , 0);
	        statusLED.setLayoutParams(ledLayoutParams);
	        statusLED.setImageDrawable(getResources().getDrawable(R.drawable.greyball));
	        layout.addView(statusLED);
    	}
    	
    	initDataLogging();
        
    }
    
    private AbstractGauge addGauge(GaugeType gType) {
    	SharedPreferences gPrefs = getSharedPreferences("G", Context.MODE_PRIVATE);
    	String jsonSettings = gPrefs.getString(gType.name(), null);
    	if (jsonSettings != null) {
	    	try {
	    		GaugeSettings settings = new GaugeSettings(gType, jsonSettings); 
	    		settings.enable();
	    		return addGauge(settings);
	    	} catch (Exception ex) {
	    		Log.e(C.TAG, "Error in addGauge(GType) - " + gType.name() + ": " + ex.getMessage(), ex);
	    	}
    	}
    	return addGauge(new GaugeSettings(gType));
    	
    }
    
    private AbstractGauge addGauge(GaugeSettings settings) {
    	if (C.D) Log.d(C.TAG, "Addin gauge " + settings.getGaugeType().name());
    	AbstractGauge g;
    	switch(settings.getGaugeType().getGaugeDetails().getGaugeStyle()) {
    	case DIAL:
    		g = new Gauge(this, settings);
    		break;
    	case TEXT:
    		g = new TextGauge(this, settings);
    		break;
    	default:
    			return null;
    	}
    	
        gauges.put(settings.getGaugeType(), g);
        AbsoluteLayout.LayoutParams gLayoutParams = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
        														LayoutParams.WRAP_CONTENT, settings.getPosX(), settings.getPosY() );
        layout.addView(g, gLayoutParams);
        
        if ((settings.getGaugeType() == GaugeType.SPEED_KPH) || (settings.getGaugeType() == GaugeType.SPEED_MPH) 
        		|| (settings.getGaugeType() == GaugeType.GPS_INFO)
        		|| (settings.getGaugeType() == GaugeType.LATERAL_G) || (settings.getGaugeType() == GaugeType.ACCELERATION_G)) {
        	if (gps == null) initGPS();
        	gps.registerSensorDataListener(g);
        } else {
        	if (controller == null) initController();
        	controller.registerSensorDataListener(g);
        }
        this.registerForContextMenu(g);
        return g;
    }
    
    private void removeGauge(GaugeType gType) {
    	
    	if (gauges.containsKey(gType)) {
    		AbstractGauge g = gauges.remove(gType);
    		g.getSettings().disable();
    		g.save();
    		this.unregisterForContextMenu(g);
    		if (controller != null) {
    			controller.unregisterSensorDataListener(g);
    		}
    		if (gps != null) {
    			gps.unregisterSensorDataListener(g);
    		}
    		layout.removeView(g);
    		g = null;
    		
    	}
    }
    
    private void loadGauges() {
    	SharedPreferences gPrefs = getSharedPreferences("G", Context.MODE_PRIVATE);
    	for (GaugeType gType : GaugeType.values()) {
    		String jsonSettings = gPrefs.getString(gType.name(), null);
    		if (C.D) Log.d(C.TAG, "loadGauges - " + gType.name() + ": " + jsonSettings);
    		if (jsonSettings != null) {
    			try {
    				GaugeSettings settings = new GaugeSettings(gType, jsonSettings);
    				if (settings.isEnabled()) {
    					addGauge(settings);
    				}
    			} catch (Exception ex) {
    				Log.e(C.TAG, "Error in loadGauges - " + gType.name() + ": " + ex.getMessage(), ex);
    			}
    		}    		
    	}
    }
    
    private void initDataLogging() {
    	SharedPreferences gPrefs = getSharedPreferences("G", Context.MODE_PRIVATE);
    	loggingEnabled = gPrefs.getBoolean("logging", false);
    	if (C.D) Log.d(C.TAG, "looggingEnabled - " + loggingEnabled);
    	if (loggingEnabled) {
    		controller.startDataLogging();
    	}
    }
    
    private void startDataLogging() {
    	SharedPreferences gPrefs = getSharedPreferences("G", Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = gPrefs.edit();
    	editor.putBoolean("logging", true);
    	editor.commit();
    	if (!loggingEnabled) {
    		controller.stopDataLogging();
    		loggingEnabled = true;
    	}
    	if (C.D) Log.d(C.TAG, "looggingEnabled - " + loggingEnabled);
    }
    
    private void stopDataLogging() {
    	SharedPreferences gPrefs = getSharedPreferences("G", Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = gPrefs.edit();
    	editor.putBoolean("logging", false);
    	editor.commit();
    	if (loggingEnabled) {
    		controller.stopDataLogging();
    		loggingEnabled = false;
    	}
    	if (C.D) Log.d(C.TAG, "loggingEnabled - " + loggingEnabled);
    }
  
    
    public Handler getHandler() {
    	return this.handler;
    }
    
    public boolean onGaugeMotionEvent(AbstractGauge gauge, MotionEvent event) { 
    	int action = event.getAction(); 
    	
    	if ( action == MotionEvent.ACTION_MOVE ) { //this.setText("x: " + mCurX + ",y: " + mCurY ); 
    		if (!gaugesLocked) {
	    		int mCurX = (int)event.getX()-(gauge.getWidth()/2) + gauge.getLeft(); 
	        	int mCurY = (int)event.getY()-(gauge.getHeight()/2) + gauge.getTop();	
	        	if (C.D) Log.d(C.TAG, "motion  x=" + mCurY + "   y=" + mCurX);
	        	if ((mCurX > 5) || (mCurY > 5) ) {
					AbsoluteLayout.LayoutParams p = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 
							mCurX, mCurY); 
					gauge.getSettings().setPosition(mCurX, mCurY);
					gauge.setLayoutParams (p);
	    		}
    		}
    	} else if ( action == MotionEvent.ACTION_UP ) {
    		if (C.D) Log.d(C.TAG, "got motion:" + action);

    		if (!gaugesLocked) {
    			gauge.save();
    			Toast.makeText(this, "Gauge position saved", Toast.LENGTH_SHORT).show();
    		} else {
    			if (calibrate) {
	    			calibrate = false;
	    			gauge.calibrate();
    			} else {
    				String t =  gauge.getToastString();
    				if (t != null) {
    					Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
    				}
   				
    			}
    		}
    		//gauge.fullSweepReset();
    		//@TODO Save postition
    	}
    	
    	return true; 
    }

	public void onDataStatusChange(DataStatus status) {
		if (status != lastDataStatus) {
			if (C.D) Log.d(C.TAG, "New data status = " + status.name());
			lastDataStatus = status;
			if (statusLED == null) return;
			// Update view in UI Thread
			handler.post(new Runnable() {
				public void run() {
					if (statusLED == null) return;
					switch (lastDataStatus) {
					case UNKNOWN:
						statusLED.setImageDrawable(getResources().getDrawable(R.drawable.greyball));
						break;
					case RECEIVING:
						statusLED.setImageDrawable(getResources().getDrawable(R.drawable.greenball));
						break;
					case STOPPED:
						statusLED.setImageDrawable(getResources().getDrawable(R.drawable.redball));
						controller.start();
						break;
					case WAITING:
						statusLED.setImageDrawable(getResources().getDrawable(R.drawable.blueball));
						break;
					}		
				}
			});
		}
		
	}
    
	private final static int MENU_LOCK = 2;
	private final static int MENU_UNLOCK = 3;
	private final static int MENU_EXIT = 4;
	private final static int MENU_ENABLE_LOG = 5;
	private final static int MENU_DISABLE_LOG = 6;
	private final static int MENU_CALIBRATE = 7;
	
	private final static int SUBMENU_GROUP_ADDGAUGE = 10;
	private final static int SUBMENU_ADDGAUGE_RPM = 11;
	private final static int SUBMENU_ADDGAUGE_AMPSLOW = 12;
	private final static int SUBMENU_ADDGAUGE_AMPSHIGH = 13;
	private final static int SUBMENU_ADDGAUGE_VOLTS = 14;
	private final static int SUBMENU_ADDGAUGE_TEMP = 15;
	private final static int SUBMENU_ADDGAUGE_SPEEDKPH = 16;
	private final static int SUBMENU_ADDGAUGE_GPSINFO = 17;
	private final static int SUBMENU_ADDGAUGE_LATERALG = 18;
	private final static int SUBMENU_ADDGAUGE_ACCELERATIONG = 19;
	private final static int SUBMENU_ADDGAUGE_SPEEDMPH = 20;
	private final static int SUBMENU_ADDGAUGE_SPEEDOLDSKOOLMPH = 21;
	
	private final static int CONTEXT_MENU_DELETE = 50;
	private final static int CONTEXT_MENU_SMALLER = 51;
	private final static int CONTEXT_MENU_LARGER = 52;
	private final static int CONTEXT_MENU_CANCEL = 53;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		SubMenu subMenu = menu.addSubMenu("Add/Remove Gauge");
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_RPM, 0, "Motor RPM");
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_SPEEDKPH, 0, "Speed km/h");
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_SPEEDMPH, 0, "Speed mph");
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_SPEEDOLDSKOOLMPH, 0, "Speed mph - Old skool Mini");
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_AMPSHIGH, 0, "High Amps 150-1800A");
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_AMPSLOW, 0, "Low Amps 0-180A");
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_VOLTS, 0, "Traction Pack Volts");
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_TEMP, 0, "Controller Temperature");
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_GPSINFO, 0, "GPS Info");
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_LATERALG, 0, "Lateral G-force");
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_ACCELERATIONG, 0, "Acceleration G-force");
		subMenu.setGroupCheckable(SUBMENU_GROUP_ADDGAUGE, true, false);
		
		menu.add(0, MENU_UNLOCK, 0, "Unlock");
		menu.add(0, MENU_LOCK, 0, "Lock");
		menu.add(0, MENU_ENABLE_LOG, 0, "Enable Log");
		menu.add(0, MENU_DISABLE_LOG, 0, "Disable Log");
		menu.add(0, MENU_CALIBRATE, 0, "Calibrate");
		menu.add(0, MENU_EXIT, 0, "Exit");
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.findItem(MENU_UNLOCK).setVisible(gaugesLocked);
		menu.findItem(MENU_CALIBRATE).setVisible(gaugesLocked);
		menu.findItem(MENU_LOCK).setVisible(!gaugesLocked);
		
		menu.findItem(MENU_DISABLE_LOG).setVisible(loggingEnabled);
		menu.findItem(MENU_ENABLE_LOG).setVisible(!loggingEnabled);
		
		menu.findItem(SUBMENU_ADDGAUGE_RPM).setChecked(gauges.containsKey(GaugeType.RPM));
		menu.findItem(SUBMENU_ADDGAUGE_SPEEDKPH).setChecked(gauges.containsKey(GaugeType.SPEED_KPH));
		menu.findItem(SUBMENU_ADDGAUGE_SPEEDMPH).setChecked(gauges.containsKey(GaugeType.SPEED_MPH));
		menu.findItem(SUBMENU_ADDGAUGE_SPEEDOLDSKOOLMPH).setChecked(gauges.containsKey(GaugeType.SPEED_OLDSKOOL_MPH));
		menu.findItem(SUBMENU_ADDGAUGE_AMPSHIGH).setChecked(gauges.containsKey(GaugeType.BATTERY_CURRENT_HIGH));
		menu.findItem(SUBMENU_ADDGAUGE_AMPSLOW).setChecked(gauges.containsKey(GaugeType.BATTERY_CURRENT_LOW));
		menu.findItem(SUBMENU_ADDGAUGE_VOLTS).setChecked(gauges.containsKey(GaugeType.PACK_VOLTAGE));
		menu.findItem(SUBMENU_ADDGAUGE_TEMP).setChecked(gauges.containsKey(GaugeType.CONTROLLER_TEMPERATURE));
		menu.findItem(SUBMENU_ADDGAUGE_GPSINFO).setChecked(gauges.containsKey(GaugeType.GPS_INFO));
		menu.findItem(SUBMENU_ADDGAUGE_LATERALG).setChecked(gauges.containsKey(GaugeType.LATERAL_G));
		menu.findItem(SUBMENU_ADDGAUGE_ACCELERATIONG).setChecked(gauges.containsKey(GaugeType.ACCELERATION_G));
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case SUBMENU_ADDGAUGE_RPM:
	    	if (item.isChecked()) {
	    		removeGauge(GaugeType.RPM);
	    	} else {
	    		AbstractGauge g = addGauge(GaugeType.RPM);
	    		g.save();
	    		Toast.makeText(this, "Added Motor RPM gauge", Toast.LENGTH_SHORT).show();
	    	}
	        return true;
	    case SUBMENU_ADDGAUGE_SPEEDKPH:
	    	if (item.isChecked()) {
	    		removeGauge(GaugeType.SPEED_KPH);
	    	} else {
	    		AbstractGauge g = addGauge(GaugeType.SPEED_KPH);
	    		g.save();
	    		Toast.makeText(this, "Added Speed km/h gauge", Toast.LENGTH_SHORT).show();
	    	}
	        return true;
	    case SUBMENU_ADDGAUGE_SPEEDMPH:
	    	if (item.isChecked()) {
	    		removeGauge(GaugeType.SPEED_MPH);
	    	} else {
	    		AbstractGauge g = addGauge(GaugeType.SPEED_MPH);
	    		g.save();
	    		Toast.makeText(this, "Added Speed mph gauge", Toast.LENGTH_SHORT).show();
	    	}
	        return true;
	    case SUBMENU_ADDGAUGE_SPEEDOLDSKOOLMPH:
	    	if (item.isChecked()) {
	    		removeGauge(GaugeType.SPEED_OLDSKOOL_MPH);
	    	} else {
	    		AbstractGauge g = addGauge(GaugeType.SPEED_OLDSKOOL_MPH);
	    		g.save();
	    		Toast.makeText(this, "Added Old skool Mini mph gauge", Toast.LENGTH_SHORT).show();
	    	}
	        return true;
	    case SUBMENU_ADDGAUGE_AMPSHIGH:
	    	if (item.isChecked()) {
	    		removeGauge(GaugeType.BATTERY_CURRENT_HIGH);
	    	} else {
	    		AbstractGauge g = addGauge(GaugeType.BATTERY_CURRENT_HIGH);
	    		g.save();
	    		Toast.makeText(this, "Added High Amp gauge", Toast.LENGTH_SHORT).show();
	    	}
	        return true;
	    case SUBMENU_ADDGAUGE_AMPSLOW:
	    	if (item.isChecked()) {
	    		removeGauge(GaugeType.BATTERY_CURRENT_LOW);
	    	} else {
	    		AbstractGauge g = addGauge(GaugeType.BATTERY_CURRENT_LOW);
	    		g.save();
	    		Toast.makeText(this, "Added Low Amp gauge", Toast.LENGTH_SHORT).show();
	    	}
	        return true;
	    case SUBMENU_ADDGAUGE_TEMP:
	    	if (item.isChecked()) {
	    		removeGauge(GaugeType.CONTROLLER_TEMPERATURE);
	    	} else {
	    		AbstractGauge g = addGauge(GaugeType.CONTROLLER_TEMPERATURE);
	    		g.save();
	    		Toast.makeText(this, "Added Controller Temperature gauge", Toast.LENGTH_SHORT).show();
	    	}
	        return true;
	    case SUBMENU_ADDGAUGE_VOLTS:
	    	if (item.isChecked()) {
	    		removeGauge(GaugeType.PACK_VOLTAGE);
	    	} else {
	    		AbstractGauge g = addGauge(GaugeType.PACK_VOLTAGE);
	    		g.save();
	    		Toast.makeText(this, "Added Traction Pack Voltage gauge", Toast.LENGTH_SHORT).show();
	    	}
	        return true;
	    case SUBMENU_ADDGAUGE_GPSINFO:
	    	if (item.isChecked()) {
	    		removeGauge(GaugeType.GPS_INFO);
	    	} else {
	    		AbstractGauge g = addGauge(GaugeType.GPS_INFO);
	    		g.save();
	    		Toast.makeText(this, "Added Satellite GPS Info gauge", Toast.LENGTH_SHORT).show();
	    	}
	        return true;
	    case SUBMENU_ADDGAUGE_LATERALG:
	    	if (item.isChecked()) {
	    		removeGauge(GaugeType.LATERAL_G);
	    	} else {
	    		AbstractGauge g = addGauge(GaugeType.LATERAL_G);
	    		g.save();
	    		Toast.makeText(this, "Added Lateral G-force gauge", Toast.LENGTH_SHORT).show();
	    	}
	        return true;
	    case SUBMENU_ADDGAUGE_ACCELERATIONG:
	    	if (item.isChecked()) {
	    		removeGauge(GaugeType.ACCELERATION_G);
	    	} else {
	    		AbstractGauge g = addGauge(GaugeType.ACCELERATION_G);
	    		g.save();
	    		Toast.makeText(this, "Added Acceleration G-force Info gauge", Toast.LENGTH_SHORT).show();
	    	}
	        return true;
	    case MENU_UNLOCK:
	    	gaugesLocked = false;
	    	return true;
	    case MENU_LOCK:
	    	gaugesLocked = true;
	    	return true;
	    case MENU_DISABLE_LOG:
	    	stopDataLogging();
	    	return true;
	    case MENU_ENABLE_LOG:
	    	startDataLogging();
	    	return true;
	    case MENU_CALIBRATE:
	    	calibrate = true;
	    	return true;
	    case MENU_EXIT:
	    	controller.stop();
	    	this.finish();
	        return true;
	    }
	    return false;
	}
    
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (gaugesLocked) {
			if (v instanceof AbstractGauge) {
				contextMenuGauge = (AbstractGauge)v;
			}
			if (C.D) Log.d(C.TAG, "Creating Context Menu for view " + v.toString());
			menu.add(0, CONTEXT_MENU_SMALLER, 0, "Reduce Gauge Size");
			menu.add(0, CONTEXT_MENU_LARGER, 0, "Increase Gauge Size");
			menu.add(0, CONTEXT_MENU_CANCEL, 0,  "Cancel");
		}
	}

	AbstractGauge contextMenuGauge = null;
	
	public boolean onContextItemSelected(MenuItem item) {
		//AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		if (C.D) Log.d(C.TAG, "Context menu item " + item.getItemId());
		GaugeSettings settings;
		switch (item.getItemId()) {
		case CONTEXT_MENU_LARGER:
			settings = contextMenuGauge.getSettings();
			if (settings.makeLarger()) {
				contextMenuGauge.applySettings();
				contextMenuGauge.save();
			}
			return true;
		case CONTEXT_MENU_SMALLER:

			settings = contextMenuGauge.getSettings();
			if (settings.makeSmaller()) {
				contextMenuGauge.applySettings();
				contextMenuGauge.save();
			}
			return true;
		case CONTEXT_MENU_DELETE:
			//deleteNote(info.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
}