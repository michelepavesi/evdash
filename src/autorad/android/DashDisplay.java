package autorad.android;

import gps.GeoLocator;

import java.util.HashMap;
import java.util.Map;

import controller.Soliton1;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;

import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import autorad.android.transport.DataStatus;
import autorad.android.transport.DataStatusChangeListener;
import autorad.android.transport.SourceType;
import autorad.android.util.Convert;
import autorad.android.widget.AbsoluteLayout;
import autorad.android.widget.gauge.AbstractGauge;
import autorad.android.widget.gauge.Gauge;
import autorad.android.widget.gauge.GaugeSettings;
import autorad.android.widget.gauge.GaugeSize;
import autorad.android.widget.gauge.GaugeStyle;
import autorad.android.widget.gauge.GaugeType;
import autorad.android.widget.gauge.TextGauge;


public class DashDisplay extends Activity implements DataStatusChangeListener {

	public boolean DEBUG_MODE = C.D;
	
	Soliton1 controller;
	GeoLocator gps;
	
	int preferredUnitType;
	
	ImageView statusLED;
	ImageView gpsStatusImg;
	ImageView loggingImg;
	TextView textLabel;
	
	Handler handler = new Handler();
	DataStatus lastDataStatus = DataStatus.UNKNOWN;
	DataStatus lastGpsStatus = DataStatus.UNKNOWN;
	boolean gaugesLocked = true;
	boolean loggingEnabled = false;
	boolean calibrate = false;
	static boolean showAd = true;
	
	AbsoluteLayout layoutL; // LEFT
	AbsoluteLayout layout;  // MIDDLE
	AbsoluteLayout layoutR; // RIGHT
	ViewFlipper flipper;
	int currentPageIdx;
	
	
	boolean contextClickHandled = false;
	
	HashMap<String, AbstractGauge> gauges = new HashMap<String, AbstractGauge>();

	int windowWidth;
	int windowHeight;
	float SCALE;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	if (C.D) Log.d("EVDASH","onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        flipper = new ViewFlipper(this);
        layout = new AbsoluteLayout(this);
        layoutL = new AbsoluteLayout(this);
        layoutR = new AbsoluteLayout(this);
        
        
        layout.setKeepScreenOn(true);
        layoutL.setKeepScreenOn(true);
        layoutR.setKeepScreenOn(true);
        
        
        TextView autoradLabel = new TextView(this);
        AbsoluteLayout.LayoutParams autoradTextLayout = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 15 , getWindow().getWindowManager().getDefaultDisplay().getHeight() - 16);
        autoradLabel.setLayoutParams(autoradTextLayout);
        // Keep this label.
        autoradLabel.setText("EVDash 2011 Open Source Version");
        autoradLabel.setTextSize(9);
        layout.addView(autoradLabel);

        
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        windowHeight = metrics.heightPixels;
        windowWidth = metrics.widthPixels;
        
        int longestSide = windowHeight > windowWidth ? windowHeight : windowWidth;
        
        SCALE = longestSide / 800f;
        
    	
        if (C.D) Log.d("EVDASH","WINDOW SCALE=" + SCALE + "(width=" + windowWidth + ", height=" + windowHeight + ")");
        
       
        
