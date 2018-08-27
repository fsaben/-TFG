package sbk.fatima.bluetoothled_v1_1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;


public class Speedometer implements android.location.GpsStatus.Listener {

    private static LocationManager locationManager = null;
    private static LocationListener locationListener = null;
    Context mcontext;

    public Speedometer(Context context) {
        mcontext=context;
        Speedometer.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
            }

            @Override
            public void onProviderDisabled(final String provider) {
            }

            @Override
            public void onProviderEnabled(final String provider) {
            }

            @Override
            public void onStatusChanged(final String provider, final int status, final Bundle extras) {
            }
        };
    }
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mcontext);
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mcontext.startActivity(intent);
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    public void startListening(final Context context) {
        if (Speedometer.locationManager == null) {
            Speedometer.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

        Speedometer.locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 500, 0, Speedometer.locationListener);
    }
    public void stopListening() {
        try
        {
            if (Speedometer.locationManager != null && Speedometer.locationListener != null) {
                Speedometer.locationManager.removeUpdates(Speedometer.locationListener);
            }
            Speedometer.locationManager = null;
        }
        catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onGpsStatusChanged(int event) {
        int Satellites = 0;
        int SatellitesInFix = 0;

        if (ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mcontext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

        int timetofix = locationManager.getGpsStatus(null).getTimeToFirstFix();

        for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
            if(sat.usedInFix()) {
                SatellitesInFix++;
            }
            Satellites++;
        }
    }
}