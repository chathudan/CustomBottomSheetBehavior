package co.com.parsoniisolutions.custombottomsheetbehavior.lib.map;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.EventMapCameraState;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils.DimensionUtils;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import org.greenrobot.eventbus.EventBus;

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
            }
        } );
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
}
