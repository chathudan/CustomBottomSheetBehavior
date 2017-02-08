package co.com.parsoniisolutions.custombottomsheetbehavior.lib.views;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.RelativeLayout;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.appbar.EventMergedAppBarVisibility;


/**
 * AppBarLayout which receives state changes from DelegatingMergedAppBarLayoutBehaviors,
 * and animates this AppBarLayout in and out
 */
public class MergedAppBarLayout extends android.support.design.widget.AppBarLayout {

    private RelativeLayout.LayoutParams mMergedPartialBackgroundLayoutParams;
    private Toolbar mMergedToolBar;
    private View mMergedPartialBackground;
    private boolean mVisible = false;
    private ViewPropertyAnimator mAppBarLayoutAnimation;
    private View.OnClickListener mOnNavigationClickListener;

    public MergedAppBarLayout( Context context ) {
        this( context, null );
    }

    public MergedAppBarLayout( Context context, AttributeSet attrs ) {
        super( context, attrs );

        try {
            EventBus.getDefault().register( this );
        } catch ( EventBusException e ) {
            Log.e( "e", "EventBusException " + e.toString() );
            e.printStackTrace();
        }

        //mMergedPartialBackgroundLayoutParams = (RelativeLayout.LayoutParams)getLayoutParams();
    }

    private @ColorRes int getFullBackgroundColorRes()     { return android.R.color.transparent; } // return R.color.colorPrimaryDark; }
    private void setFullBackGroundColor( @ColorRes int colorRes ){
        setBackgroundColor( ContextCompat.getColor( getContext(), colorRes ) );
    }
    private void setVisible( boolean visible ) {
        if ( ! visible  && mVisible ) {
            mAppBarLayoutAnimation = animate().setDuration( getResources().getInteger( android.R.integer.config_shortAnimTime ) );
            mVisible = false;
            mAppBarLayoutAnimation.setListener( new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart( Animator animation ) {
                    super.onAnimationStart( animation );
                }

                @Override
                public void onAnimationEnd( Animator animation ) {
                    super.onAnimationEnd( animation );
                    setVisibility( View.INVISIBLE );
                }
            } );
            mAppBarLayoutAnimation.alpha( 0 ).y( -getHeight()/4 ).start();
        }
        else
        if ( visible  &&  !mVisible ) {
            mMergedToolBar.setNavigationOnClickListener( mOnNavigationClickListener );

            setY( -getHeight()/4 );
            setAlpha( 0 );
            setVisibility( View.VISIBLE );
            mVisible = true;

            mAppBarLayoutAnimation = animate().setDuration( getResources().getInteger( android.R.integer.config_shortAnimTime ) );
            mAppBarLayoutAnimation.setListener( new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart( Animator animation ) {
                    super.onAnimationStart( animation );
                }

                @Override
                public void onAnimationEnd( Animator animation ) {
                    super.onAnimationEnd( animation );
                }
            });
            mAppBarLayoutAnimation.alpha( 1 ).y( 0 ).start();
        }
    }

    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onEvent( EventMergedAppBarVisibility ev ) {

        if ( mMergedToolBar == null ) {
            mMergedToolBar = (Toolbar) findViewById( R.id.expanded_toolbar );
        }
        if ( mMergedPartialBackground == null ) {
            mMergedPartialBackground = findViewById( R.id.merged_background );
            mMergedPartialBackgroundLayoutParams = (RelativeLayout.LayoutParams) mMergedPartialBackground.getLayoutParams();
        }
        if ( mMergedToolBar == null  ||  mMergedPartialBackground == null )
            return;

        // TODO - If we are between states, do not update the state

        if ( ev.getState() == EventMergedAppBarVisibility.State.BELOW_ANCHOR_POINT ) {
            setPartialBackGroundHeight( ev.getPartialBackgroundHeight() );
            setVisible( false );
        }
        else
        if ( ev.getState() == EventMergedAppBarVisibility.State.ABOVE_ANCHOR_POINT ) {
            setPartialBackGroundHeight( ev.getPartialBackgroundHeight() );
            setVisible( true );
            setFullBackGroundColor( android.R.color.transparent );
        }
        else
        if ( ev.getState() == EventMergedAppBarVisibility.State.INSIDE_TOOLBAR ) {
            setVisible( true );
            setFullBackGroundColor( android.R.color.transparent );
            setPartialBackGroundHeight( ev.getPartialBackgroundHeight() );
        }
        else
        if ( ev.getState() == EventMergedAppBarVisibility.State.TOP_OF_TOOLBAR ) {
            setVisible( true );
            setFullBackGroundColor( getFullBackgroundColorRes() );
            setPartialBackGroundHeight( ev.getPartialBackgroundHeight() );
        }
    }

    private void setPartialBackGroundHeight( int height ) {
        int newHeight = Math.max(0, height );
        if ( mMergedPartialBackgroundLayoutParams.height != newHeight ) {
            mMergedPartialBackgroundLayoutParams.height = newHeight;

            // setLayoutParams forces layout, so only call it when height actually changes
            mMergedPartialBackground.setLayoutParams( mMergedPartialBackgroundLayoutParams );
        }
    }

    public void setNavigationOnClickListener(View.OnClickListener listener){
        this.mOnNavigationClickListener = listener;
    }

}