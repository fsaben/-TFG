package sbk.fatima.tfg;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class DeviceListActivity extends Activity {

  
    // declare textview for connection status
    TextView textView1;
    
    // EXTRA string to send on to mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private LocationManager locManager;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
    }
    
    @Override
    public void onResume() 
    {
        super.onResume();
    	//*************** 
    	checkBTState();
    	checkGpsState();

    	textView1 = (TextView) findViewById(R.id.connecting);
    	textView1.setTextSize(20);
    	textView1.setText(" ");

    	// Initialize array adapter for paired devices
    	mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

    	// Find and set up the ListView for paired devices
    	ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
    	pairedListView.setAdapter(mPairedDevicesArrayAdapter);
    	pairedListView.setOnItemClickListener(mDeviceClickListener);

    	// Get the local Bluetooth adapter
    	mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    	// Get a set of currently paired devices and append to 'pairedDevices'
    	Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

    	// Add previosuly paired devices to the array
    	if (pairedDevices.size() > 0) {
    		for (BluetoothDevice device : pairedDevices) {
    			mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
    		}
    	} else {
    		String noDevices = getResources().getText(R.string.none_paired).toString();
    		mPairedDevicesArrayAdapter.add(noDevices);
    	}
    }

    // Set up on-click listener for the list (nicked this - unsure)
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            textView1.setText("Conectando...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity while taking an extra which is the MAC address.
			Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
			startActivity(i);   
        }
    };

    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        mBtAdapter=BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if(mBtAdapter==null) { 
        	System.out.println("The device does not have Bluetooth");
        } else {
            if (mBtAdapter.isEnabled()) {
            } else {
                //Prompt user to turn on Bluetooth
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
            constructor.setPositiveButton("Ajustes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent enableGps = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGps, 1);
                }
            });
            constructor.show();
        }
    }
}