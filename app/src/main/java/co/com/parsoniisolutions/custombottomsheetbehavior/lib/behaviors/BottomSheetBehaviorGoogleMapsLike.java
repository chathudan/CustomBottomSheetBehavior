package co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CustomViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPage;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.EventBottomSheetState;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.scrolltracking.ScrollTrackingBehavior;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.views.SlopSupportingNestedScrollView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.Vector;


public class BottomSheetBehaviorGoogleMapsLike<V extends View> extends ScrollTrackingBehavior<V> {

    /**
     * Callback for monitoring events about bottom sheets.
     */
    public abstract static class BottomSheetCallback {

        /**
         * Called when the bottom sheet changes its state.
         *
         * @param bottomSheet The bottom sheet view.
         * @param newState    The new state. This will be one of {@link #STATE_DRAGGING},
         *                    {@link #STATE_SETTLING}, {@link #STATE_ANCHOR_POINT},
         *                    {@link #STATE_EXPANDED},
         *                    {@link #STATE_COLLAPSED}, or {@link #STATE_HIDDEN}.
         */
        public abstract void onStateChanged(@NonNull View bottomSheet, @State int newState);

        /**
         * Called when the bottom sheet is being dragged.
         *
         * @param bottomSheet The bottom sheet view.
         * @param slideOffset The new offset of this bottom sheet within its range, from 0 to 1
         *                    when it is moving upward, and from 0 to -1 when it moving downward.
         */
        public abstract void onSlide(@NonNull View bottomSheet, float slideOffset);
    }

    /**
     * The bottom sheet is dragging.
     */
    public static final int STATE_DRAGGING = 1;

    /**
     * The bottom sheet is settling.
     */
    public static final int STATE_SETTLING = 2;

    /**
     * The bottom sheet is expanded_half_way.
     */
    public static final int STATE_ANCHOR_POINT = 3;

    /**
     * The bottom sheet is expanded.
     */
    public static final int STATE_EXPANDED = 4;

    /**
     * The bottom sheet is collapsed.
     */
    public static final int STATE_COLLAPSED = 5;

    /**
     * The bottom sheet is hidden.
     */
    public static final int STATE_HIDDEN = 6;

    /** @hide */
    @IntDef( {STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_ANCHOR_POINT, STATE_SETTLING, STATE_HIDDEN} )
    @Retention( RetentionPolicy.SOURCE )
    public @interface State {}

    private static final float HIDE_THRESHOLD = 0.5f;
    private static final float HIDE_FRICTION  = 0.1f;

    private float mMinimumVelocity;

    private int mPeekHeight;

    private int mMinOffset;
    private int mMaxOffset;

    private static final int DEFAULT_ANCHOR_POINT = 700;
    private int mAnchorPoint;

    private boolean mHideable     = true;
    private boolean mCollapseable = true;

    @State
    private int mState = STATE_ANCHOR_POINT;
    @State
    private int mLastStableState = STATE_ANCHOR_POINT;
    @State
    private int mSettlingToState = STATE_ANCHOR_POINT; // If settling, this is the state we are settling to


    private CustomViewDragHelper mViewDragHelper;

    private boolean mIgnoreEvents;

    private boolean mNestedScrolled;
    private boolean mEventCancelled;

    private int mParentHeight;

    private WeakReference<V> mViewRef;

    private WeakReference<View> mNestedScrollingChildRef;

    private Vector<BottomSheetCallback> mCallback;

    private int mActivePointerId;

    private int mInitialY;

    private boolean mTouchingScrollingChild;

    private int MIN_DISTANCE_FOR_FLING_PX;

    /**
     * Default constructor for instantiating BottomSheetBehaviors.
     */
    public BottomSheetBehaviorGoogleMapsLike() { }

    /**
     * Default constructor for inflating BottomSheetBehaviors from layout.
     *
     * @param context The {@link Context}.
     * @param attrs   The {@link AttributeSet}.
     */
    public BottomSheetBehaviorGoogleMapsLike( Context context, AttributeSet attrs ) {
        super( context, attrs );

        /**
         * Get the anchorPoint
         */
        mAnchorPoint = DEFAULT_ANCHOR_POINT;
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.CustomBottomSheetBehavior );
        if ( attrs != null ) {
            mAnchorPoint  = (int) a.getDimension( R.styleable.CustomBottomSheetBehavior_anchorPoint, 0 );
        }
        a.recycle();

