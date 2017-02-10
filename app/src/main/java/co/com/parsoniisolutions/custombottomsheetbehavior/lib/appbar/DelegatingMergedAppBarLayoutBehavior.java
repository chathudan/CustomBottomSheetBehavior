package co.com.parsoniisolutions.custombottomsheetbehavior.lib.appbar;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.NestedScrollingParent;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.EventBottomSheetPosition;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils.DimensionUtils;
import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;


/**
 * Behavior applied on an AppBarLayout within a ViewPager. It delegates visibility change actions to MergedAppBarLayout.
 */
public class DelegatingMergedAppBarLayoutBehavior extends DelegatingAppBarLayoutBehavior {

    private static final String TAG = DelegatingMergedAppBarLayoutBehavior.class.getSimpleName();

    private boolean mInit = false;

    /**
     * To avoid using multiple "peekheight=" in XML and looking flexibility allowing {@link BottomSheetBehaviorGoogleMapsLike#mPeekHeight}
     * get changed dynamically we get the {@link NestedScrollingParent} that has
     * "app:layout_behavior=" {@link BottomSheetBehaviorGoogleMapsLike} inside the {@link CoordinatorLayout}
     */
    private WeakReference<BottomSheetBehaviorGoogleMapsLike> mBottomSheetBehaviorRef;

    private boolean mVisible = false;

    public DelegatingMergedAppBarLayoutBehavior( Context context, AttributeSet attrs ) {
        super( context, attrs );
    }

