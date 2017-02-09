package co.com.parsoniisolutions.custombottomsheetbehavior.sample.simple;

import android.view.LayoutInflater;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPage;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPagerAdapter;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetViewPager;

import java.util.List;


public class BottomSheetPagerAdapterCheeseSimple extends BottomSheetPagerAdapter {

    List<CheeseData> mMainContentPagerItems;

    public BottomSheetPagerAdapterCheeseSimple( BottomSheetViewPager viewPager, List<CheeseData> mainContentPagerItems ) {
        super( viewPager );
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
        return new BottomSheetPageCheeseSimple( inflater, viewPager() );
    }

}