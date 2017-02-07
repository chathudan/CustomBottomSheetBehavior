package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SlopSupportingNestedScrollView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.List;


/**
 * Floating action buttons are used for a special type of promoted action. They are distinguished
 * by a circled icon floating above the UI and have special motion behaviors related to morphing,
 * launching, and the transferring anchor point.
 *
 * <p>Floating action buttons come in two sizes: the default and the mini. The size can be
 * controlled with the {@code fabSize} attribute.</p>
 *
 * <p>The background color of this view defaults to the your theme's {@code colorAccent}. If you
 * wish to change this at runtime then you can do so via
 * {@link #setBackgroundTintList(ColorStateList)}.</p>
 */
@CoordinatorLayout.DefaultBehavior(FloatingFrameLayout.Behavior.class)
public class FloatingFrameLayout extends VisibilityAwareFrameLayout {

    private static final String LOG_TAG = "FloatingFrameLayout";

    /**
     * Callback to be invoked when the visibility of a FloatingActionButton changes.
     */
    public abstract static class OnVisibilityChangedListener {
        /**
         * Called when a FloatingActionButton has been
         * {@link #show(InternalVisibilityChangedListener) shown}.
         *
         * @param fab the FloatingActionButton that was shown.
         */
        public void onShown( FloatingFrameLayout fab) {}

        /**
         * Called when a FloatingActionButton has been
         * {@link #hide(InternalVisibilityChangedListener) hidden}.
         *
         * @param fab the FloatingActionButton that was hidden.
         */
        public void onHidden( FloatingFrameLayout fab) {}
    }

    public FloatingFrameLayout( Context context ) { this(context, null); }
    public FloatingFrameLayout( Context context, AttributeSet attrs ) { this(context, attrs, 0); }
    public FloatingFrameLayout( Context context, AttributeSet attrs, int defStyleAttr ) { super(context, attrs, defStyleAttr); }

    /**
     * Shows the button.
     * <p>This method will animate the button show if the view has already been laid out.</p>
     */
    public void show() {
        show( null );
    }

    /**
     * Shows the button.
     * <p>This method will animate the button show if the view has already been laid out.</p>
     *
     * @param listener the listener to notify when this view is shown
     */
    public void show(@Nullable final InternalVisibilityChangedListener listener) {
        show(listener, true);
    }

    //void show( FloatingFrameLayout.OnVisibilityChangedListener listener, boolean fromUser ) {
    //    show( listener, fromUser );
    //}

    /**
     * Hides the button.
     * <p>This method will animate the button hide if the view has already been laid out.</p>
     */
    public void hide() {
        hide(null);
    }

    /**
     * Hides the button.
     * <p>This method will animate the button hide if the view has already been laid out.</p>
     *
     * @param listener the listener to notify when this view is hidden
     */
    public void hide(@Nullable InternalVisibilityChangedListener listener) {
        hide(listener, true);
    }

    //void hide( @Nullable FloatingFrameLayout.OnVisibilityChangedListener listener, boolean fromUser ) {
    //    hide( listener, fromUser);
    //}

    /**
     * Behavior designed for use with {@link FloatingFrameLayout} instances. Its main function
     * is to move {@link FloatingFrameLayout} views so that any displayed {@link Snackbar}s do
     * not cover them.
     */
    public static class Behavior extends CoordinatorLayout.Behavior<FloatingFrameLayout> {
        private static final boolean AUTO_HIDE_DEFAULT = true;

        private Rect mTmpRect;
        private InternalVisibilityChangedListener mInternalAutoHideListener;
        private boolean mAutoHideEnabled;

        private int mHideTopOffsetPx = 0;
        public void setHideTopOffsetPx( int hideTopOffsetPx ) {
            mHideTopOffsetPx = hideTopOffsetPx;
        }

        private int mHideBottomOffsetPx = 120;
        public void setHideBottomOffsetPx( int hideBottomOffsetPx ) {
            mHideBottomOffsetPx = hideBottomOffsetPx;
        }

