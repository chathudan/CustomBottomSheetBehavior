package co.com.parsoniisolutions.custombottomsheetbehavior.lib.scrolltracking;


public class ScrollVelocityTracker implements ScrollVelocityTrackerIf {
    private long  mPreviousScrollTime    = 0;
    private int   mTotalScrollDistancePx = 0; // Total distance of this scroll, since the user changed scroll direction, or paused the scroll
    private float mScrollVelocity        = 0;
    private int   mCurrentScrollY        = 0; // Since the start of this action

    public void recordScroll( int dy ) {
        mCurrentScrollY += dy;

        long now = System.currentTimeMillis();

        // If user changed the scroll direction, reset the total scroll distance measure
        if (
                ( mTotalScrollDistancePx > 0  &&  dy < 0 )  ||
                ( mTotalScrollDistancePx < 0  &&  dy > 0 )
           ) {
            mTotalScrollDistancePx = dy;
            mPreviousScrollTime    = now;
        }

        if ( mPreviousScrollTime != 0 ) {
            long elapsed = now - mPreviousScrollTime;
            mScrollVelocity = (1.0f) * (float) mTotalScrollDistancePx / (float)elapsed * 1000; // pixels per sec

            // If user paused scrolling, reset the total scroll measure
            if ( Math.abs( mScrollVelocity ) < 10 ) {
                mTotalScrollDistancePx = dy;
                mPreviousScrollTime = now;
            }
            else {
                // Otherwise keep adding up the distance
                mTotalScrollDistancePx += dy;
            }
        }
        else {
            mPreviousScrollTime = now;
            mTotalScrollDistancePx = dy;
            mScrollVelocity = (1.0f) * (float) mTotalScrollDistancePx / (float)10 * 1000; // assume 10msec for first event
        }
    }

    void clear() {
        mPreviousScrollTime    = 0;
        mScrollVelocity        = 0;
        mTotalScrollDistancePx = 0;
        mCurrentScrollY        = 0;
    }

    public float getScrollVelocity()        { return mScrollVelocity; }
    public int   getTotalScrollDistancePx() { return mTotalScrollDistancePx; }
    public int   getCurrentScrollY()        { return mCurrentScrollY; }
}
