package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * API for handling BottomSheet swipes and map clicks
 */
public class API implements APIif {

    WeakReference<MapViewWithLoading> mMapViewRef;
    WeakReference<BottomSheetViewPagerWithLoading> mViewPagerRef;

    public API ( MapViewWithLoading mapView, BottomSheetViewPagerWithLoading viewPager ) {
        registerMap( mapView );
        registerViewPager( viewPager, this );

        mapView.setBottomSheetViewPagerWithLoading( viewPager );
        BottomSheetPagerAdapterWithLoading adapter = (BottomSheetPagerAdapterWithLoading) viewPager.getAdapter();
        //adapter.setMapView( mapView );
    }

    private void registerMap( MapViewWithLoading mapView ) {
        mMapViewRef = new WeakReference<>( mapView );
    }
    private void registerViewPager( BottomSheetViewPagerWithLoading viewPager, API api ) {
        mViewPagerRef = new WeakReference<>( viewPager );
        viewPager.setApi( api );
    }

    List<OnSelectedListener> mOnSelectedListeners = new ArrayList<>();
    public void addOnSelectedListener( OnSelectedListener onSelectedListener ) {
        mOnSelectedListeners.add( onSelectedListener );
    }
    public void removeOnSelectedListener( OnSelectedListener onSelectedListener ) {
        mOnSelectedListeners.remove( onSelectedListener );
    }

    void callOnPageSelected( int position, long id ) {
        if ( mMapViewRef != null  &&  mMapViewRef.get() != null ) {
            mMapViewRef.get().animateCameraToId( id );
        }

        for ( OnSelectedListener onSelectedListener : mOnSelectedListeners ) {
            onSelectedListener.onSelected( id );
        }
    }

    public interface OnSelectedListener {
        void onSelected( long id );
    }
}