     // Gesture detection
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
            	 if (C.D)  Log.d(C.TAG, "Gesture Listener got a motion event");
                 if (gestureDetector.onTouchEvent(event)) {
                     return true;
                 }
                 return false;
             }
         };

         flipper.setOnTouchListener(gestureListener);
         flipper.setClickable(true);
        
         ImageView dot0 = new ImageView(this);
	     AbsoluteLayout.LayoutParams right0LayoutParams = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, windowWidth-(int)(26*SCALE) , 0);
	     dot0.setLayoutParams(right0LayoutParams);
	     dot0.setImageDrawable(getResources().getDrawable(R.drawable.dot));
	     layoutL.addView(dot0);
         
         ImageView dot1 = new ImageView(this);
	     AbsoluteLayout.LayoutParams right1LayoutParams = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, windowWidth-(int)(42*SCALE) , 0);
	     dot1.setLayoutParams(right1LayoutParams);
	     dot1.setImageDrawable(getResources().getDrawable(R.drawable.dot));
	     layoutL.addView(dot1);
	     
	     ImageView dot2 = new ImageView(this);
	     AbsoluteLayout.LayoutParams left0LayoutParams = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0 , 0);
	     dot2.setLayoutParams(left0LayoutParams);
	     dot2.setImageDrawable(getResources().getDrawable(R.drawable.dot));
	     layout.addView(dot2);
	     
	     ImageView dot3 = new ImageView(this);
	     dot3.setLayoutParams(right0LayoutParams);
	     dot3.setImageDrawable(getResources().getDrawable(R.drawable.dot));
	     layout.addView(dot3);
	     
	     ImageView dot4 = new ImageView(this);
	     dot4.setLayoutParams(left0LayoutParams);
	     dot4.setImageDrawable(getResources().getDrawable(R.drawable.dot));
	     layoutR.addView(dot4);
	     
	     ImageView dot5 = new ImageView(this);
	     AbsoluteLayout.LayoutParams left1LayoutParams = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, (int)(16*SCALE) , 0);
	     dot5.setLayoutParams(left1LayoutParams);
	     dot5.setImageDrawable(getResources().getDrawable(R.drawable.dot));
	     layoutR.addView(dot5);
	     
         flipper.addView(layoutL);
         flipper.addView(layout);
         flipper.addView(layoutR);
         currentPageIdx = 1;
         flipper.setDisplayedChild(currentPageIdx);
        
         setContentView(flipper);
         
         preferredUnitType = getDefaultUnits(Convert.MPH);
         
         loadGauges();
    }

   
    
    private AbsoluteLayout getCurrentLayout() {
    	return getLayout(currentPageIdx);
    }
    
    private AbsoluteLayout getLayout(int layoutIdx) {
    	switch(layoutIdx) {
        case 0:
        	return layoutL;
        	
        case 2:
        	return layoutR;
        	
        default:
        	return layout;	
        }
    }
    
    public int getPreferredUnitType() {
    	return preferredUnitType;
    }
     
    public void setText(String txt) {
    	textLabel.setText(txt);
    }
    
    private void removeGaugeFromView(AbstractGauge g) {
    	
    	getCurrentLayout().removeView(g);
		g.destroy();
		
    }
    
    public float getScale() {
    	return SCALE;
    }
    
    @Override
    protected void onStop() {
    	if (C.D) Log.d(C.TAG,"onStop ");

    	if (gps != null) {
    		gps.stop();
    	}
    	if (controller != null) {
    		controller.stop();
    	}

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
    	if (gps!= null) {
    		gps.start();
    	}
    	
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if (C.D) Log.d(C.TAG,"onPause");
    	for(AbstractGauge g : gauges.values()) {
    		g.passivate();
    	}
    	if (controller != null) {
    		controller.stop();
    	}
    	if (gps!= null) {
    		gps.pause();
    	}
    	
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (C.D) Log.d(C.TAG,"onResume");
    	if (gps!= null) {
    		gps.resume();
    	}
    	for(AbstractGauge g : gauges.values()) {
    		if (currentPageIdx == g.getSettings().getViewIdx()) {
            	g.unpassivate();
            }
    	}
    	
    }
    
    @Override
    protected void onDestroy() {
    	if (C.D) Log.d(C.TAG,"onDestroy");
    	try {
			if (controller != null) {
				controller.destroy();
				controller = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	try {
			if (gps!= null) {
				gps.stopDataLogging();
				gps.stop();
				gps.destroy();
				gps = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	try {
			for(AbstractGauge g : gauges.values()) {
				removeGaugeFromView(g);
			}
			gauges.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	super.onDestroy();
    	System.exit(0);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	// TODO Auto-generated method stub
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if (C.D) Log.d(C.TAG, "Back clicked");
    		contextClickHandled = false;
    	}
    	return super.onKeyDown(keyCode, event);
    	
    }
    
    
    private void initGPS() {
    	
    	if (gpsStatusImg == null) {
    		gpsStatusImg = new ImageView(this);
	        AbsoluteLayout.LayoutParams gpsLayoutParams = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, windowWidth-(int)(48*SCALE) , (int)(24*SCALE));
	        gpsStatusImg.setLayoutParams(gpsLayoutParams);
	        gpsStatusImg.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_gps_acquiring));
	        getCurrentLayout().addView(gpsStatusImg);
    	}
    	
    	gps = new GeoLocator(this);
    	gps.registerDataStatusChangeListener(this);
    	gps.start();
    }
    
    
    private void initController() {
    	controller = new Soliton1();
    	
    	if (statusLED == null) {
	        statusLED = new ImageView(this);
	        AbsoluteLayout.LayoutParams ledLayoutParams = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0 , (int)(16*SCALE));
	        statusLED.setLayoutParams(ledLayoutParams);
	        statusLED.setImageDrawable(getResources().getDrawable(R.drawable.greyball));
	        getCurrentLayout().addView(statusLED);
    	}

    	controller.registerDataStatusChangeListener(this);
		controller.start();
		
    	initDataLogging();
        
    }
    
    private AbstractGauge addNewGauge(GaugeType gType, int x, int y, GaugeSize size) {
    	GaugeSettings settings = new GaugeSettings(gType, currentPageIdx);
    	settings.setPosition(x, y);
    	if (size != null) {
    		settings.setSize(size);
    	}
    	AbstractGauge g = addGauge(settings);
    	g.save();	
    	return g;
    }
    
    private AbstractGauge addNewGauge(GaugeType gType) {
    	AbstractGauge g = addGauge(new GaugeSettings(gType, currentPageIdx));
    	g.save();
    	return g;
    }
    
    private AbstractGauge addGauge(GaugeSettings settings) {
    	if (C.D) Log.d(C.TAG, "Addin gauge: " + settings.getGaugeType().name());
    	AbstractGauge g;
    	GaugeStyle style = settings.getGaugeType().getGaugeDetails().getGaugeStyle();
	    switch(style) {
	    	case DIAL:
	    		g = new Gauge(this, settings);
	    		break;
	    	case TEXT:
	    		g = new TextGauge(this, settings);
	    		break;
	    	default:
	    			return null;
	    }
    	
    	
        gauges.put(settings.getId(), g);
        AbsoluteLayout.LayoutParams gLayoutParams = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
        														LayoutParams.WRAP_CONTENT, settings.getPosX(), settings.getPosY() );
        
        AbsoluteLayout l = getLayout(settings.getViewIdx());
        if (style == GaugeStyle.MAP) {
        	l.addView(g, 0, gLayoutParams);
        } else {
        	l.addView(g, gLayoutParams);
        }
        
        
        
        if ((settings.getGaugeType() == GaugeType.SPEED_KPH) 
        		|| (settings.getGaugeType() == GaugeType.SPEED_MPH)
        		|| (settings.getGaugeType() == GaugeType.GPS_INFO)
        		|| (settings.getGaugeType() == GaugeType.LATERAL_G) 
        		|| (settings.getGaugeType() == GaugeType.ACCELERATION_G)) {
        	if (gps == null) initGPS();
        	gps.registerSensorDataListener(g);
        } else {
        	if (controller == null) initController();
        	controller.registerSensorDataListener(g);
        }
        this.registerForContextMenu(g);
        if (currentPageIdx == g.getSettings().getViewIdx()) {
        	g.unpassivate();
        } else {
        	g.passivate();
        }
        
