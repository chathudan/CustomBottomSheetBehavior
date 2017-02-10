package co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.EventBottomSheetPosition;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils.DimensionUtils;

/**
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~
 ~ https://github.com/miguelhincapie/CustomBottomSheetBehavior
 */

/**
 * This class will link the Backdrop element (that can be anything extending View) with a
 * NestedScrollView (the dependency). Whenever dependecy is moved, the backdrop will be moved too
 * behaving like parallax effect.
 *
 * The backdrop need to be <bold>into</bold> a CoordinatorLayout and <bold>before</bold>
 * {@link BottomSheetBehaviorGoogleMapsLike} in the XML file to get same behavior like Google Maps.
 * It doesn't matter where the backdrop element start in XML, it will be moved following
 * Google Maps's parallax behavior.
 * @param <V>
 */
public class BackdropBottomSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
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

    public BackdropBottomSheetBehavior(Context context, AttributeSet attrs) {
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
         * value changes throughout the time, I mean, you can have a {@link android.widget.ImageView}
         * using images with different sizes and you don't want to resize them or so
         */
        if ( mBottomSheetBehaviorRef == null || mBottomSheetBehaviorRef.get() == null)
            getBottomSheetBehavior(parent);

        int peekHeight = DimensionUtils.getPeekHeight( dependency.getContext() );
        int appBarHeight = DimensionUtils.getStatusBarHeight( dependency.getContext() ) + DimensionUtils.getToolbarHeight( dependency.getContext() );
        int extraOffsetForExpanded = peekHeight - appBarHeight;

        /**
         * mCollapsedY: Y position in where backdrop get hidden behind dependency.
         * {@link BottomSheetBehaviorGoogleMapsLike#getPeekHeight()} and collapsedY are the same point on screen.
         */
        int collapsedY = dependency.getHeight() - extraOffsetForExpanded - peekHeight;
        /**
         * anchorPointY: with top being Y=0, anchorPointY defines the point in Y where could
         * happen 2 things:
         * The backdrop should be moved behind dependency view (when {@link #mCurrentChildY} got
         * positive values) or the dependency view overlaps the backdrop (when
         * {@link #mCurrentChildY} got negative values)
         */
        int anchorPointY = child.getHeight();
        /**
         * lastCurrentChildY: Just to know if we need to return true or false at the end of this
         * method.
         */
        int lastCurrentChildY = mCurrentChildY;

        mCurrentChildY = (int) ((dependency.getY() - anchorPointY) * collapsedY / (collapsedY-anchorPointY));
        if ( mCurrentChildY <= 0 ) {
            mCurrentChildY = 0;
        }
        //else
        //if ( mCurrentChildY >= dependency.getHeight() - peekHeight ) {
        //    mCurrentChildY = dependency.getHeight() - peekHeight;
        //}

        child.setY( mCurrentChildY );

        return ( lastCurrentChildY != mCurrentChildY );
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