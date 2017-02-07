package co.com.parsoniisolutions.custombottomsheetbehavior.lib.scrolltracking;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;


public class ScrollTrackingBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    public ScrollTrackingBehavior() { }
    public ScrollTrackingBehavior( Context context, AttributeSet attrs ) {
        super( context, attrs );
    }

    private WeakReference<NestedScrollingChild> mNestedScrollingChildRef;
    @Override
    public boolean onLayoutChild( CoordinatorLayout parent, V child, int layoutDirection ) {
        mNestedScrollingChildRef = new WeakReference<>( findScrollingChild( child ) );
        return true;
    }

    private NestedScrollingChild findScrollingChild( View view ) {
        if ( view instanceof NestedScrollingChild ) {
            return (NestedScrollingChild)view;
        }
        if ( view instanceof ViewGroup ) {
            ViewGroup group = (ViewGroup) view;
            for ( int i = 0, count = group.getChildCount(); i < count; i++ ) {
                NestedScrollingChild scrollingChild = findScrollingChild( group.getChildAt(i) );
                if ( scrollingChild != null ) {
                    return scrollingChild;
                }
            }
        }
        return null;
    }

    private ScrollVelocityTracker mScrollVelocityTracker = new ScrollVelocityTracker();

    @Override
    public boolean onInterceptTouchEvent( CoordinatorLayout parent, V child, MotionEvent event ) {
        if ( ! child.isShown() ) {
            return false;
        }

        int action = MotionEventCompat.getActionMasked( event );
        if ( action == MotionEvent.ACTION_DOWN ) {
            mScrollVelocityTracker.clear();
        }
        else
        if ( action == MotionEvent.ACTION_CANCEL ) {
            // We don't want to trigger a BottomSheet fling as a result of a Cancel MotionEvent (e.g., parent horizontal scroll view taking over touch events)
            mScrollVelocityTracker.clear();
        }
        return super.onInterceptTouchEvent( parent, child, event );
    }

    @Override
    public void onNestedPreScroll( CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed ) {
        NestedScrollingChild scrollingChild = mNestedScrollingChildRef.get();
        if ( target != scrollingChild ) {
            return;
        }

        mScrollVelocityTracker.recordScroll( dy );
    }

    protected float getScrollVelocity() {
        return mScrollVelocityTracker.getScrollVelocity();
    }
    protected int getTotalScrollDistancePx() {
        return mScrollVelocityTracker.getTotalScrollDistancePx();
    }
}