        /**
         * Get the peek height
         */
        a = context.obtainStyledAttributes( attrs, android.support.design.R.styleable.BottomSheetBehavior_Layout );
        setPeekHeight( a.getDimensionPixelSize( android.support.design.R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, 0 ) );
        setHideable( a.getBoolean( android.support.design.R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false ) );
        a.recycle();

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumVelocity = 4*configuration.getScaledMinimumFlingVelocity();

        MIN_DISTANCE_FOR_FLING_PX = (int) context.getResources().getDimension( R.dimen.min_distance_for_fling );
    }

    @Override
    public Parcelable onSaveInstanceState( CoordinatorLayout parent, V child ) {
        return new SavedState(super.onSaveInstanceState(parent, child), mState);
    }

    @Override
    public void onRestoreInstanceState( CoordinatorLayout parent, V child, Parcelable state ) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        // Intermediate states are restored as collapsed state
        if ( ss.state == STATE_DRAGGING  ||  ss.state == STATE_SETTLING ) {
            mState = STATE_COLLAPSED;
        } else {
            mState = ss.state;
        }

        mLastStableState = mState;
    }

    @Override
    public boolean onLayoutChild( CoordinatorLayout parent, V child, int layoutDirection ) {
        // First let the parent lay it out
        if ( mState != STATE_DRAGGING  &&  mState != STATE_SETTLING ) {
            if ( ViewCompat.getFitsSystemWindows( parent )  &&  ! ViewCompat.getFitsSystemWindows(child) ) {
                ViewCompat.setFitsSystemWindows( child, true );
            }
            parent.onLayoutChild( child, layoutDirection );
        }

        // Offset the bottom sheet
        mParentHeight = parent.getHeight();
        mMinOffset    = Math.max( 0, mParentHeight - child.getHeight() );
        mMaxOffset    = Math.max( mParentHeight - mPeekHeight, mMinOffset );

        /**
         * New behavior
         */
        if ( mState == STATE_ANCHOR_POINT ) {
            ViewCompat.offsetTopAndBottom( child, mAnchorPoint );
        }
        else
        if ( mState == STATE_EXPANDED ) {
            ViewCompat.offsetTopAndBottom( child, mMinOffset );
        }
        else
        if ( mCollapseable  &&  mHideable  &&  mState == STATE_HIDDEN ) {
            ViewCompat.offsetTopAndBottom( child, mParentHeight );
        }
        else
        if ( mCollapseable  &&  mState == STATE_COLLAPSED ) {
            ViewCompat.offsetTopAndBottom( child, mMaxOffset );
        }
        else {
            // Default to anchor
            ViewCompat.offsetTopAndBottom( child, mAnchorPoint );
        }
        if ( mViewDragHelper == null ) {
            mViewDragHelper = CustomViewDragHelper.create( parent, mDragCallback );
        }
        mViewRef = new WeakReference<>(child);
        mNestedScrollingChildRef = new WeakReference<>( findScrollingChild( child ) );

        return super.onLayoutChild( parent, child, layoutDirection );
    }


    @Override
    public boolean onInterceptTouchEvent( CoordinatorLayout parent, V child, MotionEvent event ) {
        super.onInterceptTouchEvent( parent, child, event );

        if ( ! child.isShown() ) {
            return false;
        }

        int action = MotionEventCompat.getActionMasked( event );
        if ( action == MotionEvent.ACTION_DOWN ) {
            reset();
        }

        switch ( action ) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mEventCancelled = true;
                mTouchingScrollingChild = false;
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                // Reset the ignore flag
                if ( mIgnoreEvents ) {
                    mIgnoreEvents = false;
                    return false;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                int initialX = (int) event.getX();
                mInitialY = (int) event.getY();
                if ( mState == STATE_ANCHOR_POINT ) {
                    mActivePointerId = event.getPointerId(event.getActionIndex());
                    mTouchingScrollingChild = true;
                } else {
                    View scroll = mNestedScrollingChildRef.get();
                    if (scroll != null && parent.isPointInChildBounds(scroll, initialX, mInitialY)) {
                        mActivePointerId = event.getPointerId(event.getActionIndex());
                        mTouchingScrollingChild = true;
                    }
                }
                mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID &&
                        !parent.isPointInChildBounds(child, initialX, mInitialY);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }

        //if ( action == MotionEvent.ACTION_CANCEL ) {
            // Also, we don't want to trigger a BottomSheet click as a result of cancel
            //mEventCancelled = true;
        //}

        if ( ! mIgnoreEvents  &&  mViewDragHelper.shouldInterceptTouchEvent( event ) ) {
            return true;
        }
        // We have to handle cases that the CustomViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        View scroll = mNestedScrollingChildRef.get();
        boolean ret =
                        action == MotionEvent.ACTION_MOVE  &&  scroll != null  &&
                        ! mIgnoreEvents &&
                        mState != STATE_DRAGGING &&
                        ! parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY()) &&
                        Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop();
        return ret;
    }

    @Override
    public boolean onTouchEvent( CoordinatorLayout parent, V child, MotionEvent event ) {
        if ( ! child.isShown() ) {
            return false;
        }

        int action = MotionEventCompat.getActionMasked( event );
        if ( mState == STATE_DRAGGING  &&  action == MotionEvent.ACTION_DOWN ) {
            return true;
        }

        if ( action == MotionEvent.ACTION_DOWN ) {
            reset();
        }

        mViewDragHelper.processTouchEvent( event );

        // The CustomViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if ( action == MotionEvent.ACTION_MOVE  &&  ! mIgnoreEvents ) {
            int slop = mViewDragHelper.getTouchSlop();
            if ( child instanceof SlopSupportingNestedScrollView ) {
                slop = ((SlopSupportingNestedScrollView)child).touchSlop();
            }
            if ( Math.abs(mInitialY - event.getY()) > slop ) {
                mViewDragHelper.captureChildView( child, event.getPointerId(event.getActionIndex()) );
            }
        }

        return ! mIgnoreEvents;
    }

    @Override
    public boolean onStartNestedScroll( CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes ) {
        mNestedScrolled = false;
        return ( nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL ) != 0;
    }

    @Override
    public void onNestedPreScroll( CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed ) {
        super.onNestedPreScroll( coordinatorLayout, child, target, dx, dy, consumed );

        View scrollingChild = mNestedScrollingChildRef.get();
        if ( target != scrollingChild ) {
            return;
        }

        int currentTop = child.getTop();
        int newTop     = currentTop - dy;

        // Force-stop at the anchor - do not go from collapsed to expanded in one scroll
        if (
                ( mLastStableState == STATE_COLLAPSED  &&  newTop < mAnchorPoint )  ||
                ( mLastStableState == STATE_EXPANDED   &&  newTop > mAnchorPoint )
           ) {
            consumed[1] = dy;
            ViewCompat.offsetTopAndBottom( child, mAnchorPoint - currentTop );
            dispatchOnSlide( child.getTop() );
            mNestedScrolled = true;
            return;
        }

        // Do not allow collapse if not collapseable
        if ( ! mCollapseable  &&  newTop > mAnchorPoint ) {
            consumed[1] = dy;
            ViewCompat.offsetTopAndBottom( child, mAnchorPoint - currentTop );
            dispatchOnSlide( child.getTop() );
            mNestedScrolled = true;
            return;
        }

        if ( dy > 0 ) { // Upward
            if ( newTop < mMinOffset ) {
                consumed[1] = currentTop - mMinOffset;
                ViewCompat.offsetTopAndBottom( child, -consumed[1] );
                setStateInternal( STATE_EXPANDED, STATE_EXPANDED );
            } else {
                consumed[1] = dy;
                ViewCompat.offsetTopAndBottom( child, -dy );
                setStateInternal( STATE_DRAGGING, STATE_DRAGGING );
            }
        }
        else
        if ( dy < 0 ) { // Downward
            if ( ! ViewCompat.canScrollVertically(target, -1) ) {
                if ( newTop <= mMaxOffset || mHideable ) {
                    consumed[1] = dy;
                    ViewCompat.offsetTopAndBottom( child, -dy );
                    setStateInternal( STATE_DRAGGING, STATE_DRAGGING );
                } else {
                    consumed[1] = currentTop - mMaxOffset;
                    ViewCompat.offsetTopAndBottom( child, -consumed[1] );
                    setStateInternal( STATE_COLLAPSED, STATE_COLLAPSED );
                }
            }
        }
        dispatchOnSlide( child.getTop() );
        mNestedScrolled = true;
    }

    @Override
    public void onStopNestedScroll( CoordinatorLayout coordinatorLayout, V child, View target ) {

        if ( child.getTop() == mMinOffset ) {
            setStateInternal( STATE_EXPANDED, STATE_EXPANDED );
            mLastStableState = STATE_EXPANDED;
            return;
        }
        if ( target != mNestedScrollingChildRef.get()  ) {
            return;
        }

        if ( ! mNestedScrolled )
            return;

        int top;
        int targetState;

        if ( mEventCancelled ) {
            // If event was cancelled, snap back to the last stable state
            if ( mHideable  &&  mLastStableState == STATE_HIDDEN ) {
                top = mParentHeight;
                targetState = STATE_HIDDEN;
            }
            else
            if ( mCollapseable  &&  mLastStableState == STATE_COLLAPSED ) {
                top = mMaxOffset;
                targetState = STATE_COLLAPSED;
            }
            else
            if ( mLastStableState == STATE_EXPANDED ) {
                top = mMinOffset;
                targetState = STATE_EXPANDED;
            }
            else
            {
                top = mAnchorPoint;
                targetState = STATE_ANCHOR_POINT;
            }
        }
        else
        if ( ! mNestedScrolled  &&  mLastStableState == STATE_COLLAPSED ) {
            top = mAnchorPoint;
            targetState = STATE_ANCHOR_POINT;
        }
        else
        {
            // Are we flinging up?
            float scrollVelocity    = getScrollVelocity();
            int totalScrollDistance = getTotalScrollDistancePx();

            if ( scrollVelocity > mMinimumVelocity && totalScrollDistance > MIN_DISTANCE_FOR_FLING_PX ) {
                if ( mLastStableState == STATE_COLLAPSED ) {
                    // Fling from collapsed to anchor
                    top = mAnchorPoint;
                    targetState = STATE_ANCHOR_POINT;
                }
                else
                if ( mLastStableState == STATE_ANCHOR_POINT ) {
                    if ( child.getTop() > mAnchorPoint ) {
                        // If we scrolled below anchor and are flinging up, fling back to anchor, not expanded state
                        top = mAnchorPoint;
                        targetState = STATE_ANCHOR_POINT;
                    }
                    else {
                        // Fling from anchor to expanded
                        top = mMinOffset;
                        targetState = STATE_EXPANDED;
                    }
                }
                else {
                    // We are already expanded
                    top = mMinOffset;
                    targetState = STATE_EXPANDED;
                }
            }
            else
                // Are we flinging down?
                if ( scrollVelocity < -mMinimumVelocity && totalScrollDistance < -MIN_DISTANCE_FOR_FLING_PX ) {
                    if ( mLastStableState == STATE_EXPANDED ) {
                        // Fling from expanded to anchor
                        top = mAnchorPoint;
                        targetState = STATE_ANCHOR_POINT;
                    }
                    else
                    if ( mLastStableState == STATE_ANCHOR_POINT ) {
                        // If we scrolled above anchor and are flinging down, fling back to anchor, not collapsed state
                        if ( child.getTop() < mAnchorPoint ) {
                            top = mAnchorPoint;
                            targetState = STATE_ANCHOR_POINT;
                        }
                        else
                        if ( mCollapseable ) {
                            // Fling from anchor to collapsed
                            top = mMaxOffset;
                            targetState = STATE_COLLAPSED;
                        }
                        else {
                            // Stay at anchor
                            top = mAnchorPoint;
                            targetState = STATE_ANCHOR_POINT;
                        }
                    }
                    else
                    if ( mHideable ) {
                        if ( mLastStableState == STATE_COLLAPSED ) {
                            // If we scrolled above collapsed and are flinging down, fling back to collapsed, not hidden state
                            if ( child.getTop() < mMaxOffset ) {
                                top = mMaxOffset;
                                targetState = STATE_COLLAPSED;
                            }
                            else {
                                top = mParentHeight;
                                targetState = STATE_HIDDEN;
                            }
                        }
                        else {
                            top = mMaxOffset;
                            targetState = STATE_COLLAPSED;
                        }
                    }
                    else {
                        // We are already collapsed
                        top = mMaxOffset;
                        targetState = STATE_COLLAPSED;
                    }
                }
                else
                    // Are we clicking on collapsed toolbar? If yes, expand
                    if ( scrollVelocity == 0  &&  totalScrollDistance == 0  &&  child.getTop() == mMaxOffset  &&  mLastStableState == STATE_COLLAPSED ) {
                        top = mAnchorPoint;
                        targetState = STATE_ANCHOR_POINT;
                    }
                    // Not flinging, just settle to the nearest state
                    else {
                        // Collapse?
                        int currentTop = child.getTop();
                        if ( currentTop > mAnchorPoint * 1.25 ) { // Multiply by 1.25 to account for parallax. The currentTop needs to be pulled down 50% of the anchor point before collapsing.
                            top = mMaxOffset;
                            targetState = STATE_COLLAPSED;
                        }
                        // Expand?
                        else
                        if ( currentTop < mAnchorPoint * 0.5 ) {
                            top = mMinOffset;
                            targetState = STATE_EXPANDED;
                        }
                        // Snap back to the anchor
                        else {
                            top = mAnchorPoint;
                            targetState = STATE_ANCHOR_POINT;
                        }
                    }
        }

        if ( mViewDragHelper.smoothSlideViewTo( child, child.getLeft(), top ) ) {
            setStateInternal( STATE_SETTLING, targetState );
            mSettlingToState = targetState;
            ViewCompat.postOnAnimation( child, new SettleRunnable( child, targetState, false ) );
        } else {
            setStateInternal( targetState, targetState );
        }

        mNestedScrolled = false;
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target,
            float velocityX, float velocityY) {
        return target == mNestedScrollingChildRef.get() &&
                (mState != STATE_EXPANDED ||
                        super.onNestedPreFling(coordinatorLayout, child, target,
                                velocityX, velocityY));
    }

    /**
     * Sets the height of the bottom sheet when it is collapsed.
     *
     * @param peekHeight The height of the collapsed bottom sheet in pixels.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_peekHeight
     */
    public final void setPeekHeight( int peekHeight ) {
        mPeekHeight = Math.max(0, peekHeight);
        mMaxOffset = mParentHeight - peekHeight;
    }

    /**
     * Gets the height of the bottom sheet when it is collapsed.
     *
     * @return The height of the collapsed bottom sheet.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_peekHeight
     */
    public final int getPeekHeight() {
        return mPeekHeight;
    }

    public void setAnchorPoint(int anchorPoint) {
        mAnchorPoint = anchorPoint;
    }
    public int getAnchorPoint(){
        return mAnchorPoint;
    }

    /**
     * Sets whether this bottom sheet can hide when it is swiped down.
     *
     * @param hideable {@code true} to make this bottom sheet hideable.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_hideable
     */
    public void setHideable( boolean hideable ) {
        mHideable = hideable;
    }

    public void setCollapseable( boolean collapseable ) {
        mCollapseable = collapseable;
    }

    /**
     * Gets whether this bottom sheet can hide when it is swiped down.
     *
     * @return {@code true} if this bottom sheet can hide.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_hideable
     */
    public boolean isHideable() {
        return mHideable;
    }

    public boolean isCollapseable() {
        return mCollapseable;
    }

    /**
     * Adds a callback to be notified of bottom sheet events.
     *
     * @param callback The callback to notify when bottom sheet events occur.
     */
    public void addBottomSheetCallback( BottomSheetCallback callback ) {
        if (mCallback == null)
            mCallback = new Vector<>();

        mCallback.add(callback);
    }

    /**
     * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
     * animation. Callbacks on registered listeners will be called.
     *
     * @param state One of {@link #STATE_COLLAPSED}, {@link #STATE_ANCHOR_POINT},
     *              {@link #STATE_EXPANDED} or {@link #STATE_HIDDEN}.
     */
    public final void setState( @State int state ) {
        setState( state, false );
    }

    /**
     * Sets the state of the bottom sheet. The bottom sheet will transition to that state with or without
     * animation, and callbacks on registered listeners will or will not be called, depending on noCallbacksNoAnim parameter.
     *
     * @param state One of {@link #STATE_COLLAPSED}, {@link #STATE_ANCHOR_POINT},
     *              {@link #STATE_EXPANDED} or {@link #STATE_HIDDEN}.
     */
    public final void setState( @State int state, boolean noCallbacksNoAnim ) {
        if ( mState == STATE_SETTLING  &&  state == mSettlingToState ) {
            // Nothing to do, we will settle to this state shortly
            return;
        }
        else
        if ( mState == STATE_DRAGGING ) {
            // What to do?
        }
        else
        if ( state == mState ) {
            return;
        }

        if ( mViewRef == null ) {
            // The view is not laid out yet; modify mState and let onLayoutChild handle it later
            if ( isStateStable( state ) ) {
                if ( ! mHideable  &&  state == STATE_HIDDEN ) {
                    state = STATE_COLLAPSED;
                }
                if ( ! mCollapseable  &&  state == STATE_COLLAPSED ) {
                    state = STATE_ANCHOR_POINT;
                }

                mState = state;
                mLastStableState = state;
            }

            if ( ! noCallbacksNoAnim ) {
                EventBus.getDefault().post( new EventBottomSheetState( state, state, mParentBottomSheetPage ) );
            }
            return;
        }
        V child = mViewRef.get();
        if ( child == null ) {
            return;
        }
        int top;
        if ( state == STATE_ANCHOR_POINT ) {
            top = mAnchorPoint;
        }
        else
        if ( state == STATE_EXPANDED ) {
            top = mMinOffset;
        }
        else
        if ( mHideable  &&  state == STATE_HIDDEN ) {
            top = mParentHeight;
        }
        else
        if ( mCollapseable  &&  state == STATE_COLLAPSED ) {
            top = mMaxOffset;
        }
        else {
            top = mAnchorPoint; // DEFAULT
        }

        //if ( noCallbacksNoAnim  &&  mLastStableState == STATE_EXPANDED  &&  state == STATE_ANCHOR_POINT ) {
        //    mNestedScrollingChildRef.get().scrollTo( 0, 0 );
            //onNestedFling( (CoordinatorLayout)mNestedScrollingChildRef.get().getParent(), (V)mNestedScrollingChildRef.get(), mNestedScrollingChildRef.get(), 0, 10000, true );
        //}
        //else {

        if ( ! noCallbacksNoAnim ) {
            setStateInternal( STATE_SETTLING, state );
            mSettlingToState = state;
            if ( mViewDragHelper.smoothSlideViewTo( child, child.getLeft(), top ) ) {
                ViewCompat.postOnAnimation( child, new SettleRunnable( child, state, noCallbacksNoAnim ) );
            }
        }
        else {
            setStateInternal( STATE_SETTLING, state, noCallbacksNoAnim );
            mSettlingToState = state;
            if ( mViewDragHelper.smoothSlideViewTo( child, child.getLeft(), top, 0 ) ) {
                ViewCompat.postOnAnimation( child, new SettleRunnable( child, state, noCallbacksNoAnim ) );
            }
        }
    }

    /**
     * Gets the current state of the bottom sheet.
     *
     * @return One of {@link #STATE_EXPANDED}, {@link #STATE_ANCHOR_POINT}, {@link #STATE_COLLAPSED},
     * {@link #STATE_DRAGGING}, and {@link #STATE_SETTLING}.
     */
    @State
    public final int getState() {
        return mState;
    }

    @State
    public final int getSettlingToState() {
        return mSettlingToState;
    }

    @State
    public final int getLastStableState() {
        return mLastStableState;
    }

    private void setStateInternal( @State int state, @State int targetState ) {
        setStateInternal( state, targetState, false );
    }

    private void setStateInternal( @State int state, @State int targetState, boolean noCallbacksNoAnim ) {
        if ( mState == state ) {
            return;
        }

        if ( ! noCallbacksNoAnim ) {
            EventBus.getDefault().post( new EventBottomSheetState( state, targetState, mParentBottomSheetPage ) );
        }

        mState = state;
        View bottomSheet = mViewRef.get();
        if ( mNestedScrollingChildRef.get() != null  &&  mNestedScrollingChildRef.get() instanceof SlopSupportingNestedScrollView ) {
            SlopSupportingNestedScrollView scroll = (SlopSupportingNestedScrollView) mNestedScrollingChildRef.get();
            if ( state == STATE_COLLAPSED  ||  state == STATE_ANCHOR_POINT ) {
                scroll.setApplyHighTouchSlop( true );
            }
            else {
                scroll.setApplyHighTouchSlop( false );
            }
        }

        if ( isStateStable( state ) ) {
            mLastStableState = state;

            if ( bottomSheet != null  &&  mCallback != null  &&  ! noCallbacksNoAnim ) {
                notifyStateChangedToListeners( bottomSheet, state );
            }
        }
    }

    private void notifyStateChangedToListeners( @NonNull View bottomSheet, @State int newState ) {
        for ( BottomSheetCallback bottomSheetCallback : mCallback ) {
            bottomSheetCallback.onStateChanged( bottomSheet, newState );
        }
    }

    private void notifyOnSlideToListeners( @NonNull View bottomSheet, float slideOffset ) {
        for ( BottomSheetCallback bottomSheetCallback:mCallback ) {
            bottomSheetCallback.onSlide( bottomSheet, slideOffset );
        }
    }

    private void reset() {
        mActivePointerId = CustomViewDragHelper.INVALID_POINTER;
        mEventCancelled = false;
    }

    private boolean shouldHide( View child, float yvel ) {
        if ( child.getTop() < mMaxOffset ) {
            // It should not hide, but collapse.
            return false;
        }
        final float newTop = child.getTop() + yvel * HIDE_FRICTION;
        return Math.abs(newTop - mMaxOffset) / (float) mPeekHeight > HIDE_THRESHOLD;
    }

    private View findScrollingChild( View view ) {
        if ( view instanceof NestedScrollingChild ) {
            return view;
        }
        if ( view instanceof ViewGroup ) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0, count = group.getChildCount(); i < count; i++) {
                View scrollingChild = findScrollingChild(group.getChildAt(i));
                if (scrollingChild != null) {
                    return scrollingChild;
                }
            }
        }
        return null;
    }

    private final CustomViewDragHelper.Callback mDragCallback = new CustomViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView( View child, int pointerId ) {
            if ( mState == STATE_DRAGGING ) {
                return false;
            }
            if ( mTouchingScrollingChild ) {
                return false;
            }
            if ( mState == STATE_EXPANDED  &&  mActivePointerId == pointerId ) {
                View scroll = mNestedScrollingChildRef.get();
                if (scroll != null && ViewCompat.canScrollVertically(scroll, -1)) {
                    // Let the content scroll up
                    return false;
                }
            }
            return mViewRef != null  &&  mViewRef.get() == child;
        }

        @Override
        public void onViewPositionChanged( View changedView, int left, int top, int dx, int dy ) {
            dispatchOnSlide( top );
        }

        @Override
        public void onViewDragStateChanged( int state ) {
            if ( state == CustomViewDragHelper.STATE_DRAGGING ) {
                setStateInternal( STATE_DRAGGING, STATE_DRAGGING );
            }
        }

        @Override
        public void onViewReleased( View releasedChild, float xvel, float yvel ) {

            int top;
            @State int targetState;
            if ( yvel < -mMinimumVelocity*5 ) { // Flinging up
                if ( mLastStableState == STATE_ANCHOR_POINT ) {
                    top = mMinOffset;
                    targetState = STATE_EXPANDED;
                }
                else {
                    top = mAnchorPoint;
                    targetState = STATE_ANCHOR_POINT;
                }
            }
            else
            if ( yvel > mMinimumVelocity*5 ) { // Flinging down
                if ( mLastStableState == STATE_ANCHOR_POINT ) {
                    top = mMaxOffset;
                    targetState = STATE_COLLAPSED;
                }
                else {
                    top = mAnchorPoint;
                    targetState = STATE_ANCHOR_POINT;
                }
            }
            else {
                int releasedChildTop = releasedChild.getTop();
                if ( releasedChildTop > mMaxOffset ) {
                    top = mParentHeight;
                    targetState = STATE_HIDDEN;
                }
                else
                if ( releasedChildTop <= mMaxOffset && releasedChildTop > (mAnchorPoint + mMaxOffset) / 2 ) {
                    // Photo was dragged below the bottom half-point, so settle to collapsed
                    top = mMaxOffset;
                    targetState = STATE_COLLAPSED;
                }
                else
                if ( releasedChildTop < (mMinOffset + mAnchorPoint) / 2 ) {
                    // Photo was dragged above the top half-point, so settle to expanded
                    top = mMinOffset;
                    targetState = STATE_EXPANDED;
                }
                else {
                    // Photo was not dragged far enough, so spring back to anchor
                    top = mAnchorPoint;
                    targetState = STATE_ANCHOR_POINT;
                }
            }
            //}
            //else
            //if ( mHideable  &&  shouldHide( releasedChild, yvel ) ) { // Clicking on collapsed should move up to anchor state
            //    top = mAnchorPoint;
            //    targetState = STATE_ANCHOR_POINT;
            //}
/*
            else
            if ( yvel == 0.f ) {
                int currentTop = releasedChild.getTop();
                if ( Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset) ) {
                    top = mMinOffset;
                    targetState = STATE_EXPANDED;
                } else {
                    top = mMaxOffset;
                    targetState = STATE_COLLAPSED;
                }
            } else {
                top = mMaxOffset;
                targetState = STATE_COLLAPSED;
            }
*/

            if ( mViewDragHelper.smoothSlideViewTo( releasedChild, releasedChild.getLeft(), top ) ) {
                setStateInternal( STATE_SETTLING, targetState );
                mSettlingToState = targetState;
                ViewCompat.postOnAnimation( releasedChild, new SettleRunnable( releasedChild, targetState, false ) );
            } else {
                setStateInternal( targetState, targetState );
            }
        }

        @Override
        public int clampViewPositionVertical( View child, int top, int dy ) {
            // Force stop at collapsed
            return constrain( top, mMinOffset, mMaxOffset );
        }
        int constrain( int amount, int low, int high ) {
            return amount < low ? low : (amount > high ? high : amount);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return child.getLeft();
        }

        @Override
        public int getViewVerticalDragRange( View child ) {
            return mMaxOffset - mMinOffset;
        }
    };

    private void dispatchOnSlide( int top ) {
        View bottomSheet = mViewRef.get();
        if ( bottomSheet != null  &&  mCallback != null ) {
            if ( top > mMaxOffset ) {
                notifyOnSlideToListeners( bottomSheet, (float) (mMaxOffset - top) / mPeekHeight );
            } else {
                notifyOnSlideToListeners( bottomSheet, (float) (mMaxOffset - top) / ((mMaxOffset - mMinOffset)) );
            }
        }
    }

    private class SettleRunnable implements Runnable {
        private final View mView;

        @State
        private final int mTargetState;

        private final boolean mNoCallbacks;

        SettleRunnable( View view, @State int targetState, boolean noCallbacks ) {
            mView = view;
            mTargetState = targetState;
            mNoCallbacks = noCallbacks;
        }

        @Override
        public void run() {
            if ( mViewDragHelper != null  &&  mViewDragHelper.continueSettling( true ) ) {
                ViewCompat.postOnAnimation( mView, this );
            } else {
                setStateInternal( mTargetState, mTargetState, mNoCallbacks );
                mNestedScrollingChildRef.get().scrollTo( 0, 0 );
            }
            if ( mNestedScrollingChildRef.get() != null ) {
                CoordinatorLayout cl = (CoordinatorLayout)mNestedScrollingChildRef.get().getParent();
                cl.dispatchDependentViewsChanged( mNestedScrollingChildRef.get() ); // workaround for a framework bug
            }
        }
    }

    protected static class SavedState extends View.BaseSavedState {
        @State
        final int state;

        public SavedState( Parcel source ) {
            super( source );
            // noinspection ResourceType
            state = source.readInt();
        }

        public SavedState(Parcelable superState, @State int state) {
            super(superState);
            this.state = state;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(state);
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

    /**
     * A utility function to get the {@link BottomSheetBehaviorGoogleMapsLike} associated with the {@code view}.
     *
     * @param view The {@link View} with {@link BottomSheetBehaviorGoogleMapsLike}.
     * @return The {@link BottomSheetBehaviorGoogleMapsLike} associated with the {@code view}.
     */
    @SuppressWarnings("unchecked")
    public static <V extends View> BottomSheetBehaviorGoogleMapsLike<V> from(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof BottomSheetBehaviorGoogleMapsLike)) {
            throw new IllegalArgumentException(
                    "The view is not associated with BottomSheetBehaviorGoogleMapsLike");
        }
        return (BottomSheetBehaviorGoogleMapsLike<V>) behavior;
    }

    private BottomSheetPage mParentBottomSheetPage = null;
    public void setParentBottomSheetPage( BottomSheetPage bottomSheetPage ) {
        mParentBottomSheetPage = bottomSheetPage;
    }

    public static boolean isStateStable( int state ) {
        return state == STATE_HIDDEN  ||  state == STATE_COLLAPSED  ||  state == STATE_ANCHOR_POINT  ||  state == STATE_EXPANDED;
    }

}