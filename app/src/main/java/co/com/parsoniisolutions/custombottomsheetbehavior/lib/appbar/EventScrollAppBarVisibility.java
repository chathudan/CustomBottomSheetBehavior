package co.com.parsoniisolutions.custombottomsheetbehavior.lib.appbar;

import android.support.design.widget.CoordinatorLayout;


public final class EventScrollAppBarVisibility {
    private final boolean mVisibility;
    private final CoordinatorLayout mParent;

    public EventScrollAppBarVisibility( boolean visibility, CoordinatorLayout parent ) {
        mVisibility = visibility;
        mParent     = parent;
    }

    public boolean           getVisibility() { return mVisibility; }
    public CoordinatorLayout getParent()     { return mParent; }
}