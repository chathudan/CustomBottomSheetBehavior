package co.com.parsoniisolutions.custombottomsheetbehavior.sample.withloading;

import android.view.LayoutInflater;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPage;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPagerAdapter;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.BottomSheetDataWithLoading;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.BottomSheetPagerAdapterWithLoading;
import co.com.parsoniisolutions.custombottomsheetbehavior.sample.simple.CheeseData;

import java.util.List;


public class BottomSheetPagerAdapterCheeseWithLoading extends BottomSheetPagerAdapterWithLoading {

    List<GeoCheeseData> mMainContentPagerItems;

    public BottomSheetPagerAdapterCheeseWithLoading( List<GeoCheeseData> mainContentPagerItems ) {
        mMainContentPagerItems = mainContentPagerItems;
    }

    @Override
    public int getCount() {
        return mMainContentPagerItems.size();
    }

    public GeoCheeseData getItemAtPosition( int position ) {
        return mMainContentPagerItems.get( position );
    }

    @Override
    public boolean isViewFromObject( View view, Object object ) { return view == object; }


    @Override
    public BottomSheetPage createNewPage( LayoutInflater inflater ) {
        return new BottomSheetPageCheeseWithLoading( inflater, this );
    }

    @Override
    public long getIdAtPosition( int position ) {
        return mMainContentPagerItems.get( position ).getId();
    }

    @Override
    public int getPositionForId( long id ) {
        for ( int pos = 0, size = mMainContentPagerItems.size(); pos < size; ++ pos ) {
            BottomSheetDataWithLoading item = mMainContentPagerItems.get( pos );
            if ( item.getId() == id ) {
                return pos;
            }
        }
        return -1;
    }
}