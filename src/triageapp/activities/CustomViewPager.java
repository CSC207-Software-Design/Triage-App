package triageapp.activities;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * A Custom ViewPager that has the additional functionality of 
 * disabling or enabling the swipe action between pages on
 * the current Activity screen.
 */
public class CustomViewPager extends ViewPager {

	/** Whether this CustomViewPager is enabling the swipe action on the screen. */
	private boolean isSwipeEnabled;

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.isSwipeEnabled = true; //initializes to swipeable screen.
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (this.isSwipeEnabled) {
			return super.onTouchEvent(event);
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (this.isSwipeEnabled) {
			return super.onInterceptTouchEvent(event);
		}
		return false;
	}

	/**
	 * Custom method to enable or disable swipe.
	 * @param isSwipeEnabled true to enable swipe, false otherwise.
	 */
	public void setPagingEnabled(boolean isSwipeEnabled) {
		this.isSwipeEnabled = isSwipeEnabled;
	}
}