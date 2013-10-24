package org.raegdan.bbstalker;

/*/ 
 * Based on the code by @Marek Sebera from StackOverflow (http://stackoverflow.com/users/492624/marek-sebera) 
/*/

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ActivitySwipeDetector implements View.OnTouchListener {

    static final String logTag = "ActivitySwipeDetector";
    private SwipeInterface activity;
    static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;
    
    public static final int DIRECTION_UP = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;
    public static final int DIRECTION_RIGHT = 4;

    public ActivitySwipeDetector(SwipeInterface activity){
        this.activity = activity;
    }

    public void onRightToLeftSwipe(View v){
        activity.onSwipe(v, DIRECTION_LEFT);
    }

    public void onLeftToRightSwipe(View v){
        activity.onSwipe(v, DIRECTION_RIGHT);
    }

    public void onTopToBottomSwipe(View v){
        activity.onSwipe(v, DIRECTION_DOWN);
    }

    public void onBottomToTopSwipe(View v){
        activity.onSwipe(v, DIRECTION_UP);
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()) {
	        case MotionEvent.ACTION_DOWN: {
	            downX = event.getX();
	            downY = event.getY();
	            return true;
	        }
	        case MotionEvent.ACTION_UP: {
	            upX = event.getX();
	            upY = event.getY();
	
	            float deltaX = downX - upX;
	            float deltaY = downY - upY;
	
	            // swipe horizontal?
	            if (Math.abs(deltaX) > MIN_DISTANCE) {
	                // left or right
	                if (deltaX < 0) { this.onLeftToRightSwipe(v); return true; }
	                if (deltaX > 0) { this.onRightToLeftSwipe(v); return true; }
	            }
	            else {
	                Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
	            }
	
	            // swipe vertical?
	            if (Math.abs(deltaY) > MIN_DISTANCE) {
	                // top or down
	                if (deltaY < 0) { this.onTopToBottomSwipe(v); return true; }
	                if (deltaY > 0) { this.onBottomToTopSwipe(v); return true; }
	            }
	            else {
	                v.performClick();
	            }
	        }
        }
        return false;
    }
}