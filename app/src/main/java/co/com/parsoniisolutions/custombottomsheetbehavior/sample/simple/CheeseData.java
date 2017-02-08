package co.com.parsoniisolutions.custombottomsheetbehavior.sample.simple;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetData;

import java.util.List;


public class CheeseData extends BottomSheetData {
    private final String mTitle;
    private final String mSubTitle;
    private final List<Integer> mImageResourceList;

    public CheeseData( String title, String subTitle, List<Integer> imageResourceList ) {
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