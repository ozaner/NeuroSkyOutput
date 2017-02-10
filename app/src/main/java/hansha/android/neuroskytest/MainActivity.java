package hansha.android.neuroskytest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main Activity for NeuroSky Test App
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Wrapper for Bluetooth connected EEG headset.
     */
    private NeuroSky ns = null;

    private Button startButton = null;
    private Button stopButton = null;
    private Button selectDeviceButton = null;

    //Data Display Variables
    private ListView lv = null;
    private DynamicDataAdapter dda = null;
    private ArrayList<String> titles = new ArrayList<String>(Arrays.asList(NeuroSky.BRAINWAVE_TYPES));
    private ArrayList<String> data = new ArrayList<String>();

    private void initView() {
        lv = (ListView)findViewById(R.id.EEGText);
        data.add("0");
        data.add("0");
        data.add("0");
        data.add("0");
        data.add("0");
        data.add("0");
        data.add("0");
        data.add("0");
        dda = new DynamicDataAdapter(this, titles, data);
        lv.setAdapter(dda);

        startButton =  (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Toast.makeText(MainActivity.this,"connecting ...",Toast.LENGTH_SHORT);
                start();
            }
        });

        stopButton =  (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(tgStreamReader != null) tgStreamReader.stop();
            }
        });

        selectDeviceButton =  (Button) findViewById(R.id.selectDeviceButton);
        selectDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                scanDevice();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        BluetoothAdapter ba = null;
        initBluetoothAdapter(ba); //Inits & Checks if bluetooth is enabled
        BluetoothDevice bd = null;

        ns = new NeuroSky(ba,bd);
        initView();
    }

    /**
     * Initializes Bluetooth Adapter, If cannot then return error toast.
     */
    private void initBluetoothAdapter(BluetoothAdapter ba) {
        //Verify that bluetooth is supported/enabled
        ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null || !ba.isEnabled()) {
            Toast.makeText(this, "Please enable your Bluetooth and re-run this program!",
                    Toast.LENGTH_LONG).show();
        }
    }
}
