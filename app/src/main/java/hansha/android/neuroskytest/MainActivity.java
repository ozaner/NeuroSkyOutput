package hansha.android.neuroskytest;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Main Activity for NeuroSky Test App
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Wrapper for Bluetooth connected EEG headset.
     */
    private static NeuroSky ns = null; //TODO: This is temporary, make a better solution

    private Button startButton = null;
    private Button stopButton = null;
    private Button selectDeviceButton = null;

    /**
     * Address of Bluetooth device.
     */
    BluetoothAdapter ba = null;
    BluetoothDevice bd = null;

    //Data Display Variables
    private ListView lv = null;
    private static DynamicDataAdapter dda = null; //TODO: This is temporary, make a better solution
    private ArrayList<String> titles = new ArrayList<String>(Arrays.asList(NeuroSky.BRAINWAVE_TYPES));
    private static ArrayList<String> data = new ArrayList<String>(); //TODO: This is temporary, make a better solution

    private void initView() {
        lv = (ListView)findViewById(R.id.EEGText);
        for(int i = 0; i <= 8; i++)
            data.add("Null");
        dda = new DynamicDataAdapter(this, titles, data);
        lv.setAdapter(dda);

        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Toast.makeText(MainActivity.this,"connecting ...",Toast.LENGTH_SHORT);
                ns = new NeuroSky(ba,bd);
                ns.start();
            }
        });
        startButton.setText(R.string.startText);

        stopButton =  (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(ns != null)
                    ns.stop();
                ns = null;
            }
        });
        stopButton.setText(R.string.stopText);

        selectDeviceButton =  (Button) findViewById(R.id.selectDeviceButton);
        selectDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                scanDevice();
            }
        });
        selectDeviceButton.setText(R.string.selectDeviceText);
    }

    public static void updateData() {
        for(int i = 0; i < NeuroSky.BRAINWAVE_TYPES.length; i++)
            data.set(i,""+ns.getWavePower(i));
        dda.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        //Verify that bluetooth is supported/enabled
        ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null || !ba.isEnabled()) {
            Toast.makeText(this, "Please enable your Bluetooth and re-run this program!",
                    Toast.LENGTH_LONG).show();
        }
        bd = null;

        initView();
    }

    //show device list while scanning
    private ListView list_select;
    private BTDeviceListAdapter deviceListApapter = null;
    private Dialog selectDialog;

    //The BroadcastReceiver that listens for discovered devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // update to UI
                deviceListApapter.addDevice(device);
                deviceListApapter.notifyDataSetChanged();
            }
        }
    };

    // (3) Demo of getting Bluetooth device dynamically
    public void scanDevice(){
        if(ba.isDiscovering())
            ba.cancelDiscovery();

        setUpDeviceListView();
        //register the receiver for scanning
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        ba.startDiscovery();
    }

    private void setUpDeviceListView(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_select_device, null);
        list_select = (ListView) view.findViewById(R.id.list_select);
        selectDialog = new Dialog(this, R.style.AppTheme);
        selectDialog.setContentView(view);

        //List device dialog
        deviceListApapter = new BTDeviceListAdapter(this);
        list_select.setAdapter(deviceListApapter);
        list_select.setOnItemClickListener(selectDeviceItemClickListener);

        selectDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){

            @Override
            public void onCancel(DialogInterface arg0) {
                MainActivity.this.unregisterReceiver(mReceiver);
            }

        });

        selectDialog.show();

        Set<BluetoothDevice> pairedDevices = ba.getBondedDevices();
        for(BluetoothDevice device: pairedDevices){
            deviceListApapter.addDevice(device);
        }
        deviceListApapter.notifyDataSetChanged();
    }

    //Select device operation
    private AdapterView.OnItemClickListener selectDeviceItemClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if(ba.isDiscovering())
                ba.cancelDiscovery();
            //unregister receiver
            MainActivity.this.unregisterReceiver(mReceiver);

            bd = deviceListApapter.getDevice(arg2);
            selectDialog.dismiss();
            selectDialog = null;
        }

    };

    /**
     * Make sure NeuroSky object is destroyed before app closes. If not data will accumulate.
     */
    @Override
    protected void onDestroy() {
        ns.stop();
        super.onDestroy();
    }
}
