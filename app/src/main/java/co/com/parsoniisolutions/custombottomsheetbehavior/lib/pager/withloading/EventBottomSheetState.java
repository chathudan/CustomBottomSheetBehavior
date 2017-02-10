package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import java.lang.ref.WeakReference;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPage;


public final class EventBottomSheetState {

    private final int mState;
    private final int mTargetState; // If settling, this will be the state we are settling TO
    private final WeakReference<BottomSheetPage> mBottomSheetPageRef;
    public EventBottomSheetState( int state, int targetstate, BottomSheetPage bottomSheetPage ) {
        mState       = state;
        mTargetState = targetstate;
        mBottomSheetPageRef = new WeakReference<>( bottomSheetPage );
    }
    public int state()       { return mState; }
    public int targetState() { return mTargetState; }
    public WeakReference<BottomSheetPage> bottomSheetPageRef() { return mBottomSheetPageRef; }
}