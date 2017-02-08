package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.map.MapViewWithLoading;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPagerAdapter;

import java.lang.ref.WeakReference;



public abstract class BottomSheetPagerAdapterWithLoading extends BottomSheetPagerAdapter {

    private WeakReference<MapViewWithLoading> mMapViewRef = null;
    public void setMapView( MapViewWithLoading mapView ) {
        mMapViewRef = new WeakReference<>( mapView );
    }

    @Override
    public void onPageSelected( int position ) {
        super.onPageSelected( position );

        // Animate the camera to the corresponding marker
        if ( mMapViewRef != null  &&  mMapViewRef.get() != null ) {
            long id = getIdAtPosition( position );

            mMapViewRef.get().animateCameraToId( id );
        }
    }

    public abstract long getIdAtPosition(  int position );
    public abstract int  getPositionForId( long id );
}
