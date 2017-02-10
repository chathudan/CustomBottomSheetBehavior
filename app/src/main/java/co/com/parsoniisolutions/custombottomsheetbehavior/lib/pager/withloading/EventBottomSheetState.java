package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;


import java.lang.ref.WeakReference;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPage;


public final class EventBottomSheetState {

    private final int mState;
    private final WeakReference<BottomSheetPage> mBottomSheetPageRef;
    public EventBottomSheetState( int state, BottomSheetPage bottomSheetPage ) {
        mState = state;
        mBottomSheetPageRef = new WeakReference<>( bottomSheetPage );
    }
    public int state() { return mState; }
    public WeakReference<BottomSheetPage> bottomSheetPageRef() { return mBottomSheetPageRef; }
}