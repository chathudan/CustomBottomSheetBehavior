package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class GoogleMapMarkerData extends MarkerData {
    private final Marker marker;

    public GoogleMapMarkerData( long id, Marker marker, MarkerOptions mo ) {
        super( id, mo.getPosition(), mo.getTitle(), mo.getSnippet() );
        this.marker = marker;
    }

    public Marker getMarker() { return marker; }
}