        public Behavior() {
            super();
            mAutoHideEnabled = AUTO_HIDE_DEFAULT;
        }

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs,
                    android.support.design.R.styleable.FloatingActionButton_Behavior_Layout);
            mAutoHideEnabled = a.getBoolean(
                    android.support.design.R.styleable.FloatingActionButton_Behavior_Layout_behavior_autoHide,
                    AUTO_HIDE_DEFAULT);
            a.recycle();
        }

        /**
         * Sets whether the associated FloatingActionButton automatically hides when there is
         * not enough space to be displayed. This works with {@link AppBarLayout}
         * and {@link BottomSheetBehavior}.
         *
         * @attr ref android.support.design.R.styleable#FloatingActionButton_Behavior_Layout_behavior_autoHide
         * @param autoHide true to enable automatic hiding
         */
        public void setAutoHideEnabled(boolean autoHide) {
            mAutoHideEnabled = autoHide;
        }

        /**
         * Returns whether the associated FloatingActionButton automatically hides when there is
         * not enough space to be displayed.
         *
         * @attr ref android.support.design.R.styleable#FloatingActionButton_Behavior_Layout_behavior_autoHide
         * @return true if enabled
         */
        public boolean isAutoHideEnabled() {
            return mAutoHideEnabled;
        }

        @Override
        public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams lp) {
            if (lp.dodgeInsetEdges == Gravity.NO_GRAVITY) {
                // If the developer hasn't set dodgeInsetEdges, lets set it to BOTTOM so that
                // we dodge any Snackbars
                lp.dodgeInsetEdges = Gravity.BOTTOM;
            }
        }

        @Override
        public boolean onDependentViewChanged( CoordinatorLayout parent, FloatingFrameLayout child, View dependency ) {
            if ( dependency instanceof SlopSupportingNestedScrollView ) {
                //updateVisibilityForNestedScrollView( parent, (NestedScrollView) dependency, child );
            }
            else
            if ( dependency instanceof AppBarLayout ) {
                // If we're depending on an AppBarLayout we will show/hide it automatically
                // if the FAB is anchored to the AppBarLayout
                updateVisibilityForAppBarLayout( parent, (AppBarLayout) dependency, child );
            }
            else
            if ( isBottomSheet( dependency ) ) {
                updateFabVisibilityForBottomSheet( dependency, child );
            }
            else
            if ( dependency instanceof ImageView ) {
                //updateVisibilityForAttributes( parent, dependency, child );
            }
            return false;
        }

        private static boolean isBottomSheet( @NonNull View view ) {
            final ViewGroup.LayoutParams lp = view.getLayoutParams();
            if ( lp instanceof CoordinatorLayout.LayoutParams ) {
                return ((CoordinatorLayout.LayoutParams) lp).getBehavior() instanceof BottomSheetBehavior;
            }
            return false;
        }

        @VisibleForTesting
        void setInternalAutoHideListener( InternalVisibilityChangedListener listener ) {
            mInternalAutoHideListener = listener;
        }

        private boolean shouldUpdateVisibility( View dependency, FloatingFrameLayout child ) {
            final CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            if ( ! mAutoHideEnabled ) {
                return false;
            }

            if ( lp.getAnchorId() != dependency.getId() ) {
                // The anchor ID doesn't match the dependency, so we won't automatically
                // show/hide the FAB
                return false;
            }

            // noinspection RedundantIfStatement
            if ( child.getUserSetVisibility() != VISIBLE ) {
                // The view isn't set to be visible so skip changing its visibility
                return false;
            }

            return true;
        }

        private static void getDescendantRect( ViewGroup parent, View descendant, Rect out ) {
            out.set( 0, 0, descendant.getWidth(), descendant.getHeight() );
            ViewGroupUtilsHoneycomb.offsetDescendantRect( parent, descendant, out );
        }

        private boolean updateVisibilityForNestedScrollView( CoordinatorLayout parent, SlopSupportingNestedScrollView nestedScrollView, FloatingFrameLayout child ) {
            if ( ! shouldUpdateVisibility( nestedScrollView, child ) ) {
                return false;
            }

            if ( mTmpRect == null ) {
                mTmpRect = new Rect();
            }

            // First, let's get the visible rect of the dependency
            final Rect rect = mTmpRect;
            getDescendantRect( parent, nestedScrollView, rect );

            if ( rect.top <= mHideTopOffsetPx  ) { // rltitlebar height
                // If the anchor's bottom is below the seam, we'll animate our FAB out
                child.hide( mInternalAutoHideListener, false );
            }
            else
            if ( rect.top >= parent.getHeight() - mHideBottomOffsetPx ) {
                // Hide
                child.hide( mInternalAutoHideListener, false );
                //FloatingFrameLayout tokens = (FloatingFrameLayout) ((View)child.getParent()).findViewById( R.id.tokens );
                //tokens.hide();
            }
            else {
                // Else, we'll animate our FAB back in
                child.show( mInternalAutoHideListener, false );
            }
            return true;
        }

        private boolean updateVisibilityForAttributes( CoordinatorLayout parent, View dependency, FloatingFrameLayout child ) {
            if ( ! shouldUpdateVisibility( dependency, child ) ) {
                return false;
            }

            if ( mTmpRect == null ) {
                mTmpRect = new Rect();
            }

            // There are two dependencies here:
            // 1) Collision with titlebar when scrolling up
            // 2) Backdrop colliding with titlebar when scrolling down

            // First, let's get the visible rect of the dependency
            final Rect rect = mTmpRect;
            getDescendantRect( parent, dependency, rect );

            if ( rect.top >= 1200  ) { // rltitlebar height
                // If the anchor's bottom is below the seam, we'll animate our FAB out
                child.hide( mInternalAutoHideListener, false );
            }
            else {
                // Else, we'll animate our FAB back in
                child.show( mInternalAutoHideListener, false );
            }
            return true;
        }


        private boolean updateVisibilityForAppBarLayout( CoordinatorLayout parent, AppBarLayout appBarLayout, FloatingFrameLayout child ) {
            if ( !shouldUpdateVisibility( appBarLayout, child ) ) {
                return false;
            }

            if ( mTmpRect == null ) {
                mTmpRect = new Rect();
            }

            // First, let's get the visible rect of the dependency
            final Rect rect = mTmpRect;
            getDescendantRect( parent, appBarLayout, rect );

            int appBarLayoutMinHeightForVisibleOverlappingContent = 0;
            final int topInset = 0;
            final int minHeight = ViewCompat.getMinimumHeight( appBarLayout );
            if ( minHeight != 0 ) {
                // If this layout has a min height, use it (doubled)
                appBarLayoutMinHeightForVisibleOverlappingContent = (minHeight * 2) + topInset;
            }

            // Otherwise, we'll use twice the min height of our last child
            final int childCount = appBarLayout.getChildCount();
            final int lastChildMinHeight = childCount >= 1 ? ViewCompat.getMinimumHeight( appBarLayout.getChildAt( childCount - 1 ) ) : 0;
            if ( lastChildMinHeight != 0 ) {
                appBarLayoutMinHeightForVisibleOverlappingContent = (lastChildMinHeight * 2) + topInset;
            }

            // If we reach here then we don't have a min height explicitly set. Instead we'll take a
            // guess at 1/3 of our height being visible
            if ( appBarLayoutMinHeightForVisibleOverlappingContent == 0 ) {
                appBarLayoutMinHeightForVisibleOverlappingContent = appBarLayout.getHeight() / 3;
            }
            if ( rect.bottom <= appBarLayoutMinHeightForVisibleOverlappingContent + mHideTopOffsetPx  ) { // rltitlebar height
                // If the anchor's bottom is below the seam, we'll animate our FAB out
                child.hide( mInternalAutoHideListener, false );
            }
            else {
                // Else, we'll animate our FAB back in
                child.show( mInternalAutoHideListener, false );
            }
            return true;
        }

        private boolean updateFabVisibilityForBottomSheet( View bottomSheet, FloatingFrameLayout child ) {
            if ( ! shouldUpdateVisibility( bottomSheet, child ) ) {
                return false;
            }
            CoordinatorLayout.LayoutParams lp =
                    (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            if (bottomSheet.getTop() < child.getHeight() / 2 + lp.topMargin) {
                child.hide(mInternalAutoHideListener, false);
            } else {
                child.show(mInternalAutoHideListener, false);
            }
            return true;
        }

        @Override
        public boolean onLayoutChild( CoordinatorLayout parent, FloatingFrameLayout child, int layoutDirection ) {
            // First, let's make sure that the visibility of the FAB is consistent
            final List<View> dependencies = parent.getDependencies( child );
            for ( int i = 0, count = dependencies.size(); i < count; i++ ) {
                final View dependency = dependencies.get( i );
                if ( dependency instanceof AppBarLayout ) {
                    if ( updateVisibilityForAppBarLayout( parent, (AppBarLayout) dependency, child ) ) {
                        break;
                    }
                }
                else
                if ( isBottomSheet( dependency ) ) {
                    if ( updateFabVisibilityForBottomSheet( dependency, child ) ) {
                        break;
                    }
                }
                else {
                    if ( updateVisibilityForAttributes( parent, dependency, child ) ) {
                        break;
                    }
                }
            }
            // Now let the CoordinatorLayout lay out the FAB
            parent.onLayoutChild( child, layoutDirection );

            return true;
        }
    }

    // Internal hide/show animation implementation details
    private static final int ANIM_STATE_NONE    = 0;
    private static final int ANIM_STATE_HIDING  = 1;
    private static final int ANIM_STATE_SHOWING = 2;

    private int mAnimState          = ANIM_STATE_NONE;
    private int mInternalVisibility = View.VISIBLE;
    private static final Interpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator FAST_OUT_LINEAR_IN_INTERPOLATOR = new LinearInterpolator();

    static final int SHOW_HIDE_ANIM_DURATION = 130;

    private interface InternalVisibilityChangedListener {
        void onShown();
        void onHidden();
    }

    private boolean isOrWillBeShown() {
        if ( mInternalVisibility != View.VISIBLE) {
            // If we not currently visible, return true if we're animating to be shown
            return mAnimState == ANIM_STATE_SHOWING;
        } else {
            // Otherwise if we're visible, return true if we're not animating to be hidden
            return mAnimState != ANIM_STATE_HIDING;
        }
    }

    private boolean isOrWillBeHidden() {
        if ( mInternalVisibility == View.VISIBLE ) {
            // If we currently visible, return true if we're animating to be hidden
            return mAnimState == ANIM_STATE_HIDING;
        } else {
            // Otherwise if we're not visible, return true if we're not animating to be shown
            return mAnimState != ANIM_STATE_SHOWING;
        }
    }

    private boolean shouldAnimateVisibilityChange() {
        return ViewCompat.isLaidOut( this )  &&  ! this.isInEditMode();
    }


    private boolean mAnimateSize = false;
    public void setAnimateSize( boolean animateSize ) {
        mAnimateSize = animateSize;
    }
    private void hide( @Nullable final InternalVisibilityChangedListener listener, final boolean fromUser ) {
        if ( isOrWillBeHidden() ) {
            // We either are or will soon be hidden, skip the call
            return;
        }

        animate().cancel();

        if ( shouldAnimateVisibilityChange() ) {
            mAnimState = ANIM_STATE_HIDING;

            ViewPropertyAnimator animator = animate()
                    .alpha(0f)
                    .setDuration( SHOW_HIDE_ANIM_DURATION )
                    .setInterpolator( FAST_OUT_LINEAR_IN_INTERPOLATOR )
                    .setListener( new AnimatorListenerAdapter() {
                        private boolean mCancelled;

                        @Override
                        public void onAnimationStart( Animator animation ) {
                            internalSetVisibility( View.VISIBLE, fromUser );
                            mCancelled = false;
                        }

                        @Override
                        public void onAnimationCancel( Animator animation ) {
                            mCancelled = true;
                        }

                        @Override
                        public void onAnimationEnd( Animator animation ) {
                            mAnimState = ANIM_STATE_NONE;

                            if ( ! mCancelled ) {
                                internalSetVisibility( fromUser ? View.GONE : View.INVISIBLE, fromUser );
                                if ( listener != null ) {
                                    listener.onHidden();
                                }
                            }
                        }
                    });
            if ( mAnimateSize ) {
                animator
                        .scaleX( 0f )
                        .scaleY( 0f );
            }
        } else {
            // If the view isn't laid out, or we're in the editor, don't run the animation
            internalSetVisibility( fromUser ? View.GONE : View.INVISIBLE, fromUser );
            if ( listener != null ) {
                listener.onHidden();
            }
        }
    }

    void show( @Nullable final InternalVisibilityChangedListener listener, final boolean fromUser ) {
        if ( isOrWillBeShown() ) {
            // We either are or will soon be visible, skip the call
            return;
        }

        animate().cancel();

        if ( shouldAnimateVisibilityChange() ) {
            mAnimState = ANIM_STATE_SHOWING;

            if ( mInternalVisibility != View.VISIBLE ) {
                // If the view isn't visible currently, we'll animate it from a single pixel
                setAlpha(0f);
            }

            ViewPropertyAnimator animator = animate()
                    .alpha(1f)
                    .setDuration( SHOW_HIDE_ANIM_DURATION )
                    .setInterpolator( LINEAR_OUT_SLOW_IN_INTERPOLATOR )
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            internalSetVisibility( View.VISIBLE, fromUser );
                        }

                        @Override
                        public void onAnimationEnd( Animator animation ) {
                            mAnimState = ANIM_STATE_NONE;
                            if ( listener != null ) {
                                listener.onShown();
                            }
                        }
                    });
            if ( mAnimateSize ) {
                animator
                        .scaleX( 1f )
                        .scaleY( 1f );
            }
        } else {
            internalSetVisibility( View.VISIBLE, fromUser );
            setAlpha( 1f );
            setScaleX( 1f );
            setScaleY( 1f );
            if ( listener != null ) {
                listener.onShown();
            }
        }
    }
}
