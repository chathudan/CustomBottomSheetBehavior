package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;


public class Utils {

    private static int mStatusBarHeight = -1;
    public static int getStatusBarHeight( Context context ) {
        if ( mStatusBarHeight == -1 ) {
            int result = 0;
            int resourceId = context.getResources().getIdentifier( "status_bar_height", "dimen", "android" );
            if ( resourceId > 0 ) {
                result = context.getResources().getDimensionPixelSize( resourceId );
            }
            mStatusBarHeight = result;
        }
        return mStatusBarHeight;
    }

    private static int mToolbarHeight = -1;
    public static int getToolbarHeight( Context context ) {
        if ( mToolbarHeight == -1 ) {
            int result = 0;
            TypedValue tv = new TypedValue();
            if ( context.getTheme().resolveAttribute( android.R.attr.actionBarSize, tv, true ) ) {
                result = TypedValue.complexToDimensionPixelSize( tv.data, context.getResources().getDisplayMetrics() );
            }
            mToolbarHeight = result;
        }
        return mToolbarHeight;
    }

}
