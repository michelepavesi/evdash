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

import autorad.android.C;
import autorad.android.DashDisplay;
import autorad.android.R;

import autorad.android.sensor.DataType;


public class Gauge extends AbstractGauge implements AnimationListener {


	ImageView needle;
	ImageView face;

	int endAngle;
	int startAngle;
	int restAngle;
	int minDataValue;
	int maxDataValue;
	int maxNeedleSweepDistance;
	
	float lastData = -1f;
	float lastPos = 0f;
	boolean animating = false;
	
	
	Queue<RotateAnimation> animationQueue = new LinkedList<RotateAnimation>();
	
	Animation currentAnimation;
	
	
	public Gauge(DashDisplay ctx, GaugeSettings gaugeSettings) {
		super(ctx);
		this.ctx = ctx;
		this.gaugeSettings = gaugeSettings;
		this.gaugeType = gaugeSettings.getGaugeType();
		this.details = gaugeType.getGaugeDetails();
		this.dataTypes = details.getDataTypes();
		
		this.maxNeedleSweepDistance = details.getEndAngle() - details.getRestAngle();
		this.minDataValue = details.getMinDataValue();
		this.maxDataValue = details.getMaxDataValue();
		this.startAngle = details.getStartAngle();
		this.endAngle = details.getEndAngle();
		this.restAngle = details.getRestAngle();

		this.setTag(gaugeSettings);
		
		face = new ImageView(ctx);
		needle = new ImageView(ctx);
		
		// System.gc();
		
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
		case SPEED_OLDSKOOL_MPH:
			face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.minispeed400));
			initNeedle(R.drawable.minispeedneedle400);
			
			break;
			
		case LATERAL_G:
			face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.lateralg200));
			initNeedle(R.drawable.needlevolt200);
			
			break;
		
		case ACCELERATION_G:
			face.setImageDrawable(ctx.getResources().getDrawable(R.drawable.accelerationg200));
			initNeedle(R.drawable.needlevolt200);
			
			break;
		}

		
		face.setLongClickable(true);
		//face.setOnLongClickListener(new OnLongClickListener() {
        //    public boolean onLongClick(View v) {
                
        //        return false;
        //    }
		//});
		
		face.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent event) {
				onGaugeMotionEvent(event);
				return false;
			}
		});
		
		applySettings();
		
		this.addView(face);
		this.addView(needle);
		
		rotateNeedle(details.getRestAngle(), 0);
	}
	
	public void cleanup() {
		
		ctx = null;
		currentAnimation = null;
		animationQueue.clear();
		animationQueue = null;
		face.setOnTouchListener(null);
		face.setImageDrawable(null);
		needle.setImageDrawable(null);
		needle = null;
		face = null;
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
	
	
	
	public void applySettings() {
		switch (gaugeSettings.getSize()) {
		case TINY:
			size = 100;
			break;
		case VERY_SMALL:
			size = 150;
			break;
		case SMALL:
			size = 200;
			break;
		case MEDIUM:
			size = 300;
			break;
		case LARGE:
			size = 400;
			break;
		}

		LayoutParams params = new LayoutParams(size, size);
		params.setMargins(0, 0, 0, 0);
		face.setLayoutParams(params);
		needle.setLayoutParams(params);
		rotateNeedle(1, 1);
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
		if (C.D) Log.d(C.TAG, "Full sweep reset requested");
		int angleRotation = details.getEndAngle() - (int)lastPos;
		//int angleBackRotation = details.getEndAngle() - details.getRestAngle();
		
		rotateNeedle(angleRotation, 1000);
		rotateNeedleToPoint(restAngle, 1500);
		lastData = 0;
	}
	
	public void calibrate() {
		int increment;
		switch (gaugeType) {
		case RPM:
			increment = 8;
			break;
		case BATTERY_CURRENT_LOW: case SPEED_KPH:
			increment = 9;
			break;
		default:
			increment = 10;
		}
		
		int incrementValue = (maxDataValue - minDataValue)/increment;
		for (int i = 0; i<increment; ) {
			float d1 = ((float)(incrementValue*++i))/maxDataValue;
			float angle = startAngle + (d1 * (maxNeedleSweepDistance));
			if (C.D) Log.d(C.TAG, "Calibrate: Data=" + (incrementValue*i) + " angle=" + angle);
			rotateNeedleToPoint(angle,1000);
		}
		rotateNeedleToPoint(restAngle,1500);
		
	}
	
    private boolean rotateNeedle(int degrees, int duration) {

    	if (C.D) Log.d(C.TAG, "Rotating=" + degrees + " lastPos=" + lastPos + " duration =  " + duration);
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
        	if (C.D) Log.d("EVDASH", "queued animation");
        	animationQueue.add(d);
        }
        
        return true;
    }

    private boolean rotateNeedleToPoint(float toDegrees, int duration) {

    	// Work out a duration for this animation
    	float distance = toDegrees - lastPos;
    	if (duration == 0) {
    		duration = (int)(2000 * distance/maxNeedleSweepDistance);
    	}
    	if (duration < 0) {
    		duration *= -1;
    	}
    	
    	if (C.D) Log.d(C.TAG, "Rotating To=" + toDegrees + " lastPos=" + lastPos + "duration =  " + duration);
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
        	if (C.D) Log.d(C.TAG, "queued animation");
        	animationQueue.add(d);
        }
        
        return true;
    }

    
	public void onAnimationEnd(Animation arg0) {	
		//animating = false;
		if (!animationQueue.isEmpty()) {
			currentAnimation = animationQueue.remove();
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
		ctx.getHandler().post(new Runnable() {
			public void run() {
				if (currentAnimation != null) {
					needle.startAnimation(currentAnimation);
				}
			}
		});
	}

	public void onAnimationRepeat(Animation arg0) {	
	}

	public void onAnimationStart(Animation arg0) {
		animating = true;
	}
	
	public void onData(DataType type, float... data) {
		//if (C.D) Log.d("EVDASH", "Gauge Received Data:" + data);
		float d = data[0];
		if (lastData == d) return;
		
		
		if (d < minDataValue) {
			if (C.D) Log.d(C.TAG, "Gauge Data Below Minimum");
			rotateNeedleToPoint(startAngle,0);	
		} else if (d > maxDataValue) {
			if (C.D) Log.d(C.TAG, "Gauge Data Above Maximum ");
			return;
			//rotateNeedleToPoint(endAngle,0);
		} else {
			
			float d1 = d/maxDataValue;
			if (C.D) Log.d(C.TAG, "data=" + data + " maxDataV=" + maxDataValue + " d/md=" + d1 );
			
			float angle = restAngle + (d1 * (maxNeedleSweepDistance));
			
			if (C.D) Log.d(C.TAG, "Gauge rotate to angle " + angle);
			rotateNeedleToPoint(angle,0); 
		}
		lastData = d;
		
	}
	

	protected void onGaugeMotionEvent(MotionEvent event) {
		ctx.onGaugeMotionEvent(this, event);
	}

	


	
}
