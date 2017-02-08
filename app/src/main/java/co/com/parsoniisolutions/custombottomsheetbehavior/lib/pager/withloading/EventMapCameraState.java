package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;


public final class EventMapCameraState {

    public enum State {
        START,   // A map camera animation starts
        CANCEL,  // A map camera animatin is cancelled
        FINISH   // A map camera animation finishes
    };

    private final State mState;
    public EventMapCameraState( State state ) { mState = state; }
    public State state() { return mState; }
}