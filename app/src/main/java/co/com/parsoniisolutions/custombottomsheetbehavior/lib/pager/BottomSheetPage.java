package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.BottomSheetBehaviorGoogleMapsLike;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.FloatingFrameLayout;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.ScrollAwareFABBehavior;

import java.lang.ref.WeakReference;


/**
 * A View Model for one page in a BottomSheet ViewPager.
 * The inflated view will get recycled for efficiency - we will only bind new data when needed.
 */
public class BottomSheetPage {

    public BottomSheetPage( LayoutInflater inflater, BottomSheetPagerAdapter bottomSheetPagerAdapter ) {
        mPagerAdapterRef = new WeakReference<>( bottomSheetPagerAdapter );
        mInflatedView = inflater.inflate( layoutRes(), null );
        initializeUI();

        mInflatedView.setTag( R.id.BOTTOM_SHEET_PAGE, this );
    }

    protected View               mInflatedView;
    public View inflatedView() {
        return mInflatedView;
    }

    public @LayoutRes int layoutRes() { throw new UnsupportedOperationException( "You must subclass BottomSheetPage and override layoutRes()" ); }

    void setCorrespondingAdapterPosition( int position ) {
        setUI( position );
    }

    private WeakReference<BottomSheetPagerAdapter> mPagerAdapterRef;
    protected BottomSheetPagerAdapter pagerAdapter() { return mPagerAdapterRef.get(); }

    //private   View mFabFloatingFrameLayout;
    protected View mNestedScrollView;

    public int getBottomSheetState() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)mInflatedView.findViewById( R.id.bottom_sheet ).getLayoutParams();
        BottomSheetBehaviorGoogleMapsLike behavior = (BottomSheetBehaviorGoogleMapsLike)params.getBehavior();
        return behavior.getState();
    }

    public void setBottomSheetState( int newState, boolean noanim ) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)mInflatedView.findViewById( R.id.bottom_sheet ).getLayoutParams();
        BottomSheetBehaviorGoogleMapsLike behavior = (BottomSheetBehaviorGoogleMapsLike)params.getBehavior();
        behavior.setState( newState, noanim );
    }

    protected void initializeUI() {
        mNestedScrollView = mInflatedView.findViewById( R.id.bottom_sheet );
        setFabBehaviorParameters();
        setOnBottomSheetStateChangedListener();
    }
    protected void setUI( int position ) { }

    /**
     * Close any database cursors or perform any other cleanup necessary
     */
    public void onDestroy() { }

    /**
     * We need to observe the BottomSheet.
     * When user triggers a BottomSheet state change, other BottomSheets in the PagerAdapter should reflect this as well.
     */
    private void setOnBottomSheetStateChangedListener() {
        final BottomSheetPage bottomSheetPage = this;
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)mInflatedView.findViewById( R.id.bottom_sheet ).getLayoutParams();
        BottomSheetBehaviorGoogleMapsLike behavior = (BottomSheetBehaviorGoogleMapsLike)params.getBehavior();
        behavior.addBottomSheetCallback( new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged( @NonNull View bottomSheet, @BottomSheetBehaviorGoogleMapsLike.State int newState ) {
                if ( mPagerAdapterRef.get() != null ) {
                    mPagerAdapterRef.get().onBottomSheetStateChanged( newState, bottomSheetPage );
                }
            }

            @Override
            public void onSlide( @NonNull View bottomSheet, float slideOffset ) { }
        });
    }

    private void setFabBehaviorParameters() {
        FloatingActionButton ffl = (FloatingActionButton) mInflatedView.findViewById( R.id.fab);
        if ( ffl != null ) {
            int fabHeight = (int)ffl.getContext().getResources().getDimension( R.dimen.fab_size );
            int toolbarHeight = 0;
            TypedValue tv = new TypedValue();
            if ( ffl.getContext().getTheme().resolveAttribute( android.R.attr.actionBarSize, tv, true ) ) {
                toolbarHeight = TypedValue.complexToDimensionPixelSize( tv.data, ffl.getContext().getResources().getDisplayMetrics() );
            }

            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ffl.getLayoutParams();
            ScrollAwareFABBehavior behavior = (ScrollAwareFABBehavior)params.getBehavior();
            behavior.setOffsetValue( toolbarHeight + fabHeight / 2 );
            //behavior.setHideTopOffsetPx( toolbarHeight + fabHeight / 2 );
            //behavior.setHideBottomOffsetPx( toolbarHeight );//+ (int)(6 * MainActivity.sDensity) );
            //ffl.setAnimateSize( true );
            params.setBehavior( behavior );
        }
    }

}