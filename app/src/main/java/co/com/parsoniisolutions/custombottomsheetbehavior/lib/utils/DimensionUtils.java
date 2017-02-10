package co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;


public class DimensionUtils {

    private static int mMergedToolbarHideThreshold = -1;
    public static int getMergedToolbarHideThreshold( Context context ) {
        if ( mMergedToolbarHideThreshold == -1 ) {
            mMergedToolbarHideThreshold = (int)context.getResources().getDimension( R.dimen.merged_toolbar_hide_threshold );
        }
        return mMergedToolbarHideThreshold;
    }


    private static int mFabSize = -1;
    public static int getFabSize( Context context ) {
        if ( mFabSize == -1 ) {
            mFabSize = (int)context.getResources().getDimension( R.dimen.fab_size );
        }
        return mFabSize;
    }

    private static int mAnchorHeight = -1;
    public static int getAnchorHeight( Context context ) {
        if ( mAnchorHeight== -1 ) {
            mAnchorHeight = (int)context.getResources().getDimension( R.dimen.anchor_point );
        }
        return mAnchorHeight;
    }

    private static int mPeekHeight = -1;
    public static int getPeekHeight( Context context ) {
        if ( mPeekHeight == -1 ) {
            mPeekHeight = (int)context.getResources().getDimension( R.dimen.bottom_sheet_peek_height );
        }
        return mPeekHeight;
    }

    private static int mScrollToolbarPaddingTop = -1;
    public static int getScrollToolbarPaddingTop( Context context ) {
        if ( mScrollToolbarPaddingTop == -1 ) {
            mScrollToolbarPaddingTop = (int)context.getResources().getDimension( R.dimen.scroll_toolbar_padding_top );
        }
        return mScrollToolbarPaddingTop;
    }

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

    private static int mSoftButtonsBarHeight = -1;
    private int getSoftButtonsBarHeight( Activity activity ) {
        // getRealMetrics is only available with API 17 and +
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics( metrics );
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics( metrics );
            int realHeight = metrics.heightPixels;
            if ( realHeight > usableHeight )
                mSoftButtonsBarHeight = realHeight - usableHeight;
            else
                mSoftButtonsBarHeight = 0;
        }
        return mSoftButtonsBarHeight;
    }
}