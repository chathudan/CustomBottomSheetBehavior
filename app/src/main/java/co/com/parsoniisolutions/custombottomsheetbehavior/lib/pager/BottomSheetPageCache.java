package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager;

import android.view.LayoutInflater;


class BottomSheetPageCache extends SoftCache<BottomSheetPage> {

    BottomSheetPageCache( Class<BottomSheetPage> typeParameterClass ) { super( typeParameterClass ); }

    @Override
    public BottomSheetPage runWhenCacheEmpty( LayoutInflater inflater, BottomSheetPagerAdapter bottomSheetPagerAdapter ) {
        return bottomSheetPagerAdapter.createNewPage( inflater );
    }
}