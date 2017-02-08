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

    @Override
    protected void setNewAdapterPosition( int position ) {
        mPosition = position;
        setUILoading();
        // Ask for new data asynchronously
        getBottomSheetDataAsync( position, new OnBottomSheetDataLoadedCallback() {
            @Override
            public void onDataLoaded( BottomSheetData bottomSheetData ) {
                setUI( bottomSheetData );
            }
        } );
    }

    public void setUILoading() { throw new UnsupportedOperationException( "You must subclass BottomSheetPage and override setUILoading()" ); }

    public interface OnBottomSheetDataLoadedCallback {
        void onDataLoaded( BottomSheetData bottomSheetData );
    }
    protected void getBottomSheetDataAsync( int position, OnBottomSheetDataLoadedCallback cb ) {
        throw new UnsupportedOperationException( "You must subclass BottomSheetPage and override getBottomSheetDataAsync()" );
    }

}