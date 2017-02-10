package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.EventBottomSheetState;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.EventViewPagerPageSelected;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.EventViewPagerScrollStateChanged;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class BottomSheetViewPager extends ViewPager {

    public BottomSheetViewPager( Context context ) { this( context, null ); }
    public BottomSheetViewPager( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init();
    }

    /**
     * Since we cache mTopOfCollapsedSheetY, we need to update it if Configuration changes
     * @param configuration
     */
    @Override
    public void onConfigurationChanged( Configuration configuration ) {
        super.onConfigurationChanged( configuration );

        int peekHeight   = (int)getContext().getResources().getDimension( R.dimen.bottom_sheet_peek_height );
        int screenHeight = (int)(configuration.screenHeightDp * getContext().getResources().getDisplayMetrics().density);
        mTopOfCollapsedSheetY = screenHeight - peekHeight;
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels ) { }
        @Override
        public void onPageSelected( int position ) { EventBus.getDefault().post( new EventViewPagerPageSelected( position ) ); }
        @Override
        public void onPageScrollStateChanged( int state ) { EventBus.getDefault().post( new EventViewPagerScrollStateChanged( state ) );}
    };

    private int mTopOfCollapsedSheetY;
    private void init() {
        int peekHeight   = (int)getContext().getResources().getDimension( R.dimen.bottom_sheet_peek_height );
        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        mTopOfCollapsedSheetY = screenHeight - peekHeight;

        addOnPageChangeListener( mOnPageChangeListener );
        registerEventBus();
    }

    private void registerEventBus() {
        try {
            EventBus.getDefault().register( this );
        } catch ( EventBusException e ) {
            e.printStackTrace();
        }
    }

    private boolean mIgnoreEvents = false;
    @Override
    public boolean onInterceptTouchEvent( MotionEvent ev ) {

        int action = MotionEventCompat.getActionMasked( ev );
        if ( action == MotionEvent.ACTION_DOWN ) {

            int state = bottomSheetState();
            // Is it hidden?
            if ( state == BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN ) {
                mIgnoreEvents = true;
            }
            // Is it collapsed?
            else
            if ( state == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED ) {
                // Is the touch inside the peekHeight?
                int y = (int) ev.getY();
                if ( y < mTopOfCollapsedSheetY ) {
                    mIgnoreEvents = true;
                }
                else {
                    mIgnoreEvents = false;
                }
            }
            else {
                mIgnoreEvents = false;
            }
        }

        if ( mIgnoreEvents ) {
            return true; // Intercept the touch, child viewpager will not receive onTouch
        }
        else {
            return super.onInterceptTouchEvent( ev );
        }
    }

    @Override
    public boolean onTouchEvent( MotionEvent ev ) {
        if ( mIgnoreEvents ) {
            return false;    // Return false, we did not handle the touch, let parent googlemap handle it
        }
        else {
            return super.onTouchEvent( ev );
        }
    }

    /**
     * Current state, which might include STATE_DRAGGING and STATE_SETTLING
     */
    private int mBottomSheetState       = BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED;

    /**
     * The current stable state, or of STATE_DRAGGING then the last stable state, or if STATE_SETTLING then the state settling TO
     */
    private int mTargetBottomSheetState = BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED;

    public void setBottomSheetState( int state, boolean noanim ) {
        mBottomSheetState = state;
        mTargetBottomSheetState = state;

        //EventBus.getDefault().post( new EventBottomSheetState( state ) );

        BottomSheetPage bottomSheetPage = selectedBottomSheetPage();
        if ( bottomSheetPage != null ) {
            bottomSheetPage.setBottomSheetState( state, noanim );
        }
    }

    public int bottomSheetState() {
        return mBottomSheetState;
    }
    public int targetBottomSheetState() {
        return mTargetBottomSheetState;
    }

    private @Nullable BottomSheetPage selectedBottomSheetPage() {
        BottomSheetPagerAdapter adp = (BottomSheetPagerAdapter) getAdapter();
        int pos = getCurrentItem();
        return adp.getBottomSheetAtPosition( pos );
    }

    @Subscribe( sticky = false, threadMode = ThreadMode.MAIN )
    public void onEvent( EventViewPagerPageSelected ev ) {
    }


    // Only the page the user is interacting with will broadcast this event
    @Subscribe( sticky = false, threadMode = ThreadMode.MAIN )
    public void onEvent( EventBottomSheetState ev ) {
        mBottomSheetState = ev.state();

        if ( ev.state() == BottomSheetBehaviorGoogleMapsLike.STATE_SETTLING ) {
            mTargetBottomSheetState = ev.targetState();
        }
        else
        if ( ev.state() == BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING ) {
            // Stay at the last state
        }
        else {
            mTargetBottomSheetState = ev.state();
        }

        // Do not mirror transient states like SETTLING and DRAGGING
        if ( BottomSheetBehaviorGoogleMapsLike.isStateStable( ev.state() ) ) {
            mirrorBottomSheetStates( ev.state(), ev.bottomSheetPageRef() != null ? ev.bottomSheetPageRef().get() : null );
        }
    }

    private void mirrorBottomSheetStates( int newState, BottomSheetPage sourcePage ) {
        // Iterate over all instantiated views, skip the source bottomsheepage when mirroring
        BottomSheetPagerAdapter adp = (BottomSheetPagerAdapter) getAdapter();

        for ( int i = 0, size = adp.allViews().size(); i < size; ++i ) {
            View view = adp.allViews().valueAt( i );
            BottomSheetPage bsp = (BottomSheetPage) view.getTag( R.id.BOTTOM_SHEET_PAGE );
            if ( bsp == null )
                continue;
            if ( bsp.equals( sourcePage ) ) {
                continue;
            }

            if ( !(newState == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED    ||
                   newState == BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT ||
                   newState == BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED     ||
                   newState == BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN )
                    )
                continue;

            bsp.setBottomSheetState( newState, true );
        }
    }
}