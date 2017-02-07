package co.com.parsoniisolutions.custombottomsheetbehavior.sample;

import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.widget.TextView;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPage;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.BottomSheetPagerAdapter;


public class BottomSheetPageCheese extends BottomSheetPage {

    public BottomSheetPageCheese( LayoutInflater inflater, BottomSheetPagerAdapter bottomSheetPagerAdapter ) {
        super( inflater, bottomSheetPagerAdapter );
    }

    @Override
    public @LayoutRes int layoutRes() { return R.layout.bottom_pager_content; }

    private TextView  mTitle;
    private TextView  mSubTitle;
    private ViewPager mPhotosViewPager;

    @Override
    protected void initializeUI() {
        super.initializeUI();

        mTitle           = (TextView)inflatedView().findViewById( R.id.bottom_sheet_title );
        mSubTitle        = (TextView)inflatedView().findViewById( R.id.text_dummy1 );
        mPhotosViewPager = (ViewPager) inflatedView().findViewById( R.id.photos_view_pager );
    }

    @Override
    protected void setUI( int position ) {
        MainContentViewPagerAdapter adapter = (MainContentViewPagerAdapter)pagerAdapter();
        if ( adapter == null )
            return;

        MainContentPagerItem item = adapter.getItemAtPosition( position );
        mTitle.setText( item.getTitle() );
        mSubTitle.setText( item.getSubTitle() );
        
        ItemPagerAdapter photos_adapter = new ItemPagerAdapter( mPhotosViewPager.getContext(), item.getImageResourceList() );
        mPhotosViewPager.setAdapter(photos_adapter);
    }

}
