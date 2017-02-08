package co.com.parsoniisolutions.custombottomsheetbehavior.sample.simple;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetViewPager;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils.DimensionUtils;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.views.MergedAppBarLayout;
import co.com.parsoniisolutions.custombottomsheetbehavior.sample.CheeseData;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;


/**
 * Most basic demo, no async loading, no coordination with map
 */
public class MainActivitySimple extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main_simple );
        MapView mapView = (MapView)findViewById( R.id.map );
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

        // Let's create some dummy data
        List<CheeseData> pageList = new ArrayList<>();
        for ( int i = 1; i < 8; ++i ) {
            List<Integer> drawableList;
            if ( i == 1 ) {
                drawableList = new ArrayList<Integer>() {{ add( R.drawable.cheese_1 ); add( R.drawable.cheese_2 );}};
            }
            else
            if ( i == 2 ) {
                drawableList = new ArrayList<Integer>() {{ add( R.drawable.cheese_3 ); add( R.drawable.cheese_4 ); add( R.drawable.cheese_5 ); }};
            }
            else
            if ( i == 3 ) {
                drawableList = new ArrayList<Integer>() {{ add( R.drawable.cheese_6 ); }};
            }
            else {
                drawableList = new ArrayList<Integer>() {{ add( R.drawable.cheese_default ); }};
            }

            CheeseData item = new CheeseData( "Title " + i, "Description " + i, drawableList );

            pageList.add( item );
        }

        BottomSheetPagerAdapterCheeseSimple adapter = new BottomSheetPagerAdapterCheeseSimple( pageList );
        final BottomSheetViewPager bottomSheetViewPager = (BottomSheetViewPager) findViewById( R.id.view_pager_main_content );
        bottomSheetViewPager.setAdapter( adapter );
        bottomSheetViewPager.setOffscreenPageLimit( 0 ); // Set to zero for demo to illustrate the ViewPager View recycling. Will probably want a larger value in production.
        bottomSheetViewPager.setBottomSheetState( BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT, false );

        MergedAppBarLayout mergedAppBarLayout = (MergedAppBarLayout) findViewById( R.id.merged_appbarlayout );
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
        int toolbarHeight = 0;
        TypedValue tv = new TypedValue();
        if ( getTheme().resolveAttribute( android.R.attr.actionBarSize, tv, true ) ) {
            toolbarHeight = TypedValue.complexToDimensionPixelSize( tv.data, getResources().getDisplayMetrics() );
        }

        googleMap.setPadding( 0, toolbarHeight + DimensionUtils.getStatusBarHeight( this ), 0, 0 );

        googleMap.getUiSettings().setCompassEnabled( true );
        googleMap.getUiSettings().setMyLocationButtonEnabled( true );

        googleMap.getUiSettings().setZoomControlsEnabled( false );
        googleMap.getUiSettings().setAllGesturesEnabled( true );

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target( new LatLng( 40.0f, -120.0f ) )
                .zoom( 4.0f )
                .build();
        googleMap.animateCamera( CameraUpdateFactory.newCameraPosition( cameraPosition ) );
    }

}