//        if (style == GaugeStyle.MAP) {
//	        for(int i=0; i<l.getChildCount(); i++) {
//	        	View ag = l.getChildAt(i);
//	        	if (!(ag instanceof MapGauge)) {
//	        		l.bringChildToFront(ag);
//	        	}
//	        }
//        }
        
        return g;
    }
    
    private void removeGauge(AbstractGauge gauge) {
    	if (gauge == null) return;
    	String id = gauge.getSettings().getId();
    	if (gauges.containsKey(id)) {
    		AbstractGauge g = gauges.remove(id);
    		
    		SharedPreferences gPrefs = getSharedPreferences("G", Context.MODE_PRIVATE);
    		Editor editor = gPrefs.edit();
    		editor.remove(id);
    		editor.commit();
    		
    		this.unregisterForContextMenu(g);
    		if (controller != null) {
    			controller.unregisterSensorDataListener(g);
    		}
    		if (gps != null) {
    			gps.unregisterSensorDataListener(g);
    		}
    		removeGaugeFromView(g);
    		
    		g = null;
    	}
    	System.gc();
    }
    
    private void loadGauges() {
    	SharedPreferences gPrefs = getSharedPreferences("G", Context.MODE_PRIVATE);
    	boolean hasGauge = false;
    	Map<String, ?> all = gPrefs.getAll();
    	for (String key : all.keySet()) {
    		if (key.equals("logging")) return;
    			String jsonSettings;
    		try {
    			jsonSettings = gPrefs.getString(key, null);
    		} catch (Exception exception) {
    			continue;
    		}
    		if (C.D) Log.d(C.TAG, "loadGauges - " + key + ": " + jsonSettings);
    		if (key.length() < 30) {
    			if (C.D) Log.d(C.TAG, "GaugeType.valueOf " + GaugeType.valueOf(key));
    		}
    		if (jsonSettings != null) {
    			try {
    				GaugeSettings settings = new GaugeSettings(key, jsonSettings);
    				if (settings.isEnabled()) {
    					addGauge(settings);
    					hasGauge = true;
    				}
    			} catch (Exception ex) {
    				Log.e(C.TAG, "Error in loadGauges - removing " + key + ": " + ex.getMessage(), ex);
    				Editor editor = gPrefs.edit();
    				editor.remove(key);
    				editor.commit();
    			}
    		}    		
    	}
    	if (!hasGauge) {
    		// Add default Gauges
    		//AbstractGauge g = 
    		addNewGauge(GaugeType.SPEED_MPH,(int)(25*SCALE), (int)(10*SCALE), GaugeSize.LARGE);
    		//AbsoluteLayout.LayoutParams p = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 
			//		25, 10); 
			//g.getSettings().setPosition(25, 10);
			//g.setLayoutParams (g.getAbsoluteLayout());
			
    		//g.save();
    		
    		
    		//AbstractGauge g1 = 
    		addNewGauge(GaugeType.GPS_INFO, (int)(430*SCALE), (int)(185*SCALE), GaugeSize.SMALL);
    		//AbsoluteLayout.LayoutParams p1 = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 
			//		560, 185); 
			//g1.getSettings().setPosition(560, 185);
			//g1.setLayoutParams(p1);
    		//g1.save();
    		
    		//AbstractGauge g2 = 
    		addNewGauge(GaugeType.LATERAL_G, (int)(380*SCALE), (int)(30*SCALE), GaugeSize.VERY_SMALL);
    		//AbsoluteLayout.LayoutParams p2 = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 
    		//		433,30); 
    		//g2.getSettings().makeSmaller();
    		//g2.getSettings().makeSmaller();
			//g2.getSettings().setPosition(433,30);
			//g2.applySettings();
			//g2.setLayoutParams(g2.getAbsoluteLayout());
    		//g2.save();
    		
    		//AbstractGauge g3 = 
    		addNewGauge(GaugeType.ACCELERATION_G, (int)(540*SCALE), (int)(30*SCALE), GaugeSize.VERY_SMALL);
    		//AbsoluteLayout.LayoutParams p3 = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 
    		//		600,30); 
    		//g3.getSettings().makeSmaller();
    		//g3.getSettings().makeSmaller();
			//g3.getSettings().setPosition(600,30);
			//g3.applySettings();
			//g3.setLayoutParams(g3.getAbsoluteLayout());
			//g3.setLayoutParams (p3);
			
    		//g3.save();
    		
    		
    		
    	}
    }
    
    private void initDataLogging() {
    	SharedPreferences gPrefs = getSharedPreferences("C", Context.MODE_PRIVATE);
    	loggingEnabled = gPrefs.getBoolean("logging", false);
    	if (C.D) Log.d(C.TAG, "loggingEnabled - " + loggingEnabled);
    	if (loggingEnabled) {
    		startDataLogging();
    	}
    }

    private void setDefaultUnits(int unitType) {
    	SharedPreferences gPrefs = getSharedPreferences("C", Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = gPrefs.edit();
    	editor.putInt("unit", unitType);
    	preferredUnitType = unitType;
    	editor.commit();
    }
    
    private int getDefaultUnits(int defaultUnitType) {
    	SharedPreferences gPrefs = getSharedPreferences("C", Context.MODE_PRIVATE);
    	return gPrefs.getInt("unit", defaultUnitType);
    }
    
    private void startDataLogging() {
    	SharedPreferences gPrefs = getSharedPreferences("C", Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = gPrefs.edit();
    	editor.putBoolean("logging", true);
    	editor.commit();
    	if (!loggingEnabled) {
    		if (controller != null) {
    			controller.startDataLogging();
    		}
    		if (gps != null) {
    			gps.startDataLogging();
    		}
    		loggingEnabled = true;
    	}
    	if (loggingImg == null) {
    		loggingImg = new ImageView(this);
	        AbsoluteLayout.LayoutParams ledLayoutParams = new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0 , (int)(32*SCALE));
	        loggingImg.setLayoutParams(ledLayoutParams);
	        loggingImg.setImageDrawable(getResources().getDrawable(R.drawable.logger));
	        getCurrentLayout().addView(loggingImg);
    	}

    	if (C.D) Log.d(C.TAG, "looggingEnabled - " + loggingEnabled);
    }
    
    private void stopDataLogging() {
    	SharedPreferences gPrefs = getSharedPreferences("C", Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = gPrefs.edit();
    	editor.putBoolean("logging", false);
    	editor.commit();
    	if (loggingEnabled) {
    		if (controller != null) {
    			controller.stopDataLogging();
    		}
    		if (gps != null) {
    			gps.stopDataLogging();
    		}
    		loggingEnabled = false;
    	}
    	if (loggingImg != null) {
    		getCurrentLayout().removeView(loggingImg);
	        loggingImg = null;
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
    			Toast.makeText(this, R.string.TOAST_POSITION_SAVED, Toast.LENGTH_SHORT).show();
    		} else {
    			//if (calibrate) {
	    		//	calibrate = false;
	    		//	gauge.calibrate();
    			//} else {
    				String t =  gauge.getToastString();
    				if (t != null) {
    					Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
    				}
   				
    			//}
    		}
    	}
    	
    	return true; 
    }

	public void onDataStatusChange(SourceType source, final DataStatus status) {
		switch (source) {
		case MOTOR_CONTROLLER:
			if (C.D) Log.i(C.TAG, "New data status = " + status.name());
			if (status == lastDataStatus) return;
			lastDataStatus = status;
			if (statusLED == null) return;
			// Update view in UI Thread
			handler.post(new Runnable() {
				public void run() {
					if (statusLED == null) return;
					switch (status) {
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
			break;
			
		case GPS:
			if (gpsStatusImg == null) return;
			if (status == lastGpsStatus) return;
			lastGpsStatus = status;
			// Update view in UI Thread
			handler.post(new Runnable() {
				public void run() {
					switch (status) {
					case UNKNOWN:
						gpsStatusImg.setVisibility(8);
						break;
					case RECEIVING:
						gpsStatusImg.setVisibility(0);
						gpsStatusImg.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_gps_on));
						break;
					case STOPPED:
						gpsStatusImg.setVisibility(8);
						break;
					case WAITING:
						gpsStatusImg.setVisibility(0);
						gpsStatusImg.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_gps_acquiring));
						break;
					}		
				}
			});
			break;
		}
		//if (status != lastDataStatus) {
			
		//}
		
	}
    
	private final static int MENU_LOCK = 2;
	private final static int MENU_UNLOCK = 3;
	private final static int MENU_EXIT = 4;
	private final static int MENU_ENABLE_LOG = 5;
	private final static int MENU_DISABLE_LOG = 6;
	//private final static int MENU_PAY = 7;
	private final static int MENU_ABOUT = 8;
	
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
	//private final static int SUBMENU_ADDGAUGE_SPEEDOLDSKOOLMPH = 21;
	private final static int SUBMENU_ADDGAUGE_SPEEDKNOTS = 23;
	
	private final static int  SUBMENU_SOLITON_CONSOLE = 30;
	
	private final static int SUBMENU_GROUP_UNITS = 40;
	private final static int SUBMENU_UNITS_KM = 41;
	private final static int SUBMENU_UNITS_MILES = 42;
	private final static int SUBMENU_UNITS_KNOTS = 43;
	
	private final static int CONTEXT_MENU_REMOVE = 50;
	private final static int CONTEXT_MENU_SMALLER = 51;
	private final static int CONTEXT_MENU_LARGER = 52;
	private final static int CONTEXT_MENU_CALIBRATE = 54;
	private final static int CONTEXT_MENU_TOGGLE_SATELLITE = 55;
	private final static int CONTEXT_MENU_SEARCH = 56;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		SubMenu subMenu = menu.addSubMenu(R.string.MENU_ADD_REMOVE);
		
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_SPEEDKPH, 0, R.string.MENU_ADD_KPH);
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_SPEEDMPH, 0, R.string.MENU_ADD_MPH);
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_SPEEDKNOTS, 0, R.string.MENU_ADD_KNOTS);
		//subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_SPEEDOLDSKOOLMPH, 0, R.string.MENU_ADD_MPH_MINI);
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_GPSINFO, 0, R.string.MENU_ADD_GPSINFO);
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_LATERALG, 0, R.string.MENU_ADD_LATG);
		subMenu.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_ACCELERATIONG, 0, R.string.MENU_ADD_ACCG);
		
		SubMenu subMenuSol = menu.addSubMenu("Soliton1");
		
		subMenuSol.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_SOLITON_CONSOLE, 0, "Start Soliton1 Web Console");

		subMenuSol.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_RPM, 0, R.string.MENU_ADD_RPM);
		subMenuSol.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_AMPSHIGH, 0, R.string.MENU_ADD_AMPS);
		subMenuSol.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_AMPSLOW, 0, R.string.MENU_ADD_LOWAMPS);
		subMenuSol.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_VOLTS, 0, R.string.MENU_ADD_VOLT);
		subMenuSol.add(SUBMENU_GROUP_ADDGAUGE, SUBMENU_ADDGAUGE_TEMP, 0, R.string.MENU_ADD_TEMP);
		

		menu.add(0, MENU_UNLOCK, 0, R.string.MENU_UNLOCK);
		menu.add(0, MENU_LOCK, 0, R.string.MENU_LOCK);
		
		
		menu.add(0, MENU_ABOUT, 0, R.string.MENU_ABOUT);
				
		menu.add(0, MENU_EXIT, 0, R.string.MENU_EXIT);

		SubMenu subMenu2 = menu.addSubMenu(R.string.MENU_SET_UNITS);
		
		subMenu2.add(SUBMENU_GROUP_UNITS, SUBMENU_UNITS_KM, 0, R.string.MENU_SET_UNITS_KM);
		subMenu2.add(SUBMENU_GROUP_UNITS, SUBMENU_UNITS_MILES, 0, R.string.MENU_SET_UNITS_MILES);
		subMenu2.add(SUBMENU_GROUP_UNITS, SUBMENU_UNITS_KNOTS, 0, R.string.MENU_SET_UNITS_KNOTS);

		menu.add(0, MENU_ENABLE_LOG, 0, R.string.MENU_LOG);
		menu.add(0, MENU_DISABLE_LOG, 0, R.string.MENU_STOPLOG);

		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.findItem(MENU_UNLOCK).setVisible(gaugesLocked);
		menu.findItem(MENU_LOCK).setVisible(!gaugesLocked);
		
		menu.findItem(MENU_DISABLE_LOG).setVisible(loggingEnabled);
		menu.findItem(MENU_ENABLE_LOG).setVisible(!loggingEnabled);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
	        
	    case SUBMENU_ADDGAUGE_RPM:
	    	addNewGauge(GaugeType.RPM);
	    	return true;
	        
	    case SUBMENU_ADDGAUGE_SPEEDKPH:
	    	addNewGauge(GaugeType.SPEED_KPH);
	    	return true;
	        
	    case SUBMENU_ADDGAUGE_SPEEDMPH:
	    	addNewGauge(GaugeType.SPEED_MPH);
	    	return true;
	    	

	    //case SUBMENU_ADDGAUGE_SPEEDOLDSKOOLMPH:
	    //	addNewGauge(GaugeType.SPEED_OLDSKOOL_MPH);
	    //	return true;
	        
	    case SUBMENU_ADDGAUGE_AMPSHIGH:
	    	addNewGauge(GaugeType.BATTERY_CURRENT_HIGH);
	    	return true;
	        
	    case SUBMENU_ADDGAUGE_AMPSLOW:
	    	addNewGauge(GaugeType.BATTERY_CURRENT_LOW);
	        return true;
	        
	    case SUBMENU_ADDGAUGE_TEMP:
	    	addNewGauge(GaugeType.CONTROLLER_TEMPERATURE);
	        return true;
	        
	    case SUBMENU_ADDGAUGE_VOLTS:
	    	addNewGauge(GaugeType.PACK_VOLTAGE);
	        return true;
	        
	    case SUBMENU_ADDGAUGE_GPSINFO:
	    	addNewGauge(GaugeType.GPS_INFO);
	        return true;
	        
	    case SUBMENU_ADDGAUGE_LATERALG:
	    	addNewGauge(GaugeType.LATERAL_G);
	        return true;
	        
	    case SUBMENU_ADDGAUGE_ACCELERATIONG:
	    	addNewGauge(GaugeType.ACCELERATION_G);
	        return true;
	        
	    case SUBMENU_UNITS_KM:
	    	setDefaultUnits(Convert.KPH);
	    	return true;
	    	
	    case SUBMENU_UNITS_MILES:
	    	setDefaultUnits(Convert.MPH);
	    	return true;
	    case SUBMENU_UNITS_KNOTS:
	    	setDefaultUnits(Convert.KNOT);
	    	return true;
	    	
	    case SUBMENU_SOLITON_CONSOLE:
	    	// Todo WebView
	    	//Intent iSol = new Intent(this, DonateWebView.class);
	    	//Bundle bSol = new Bundle();
	    	//bSol.putString("URL", "http://169.254.0.1/");
	    	//iSol.putExtras(bSol);
	    	//startActivity(iSol);
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

	    case MENU_ABOUT:
	    	showAboutDialog();
	    	return true;
