package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import android.content.Context;
import android.util.AttributeSet;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetData;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetViewPager;


public class BottomSheetViewPagerWithLoading extends BottomSheetViewPager {

    public BottomSheetViewPagerWithLoading( Context context ) { this( context, null ); }
    public BottomSheetViewPagerWithLoading( Context context, AttributeSet attrs ) {
        super( context, attrs );

        mViewPagerContentRefresher = new ViewPagerContentRefresher( this );
    }

    private ViewPagerContentRefresher mViewPagerContentRefresher;
    public void addTask( BottomSheetPageWithLoading bottomSheetPage, BottomSheetData bottomSheetData ) {
        mViewPagerContentRefresher.addTask( bottomSheetPage, bottomSheetData );
    }


    public void scrollToId( long id, boolean animate ) {
        BottomSheetPagerAdapterWithLoading adapter = (BottomSheetPagerAdapterWithLoading) getAdapter();
        if ( adapter == null )
            return;
        int position = adapter.getPositionForId( id );
        setCurrentItem( position, animate );
    }

}