package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager;

import android.view.LayoutInflater;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Stack;


public abstract class SoftCache<T> {

    private Stack<Reference<T>> mRecyclingStack;

    final Class<T> classType;

    public SoftCache( Class<T> typeParameterClass ) {
        this.classType = typeParameterClass;
        mRecyclingStack = new Stack<>();
    }

    /* implement this to create new object of type T if cache is empty */
    public abstract T runWhenCacheEmpty( LayoutInflater inflater, BottomSheetPagerAdapter pagerAdapter );

    /*
     * retrieves last item from cache, or creates a new T object if cache is empty
     */
    public T get( LayoutInflater inflater, BottomSheetPagerAdapter pagerAdapter ) {
        T itemCached = null;

        if ( mRecyclingStack.isEmpty() ) {
            itemCached = runWhenCacheEmpty( inflater, pagerAdapter );
        }
        else {
            SoftReference<T> softRef = (SoftReference<T>) mRecyclingStack.pop();

            Object obj = softRef.get();

            /*
             * if referent object is empty(due to GC) then create a new
             * object
             */
            if ( obj == null ) {
                itemCached = runWhenCacheEmpty( inflater, pagerAdapter );
            }
            /*
             * otherwise restore from cache by casting the referent as the
             * class Type that was passed to constructor
             */
            else {
                itemCached = (classType.cast( softRef.get() ));
            }
        }
        return itemCached;
    }

    /* saves a view object to the cache by reference */
    public void put( T item ) {
        SoftReference<T> ref = new SoftReference<>( item );
        mRecyclingStack.push( ref );
    }
}