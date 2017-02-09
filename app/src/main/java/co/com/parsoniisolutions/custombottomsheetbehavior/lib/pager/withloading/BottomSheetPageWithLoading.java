package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import android.view.LayoutInflater;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPage;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPagerAdapter;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetViewPager;


/**
 * A View Model for one page in a BottomSheet ViewPager.
 * Supports asynchronous data loading.
 */
public class BottomSheetPageWithLoading extends BottomSheetPage {

    public BottomSheetPageWithLoading( LayoutInflater inflater, BottomSheetViewPagerWithLoading bottomSheetViewPager ) {
        super( inflater, bottomSheetViewPager );
    }

    @Override
    protected void setNewAdapterPosition( int position ) {
        mPosition = position;
        setUILoading();
        // Ask for new data asynchronously
        final BottomSheetPageWithLoading bottomSheetPageWithLoading = this;
        getBottomSheetDataAsync( position, new OnBottomSheetDataLoadedCallback() {
            @Override
            public void onDataLoaded( BottomSheetDataWithLoading bottomSheetData ) {
                // Are we still viewing the same object? If not, ignore this load
                BottomSheetPagerAdapterWithLoading adp = (BottomSheetPagerAdapterWithLoading) mViewPagerRef.get().getAdapter();
                if ( adp != null  &&  adp.getIdAtPosition( mPosition ) == bottomSheetData.getId() ) {
                    viewPager().addTask( bottomSheetPageWithLoading, bottomSheetData );
                }
            }
        } );
    }

    public BottomSheetViewPagerWithLoading viewPager() { return (BottomSheetViewPagerWithLoading)mViewPagerRef.get(); }

    public void setUILoading() { throw new UnsupportedOperationException( "You must subclass BottomSheetPage and override setUILoading()" ); }

    public interface OnBottomSheetDataLoadedCallback {
        void onDataLoaded( BottomSheetDataWithLoading bottomSheetData );
    }
    protected void getBottomSheetDataAsync( int position, OnBottomSheetDataLoadedCallback cb ) {
        throw new UnsupportedOperationException( "You must subclass BottomSheetPage and override getBottomSheetDataAsync()" );
    }

}