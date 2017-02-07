package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;


class VisibilityAwareFrameLayout extends FrameLayout {

    private int mUserSetVisibility;

    public VisibilityAwareFrameLayout( Context context ) { this( context, null ); }
    public VisibilityAwareFrameLayout( Context context, AttributeSet attrs ) { this( context, attrs, 0 ); }
    public VisibilityAwareFrameLayout( Context context, AttributeSet attrs, int defStyleAttr ) {
        super( context, attrs, defStyleAttr );
        mUserSetVisibility = getVisibility();
    }

    @Override
    public void setVisibility( int visibility ) {
        internalSetVisibility( visibility, true );
    }

    final void internalSetVisibility( int visibility, boolean fromUser ) {
        super.setVisibility( visibility );
        if ( fromUser ) {
            mUserSetVisibility = visibility;
        }
    }

    final int getUserSetVisibility() {
        return mUserSetVisibility;
    }
}