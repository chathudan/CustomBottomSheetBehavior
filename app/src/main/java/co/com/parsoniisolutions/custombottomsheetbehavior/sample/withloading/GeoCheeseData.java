package co.com.parsoniisolutions.custombottomsheetbehavior.sample.withloading;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading.BottomSheetDataWithLoading;

import java.util.List;


public class GeoCheeseData extends BottomSheetDataWithLoading {
    private final String title;
    private final String subTitle;
    private final List<Integer> imageResourceList;

    private GeoCheeseData( String title, String subTitle, List<Integer> imageResourceList, long id, LocationType locationType, int late6, int lone6, int swlate6, int swlone6, int nelate6, int nelone6 ) {
        super( id, locationType, late6, lone6, swlate6, swlone6, nelate6, nelone6 );
        this.title = title;
        this.subTitle = subTitle;
        this.imageResourceList = imageResourceList;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public List<Integer> getImageResourceList() {
        return imageResourceList;
    }


    public static class Builder extends BottomSheetDataWithLoading.Builder {
        private String title    = "";
        private String subtitle = "";
        private List<Integer> imageResourceList;

        public Builder setTitle( String title ) {
            this.title = title;
            return this;
        }

        public Builder setSubTitle( String subtitle ) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder setImageResourceList( List<Integer> imageResourceList ) {
            this.imageResourceList = imageResourceList;
            return this;
        }

        public GeoCheeseData build() {
            return new GeoCheeseData( title, subtitle, imageResourceList, id, locationType, late6, lone6, swlate6, swlone6, nelate6, nelone6 );
        }
    }

}