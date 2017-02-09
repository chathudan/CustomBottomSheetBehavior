package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import android.support.v4.view.ViewPager;
import android.util.Log;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED;
import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING;
import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN;
import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_SETTLING;


/**
 * Delays BottomSheetPageWithLoading onLoadFinished until ViewPager is settled, and map has finished animating,
 * to avoid hiccup during swipes and hiccup in map camera animation.
 * This class should only be accessed from the UI thread.
 */
public class ViewPagerContentRefresher {

    private Map<BottomSheetPageWithLoading, BottomSheetData> mQueue = new HashMap<>();
    private int mViewPagerState   = ViewPager.SCROLL_STATE_IDLE;
    private int mBottomSheetState = STATE_COLLAPSED;

    public ViewPagerContentRefresher() {
        registerEventBus();
    }

    private void registerEventBus() {
        try {
            EventBus.getDefault().register( this );
        } catch ( EventBusException e ) {
            e.printStackTrace();
        }
    }

    // Do not set the UI when the map is visible and moving
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

    // Do not set the UI when the view pager is sliding
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onEvent( EventViewPagerScrollStateChanged ev ) {
        mViewPagerState = ev.state();
        if ( ev.state() == ViewPager.SCROLL_STATE_IDLE ) {
            if ( canEmptyQueue() ) {
                emptyQueue();
            }
        }
    }

    // Do not set the UI when the BottomSheet is moving
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onEvent( EventBottomSheetState ev ) {
        mBottomSheetState = ev.state();
        if ( ev.state() != STATE_DRAGGING  &&  ev.state() != STATE_SETTLING ) {
            if ( canEmptyQueue() ) {
                emptyQueue();
            }
        }
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
        Log.e("e","Adding task");

        if ( canEmptyQueue() ) {
            // Execute it immediately
            bottomSheetPage.setUI( bottomSheetData );
        }
        else {
            // Queue it for later
            Log.e("e","Queing task");
            mQueue.put( bottomSheetPage, bottomSheetData );
        }
    }

    private boolean canEmptyQueue() {
        if ( mViewPagerState != ViewPager.SCROLL_STATE_IDLE ) {
            return false;
        }

        if ( mBottomSheetState == STATE_DRAGGING  ||  mBottomSheetState  == STATE_SETTLING ) {
            return false;
        }

        if ( isMapVisiblyAnimating() ) {
            return false;
        }

        return true;
    }

    // The map has to be visible, and camera animating
    private boolean isMapVisiblyAnimating() {
        if ( mBottomSheetState == STATE_COLLAPSED  ||  mBottomSheetState == STATE_HIDDEN ) {
            if ( mMapCameraState == EventMapCameraState.State.START  ||  mMapCameraState == EventMapCameraState.State.CANCEL ) {
                return true;
            }
        }

        Log.e("E","MAP STATE IS " + mMapCameraState );
        return false;
    }

}