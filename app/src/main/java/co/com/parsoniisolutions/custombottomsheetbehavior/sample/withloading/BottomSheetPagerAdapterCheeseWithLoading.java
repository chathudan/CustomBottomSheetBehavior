package co.com.parsoniisolutions.custombottomsheetbehavior.sample.withloading;

import android.view.LayoutInflater;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPage;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPagerAdapter;
import co.com.parsoniisolutions.custombottomsheetbehavior.sample.CheeseData;
import co.com.parsoniisolutions.custombottomsheetbehavior.sample.simple.BottomSheetPageCheeseSimple;

import java.util.List;


public class BottomSheetPagerAdapterCheeseWithLoading extends BottomSheetPagerAdapter {

    List<CheeseData> mMainContentPagerItems;

    public BottomSheetPagerAdapterCheeseWithLoading( List<CheeseData> mainContentPagerItems ) {
        mMainContentPagerItems = mainContentPagerItems;
    }

    @Override
    public int getCount() {
        return mMainContentPagerItems.size();
    }

    public CheeseData getItemAtPosition( int position ) {
        return mMainContentPagerItems.get( position );
    }

    @Override
    public boolean isViewFromObject( View view, Object object ) { return view == object; }


    @Override
    public BottomSheetPage createNewPage( LayoutInflater inflater ) {
        return new BottomSheetPageCheeseWithLoading( inflater, this );
    }

}