package co.com.parsoniisolutions.custombottomsheetbehavior.sample;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.BottomSheetBehaviorGoogleMapsLike;
import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetViewPager;


public class MainActivity extends AppCompatActivity {


    int[] mDrawables = {
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3
    };

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main );
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

        Toolbar toolbar = (Toolbar) findViewById( R.id.scrolltoolbar );
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(" ");
        }

        /**
         * If we want to listen for states callback
         */
        /*
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        final BottomSheetBehaviorGoogleMapsLike behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    default:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
*/
        //AppBarLayout mergedAppBarLayout = (AppBarLayout) findViewById(R.id.merged_appbarlayout);
        //DelegatingMergedAppBarLayoutBehavior mergedAppBarLayoutBehavior = DelegatingMergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        //mergedAppBarLayoutBehavior.setToolbarTitle("Title Dummy");
        /*
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
            }
        });
        */

        List<MainContentPagerItem> pageList = new ArrayList<>();
        for ( int i = 1; i < 10; ++i ) {
            MainContentPagerItem item = new MainContentPagerItem( "Title " + i, "Description " + i, new ArrayList<Integer>() {{ add( R.drawable.cheese_3 ); }} );
            pageList.add( item );
        }

        MainContentViewPagerAdapter adapter = new MainContentViewPagerAdapter( pageList );
        BottomSheetViewPager bottomSheetViewPager = (BottomSheetViewPager) findViewById( R.id.view_pager_main_content );
        bottomSheetViewPager.setAdapter( adapter );
        bottomSheetViewPager.setOffscreenPageLimit( 0 );
        bottomSheetViewPager.setBottomSheetState( BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT, false );

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

        //bottomSheetTextView = (TextView) bottomSheet.findViewById(R.id.bottom_sheet_title);
        //ItemPagerAdapter adapter = new ItemPagerAdapter(this,mDrawables);
        //ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        //viewPager.setAdapter(adapter);

        //behavior.setState( BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT );
    }

    private void mapReady( GoogleMap googleMap ) {
        // Set the initial map state
        int toolbarHeight = 0;
        TypedValue tv = new TypedValue();
        if ( getTheme().resolveAttribute( android.R.attr.actionBarSize, tv, true ) ) {
            toolbarHeight = TypedValue.complexToDimensionPixelSize( tv.data, getResources().getDisplayMetrics() );
        }

        googleMap.setPadding( 0, toolbarHeight + Utils.getStatusBarHeight( this ), 0, 0 );

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
