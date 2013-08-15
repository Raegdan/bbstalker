package org.raegdan.bbstalker;

/*/ 
 * MyLocation class
 * Based on the code by @Fedor from StackOverflow (http://stackoverflow.com/users/95313/fedor) 
/*/

import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.Time;

public class MyLocation {
    Timer timer1;
    LocationManager lm;
    LocationResult locationResult;
    boolean gps_enabled=false;
    boolean network_enabled=false;
    
    final static protected int TIMEOUT = 20000;
    final static protected int CACHE_TIMEOUT = 300000;
    
    final static protected int EC_NO_ERR = 0;
    final static protected int EC_NO_PROVIDERS = 1;
    final static protected int EC_NO_DATA = 2;
    
    HashMap<String, Object> Cache;
    
    public MyLocation()
    {
    	Cache = new HashMap<String, Object>();
    	
    	Cache.put("time", new Time().toMillis(true));
    	Cache.put("cache", new Location(""));
	}

    public boolean getLocation(Context context, LocationResult result)
    {
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        locationResult=result;
        
    	Time t = new Time();
    	t.setToNow();
    	if ((t.toMillis(true) - (Long) Cache.get("time")) < CACHE_TIMEOUT)
    	{
    		locationResult.gotLocation((Location) Cache.get("cache"), EC_NO_ERR, true);
    		return false;
    	}
    	
        if(lm==null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try {gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);} catch (Exception ex) {}
        try {network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);} catch (Exception ex) {}

        //don't start listeners if no provider is enabled
        if(!gps_enabled && !network_enabled)
        {
            locationResult.gotLocation(null, EC_NO_PROVIDERS, false);
        	return false;
        }
        
        if(gps_enabled)
            lm.requestLocationUpdates (LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        if(network_enabled)
            lm.requestLocationUpdates (LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        timer1=new Timer();
        timer1.schedule(new NoData(), TIMEOUT);
        return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerNetwork);
            Cache.put("cache", location);
            Time t = new Time();
            t.setToNow();
            Cache.put("time", t.toMillis(true));
            locationResult.gotLocation(location, EC_NO_ERR, false);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerGps);
            Cache.put("cache", location);
            Time t = new Time();
            t.setToNow();
            Cache.put("time", t.toMillis(true));
            locationResult.gotLocation(location, EC_NO_ERR, false);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    class NoData extends TimerTask {
        @Override
        public void run() {
             lm.removeUpdates(locationListenerGps);
             lm.removeUpdates(locationListenerNetwork);

             locationResult.gotLocation(null, EC_NO_DATA, false);
        }
    }

    public static abstract class LocationResult{
        public abstract void gotLocation(Location location, int ErrCode, boolean cached);
    }
}