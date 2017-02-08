package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import android.view.LayoutInflater;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPage;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPagerAdapter;


/**
 * A View Model for one page in a BottomSheet ViewPager.
 * Supports asynchronous data loading.
 */
public class BottomSheetPageWithLoading extends BottomSheetPage {

    public BottomSheetPageWithLoading( LayoutInflater inflater, BottomSheetPagerAdapter bottomSheetPagerAdapter ) {
        super( inflater, bottomSheetPagerAdapter );
    }

    //@Override
    //protected void getBottomSheetDataAsync();

    // Subclass needs to implement this
    //public void processLoadedData( BottomSheetData bottomSheetData ) { }
}