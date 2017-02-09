package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetData;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPagerAdapter;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetViewPager;

import java.lang.ref.WeakReference;


public abstract class BottomSheetPagerAdapterWithLoading extends BottomSheetPagerAdapter {

    public BottomSheetPagerAdapterWithLoading( BottomSheetViewPagerWithLoading viewPager ) {
        super( viewPager );
    }

    protected BottomSheetViewPagerWithLoading viewPager() { return (BottomSheetViewPagerWithLoading)mViewPagerRef.get(); }

    public abstract long getIdAtPosition(  int position );
    public abstract int  getPositionForId( long id );
}
