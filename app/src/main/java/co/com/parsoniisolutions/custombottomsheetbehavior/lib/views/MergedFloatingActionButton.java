package co.com.parsoniisolutions.custombottomsheetbehavior.lib.views;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.EventBottomSheetPosition;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils.DimensionUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MergedFloatingActionButton extends FloatingActionButton {

    public MergedFloatingActionButton( Context context ) { this( context, null ); }
    public MergedFloatingActionButton( Context context, AttributeSet attrs ) { this( context, attrs, 0 ); }
    public MergedFloatingActionButton( Context context, AttributeSet attrs, int defStyleAttr ) {
        super( context, attrs, defStyleAttr );
        registerEventBus();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setFabBehaviorParameters(); // This can only be set after the view is added to its parent
    }

    private void setFabBehaviorParameters() {
        // Wait until parent height is measured
        post( new Runnable() {
            @Override
            public void run() {
                int fabSize       = DimensionUtils.getFabSize( getContext() );
                int toolbarHeight = DimensionUtils.getToolbarHeight( getContext() );
                setHidePositionTop( toolbarHeight + fabSize/2 );

                int peekHeight = DimensionUtils.getPeekHeight( getContext() );
                int parentHeight = ((View)getParent()).getHeight();

                setHidePositionBottom( parentHeight - peekHeight + fabSize/2 );
                updateVisibility();
            }
        } );
    }

    private void registerEventBus() {
        try {
            EventBus.getDefault().register( this );
        } catch ( EventBusException e ) {
            e.printStackTrace();
        }
    }

    /**
     * One of the point used to set hide() or show() in FAB
     */
    private float hidePositionTop    = -1;
    private float hidePositionBottom = -1;

    /**
     * Define one of the point in where the FAB should be hide when it reaches that point.
     */
    public void setHidePositionTop( int top ) {
        this.hidePositionTop = top;
    }
    public void setHidePositionBottom( int bottom ) {
        this.hidePositionBottom = bottom;
    }

    private int mBottomSheetPosition = -1;
    // Do not set the UI when the BottomSheet is moving
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onEvent( EventBottomSheetPosition ev ) {
        mBottomSheetPosition = ev.position();

        int newY = mBottomSheetPosition - DimensionUtils.getFabSize( getContext() )/2;
        setY( newY );

        if ( hidePositionBottom < 0  ||  hidePositionTop < 0 )
            return;

        updateVisibility();
    }

    private void updateVisibility() {
        if ( getY() < hidePositionTop  ||  getY() > hidePositionBottom ) {
            hide();
        }
        else {
            show();
        }
    }

}
