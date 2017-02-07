package co.com.parsoniisolutions.custombottomsheetbehavior.sample;

import java.util.List;


public class MainContentPagerItem {
    private final String mTitle;
    private final String mSubTitle;
    private final List<Integer> mImageResourceList;

    public MainContentPagerItem( String title, String subTitle, List<Integer> imageResourceList ) {
        mTitle    = title;
        mSubTitle = subTitle;
        mImageResourceList = imageResourceList;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public List<Integer> getImageResourceList() {
        return mImageResourceList;
    }
}
