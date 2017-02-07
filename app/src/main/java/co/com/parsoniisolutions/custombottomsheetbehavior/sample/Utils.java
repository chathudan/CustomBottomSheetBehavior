package co.com.parsoniisolutions.custombottomsheetbehavior.sample;


import android.content.Context;
import android.util.Log;


public class Utils {

    private static int mStatusBarHeight = 0;

    public static int getStatusBarHeight( Context context ) {
        if ( mStatusBarHeight == 0 ) {
            int result = 0;
            int resourceId = context.getResources().getIdentifier( "status_bar_height", "dimen", "android" );
            if ( resourceId > 0 ) {
                result = context.getResources().getDimensionPixelSize( resourceId );
            }
            mStatusBarHeight = result;
        }
        return mStatusBarHeight;
    }

}
