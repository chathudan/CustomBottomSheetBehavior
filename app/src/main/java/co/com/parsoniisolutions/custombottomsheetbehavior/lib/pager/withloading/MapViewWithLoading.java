package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils.DimensionUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ConcurrentHashMap;

import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED;
import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING;
import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN;
import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_SETTLING;
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
                        collapseBottomSheetIfHidden();
                        scrollBottomSheetPagerToMarker( marker );
                        return false;
                    }
                } );

                mGoogleMap.setOnMapClickListener( new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick( LatLng latLng ) {
                        hideBottomSheet();
                    }
                } );

                mGoogleMap.setOnCameraMoveStartedListener(
                        new GoogleMap.OnCameraMoveStartedListener() {
                            @Override
                            public void onCameraMoveStarted( int i ) {
                                EventBus.getDefault().post( new EventMapCameraState( START ) );
                            }
                        } );

                mGoogleMap.setOnCameraIdleListener(
                        new GoogleMap.OnCameraIdleListener() {
                            @Override
                            public void onCameraIdle() {
                                EventBus.getDefault().post( new EventMapCameraState( FINISH ) );
                            }
                        } );
            }
        } );

        registerEventBus();
    }

    private void registerEventBus() {
        try {
            EventBus.getDefault().register( this );
        } catch ( EventBusException e ) {
            e.printStackTrace();
        }
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

        // Find the position, animate the scroll if it is a direct neighbor, no anim otherwise
        // int position = googleMapMarkerData.get
        boolean anim = false;

        mBottomSheetViewPagerWithLoading.scrollToId( id, anim );

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

    private void collapseBottomSheetIfHidden() {
        if ( mBottomSheetViewPagerWithLoading.bottomSheetState() == STATE_HIDDEN ) {
            mBottomSheetViewPagerWithLoading.setBottomSheetState( STATE_COLLAPSED, false );
        }
    }

    private void hideBottomSheet() {
        mBottomSheetViewPagerWithLoading.setBottomSheetState( STATE_HIDDEN, false );
    }

    // Make the map respond to the BottomSheet state - show the zoom controls above the BottomSheet when collapsed
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onEvent( EventBottomSheetState ev ) {
        if ( mGoogleMap == null )
            return;

        if ( ev.state() != STATE_HIDDEN ) {
            mGoogleMap.getUiSettings().setZoomControlsEnabled( false );
        }
        else {
            mGoogleMap.getUiSettings().setZoomControlsEnabled( true );
        }
    }

}
