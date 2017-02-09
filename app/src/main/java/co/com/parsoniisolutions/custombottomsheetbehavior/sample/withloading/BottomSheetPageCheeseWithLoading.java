package co.com.parsoniisolutions.custombottomsheetbehavior.sample.withloading;

import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.BottomSheetDataWithLoading;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPagerAdapter;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetData;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.BottomSheetPageWithLoading;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.BottomSheetViewPagerWithLoading;
import co.com.parsoniisolutions.custombottomsheetbehavior.sample.PhotosPagerAdapter;


public class BottomSheetPageCheeseWithLoading extends BottomSheetPageWithLoading {

    public BottomSheetPageCheeseWithLoading( LayoutInflater inflater, BottomSheetViewPagerWithLoading viewPager ) {
        super( inflater, viewPager );
    }

    @Override
    public @LayoutRes int layoutRes() { return R.layout.bottom_pager_content; }

    private TextView  mTitle;
    private TextView  mSubTitle;
    private ViewPager mPhotosViewPager;

    @Override
    protected void initializeUI() {
        super.initializeUI();

        mTitle           = (TextView)inflatedView().findViewById( R.id.bottom_sheet_title );
        mSubTitle        = (TextView)inflatedView().findViewById( R.id.text_dummy1 );
        mPhotosViewPager = (ViewPager) inflatedView().findViewById( R.id.photos_view_pager );
    }

    @Override
    public void setUI( BottomSheetData bottomSheetData ) {
        GeoCheeseData cheeseData = (GeoCheeseData) bottomSheetData;
        mTitle.setText( cheeseData.getTitle() );
        mSubTitle.setText( cheeseData.getSubTitle() );

        try {
            Thread.sleep(0); // Simulate a complex UI which takes time to set
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }

        PhotosPagerAdapter photos_adapter = new PhotosPagerAdapter( mPhotosViewPager.getContext(), cheeseData.getImageResourceList() );
        mPhotosViewPager.setAdapter(photos_adapter);
    }

    @Override
    public void setUILoading() {
        mTitle.setText( "Loading ..." );
        mSubTitle.setText( "Loading ..." );

        List<Integer> defaultLoadingPhoto = new ArrayList<Integer>(){{ add( R.drawable.cheese_loading ); }};
        PhotosPagerAdapter photos_adapter = new PhotosPagerAdapter( mPhotosViewPager.getContext(), defaultLoadingPhoto );
        mPhotosViewPager.setAdapter(photos_adapter);
    }

    @Override
    protected void getBottomSheetDataAsync( final int position, final OnBottomSheetDataLoadedCallback cb ) {
        // TODO - cancel the task if another request comes for a different position
        // Load the data asynchronously ...
        AsyncTask<Void,Void,BottomSheetDataWithLoading> loadDataAsyncTask = new AsyncTask<Void, Void, BottomSheetDataWithLoading>() {

            private BottomSheetPagerAdapterCheeseWithLoading adp;

            @Override
            protected void onPreExecute() {
                adp = (BottomSheetPagerAdapterCheeseWithLoading)(mViewPagerRef.get().getAdapter());
            }

            @Override
            protected BottomSheetDataWithLoading doInBackground( Void... params ) {
                // Simulate a longer loading time
                try {
                    Thread.sleep( 500 );
                } catch ( InterruptedException e ) { }

                if ( adp == null )
                    return null;
                GeoCheeseData item = adp.getItemAtPosition( position );
                return item;
            }

            @Override
            protected void onPostExecute( BottomSheetDataWithLoading bottomSheetData ) {
                cb.onDataLoaded( bottomSheetData );
            }
        };
        loadDataAsyncTask.execute();
    }

}
