package hansha.android.neuroskytest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * TgStreamHandler implementation for this app.
 */
public class NeuroSky implements TgStreamHandler {

    public static final String[] BRAINWAVE_TYPES = {"Delta","Theta","Low Alpha","High Alpha",
            "Low Beta","High Beta","Low Gamma","Middle Gamma"};

    /**
     * EEG Data, i.e The brainwaves
     */
    private EEGPower EEGData = null;

    /**
     * Signal state
     */
    private int poorSignal = 0;

    /**
     * Reads data output of Neurosky headset.
     */
    private TgStreamReader tgStreamReader;

    /**
     * Adapter for Neurosky Hardware.
     */
    private BluetoothAdapter ba;

    /**
     * Connected Bluetooth device (Mindwave).
     */
    private BluetoothDevice bd;

    private Handler LinkDetectedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MindDataType.CODE_RAW:
                    //updateWaveView(msg.arg1);
                    break;
                case MindDataType.CODE_EEGPOWER:
                    EEGPower power = (EEGPower)msg.obj;
                    EEGData = power.isValidate() ? power : EEGData;
                    MainActivity.updateData();//TODO: This is temporary, make a better solution
                    break;
                case MindDataType.CODE_POOR_SIGNAL:
                    poorSignal = msg.arg1;
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * Returns a new NeuroSky object with the given bluetoothDevice.
     * @param ba - BluetoothAdapter
     * @param bd - BluetoothDevice
     */
    public NeuroSky (BluetoothAdapter ba, BluetoothDevice bd) {
        this.ba = ba;
        this.bd = bd;
        tgStreamReader = createStreamReader(bd);
    }

    /**
     * Returns the power of a given brainwave at the moment.
     * @param ID - Brainwave Type ID, defined by WAVE_LIST
     * @return that brainwaves' power at the moment.
     */
    public int getWavePower(int ID) {
        if(EEGData != null)
            switch(ID) {
                case 0:
                    return EEGData.delta;
                case 1:
                    return EEGData.theta;
                case 2:
                    return EEGData.lowAlpha;
                case 3:
                    return EEGData.highAlpha;
                case 4:
                    return EEGData.lowBeta;
                case 5:
                    return EEGData.highBeta;
                case 6:
                    return EEGData.lowGamma;
                case 7:
                    return EEGData.middleGamma;
                default:
                    return -1;
            }
        return -1;
    }

    /**
     * Returns the power of a given brainwave at the moment.
     * @param type - Brainwave Type ID, defined by WAVE_LIST
     * @return that brainwaves' power at the moment.
     */
    public int getWavePower(String type) {
        return getWavePower(Arrays.asList(BRAINWAVE_TYPES).indexOf(type));
    }

    @Override
    public void onDataReceived(int datatype, int data, Object obj) {
        Message msg = LinkDetectedHandler.obtainMessage();
        msg.what = datatype;
        msg.arg1 = data;
        msg.obj = obj;
        LinkDetectedHandler.sendMessage(msg);
    }

    @Override
    public void onStatesChanged(int connectionState) {
        switch (connectionState) {
            case ConnectionStates.STATE_CONNECTED:
                tgStreamReader.start();
                break;
        }
    }

    @Override
    public void onChecksumFail(byte[] bytes, int i, int i1) {}

    @Override
    public void onRecordFail(int i) {}

    /**
     * If the TgStreamReader is created, just change the bluetooth
     * else create TgStreamReader, set data receiver, TgStreamHandler and parser
     * @param bd
     * @return TgStreamReader
     */
    private TgStreamReader createStreamReader(BluetoothDevice bd){
        if(tgStreamReader == null){
            tgStreamReader = new TgStreamReader(bd,this);
            tgStreamReader.startLog();
        }else{
            tgStreamReader.changeBluetoothDevice(bd);
            tgStreamReader.setTgStreamHandler(this);
        }
        return tgStreamReader;
    }

    /**
     * Starts and connects to bluetooth device, given in constructor.
     */
    public void start(){
        createStreamReader(bd);

        tgStreamReader.connectAndStart();
    }

    /**
     * Destroys the tgStreamReader object if one exists. Only 1 should exist at any one time.
     */
    public void stop() {
        if(tgStreamReader!= null){
            tgStreamReader.stop();
            tgStreamReader.close();
            tgStreamReader= null;
        }
    }
}
