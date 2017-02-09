package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager;

import android.view.LayoutInflater;


class BottomSheetPageSoftCache extends SoftCache<BottomSheetPage> {

    BottomSheetPageSoftCache( Class<BottomSheetPage> typeParameterClass ) { super( typeParameterClass ); }

    @Override
    public BottomSheetPage runWhenCacheEmpty( LayoutInflater inflater, BottomSheetPagerAdapter bottomSheetPagerAdapter ) {
        return bottomSheetPagerAdapter.createNewPage( inflater );
    }
}