package org.codepond.wizardroid.layouts;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by uniqa on 10/23/14.
 */
public class ScrollableViewPager extends ViewPager {
    boolean xScored = false, yScored = false;

    public ScrollableViewPager(Context context) {
        super(context);
    }

    public ScrollableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            xScored = false;
            yScored = false;
            requestDisallowInterceptTouchEvent(false);
        }
        return super.onTouchEvent(ev);
    }

    GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!yScored && Math.abs(distanceX) > Math.abs(distanceY)) {
                xScored = true;
                yScored = false;
            }
            if (xScored) {
                requestDisallowInterceptTouchEvent(true);
            } else if (!xScored && Math.abs(distanceY) > Math.abs(distanceX)) {
                xScored = false;
                yScored = true;
                requestDisallowInterceptTouchEvent(false);
            }
            return true;
        }

    };
    final GestureDetector mGestureDetector = new GestureDetector(getContext(), mOnGestureListener);
}
