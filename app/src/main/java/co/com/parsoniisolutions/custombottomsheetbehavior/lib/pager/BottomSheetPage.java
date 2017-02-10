package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager;

import android.support.annotation.LayoutRes;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.appbar.DelegatingMergedAppBarLayoutBehavior;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils.DimensionUtils;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.appbar.DelegatingScrollingAppBarLayoutBehavior;

import java.lang.ref.WeakReference;


/**
 * A View Model for one page in a BottomSheet ViewPager.
 * The inflated view will get recycled for efficiency - we will only bind new data when needed.
 */
public class BottomSheetPage {

    protected WeakReference<BottomSheetViewPager> mViewPagerRef;

    public BottomSheetPage( LayoutInflater inflater, BottomSheetViewPager bottomSheetViewPager ) {
        mViewPagerRef = new WeakReference<>( bottomSheetViewPager );
        mInflatedView = inflater.inflate( layoutRes(), null );
        initializeUI();

        mInflatedView.setTag( R.id.BOTTOM_SHEET_PAGE, this );
    }

    protected View                  mInflatedView;
    public    View inflatedView() { return mInflatedView; }

    public @LayoutRes int layoutRes() { throw new UnsupportedOperationException( "You must subclass BottomSheetPage and override layoutRes()" ); }

    protected int mPosition = -1;
    protected void setNewAdapterPosition( int position ) {
        mPosition = position;

        // Ask for new data synchronously
        BottomSheetData bottomSheetData = getBottomSheetData( position );
        setUI( bottomSheetData );
    }


    //private   View mFabFloatingFrameLayout;
    protected View mNestedScrollView;

    public void setBottomSheetState( int newState, boolean noanim ) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)mInflatedView.findViewById( R.id.bottom_sheet ).getLayoutParams();
        BottomSheetBehaviorGoogleMapsLike behavior = (BottomSheetBehaviorGoogleMapsLike)params.getBehavior();
        behavior.setState( newState, noanim );
    }

    protected void initializeUI() {
        mNestedScrollView = mInflatedView.findViewById( R.id.bottom_sheet );
        setBottomSheetBehaviorParameters();
        setDelegatingMergedToolbarParameters();
        setDelegatingScrollToolbarParameters();
    }

    public void setUI( BottomSheetData bottomSheetData ) { throw new UnsupportedOperationException( "You must subclass BottomSheetPage and override setUI()" ); }

    /**
     * Close any database cursors or perform any other cleanup necessary
     */
    public void onDestroy() { }

    private void setBottomSheetBehaviorParameters() {
        // Resize the bottom sheet to account for any possible difference between the peek height and appbar height
        // When the bottomsheet is fully expanded, we want to align the bottom edge of the peek rectangle with the bottom edge of the appbar
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mNestedScrollView.getLayoutParams();
        BottomSheetBehaviorGoogleMapsLike behavior = (BottomSheetBehaviorGoogleMapsLike) params.getBehavior();
        behavior.setParentBottomSheetPage( this );

        // Wait for layout so we can get actual height
        mNestedScrollView.post( new Runnable() {
            @Override
            public void run() {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mNestedScrollView.getLayoutParams();
                int appBarHeight = DimensionUtils.getStatusBarHeight( mNestedScrollView.getContext() ) + DimensionUtils.getToolbarHeight( mNestedScrollView.getContext() );
                int peekHeight = DimensionUtils.getPeekHeight( mNestedScrollView.getContext() );
                int extraOffsetForExpanded = peekHeight - appBarHeight;
                params.height = mNestedScrollView.getHeight() + extraOffsetForExpanded;
                mNestedScrollView.setLayoutParams( params );
            }
        } );
    }

    private void setDelegatingMergedToolbarParameters() {
        View mergedAppBar = mInflatedView.findViewById( R.id.delegating_merged_appbarlayout );
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mergedAppBar.getLayoutParams();
        //params.height = DimensionUtils.getStatusBarHeight( mergedAppBar.getContext() ) + DimensionUtils.getToolbarHeight( mergedAppBar.getContext() );
        DelegatingMergedAppBarLayoutBehavior behavior = (DelegatingMergedAppBarLayoutBehavior) params.getBehavior();
        behavior.setToolbarTop( DimensionUtils.getStatusBarHeight( mergedAppBar.getContext() ) );
        behavior.setToolbarBottom( DimensionUtils.getStatusBarHeight( mergedAppBar.getContext() ) + DimensionUtils.getToolbarHeight( mergedAppBar.getContext() ) );
        behavior.setParentBottomSheetPage( this );
        //mergedAppBar.setLayoutParams( params ); // force layout for new height to take effect
    }

    private void setDelegatingScrollToolbarParameters() {
        View scrollAppBar = mInflatedView.findViewById( R.id.delegating_scroll_appbarlayout );
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) scrollAppBar.getLayoutParams();
        DelegatingScrollingAppBarLayoutBehavior behavior = (DelegatingScrollingAppBarLayoutBehavior) params.getBehavior();
        behavior.setParentBottomSheetPage( this );
    }

    /**
     * Returns true if this BottomSheetPage is currently shown (selected) in the ViewPager
     */
    // This doesn't always work, because this page can be visible while position in adapter has changed
    // As a workaround, we will refresh FAB horizontal position when state changes
    public boolean isSelected() {
        if ( mViewPagerRef.get() == null ) {
            return false;
        }
        return mViewPagerRef.get().getCurrentItem() == mPosition;
    }
    protected BottomSheetData getBottomSheetData( int position ) {
        throw new UnsupportedOperationException( "You must subclass BottomSheetPage and override getBottomSheetData()" );
    }
}