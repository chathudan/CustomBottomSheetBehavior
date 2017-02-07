package co.com.parsoniisolutions.custombottomsheetbehavior.lib.scrolltracking;


public interface ScrollVelocityTrackerIf {
    void recordScroll( int dy );

    float getScrollVelocity();
    int   getTotalScrollDistancePx();
    int   getCurrentScrollY();
}
