package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;


public final class EventViewPagerScrollStateChanged {

    private final int mState;
    public EventViewPagerScrollStateChanged( int state ) { mState = state; }
    public int state() { return mState; }
}