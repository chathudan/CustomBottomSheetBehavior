package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager.withloading;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;


public class MarkerData {

    private long   id;
    private LatLng position;
    private String title;
    private String snippet;

    public MarkerData( long id, LatLng position, String title, String snippet ) {
        this.id       = id;
        this.position = position;
        this.title    = title;
        this.snippet  = snippet;
    }

    public void setId( long id )               { this.id       = id;       }
    public void setPosition( LatLng position ) { this.position = position; }
    public void setTitle(    String title )    { this.title    = title;    }
    public void setSnippet(  String snippet )  { this.snippet  = snippet;  }
    public long   getId()       { return id;       }
    public LatLng getPosition() { return position; }
    public String getTitle()    { return title;    }
    public String getSnippet()  { return snippet;  }

    @Override
    public boolean equals( @Nullable Object aThat ) {
        if ( aThat == null )
            return false;
        MarkerData that = (MarkerData) aThat;

        if ( id != that.getId() )
            return false;

        return true;
    }

    @Override
    public String toString() { return "id=" + " title=" + title + " snippet=" + snippet; }
}