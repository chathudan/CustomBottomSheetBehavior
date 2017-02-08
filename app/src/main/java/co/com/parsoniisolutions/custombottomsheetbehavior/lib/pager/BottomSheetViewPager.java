package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.Vector;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.BottomSheetBehaviorGoogleMapsLike;


public class BottomSheetViewPager extends ViewPager {

    public BottomSheetViewPager( Context context ) {
        this( context, null );
    }

    public BottomSheetViewPager( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init();
    }

    /**
     *
     */
    private int mTopOfCollapsedSheetY;
    private void init() {
        int peekHeight   = (int)getContext().getResources().getDimension( R.dimen.bottom_sheet_peek_height );
        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        mTopOfCollapsedSheetY = screenHeight - peekHeight;

        addOnPageChangeListener( mOnPageChangeListener );
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageSelected( final int position ) {
            // Scroll the map and show the infowindow for this point
            BottomSheetPagerAdapter adapter = (BottomSheetPagerAdapter)getAdapter();
            adapter.onPageSelected( position );
        }

        @Override
        public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels ) { }
        @Override
        public void onPageScrollStateChanged( int state ) { }
    };

    @Override
    public boolean onInterceptTouchEvent( MotionEvent ev ) {

        int state = bottomSheetState();

        // Is it hidden?
        if ( state == BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN ) {
            return false;
        }

        // Is it collapsed?
        if ( state == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED ) {
            // Is the touch inside the peekHeight?
            int action = MotionEventCompat.getActionMasked( ev );
            if ( action == MotionEvent.ACTION_DOWN ) {
                int y     = (int) ev.getY();
                if ( y > mTopOfCollapsedSheetY ) {
                    return super.onInterceptTouchEvent( ev );
                }
            }
            else {
                // Do not intercept touch, let the google map underneath handle it
                return super.onInterceptTouchEvent( ev );
            }
        }

        return super.onInterceptTouchEvent( ev );
    }

    @Override
    public boolean onTouchEvent( MotionEvent ev ) {

        int state = bottomSheetState();
        // Is it hidden? If yes, pass on touches to map underneath
        if ( state == BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN ) {
            return false;
        }

        // Is it collapsed? Pass on touches above the peekheight to map underneath
        if ( state == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED ) {
            // Is the touch inside the peekHeight?
            int action = MotionEventCompat.getActionMasked( ev );
            if ( action == MotionEvent.ACTION_DOWN ) {
                int y     = (int) ev.getY();

                if ( y > mTopOfCollapsedSheetY ) {
                    return super.onTouchEvent( ev );
                }
                else {
                    return false;
                }
            }
            else {
                // Do not intercept touch, let the google map underneath handle it
                return super.onTouchEvent( ev );
            }
        }

        return super.onTouchEvent( ev );
    }

    private int mBottomSheetState = BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED;
    public void setBottomSheetState( int state, boolean noanim ) {
        mBottomSheetState = state;

        BottomSheetPage bottomSheetPage = selectedBottomSheetPage();
        if ( bottomSheetPage != null ) {
            bottomSheetPage.setBottomSheetState( state, noanim );
        }
    }

    public int bottomSheetState() {
        BottomSheetPage bottomSheetPage = selectedBottomSheetPage();
        if ( bottomSheetPage != null ) {
            return bottomSheetPage.getBottomSheetState();
        }

        return mBottomSheetState;
    }

    private @Nullable BottomSheetPage selectedBottomSheetPage() {
        BottomSheetPagerAdapter adp = (BottomSheetPagerAdapter) getAdapter();
        int pos = getCurrentItem();
        return adp.getBottomSheetAtPosition( pos );
    }

    public void addBottomSheetCallback( BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback bottomSheetCallback ) {
        BottomSheetPagerAdapter adp = (BottomSheetPagerAdapter) getAdapter();
        adp.addBottomSheetCallback( bottomSheetCallback );
    }

}