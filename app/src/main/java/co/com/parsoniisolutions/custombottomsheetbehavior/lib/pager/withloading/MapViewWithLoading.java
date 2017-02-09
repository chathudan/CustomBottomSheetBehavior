package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils.DimensionUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ConcurrentHashMap;

import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.EventMapCameraState.State.CANCEL;
import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.EventMapCameraState.State.FINISH;
import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.EventMapCameraState.State.START;


public class MapViewWithLoading extends MapView {

    public MapViewWithLoading( Context context ) { super( context ); init( context ); }
    public MapViewWithLoading( Context context, AttributeSet attributeSet ) { super( context, attributeSet ); init( context ); }
    public MapViewWithLoading( Context context, AttributeSet attributeSet, int i ) { super( context, attributeSet, i ); init( context ); }
    public MapViewWithLoading( Context context, GoogleMapOptions googleMapOptions ) { super( context, googleMapOptions ); init( context ); }

    private GoogleMap mGoogleMap = null;
    private void init( final Context context ) {
        final int topPadding = DimensionUtils.getToolbarHeight( context ) + DimensionUtils.getStatusBarHeight( context );

        getMapAsync( new OnMapReadyCallback() {
            @Override
            public void onMapReady( GoogleMap googleMap ) {
                mGoogleMap = googleMap;
                mGoogleMap.setPadding( 0, topPadding, 0, 0 );
                mGoogleMap.setOnMarkerClickListener( new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick( Marker marker ) {
                        scrollBottomSheetPagerToMarker( marker );
                        return false;
                    }
                } );

                mGoogleMap.setOnCameraMoveStartedListener(
                        new GoogleMap.OnCameraMoveStartedListener() {
                            @Override
                            public void onCameraMoveStarted( int i ) {
                                Log.e( "e", "CAMERA MOVE STARTED" );
                                EventBus.getDefault().post( new EventMapCameraState( START ) );
                            }
                        } );

                mGoogleMap.setOnCameraIdleListener(
                        new GoogleMap.OnCameraIdleListener() {
                            @Override
                            public void onCameraIdle() {
                                Log.e( "e", "CAMERA IDLE" );
                                EventBus.getDefault().post( new EventMapCameraState( FINISH ) );
                            }
                        } );
            }
        } );
    }

    private void scrollBottomSheetPagerToMarker( Marker marker ) {
        // Scroll the view pager to this marker
        GoogleMapMarkerData googleMapMarkerData = findGoogleMapMarkerData( marker );
        if ( googleMapMarkerData == null ) {
            return;
        }

        long id = googleMapMarkerData.getId();

        if ( mBottomSheetViewPagerWithLoading == null ) {
            return;
        }

        mBottomSheetViewPagerWithLoading.scrollToId( id, true );

    }

    private BottomSheetViewPagerWithLoading mBottomSheetViewPagerWithLoading = null;
    void setBottomSheetViewPagerWithLoading( BottomSheetViewPagerWithLoading pager ) {
        mBottomSheetViewPagerWithLoading = pager;
    }

    public void animateCameraWithEvents( CameraUpdate cameraUpdate ) {
        if ( mGoogleMap != null ) {
            EventBus.getDefault().post( new EventMapCameraState( START ) );
            mGoogleMap.animateCamera( cameraUpdate, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    EventBus.getDefault().post( new EventMapCameraState( FINISH ) );
                }

                @Override
                public void onCancel() {
                    EventBus.getDefault().post( new EventMapCameraState( CANCEL ) );
                }
            } );
        }
    }

    private ConcurrentHashMap<Long, GoogleMapMarkerData> mAddedMarkers = new ConcurrentHashMap<>();

    // Add a marker to the map and remember it
    public void addMarker( long id, MarkerOptions mo ) {
        Marker marker = mGoogleMap.addMarker( mo );
        GoogleMapMarkerData googleMapMarkerData = new GoogleMapMarkerData( id, marker, mo );
        mAddedMarkers.put( id, googleMapMarkerData );
    }

    private GoogleMapMarkerData findGoogleMapMarkerData( Marker marker ) {
        for ( GoogleMapMarkerData googleMapMarkerData : mAddedMarkers.values() ) {
            if ( googleMapMarkerData.getMarker().equals( marker ) ) {
                return googleMapMarkerData;
            }
        }
        return null;
    }

    public void animateCameraToId( long id ) {
        GoogleMapMarkerData googleMapMarkerData = mAddedMarkers.get( id );
        if ( googleMapMarkerData == null )
            return;
        googleMapMarkerData.getMarker().showInfoWindow();
        animateCameraWithEvents( CameraUpdateFactory.newLatLng( googleMapMarkerData.getPosition() ) );
    }
}
