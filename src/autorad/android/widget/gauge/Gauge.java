package autorad.android.widget.gauge;


import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;

import android.widget.ImageView;
import android.widget.Toast;

import autorad.android.C;
import autorad.android.DashDisplay;
import autorad.android.R;

import autorad.android.sensor.DataType;



public class Gauge extends AbstractGauge implements AnimationListener {

	String TAG;

	ImageView needle;
	ImageView face;

	int endAngle;
	int startAngle;
	int restAngle;
	int minDataValue;
	int maxDataValue;
	int maxNeedleSweepDistance;
	
	boolean recalibrate;
	float calibrationOffset = 0f;
	
	float lastData = -1f;
	float lastPos = 0f;
	boolean animating = false;
	
	boolean passivated;
	
	Queue<RotateAnimation> animationQueue = new LinkedList<RotateAnimation>();
	
	Animation currentAnimation;
	
	
	public Gauge(DashDisplay ctx, GaugeSettings gaugeSettings) {
		super(ctx);
		this.ctx = ctx;
		this.gaugeSettings = gaugeSettings;
		this.gaugeType = gaugeSettings.getGaugeType();
		this.details = gaugeType.getGaugeDetails();
		this.dataTypes = details.getDataTypes();
		this.TAG = gaugeType.name();
		
		this.maxNeedleSweepDistance = details.getEndAngle() - details.getRestAngle();
		this.minDataValue = details.getMinDataValue();
		this.maxDataValue = details.getMaxDataValue();
		this.startAngle = details.getStartAngle();
		this.endAngle = details.getEndAngle();
		this.restAngle = details.getRestAngle();

		this.setTag(gaugeSettings);
		passivated = true;
		//if (!passive) {
		//	initImageViews();
		//	applySettings();
		//	this.addView(face);
		//	this.addView(needle);
		//	rotateNeedle(details.getRestAngle(), 0);
		//}		
	}
	
	public void passivate() {
		if (passivated) return;
		passivated = true;
		Log.d(TAG, "passivate " + getSettings().getGaugeType());
		animationQueue.clear();
		currentAnimation = null;
		
		if (face != null) {
			removeView(face);
			face.setOnTouchListener(null);
			face.setImageDrawable(null);
			face = null;
		}
		if (needle != null) {
			removeView(needle);
			needle.setImageDrawable(null);
			needle = null;
		}
	}
	
	public void unpassivate() {
		Log.d(TAG, "unpassivate " + getSettings().getGaugeType());
		if (!passivated) return;
		initImageViews();
		applySettings();
		addView(face);
		addView(needle);
		
		passivated = false;
		currentAnimation = null;
		animationQueue.clear();
		fullSweepReset();
		Log.d(TAG, "unpassivated " + getSettings().getGaugeType());
	}
	
	public void destroy() {
		
		ctx = null;
		currentAnimation = null;
		if (animationQueue != null) {
			animationQueue.clear();
			animationQueue = null;
		}
		if (face != null) {
			removeView(face);
			face.setOnTouchListener(null);
			face.setImageDrawable(null);
			face = null;
		}
		if (needle != null) {
			removeView(needle);
			needle.setImageDrawable(null);
			needle = null;
		}
		
		gaugeType = null;
		dataTypes = null;
		gaugeSettings= null;
		details = null;
		
		
	}
	
	public String getToastString() {
		if (lastData == -1) {
			return null;
		} else {
			return lastData + " " + details.getUnits();
		}
	}
	
