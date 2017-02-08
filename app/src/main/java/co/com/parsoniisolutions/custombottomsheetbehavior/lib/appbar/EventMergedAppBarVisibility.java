package co.com.parsoniisolutions.custombottomsheetbehavior.lib.appbar;

import android.support.design.widget.CoordinatorLayout;


public final class EventMergedAppBarVisibility {
    public enum State {
        BELOW_ANCHOR_POINT,
        ABOVE_ANCHOR_POINT,
        INSIDE_TOOLBAR,
        TOP_OF_TOOLBAR;
    }

    private final boolean           mVisibility;
    private final int               mChildY;
    private final State             mState;
    private final int               mPartialBackgroundHeight;
    private final CoordinatorLayout mParent;

    public EventMergedAppBarVisibility( boolean visibility, int childY, State state, int partialBackgroundHeight, CoordinatorLayout parent ) {
        mVisibility              = visibility;
        mChildY                  = childY;
        mState                   = state;
        mPartialBackgroundHeight = partialBackgroundHeight;
        mParent                  = parent;
    }

    public boolean getVisibility()              { return mVisibility; }
    public int     getChildY()                  { return mChildY;     }
    public State   getState()                   { return mState;      }
    public int     getPartialBackgroundHeight() { return mPartialBackgroundHeight; }
    public CoordinatorLayout getParent() { return mParent;     }

}