//	    case MENU_HELP:
//	    	Intent ihelp = new Intent(this, DonateWebView.class);
//	    	Bundle bhelp = new Bundle();
//	    	bhelp.putString("URL", "http://www.rad.co.nz/evspeedo/help.html");
//	    	ihelp.putExtras(bhelp);
//	    	startActivity(ihelp);
//	    	return true;
	    	
	    case MENU_EXIT:
	    	//if (controller != null) {
	    	//	controller.stop();
	    	//}
	    	this.finish();
	        return true;
	    }
	    return false;
	}
    
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		contextClickHandled = true;
		if (gaugesLocked) {
			if (v instanceof AbstractGauge) {
				contextMenuGauge = (AbstractGauge)v;
			}
			if (C.D) Log.d(C.TAG, "Creating Context Menu for view " + v.toString());
			
			menu.add(0, CONTEXT_MENU_SMALLER, 0, R.string.MENU_GAUGE_SMALLER);
			menu.add(0, CONTEXT_MENU_LARGER, 0, R.string.MENU_GAUGE_BIGGER);
			menu.add(0, CONTEXT_MENU_CALIBRATE, 0, R.string.MENU_GAUGE_CALIBRATE);
			
			menu.add(0, CONTEXT_MENU_REMOVE, 0,  R.string.MENU_GAUGE_REMOVE);
		}
	}

	AbstractGauge contextMenuGauge = null;
	
	public boolean onContextItemSelected(MenuItem item) {
		//AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		if (C.D) Log.d(C.TAG, "Context menu item " + item.getItemId());
		contextClickHandled = false;
		GaugeSettings settings;
		if (contextMenuGauge == null) return true;
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
		case CONTEXT_MENU_CALIBRATE:
			contextMenuGauge.calibrate();
			return true;
			
		case CONTEXT_MENU_REMOVE:
			removeGauge(contextMenuGauge);
			return true;
						
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public void onContextMenuClosed(Menu menu) {
		contextClickHandled = false;
		super.onContextMenuClosed(menu);
	}
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

	class MyGestureDetector extends SimpleOnGestureListener {
		    @Override
		    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		    	if (C.D) Log.d(C.TAG, "Got onFling gesture");
		    	try {
		    		
		            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
		                return false;
		            // right to left swipe
		            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		                
		                if (currentPageIdx == 2) return false;
		                
		                ViewGroup old = (ViewGroup)flipper.getChildAt(currentPageIdx);
		                ViewGroup nw = (ViewGroup)flipper.getChildAt(currentPageIdx+1);
		                switchViews(old, nw);
		                
		                for (AbstractGauge g : gauges.values()) {
		                	int gIdx = g.getSettings().getViewIdx();
		                	if (gIdx == currentPageIdx) {
		                		g.passivate();
		                	}
		                }

		                for (AbstractGauge g : gauges.values()) {
		                	int gIdx = g.getSettings().getViewIdx();
		                	if (gIdx == (currentPageIdx+1)) {
		                		g.unpassivate();
		                	}
		                }

		                flipper.setInAnimation(inFromRightAnimation());
		                flipper.setOutAnimation(outToLeftAnimation());
		                currentPageIdx++;
		                flipper.showNext();
		                
		            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		                
		                if (currentPageIdx == 0) return false;
		                
		                
		                ViewGroup old = (ViewGroup)flipper.getChildAt(currentPageIdx);
		                ViewGroup nw = (ViewGroup)flipper.getChildAt(currentPageIdx-1);
		                switchViews(old, nw);
		                
		                for (AbstractGauge g : gauges.values()) {
		                	int gIdx = g.getSettings().getViewIdx();
		                	if (gIdx == currentPageIdx) {
		                		g.passivate();
		                	}
		                }
		                
		                for (AbstractGauge g : gauges.values()) {
		                	int gIdx = g.getSettings().getViewIdx();
		                	if (gIdx == (currentPageIdx-1)) {
		                		g.unpassivate();
		                	}
		                }
		                
		                flipper.setInAnimation(inFromLeftAnimation());
		                flipper.setOutAnimation(outToRightAnimation());
		                currentPageIdx--;
		                flipper.showPrevious();
		            }
		        } catch (Exception e) {
		            Log.e(C.TAG, e.getLocalizedMessage(), e);
		        }
		        return false;
		    }
		    /*
		    @Override
		    public void onLongPress(MotionEvent e) {
		    	if (contextClickHandled) {
		    		if (C.D) Log.d(C.TAG, "Long Click already handled");
		    		return;
		    	}
		    	AbsoluteLayout l;
		    	switch(currentPageIdx) {
		    	case 0:
		    		l = layoutL;
		    		break;
		    	case 1:
		    		l = layout;
		    		break;
		    	default:
		    		l = layoutR;
		    			
		    	}
		    	super.onLongPress(e);
		    }
		    */

	 }

	 private void switchViews(ViewGroup old, ViewGroup nw) {


		 if (statusLED != null) {
			 old.removeView(statusLED);
			 nw.addView(statusLED);
		 }
		 if (gpsStatusImg != null) {
			 old.removeView(gpsStatusImg);
			 nw.addView(gpsStatusImg);
		 }
		 if (loggingImg != null) {
			 old.removeView(loggingImg);
			 nw.addView(loggingImg);
		 }

		 System.gc();
		  
	 }
	 
	 public View.OnTouchListener getParentOnTouchListener() {
		 return gestureListener;
	 }

	 private Animation inFromRightAnimation() {
		 Animation inFromRight = new TranslateAnimation(
		 Animation.RELATIVE_TO_PARENT, +1.0f,
		 Animation.RELATIVE_TO_PARENT, 0.0f,
		 Animation.RELATIVE_TO_PARENT, 0.0f,
		 Animation.RELATIVE_TO_PARENT, 0.0f);
		 inFromRight.setDuration(500);
		 inFromRight.setInterpolator(new AccelerateInterpolator());
		 return inFromRight;
	 }

	 private Animation outToLeftAnimation() {
		 Animation outtoLeft = new TranslateAnimation(
		 Animation.RELATIVE_TO_PARENT, 0.0f,
		 Animation.RELATIVE_TO_PARENT, -1.0f,
		 Animation.RELATIVE_TO_PARENT, 0.0f,
		 Animation.RELATIVE_TO_PARENT, 0.0f);
		 outtoLeft.setDuration(500);
		 outtoLeft.setInterpolator(new AccelerateInterpolator());
		 return outtoLeft;
	}

	private Animation inFromLeftAnimation() {
		 Animation inFromLeft = new TranslateAnimation(
		 Animation.RELATIVE_TO_PARENT, -1.0f,
		 Animation.RELATIVE_TO_PARENT, 0.0f,
		 Animation.RELATIVE_TO_PARENT, 0.0f,
		 Animation.RELATIVE_TO_PARENT, 0.0f);
		 inFromLeft.setDuration(500);
		 inFromLeft.setInterpolator(new AccelerateInterpolator());
		 return inFromLeft;
	}

	private Animation outToRightAnimation() {
		 Animation outtoRight = new TranslateAnimation(
		 Animation.RELATIVE_TO_PARENT, 0.0f,
		 Animation.RELATIVE_TO_PARENT, +1.0f,
		 Animation.RELATIVE_TO_PARENT, 0.0f,
		 Animation.RELATIVE_TO_PARENT, 0.0f);
		 outtoRight.setDuration(500);
		 outtoRight.setInterpolator(new AccelerateInterpolator());
		 return outtoRight;
	}

	private void showAboutDialog() {
		final Dialog dialog = new Dialog(this);
    	dialog.setTitle("About EVDash 2011");
    	dialog.setCancelable(true);
    	
    	LinearLayout layout = new LinearLayout(this);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	
    	    	
    	TextView text0 = new TextView(this);
    	text0.setTextSize(14);
    	
    	// Do not modify
		text0.setText(" Originally developed by AUTO-RAD INDUSTRIES in New Zealand.\n" +
					  " This is the open source version of \"EV Speedo\" available in the Android Market.\n"); 
					  		
					  
		// Other Contributors add any about stuff here.
				
    	layout.addView(text0);
    	
    	TextView text = new TextView(this);
    	text.setTextSize(14);
		text.setText("\n\nCode License: GNU GPL v3 \n\n");
    	layout.addView(text);
    	
    	dialog.addContentView(layout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    	
    	//now that the dialog is set up, it's time to show it    
    	dialog.show();
	}
		 

}