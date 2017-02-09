package co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils.DimensionUtils;


/**
 * Hides the BottomSheet shadow when the BottomSheet is Hidden.
  */
public class BottomSheetShadowBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
    /**
     * To avoid using multiple "peekheight=" in XML and looking flexibility allowing {@link BottomSheetBehaviorGoogleMapsLike#mPeekHeight}
     * get changed dynamically we get the {@link NestedScrollingParent} that has
     * "app:layout_behavior=" {@link BottomSheetBehaviorGoogleMapsLike} inside the {@link CoordinatorLayout}
     */
    private WeakReference<BottomSheetBehaviorGoogleMapsLike> mBottomSheetBehaviorRef;
    /**
     * Following {@link #onDependentViewChanged}'s docs mCurrentChildY just save the child Y
     * position.
     */
    private int mCurrentChildY;

    public BottomSheetShadowBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if ( dependency instanceof NestedScrollingParent ) {
            try {
                BottomSheetBehaviorGoogleMapsLike.from(dependency);
                return true;
            }
            catch (IllegalArgumentException e){}
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged( CoordinatorLayout parent, View child, View dependency ) {

        /**
         * collapsedY and anchorPointY are calculated every time looking for
         * flexibility, in case that dependency's height, child's height or {@link BottomSheetBehaviorGoogleMapsLike#getPeekHeight()}'s
         * value changes throught the time, I mean, you can have a {@link android.widget.ImageView}
         * using images with different sizes and you don't want to resize them or so
         */
        if ( mBottomSheetBehaviorRef == null  ||  mBottomSheetBehaviorRef.get() == null ) {
            getBottomSheetBehavior( parent );
        }

        int oldVisibility = child.getVisibility();
        int newVisibility;
        if ( dependency.getY() >= dependency.getHeight() ) {
            newVisibility = View.INVISIBLE;
        }
        else {
            newVisibility = View.VISIBLE;
        }

        if ( oldVisibility != newVisibility ) {
            child.setVisibility( newVisibility );
            return true;
        } else {
            return false;
        }
    }

    /**
     * Look into the CoordiantorLayout for the {@link BottomSheetBehaviorGoogleMapsLike}
     * @param coordinatorLayout with app:layout_behavior= {@link BottomSheetBehaviorGoogleMapsLike}
     */
    private void getBottomSheetBehavior(@NonNull CoordinatorLayout coordinatorLayout) {

        for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
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
}