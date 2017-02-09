package co.com.parsoniisolutions.custombottomsheetbehavior.lib.views;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.appbar.EventScrollAppBarVisibility;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils.DimensionUtils;


/**
 * AppBarLayout which receives state changes from DelegatingScrollAppBarLayoutBehaviors,
 * and animates this AppBarLayout in and out
 */
public class ScrollAppBarLayout extends android.support.design.widget.AppBarLayout {

    public ScrollAppBarLayout( Context context ) {
        this( context, null );
    }

    public ScrollAppBarLayout( Context context, AttributeSet attrs ) {
        super( context, attrs );

        try {
            EventBus.getDefault().register( this );
        } catch ( EventBusException e ) {
            Log.e( "e", "EventBusException " + e.toString() );
            e.printStackTrace();
        }
    }

    private Toolbar mScrollToolBar = null;
    private boolean mScrollingAppBarVisible = true;
    private ValueAnimator mAppBarYValueAnimator = null;

    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onEvent( EventScrollAppBarVisibility ev ) {
        if ( mScrollToolBar == null ) {
            mScrollToolBar = (Toolbar) findViewById( R.id.scrolltoolbar );
//mScrollToolBar.setTitle( "" );
//DrawerLayout dl = (DrawerLayout) getView().getRootView().findViewById( R.id.drawer_layout );
//CustomActionBarDrawerToggle drawerToggle = new CustomActionBarDrawerToggle( (DrawerActivity)getActivity(), dl, mScrollToolBar );
//drawerToggle.syncState();
        }

        final boolean visible = ev.getVisibility();

        if ( mAppBarYValueAnimator == null  ||  mScrollingAppBarVisible != visible ) {
            if ( visible ) {
                //((AppCompatActivity)mScrollToolBar.getContext()).setSupportActionBar( mScrollToolBar );
            }

            int curY = (int) getY();
            int newY = visible ? getSettledOffsetFromTop( getContext() ) : -getHeight();

            mAppBarYValueAnimator = ValueAnimator.ofFloat( curY, newY );

            mAppBarYValueAnimator.setDuration( getResources().getInteger( android.R.integer.config_shortAnimTime ) );
            mAppBarYValueAnimator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate( ValueAnimator animation ) {
                    setY( (Float) animation.getAnimatedValue() );

                }
            } );
            mAppBarYValueAnimator.addListener( new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart( Animator animation ) {
                    super.onAnimationStart( animation );
                    mScrollingAppBarVisible = visible;
                    if ( visible ) {
//((AppCompatActivity)getActivity()).setSupportActionBar( mScrollToolBar );
                    }
                }

                @Override
                public void onAnimationEnd( Animator animation ) {
                    if ( ! visible ) {
                    }
                    super.onAnimationEnd( animation );
                }
            } );
            mAppBarYValueAnimator.start();
        }
    }


    private int getSettledOffsetFromTop( Context context ) {
        return DimensionUtils.getStatusBarHeight( context ) + DimensionUtils.getScrollToolbarPaddingTop( context );
    }
}