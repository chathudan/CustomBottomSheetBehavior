package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import android.support.v4.view.ViewPager;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetViewPager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED;
import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN;


/**
 * Delays BottomSheetPageWithLoading onLoadFinished until ViewPager is settled, and map has finished animating,
 * to avoid hiccup during swipes and hiccup in map camera animation.
 * This class should only be accessed from the UI thread.
 */
public class ViewPagerContentRefresher {

    private Map<BottomSheetPageWithLoading, BottomSheetData> mQueue = new HashMap<>();
    private WeakReference<BottomSheetViewPager> mBottomSheetViewPager = null;
    private int mViewPagerState = ViewPager.SCROLL_STATE_IDLE;

    public ViewPagerContentRefresher( BottomSheetViewPager bottomSheetViewPager ) {
        registerViewPager( bottomSheetViewPager );
        registerEventBus();
    }

    private void registerEventBus() {
        try {
            EventBus.getDefault().register( this );
        } catch ( EventBusException e ) {
            e.printStackTrace();
        }
    }

    private EventMapCameraState.State mMapCameraState = EventMapCameraState.State.FINISH; // Default is no camera animation in progress
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onEvent( EventMapCameraState ev ) {
        mMapCameraState = ev.state();
        if ( ev.state() == EventMapCameraState.State.CANCEL  ||  ev.state() == EventMapCameraState.State.FINISH ) {
            if ( canEmptyQueue() ) {
                emptyQueue();
            }
        }
    }

    private void registerViewPager( BottomSheetViewPager viewPager ) {
        mBottomSheetViewPager = new WeakReference<>( viewPager );

        viewPager.addOnPageChangeListener( new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged( int state ) {
                mViewPagerState = state;

                if ( state == ViewPager.SCROLL_STATE_IDLE ) {
                    if ( canEmptyQueue() ) {
                        emptyQueue();
                    }
                }
            }
            @Override
            public void onPageSelected( int position ) { }
            @Override
            public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels ) { }
        } );
    }

    // ViewPager is now idle, let's empty the queue
    private void emptyQueue() {
        for ( Map.Entry<BottomSheetPageWithLoading, BottomSheetData> e : mQueue.entrySet() ) {
            BottomSheetPageWithLoading bottomSheetPage = e.getKey();
            BottomSheetData bottomSheetData = e.getValue();

            if ( bottomSheetPage != null ) {
                bottomSheetPage.setUI( bottomSheetData );
            }
        }
        mQueue.clear();
    }

    public void addTask( BottomSheetPageWithLoading bottomSheetPage, BottomSheetData bottomSheetData ) {
        if ( mBottomSheetViewPager.get() == null )
            return;

        if ( canEmptyQueue() ) {
            // Execute it immediately
            bottomSheetPage.setUI( bottomSheetData );
        }
        else {
            // Queue it for later
            mQueue.put( bottomSheetPage, bottomSheetData );
        }
    }

    private boolean canEmptyQueue() {
        if ( mViewPagerState != ViewPager.SCROLL_STATE_IDLE ) {
            return false;
        }

        if ( mBottomSheetViewPager.get() == null )
            return false;

        int bottomSheetState = mBottomSheetViewPager.get().bottomSheetState();
        if ( isMapVisiblyAnimating( bottomSheetState ) ) {
            return false;
        }

        return true;
    }

    // The map has to be visible, and camera animating
    private boolean isMapVisiblyAnimating( int bottomSheetState ) {
        if ( bottomSheetState == STATE_COLLAPSED  ||  bottomSheetState == STATE_HIDDEN ) {
            if ( mMapCameraState == EventMapCameraState.State.START ) {
                return true;
            }
        }

        return false;
    }

}