	private void initImageViews() {
		if (face != null) return;
		face = new ImageView(ctx);
		needle = new ImageView(ctx);
		try {
			switch (gaugeType) {
			
			case RPM:
				face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.rpm400));
				initNeedle(R.drawable.needle400);
				
				break;
				
			case PACK_VOLTAGE:
				face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.batteryvolt200));
				initNeedle(R.drawable.needlevolt200);
				
				break;
				
			case CONTROLLER_TEMPERATURE:
				face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.temp200));
				initNeedle(R.drawable.needlevolt200);
				
				break;
				
			case BATTERY_CURRENT_LOW:
				face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.amps400));
				initNeedle(R.drawable.needle400);
				break;
				
			case BATTERY_CURRENT_HIGH:
				face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.highamps400));
				initNeedle(R.drawable.needle400);
				
				break;
			
			case SPEED_KPH:
				face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.kmph400));
				initNeedle(R.drawable.needle400);
				
				break;
			case SPEED_MPH:
				face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.mph400));
				initNeedle(R.drawable.needle400);
				
				break;
			//case SPEED_OLDSKOOL_MPH:
			//	face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.minispeed400));
			//	initNeedle(R.drawable.minispeedneedle400);
				
			//	break;

				
			case LATERAL_G:
				face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.lateralg200));
				initNeedle(R.drawable.needlevolt200);
				
				break;
			
			case ACCELERATION_G:
				face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.accelerationg200));
				initNeedle(R.drawable.needlevolt200);
				
				break;
			}
		} catch (OutOfMemoryError oo) {
    		Toast.makeText(ctx, R.string.TOAST_TOO_MANY_GAUGES, Toast.LENGTH_SHORT).show();
    		return;
    	}
		

		face.setLongClickable(true);		
		face.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent event) {
				Gauge.this.ctx.getParentOnTouchListener().onTouch(arg0, event);
				
				onGaugeMotionEvent(event);
				
				return false;
			}
		});

	}
	
	public void applySettings() {
		if (face == null) return;
		float SCALE = ctx.getScale();
		switch (gaugeSettings.getSize()) {
		case TINY:
			size = (int)(67*SCALE);
			break;
		case TINY1:
			size = (int)(100*SCALE);
			break;
		case VERY_SMALL:
			size = (int)(150*SCALE);
			break;
		case VERY_SMALL1:
			size = (int)(200*SCALE);
			break;
		case SMALL:
			size = (int)(250*SCALE);
			break;
		case SMALL1:
			size = (int)(275*SCALE);
			break;
		case MEDIUM:
			size = (int)(300*SCALE);
			break;
		case MEDIUM1:
			size = (int)(325*SCALE);
			break;
		case LARGE:
			size = (int)(350*SCALE); //400;
			break;
		case LARGE1:
			size = (int)(375*SCALE); //400;
			break;
		case LARGE2:
			size = (int)(400*SCALE);
			break;
		case VERYLARGE:
			size = (int)(440*SCALE);
			break;
		case VERYLARGE1:
			size = (int)(480*SCALE);
			break;
		}

		if (C.D) Log.d("EVDASH","Apply Settings Scale=" + SCALE + " Final Size=" + size);
		
		LayoutParams params = new LayoutParams(size, size);
		params.setMargins(0, 0, 0, 0);
		face.setLayoutParams(params);
		face.setAdjustViewBounds(true);
		needle.setLayoutParams(params);
		needle.setAdjustViewBounds(true);
		rotateNeedleToPoint(restAngle,500);
		lastData = -1f;
		
	}

	public void initNeedle(int drawableId) {
		
		//Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableId);
		//needle.setImageDrawable(new BitmapDrawable(bitmap));

		needle.setImageDrawable(ctx.getResources().getDrawable(drawableId));	
		
		//LayoutParams params = new LayoutParams(size, size);
		//params.setMargins(0, 0, 0, 0);
		//needle.setLayoutParams(params);
		
		
	}
	
	public void fullSweepReset() {
		Log.d(TAG, "Full sweep reset requested");
		int angleRotation = details.getEndAngle() - (int)lastPos;
		//int angleBackRotation = details.getEndAngle() - details.getRestAngle();
		
		rotateNeedle(angleRotation, 1000);
		rotateNeedleToPoint(restAngle, 1500);
		
	}
	
	public void calibrate() {
		//int increment;
		switch (gaugeType) {
		case LATERAL_G:
			recalibrate = true;
			return;
		case ACCELERATION_G:
			recalibrate = true;
			return;
		case RPM:
			//increment = 8;
			break;
		case BATTERY_CURRENT_LOW: case SPEED_KPH:
			//increment = 9;
			break;
		default:
			//increment = 10;
		}
		
//		int incrementValue = (maxDataValue - minDataValue)/increment;
//		for (int i = 0; i<increment; ) {
//			float d1 = ((float)(incrementValue*++i))/maxDataValue;
//			float angle = startAngle + (d1 * (maxNeedleSweepDistance));
//			if (C.D) Log.d(TAG, "Calibrate: Data=" + (incrementValue*i) + " angle=" + angle);
//			rotateNeedleToPoint(angle,1000);
//		}
//		rotateNeedleToPoint(restAngle,1500);
		fullSweepReset();
	}
	
    private boolean rotateNeedle(int degrees, int duration) {

    	if (C.D) Log.d(TAG, "Rotating=" + degrees + " lastPos=" + lastPos + " duration =  " + duration);
    	float nextPos = lastPos+degrees;
        RotateAnimation d = new RotateAnimation(lastPos, nextPos, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
        d.setDuration(duration);
        d.setAnimationListener(this);
        d.setFillAfter(true);
        
        lastPos = nextPos;
        
        if (currentAnimation == null) {
        	//animating = true;
        	//needle.startAnimation(d);
        	currentAnimation = d;
        	runCurrentAnimationOnUiThread();
        } else {
        	if (C.D) Log.d(TAG, "queued animation");
        	animationQueue.add(d);
        }
        
        return true;
    }

    private boolean rotateNeedleToPoint(float toDegrees, int duration) {

    	// Work out a duration for this animation
    	float distance = toDegrees - lastPos;
    	if (duration == 0) {
    		duration = (int)(1000 * distance/maxNeedleSweepDistance);
    	}
    	if (duration < 0) {
    		duration *= -1;
    	}
    	
    	if (C.D) Log.d(TAG, "Rotating To=" + toDegrees + " lastPos=" + lastPos + "duration =  " + duration);
        RotateAnimation d = new RotateAnimation(lastPos, toDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
        d.setDuration(duration);
        d.setAnimationListener(this);
        d.setFillAfter(true);
        
        lastPos = toDegrees;
        
        if (currentAnimation == null) {
        	//animating = true;
        	//needle.startAnimation(d);
        	currentAnimation = d;
        	runCurrentAnimationOnUiThread();
        } else {
        	if (C.D) Log.d(TAG, "queued animation");
        	animationQueue.add(d);
        }
        
        return true;
    }

    
	public void onAnimationEnd(Animation arg0) {	
		//animating = false;
		if (!animationQueue.isEmpty()) {
			currentAnimation = animationQueue.remove();
			if (animationQueue.size() > 5) {
				currentAnimation.setDuration(currentAnimation.getDuration()/50);
			} else if (animationQueue.size() > 2) {
				currentAnimation.setDuration(currentAnimation.getDuration()/15);
			}
			runCurrentAnimationOnUiThread();
			//ctx.getHandler().post(new Runnable() {
			//	public void run() {
			//		needle.startAnimation(animationQueue.remove());
			//	}
			//});
		} else {
			currentAnimation = null;
		}
	}
	
	private void runCurrentAnimationOnUiThread() {
		if (ctx != null) {
			ctx.getHandler().post(new Runnable() {
				public void run() {
					if (currentAnimation != null) {
						if (!passivated && (needle != null)) {
							needle.startAnimation(currentAnimation);
						}
					}
				}
			});
		}
	}

	public void onAnimationRepeat(Animation arg0) {	
	}

	public void onAnimationStart(Animation arg0) {
		animating = true;
	}
	
	public void onData(DataType type, float... data) {
		if (passivated) return;
		//
		float d = Math.round(data[0]*10)/10;
		//if (C.D) Log.d(TAG, "onData:" + d);
		if (recalibrate) {
			switch (gaugeType) {
			case LATERAL_G: case ACCELERATION_G:
				calibrationOffset = -1*d;
				recalibrate = false;
				break;
			}
		}
		d += calibrationOffset;
		if (lastData == d) return;
		
		
		
		if (d < minDataValue) {
			if (C.D) Log.d(TAG, "Gauge Data Below Minimum");
			rotateNeedleToPoint(startAngle,0);	
		} else if (d > maxDataValue) {
			if (C.D) Log.d(TAG, "Gauge Data Above Maximum ");
			return;
			//rotateNeedleToPoint(endAngle,0);
		} else {
			
			float d1 = d/maxDataValue;
			if (C.D) Log.d(TAG, "data=" + data + " maxDataV=" + maxDataValue + " d/md=" + d1 );
			
			float angle = restAngle + (d1 * (maxNeedleSweepDistance));
			
			if (C.D) Log.d(TAG, "Gauge rotate to angle " + angle);
			rotateNeedleToPoint(angle,0); 
		}
		lastData = d;
		
	}
	

	protected void onGaugeMotionEvent(MotionEvent event) {
		ctx.onGaugeMotionEvent(this, event);
	}


	
}
