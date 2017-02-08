package co.com.parsoniisolutions.custombottomsheetbehavior.lib.appbar;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPage;


/**
 * Behavior applied on an AppBarLayout within a ViewPager. It delegates visibility change actions.
 */
public  class DelegatingAppBarLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {

    public DelegatingAppBarLayoutBehavior( Context context, AttributeSet attrs ) {
        super( context, attrs );
    }

    private BottomSheetPage mParentBottomSheetPage = null;
    public void setParentBottomSheetPage( BottomSheetPage bottomSheetPage ) {
        mParentBottomSheetPage = bottomSheetPage;
    }
    protected boolean publish() {
        return mParentBottomSheetPage.isSelected();
    }
}