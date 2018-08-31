package sbk.fatima.bluetoothled_v1_1;

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
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends Activity implements LocationListener {

    Button btnOn, btnOff, btnDisconnect;
    TextView currentSpeed;
    Handler bluetoothIn;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private LocationManager locManager = null;

    private ConnectedThread mConnectedThread;

    //SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //UUID SERIAL_UUID = device.getUuids()[0].getUuid(); //if you don't know the UUID of the bluetooth device service, you can get it like this from android cache

    // String for MAC address
    private static String address = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Link the buttons and textViews to respective views
        btnOn = (Button) findViewById(R.id.buttonOn);
        btnOff = (Button) findViewById(R.id.buttonOff);
        btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
        currentSpeed = (TextView) findViewById(R.id.speed);

        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        this.onLocationChanged(null);
        checkGpsState();
        checkBTState();

        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("0");    // Send "0" via Bluetooth
            }
        });

        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("1");    // Send "1" via Bluetooth
            }
        });

        btnDisconnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectedThread.write("q");
                onPause();
                finish();
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            Method method;

            method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class } );
            return (BluetoothSocket) method.invoke(device, 1);
        }
        catch (Exception e) {
            System.out.println("Error creating socket: " + e);
            System.out.println("Will try with another kind of socket");
            try {
                return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
            }
            catch (Exception e2) {
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
            System.out.println("La creacción del Socket fallo");
            System.out.println("Error: " + e);
        }
        // Establish the Bluetooth socket connection.
        try
        {
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
        mConnectedThread.write("0");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try {
            if (locManager != null) {
                locManager.removeUpdates(this);
                locManager = null;
            }
            System.out.println("Finish gps updates");

        } catch (Exception e1){
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

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void checkGpsState() {
        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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

    @Override
    public void onLocationChanged(Location location) {
        if(location == null){
            currentSpeed.setText("-");
        }else{
            currentSpeed.setText(String.format("%.2f", location.getSpeed())); //imprimir velocidad con 2 decimales
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        checkGpsState();
    }

    @Override
    public void onProviderEnabled(String provider) {
        System.out.println("Gps enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        System.out.println("Gps disabled");
        checkGpsState();
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
            } catch (IOException e) { }

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
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}

