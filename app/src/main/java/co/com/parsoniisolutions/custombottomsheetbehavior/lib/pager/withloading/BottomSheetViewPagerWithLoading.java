package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import android.content.Context;
import android.util.AttributeSet;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetData;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetViewPager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class BottomSheetViewPagerWithLoading extends BottomSheetViewPager {

    public BottomSheetViewPagerWithLoading( Context context ) { this( context, null ); }
    public BottomSheetViewPagerWithLoading( Context context, AttributeSet attrs ) { super( context, attrs ); }

    public void scrollToId( long id, boolean animate ) {
        BottomSheetPagerAdapterWithLoading adapter = (BottomSheetPagerAdapterWithLoading) getAdapter();
        if ( adapter == null )
            return;
        int position = adapter.getPositionForId( id );
        setCurrentItem( position, animate );
    }

    private API api;
    public void setApi( API api ) {
        this.api = api;
    }

    @Subscribe( sticky = false, threadMode = ThreadMode.MAIN )
    @Override
    public void onEvent( EventViewPagerPageSelected ev ) {
        super.onEvent( ev );

        int position = ev.position();
        BottomSheetPagerAdapterWithLoading adapter = (BottomSheetPagerAdapterWithLoading) getAdapter();
        long id = adapter.getIdAtPosition( position );
        api.callOnPageSelected( position, id );
    }

    private ViewPagerContentRefresher mViewPagerContentRefresher = new ViewPagerContentRefresher();
    public void addTask( BottomSheetPageWithLoading bottomSheetPage, BottomSheetData bottomSheetData ) {
        mViewPagerContentRefresher.addTask( bottomSheetPage, bottomSheetData );
    }

}