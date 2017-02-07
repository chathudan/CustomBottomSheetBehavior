package co.com.parsoniisolutions.custombottomsheetbehavior.sample;

import android.view.LayoutInflater;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPage;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPagerAdapter;

import java.util.List;


public class MainContentViewPagerAdapter extends BottomSheetPagerAdapter {

    List<MainContentPagerItem> mMainContentPagerItems;

    public MainContentViewPagerAdapter( List<MainContentPagerItem> mainContentPagerItems ) {
        mMainContentPagerItems = mainContentPagerItems;
    }

    @Override
    public int getCount() {
        return mMainContentPagerItems.size();
    }

    public MainContentPagerItem getItemAtPosition( int position ) {
        return mMainContentPagerItems.get( position );
    }

    @Override
    public boolean isViewFromObject( View view, Object object ) { return view == object; }


    @Override
    public BottomSheetPage createNewPage( LayoutInflater inflater ) {
        return new BottomSheetPageCheese( inflater, this );
    }

}