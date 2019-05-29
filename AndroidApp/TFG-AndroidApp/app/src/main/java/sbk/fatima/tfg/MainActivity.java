package sbk.fatima.tfg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener {

    Button btnOn, btnOff, btnDisconnect;
    TextView currentSpeed, ledStatus;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private LocationManager locManager = null;
    private Polyline gpsTrack;
    private GoogleMap mMap;

    private ConnectedThread mConnectedThread;

    //SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //UUID SERIAL_UUID = device.getUuids()[0].getUuid(); //if you don't know the UUID of the bluetooth device service, you can get it like this from android cache

    private static String address = null;
    private Float speed = null;
    private Boolean speedState = false;
    private LatLng currentposition = null;
    private List<LatLng> points = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Link the buttons and textViews to respective views
        btnOn = (Button) findViewById(R.id.buttonOn);
        btnOff = (Button) findViewById(R.id.buttonOff);
        btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
        currentSpeed = (TextView) findViewById(R.id.speed);
        ledStatus = (TextView) findViewById(R.id.ledStatus);

        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        checkGpsPermission();

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        this.onLocationChanged(null);

        checkGpsState();
        checkBTState();

        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("0");    // Send "0" via Bluetooth
                ledStatus.setText("OFF");
            }
        });

        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("1");    // Send "1" via Bluetooth
                ledStatus.setText("ON");
            }
        });

        btnDisconnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectedThread.write("q");  //Send "q" to end Bluetooth conexion
                onPause();
                finish();
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            Method method;

            method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            return (BluetoothSocket) method.invoke(device, 1);
        } catch (Exception e) {
            System.out.println("Error creating socket: " + e);
            System.out.println("Will try with another kind of socket");
            try {
                return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
            } catch (Exception e2) {
                System.out.println("Error creating second socket: " + e2);
                return null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        System.out.println("Device created");
        try {
            btSocket = createBluetoothSocket(device);
            System.out.println("Socket created");
        } catch (IOException e) {
            System.out.println("Socket creation failed");
            System.out.println("Error: " + e);
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
            System.out.println("Connected");
        } catch (IOException e1) {
            System.out.println(e1);
            try {
                btSocket.close();
            } catch (IOException e2) {
                System.out.println(e2);
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("f");  //"f" means users speed its not above limit
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (locManager != null) {
                locManager.removeUpdates(this);
                locManager = null;
            }
            System.out.println("Finish gps updates");

        } catch (Exception e1) {
            System.out.println(e1);
        }

        try {
            System.out.println("Bluetooth socket closed");
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();

        } catch (IOException e2) {
            System.out.println(e2);
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {
        if (btAdapter.isEnabled()) {
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    private void checkGpsState() {
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        } else {
            AlertDialog.Builder constructor = new AlertDialog.Builder(this);
            constructor.setCancelable(false);
            constructor.setTitle("GPS desactivado");
            constructor.setMessage("El GPS esta desactivado, es necesario el GPS para obtener la velocidad");
            constructor.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent enableGps = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGps, 1);
                }
            });
            constructor.show();
        }
    }

    private void checkGpsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            currentSpeed.setText("-");
        } else {
            //update speed
            speed = location.getSpeed();
            currentSpeed.setText(String.format("%.2f", speed)); //print speed with 2 decimals

            if (speed > 1.3 && !speedState) {
                mConnectedThread.write("t");
                speedState = true;
            } else if (speedState) {
                mConnectedThread.write("f");
                speedState = false;
            }
            System.out.println("Speed is " + speed + " speedState is " + speedState);

            //update map
            currentposition = new LatLng(location.getLatitude(), location.getLongitude());
            //mMap.addMarker(new MarkerOptions().position(currentposition).title("Marker in current position"));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentposition, 17));

            updateTrack(currentposition);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        checkGpsState();
    }

    @Override
    public void onProviderEnabled(String provider) {
        System.out.println("Gps is active");
    }

    @Override
    public void onProviderDisabled(String provider) {
        System.out.println("Gps disabled");
        checkGpsState();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(R.color.accent_material_light));
        polylineOptions.width(5);
        gpsTrack = mMap.addPolyline(polylineOptions);

        checkGpsPermission();

        mMap.setMyLocationEnabled(true);

        currentposition = new LatLng(40.416764, -3.703833);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentposition, 17));
    }

    //Class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                System.out.println("Connection failed");
                finish();
            }
        }
    }

    private void updateTrack(LatLng lastKnownLatLng) {
        List<LatLng> points = gpsTrack.getPoints();
        points.add(lastKnownLatLng);
        gpsTrack.setPoints(points);
    }
}