    @Override
    public boolean layoutDependsOn( CoordinatorLayout parent, View child, View dependency ) {
        if ( dependency instanceof NestedScrollingParent ) {
            try {
                BottomSheetBehaviorGoogleMapsLike.from( dependency );
                return true;
            }
            catch ( IllegalArgumentException e ) { }
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged( CoordinatorLayout parent, View child, View dependency ) {

        if ( ! mInit ) {
            init( parent, child );
        }

        if ( publish() ) {
            EventBus.getDefault().post( new EventBottomSheetPosition( (int) (dependency.getY()) ) );
        }

        /**
         * Following docs we should return true if the Behavior changed the child view's size or position, false otherwise
         */
        boolean childMoved = false;

        if ( isDependencyYBelowAnchorPoint( parent, dependency ) ) {
            if ( publish() ) {
                EventBus.getDefault().post( new EventMergedAppBarVisibility( true, (int) child.getY(), EventMergedAppBarVisibility.State.BELOW_ANCHOR_POINT, 0, parent ) );
            }
            childMoved = false;
        }
        else
        if ( isDependencyYBetweenAnchorPointAndToolbar( parent, child,dependency ) ) {
            if ( publish() ) {
                EventBus.getDefault().post( new EventMergedAppBarVisibility( true, (int) child.getY(), EventMergedAppBarVisibility.State.ABOVE_ANCHOR_POINT, 0, parent ) );
            }
            childMoved = ! mVisible;
        }
        else
        if ( isDependencyYBelowToolbar( child, dependency )  &&  ! isDependencyYReachTop( dependency ) ) {
            int partialHeight = (int)(toolbarBottom - dependency.getY());
            if ( publish() ) {
                EventBus.getDefault().post( new EventMergedAppBarVisibility( true, (int) child.getY(), EventMergedAppBarVisibility.State.INSIDE_TOOLBAR, partialHeight, parent ) );
            }
            childMoved = ! mVisible;
        }
        else
        if ( isDependencyYBelowStatusToolbar( child, dependency )  ||  isDependencyYReachTop( dependency ) ) {
            int partialHeight = (int)(toolbarBottom - dependency.getY());
            if ( publish() ) {
                EventBus.getDefault().post( new EventMergedAppBarVisibility( true, (int) child.getY(), EventMergedAppBarVisibility.State.TOP_OF_TOOLBAR, partialHeight, parent ) ); // end state
            }
            childMoved = ! mVisible;
        }

        return childMoved;
    }

    private void init( @NonNull CoordinatorLayout parent, @NonNull View child ) {

        AppBarLayout appBarLayout = (AppBarLayout) child;
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            appBarLayout.setOutlineProvider( ViewOutlineProvider.BACKGROUND );
        }

        getBottomSheetBehavior(parent);

        child.setVisibility(mVisible ? View.VISIBLE : View.INVISIBLE);
        mInit = true;
    }

    /**
     * Look into the CoordinatorLayout for the {@link BottomSheetBehaviorGoogleMapsLike}
     * @param coordinatorLayout with app:layout_behavior= {@link BottomSheetBehaviorGoogleMapsLike}
     */
    private void getBottomSheetBehavior( @NonNull CoordinatorLayout coordinatorLayout ) {

        for ( int i = 0; i < coordinatorLayout.getChildCount(); i++ ) {
            View child = coordinatorLayout.getChildAt(i);

            if ( child instanceof NestedScrollingParent ) {

                try {
                    BottomSheetBehaviorGoogleMapsLike temp = BottomSheetBehaviorGoogleMapsLike.from(child);
                    mBottomSheetBehaviorRef = new WeakReference<>(temp);
                    break;
                }
                catch (IllegalArgumentException e){}
            }
        }
    }

    private boolean isDependencyYBelowAnchorPoint( @NonNull CoordinatorLayout parent, @NonNull View dependency ) {

        if ( mBottomSheetBehaviorRef == null  ||  mBottomSheetBehaviorRef.get() == null )
            getBottomSheetBehavior( parent );
        return dependency.getY() > mBottomSheetBehaviorRef.get().getAnchorPoint() + DimensionUtils.getMergedToolbarHideThreshold( dependency.getContext() );
    }

    private boolean isDependencyYBetweenAnchorPointAndToolbar( @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency ) {
        if ( mBottomSheetBehaviorRef == null  ||  mBottomSheetBehaviorRef.get() == null )
            getBottomSheetBehavior( parent );
        return dependency.getY() <= mBottomSheetBehaviorRef.get().getAnchorPoint() + DimensionUtils.getMergedToolbarHideThreshold( dependency.getContext() )  &&  dependency.getY() > toolbarBottom;
    }

    private int toolbarBottom = 0;
    private int toolbarTop    = 0;
    public void setToolbarBottom( int toolbarBottom ) { this.toolbarBottom = toolbarBottom; }
    public void setToolbarTop(    int toolbarTop )    { this.toolbarTop = toolbarTop; }

    private boolean isDependencyYBelowToolbar( @NonNull View child, @NonNull View dependency ) {
        return dependency.getY() <= toolbarBottom  &&  dependency.getY() > toolbarTop;
    }

    private boolean isDependencyYBelowStatusToolbar( @NonNull View child, @NonNull View dependency ){
        return dependency.getY() <= toolbarTop;
    }

    private boolean isDependencyYReachTop( @NonNull View dependency ){
        return dependency.getY() == 0;
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, View child) {
        return new SavedState(super.onSaveInstanceState(parent, child), mVisible);
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, View child, Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        this.mVisible = ss.mVisible;
    }

    protected static class SavedState extends View.BaseSavedState {

        final boolean mVisible;

        public SavedState(Parcel source) {
            super(source);
            mVisible = source.readByte() != 0;
        }

        public SavedState(Parcelable superState, boolean visible) {
            super(superState);
            this.mVisible = visible;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (mVisible ? 1 : 0));
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    public static <V extends View> DelegatingMergedAppBarLayoutBehavior from(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof DelegatingMergedAppBarLayoutBehavior)) {
            throw new IllegalArgumentException("The view is not associated with " + "DelegatingMergedAppBarLayoutBehavior");
        }
        return (DelegatingMergedAppBarLayoutBehavior) behavior;
    }
}