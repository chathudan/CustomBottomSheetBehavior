package co.com.parsoniisolutions.custombottomsheetbehavior.sample.withloading;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.map.MapViewWithLoading;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetViewPager;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.BottomSheetViewPagerWithLoading;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.views.MergedAppBarLayout;

import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.BottomSheetDataWithLoading.LocationType.POINT;


/**
 * More advanced demo with async loading and coordination with map
 */
public class MainActivityWithLoading extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main_with_loading );
        MapViewWithLoading mapView = (MapViewWithLoading) findViewById( R.id.map );
        mapView.onCreate( null ); // Was getting weird exceptions if passed bundle: java.lang.RuntimeException: Unable to start activity
        mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize( getApplicationContext() );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        mapView.getMapAsync( new OnMapReadyCallback() {
            @Override
            public void onMapReady( GoogleMap googleMap ) {
                mapReady( googleMap );
            }
        } );

        Toolbar toolbar = (Toolbar) findViewById( R.id.scrolltoolbar );
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(" ");
        }

        List<GeoCheeseData> data = getCheeseData();

        BottomSheetPagerAdapterCheeseWithLoading adapter = new BottomSheetPagerAdapterCheeseWithLoading( data );
        final BottomSheetViewPagerWithLoading bottomSheetViewPager = (BottomSheetViewPagerWithLoading) findViewById( R.id.view_pager_main_content );
        bottomSheetViewPager.setAdapter( adapter );
        bottomSheetViewPager.setOffscreenPageLimit( 0 );
        bottomSheetViewPager.setBottomSheetState( BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT, false );

        // Link the adapter and the map
        mapView.setBottomSheetViewPagerWithLoading( bottomSheetViewPager );
        adapter.setMapView( mapView );

        MergedAppBarLayout mergedAppBarLayout = (MergedAppBarLayout) findViewById(R.id.merged_appbarlayout);
        mergedAppBarLayout.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                bottomSheetViewPager.setBottomSheetState( BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED, false );
            }
        } );

        /**
         * Listen for page swipe callbacks
         */
        bottomSheetViewPager.addOnPageChangeListener( new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels ) {
                Log.d("pager position:", String.valueOf( position) );
            }

            @Override
            public void onPageSelected( int position ) { }

            @Override
            public void onPageScrollStateChanged( int state ) { }
        } );


        /**
         * Listen for BottomSheet callbacks
         */
        bottomSheetViewPager.addBottomSheetCallback( new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged( @NonNull View bottomSheet, @BottomSheetBehaviorGoogleMapsLike.State int newState ) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d( "bottomsheet-", "STATE_COLLAPSED" );
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d( "bottomsheet-", "STATE_DRAGGING" );
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d( "bottomsheet-", "STATE_EXPANDED" );
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        Log.d( "bottomsheet-", "STATE_ANCHOR_POINT" );
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d( "bottomsheet-", "STATE_HIDDEN" );
                        break;
                    default:
                        Log.d( "bottomsheet-", "STATE_SETTLING" );
                        break;
                }
            }

            @Override
            public void onSlide( @NonNull View bottomSheet, float slideOffset ) { }
        } );
    }


    /**
     * Set the initial map state
     * @param googleMap
     */
    private void mapReady( GoogleMap googleMap ) {
        googleMap.getUiSettings().setCompassEnabled( true );
        googleMap.getUiSettings().setMyLocationButtonEnabled( true );

        googleMap.getUiSettings().setZoomControlsEnabled( false );
        googleMap.getUiSettings().setAllGesturesEnabled( true );

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target( new LatLng( 40.0f, -120.0f ) )
                .zoom( 4.0f )
                .build();

        MapViewWithLoading mapView = (MapViewWithLoading) findViewById( R.id.map );
        mapView.animateCameraWithEvents( CameraUpdateFactory.newCameraPosition( cameraPosition ) );

        // Add some markers to the map
        List<GeoCheeseData> geoCheeseDataList = getCheeseData();
        for ( GeoCheeseData item : geoCheeseDataList ) {
            MarkerOptions mo = new MarkerOptions()
                    .title( item.getTitle() )
                    .snippet( item.getSubTitle() )
                    .position( new LatLng( 1e-6*item.getLate6(), 1e-6*item.getLone6() ) );
            mapView.addMarker( item.getId(), mo );
        }
    }


    // Let's create some dummy data
    private List<GeoCheeseData> getCheeseData() {
        List<GeoCheeseData> data = new ArrayList<>();
        for ( int i = 1; i < 8; ++i ) {
            List<Integer> imageResList;
            if ( i == 1 ) {
                imageResList = new ArrayList<Integer>() {{ add( R.drawable.cheese_1 ); add( R.drawable.cheese_2 );}};
            }
            else
            if ( i == 2 ) {
                imageResList = new ArrayList<Integer>() {{ add( R.drawable.cheese_3 ); add( R.drawable.cheese_4 ); add( R.drawable.cheese_5 ); }};
            }
            else
            if ( i == 3 ) {
                imageResList = new ArrayList<Integer>() {{ add( R.drawable.cheese_6 ); }};
            }
            else {
                imageResList = new ArrayList<Integer>() {{ add( R.drawable.cheese_default ); }};
            }

            GeoCheeseData item = (GeoCheeseData)new GeoCheeseData.Builder()

                    .setTitle( "Title " + i )
                    .setSubTitle( "Description " + i )
                    .setImageResourceList( imageResList )

                    .setId( i )
                    .setLocationType( POINT )
                    .setLate6( (int)(1e6*(32+Math.random()*14)) )
                    .setLone6( (int)(1e6*(-108 - Math.random()*18)) )

                    .build();

            data.add( item );
        }

        return data;
    }
}