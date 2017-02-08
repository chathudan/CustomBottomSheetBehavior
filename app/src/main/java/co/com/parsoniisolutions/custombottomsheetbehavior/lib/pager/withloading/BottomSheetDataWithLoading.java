package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;


import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetData;


public class BottomSheetDataWithLoading extends BottomSheetData {

    private long id;

    public enum LocationType {
        POINT,
        BOX
    };

    private final LocationType locationType;
    private final int late6;
    private final int lone6;

    private final int swlate6;
    private final int swlone6;
    private final int nelate6;
    private final int nelone6;

    protected BottomSheetDataWithLoading( long id, LocationType locationType, int late6, int lone6, int swlate6, int swlone6, int nelate6, int nelone6 ) {
        this.id = id;
        this.locationType = locationType;
        this.late6 = late6;
        this.lone6 = lone6;
        this.swlate6 = swlate6;
        this.swlone6 = swlone6;
        this.nelate6 = nelate6;
        this.nelone6 = nelone6;
    }

    public static class Builder {
        protected long id = 0;
        protected LocationType locationType = LocationType.POINT;
        protected int late6  = 0;
        protected int lone6  = 0;

        protected int swlate6 = 0;
        protected int swlone6 = 0;
        protected int nelate6 = 0;
        protected int nelone6 = 0;

        public Builder setId( long id ) {
            this.id = id;
            return this;
        }

        public Builder setLocationType( LocationType locationType ) {
            this.locationType = locationType;
            return this;
        }

        public Builder setLate6( int late6 ) {
            this.late6 = late6;
            return this;
        }

        public Builder setLone6( int lone6 ) {
            this.lone6 = lone6;
            return this;
        }

        public Builder setSwLate6( int swlate6 ) {
            this.swlate6 = swlate6;
            return this;
        }

        public Builder setSwLone6( int swlone6 ) {
            this.swlone6 = swlone6;
            return this;
        }

        public Builder setNeLate6( int nelate6 ) {
            this.nelate6 = nelate6;
            return this;
        }

        public Builder setNeLone6( int nelone6 ) {
            this.nelone6 = nelone6;
            return this;
        }

        public BottomSheetDataWithLoading build() {
            return new BottomSheetDataWithLoading( id, locationType, late6, lone6, swlate6, swlone6, nelate6, nelone6 );
        }
    }

    public long getId()    { return id; }
    public int  getLate6() { return late6; }
    public int  getLone6() { return lone6; }
}
