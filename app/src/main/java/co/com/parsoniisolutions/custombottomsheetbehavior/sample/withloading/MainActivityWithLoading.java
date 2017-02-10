package co.com.parsoniisolutions.custombottomsheetbehavior.sample.withloading;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.MapViewWithLoading;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.API;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.BottomSheetViewPagerWithLoading;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.views.MergedAppBarLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT;
import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED;
import static co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.BottomSheetDataWithLoading.LocationType.POINT;


/**
 * More advanced demo with async loading and coordination with map
 */
public class MainActivityWithLoading extends AppCompatActivity {

    private API mApi;

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


        final BottomSheetViewPagerWithLoading bottomSheetViewPager = (BottomSheetViewPagerWithLoading) findViewById( R.id.view_pager_main_content );
        BottomSheetPagerAdapterCheeseWithLoading adapter = new BottomSheetPagerAdapterCheeseWithLoading( bottomSheetViewPager, getCheeseData() );
        bottomSheetViewPager.setAdapter( adapter );
        bottomSheetViewPager.setOffscreenPageLimit( 0 );
        bottomSheetViewPager.setBottomSheetState( BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED, false );


        /**
         * Set up the Map Toolbar
         */
        final Toolbar scroll_toolbar = (Toolbar) findViewById( R.id.scrolltoolbar );
        scroll_toolbar.setTitle( "" );
        scroll_toolbar.inflateMenu( R.menu.scroll );
        scroll_toolbar.setOnMenuItemClickListener( new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick( MenuItem item ) {
                if ( item.getItemId() == R.id.action_search ) {
                    Toast.makeText( getApplicationContext(), "Clicked search", Toast.LENGTH_LONG ).show();
                    mApi.setBottomSheetState( STATE_ANCHOR_POINT );
                    scroll_toolbar.postDelayed( new Runnable() {
                        @Override
                        public void run() {
                            mApi.setBottomSheetState( STATE_COLLAPSED );
                        }
                    }, 100 );
                    return true;
                }
                else
                if ( item.getItemId() == R.id.action_info ) {
                    Toast.makeText( getApplicationContext(), "Clicked info", Toast.LENGTH_LONG ).show();
                    return true;
                }
                return false;
            }
        });


        /**
         * Set up the BottomSheet Appbar
         */
        MergedAppBarLayout mergedAppBarLayout = (MergedAppBarLayout) findViewById( R.id.merged_appbarlayout );
        mergedAppBarLayout.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                bottomSheetViewPager.setBottomSheetState( BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED, false );
            }
        } );

        Toolbar merged_toolbar = (Toolbar) findViewById( R.id.merged_toolbar );
        merged_toolbar.setTitle( "Bottomsheet Title" );
        merged_toolbar.inflateMenu( R.menu.merged );
        merged_toolbar.setOnMenuItemClickListener( new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick( MenuItem item ) {
                if ( item.getItemId() == R.id.action_edit ) {
                    Toast.makeText( getApplicationContext(), "Clicked edit", Toast.LENGTH_LONG ).show();
                    return true;
                }
                else
                if ( item.getItemId() == R.id.action_delete ) {
                    Toast.makeText( getApplicationContext(), "Clicked delete", Toast.LENGTH_LONG ).show();
                    return true;
                }
                return false;
            }
        });


        /**
         * Get an API for interacting with the map+bottomsheet viewpager combo
         */
        mApi = new API( mapView, bottomSheetViewPager );


        /**
         * Listen for selections
         */
        mApi.addOnSelectedListener(
                new API.OnSelectedListener() {
                    @Override
                    public void onSelected( long id ) {
                        Log.e( "e", "Selected id " + id );
                    }
                });
    }


    /**
     * Set the initial map state
     * @param googleMap
     */
    private void mapReady( GoogleMap googleMap ) {
        googleMap.getUiSettings().setCompassEnabled( true );
        googleMap.getUiSettings().setMyLocationButtonEnabled( true );

        googleMap.getUiSettings().setZoomControlsEnabled( true );
        googleMap.getUiSettings().setAllGesturesEnabled( true );
        googleMap.getUiSettings().setMapToolbarEnabled( false );

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


    /**
     * Let's create some dummy data
     */
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