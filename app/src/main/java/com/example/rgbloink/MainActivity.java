package com.example.rgbloink;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import com.example.rgbloink.MyBluetoothService;


public class MainActivity extends AppCompatActivity {


    private final static int REQUEST_ENABLE_BT = 1;
    private ImageView imageView;
    private Button button;
    private Bitmap bitmap;
    private String bluetooth_device;
    private boolean connected = false;
    private TextView bluetooth_status_text;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private BluetoothDevice final_device;
    private BluetoothAdapter bluetoothAdapter;
    private MyBluetoothService bluetoothService;
    private SeekBar brightness_bar;
    private RadioGroup mode_group;


    private  boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int loc = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int loc2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestPermissions();

        //get the name of the bluetooth device the app will connect to
        bluetooth_device = getResources().getString(R.string.bluetooth_device);
        //bluetoothService = new MyBluetoothService(t);
        connected = false;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        boolean thing = bluetoothAdapter.startDiscovery();

        bluetoothAdapter.cancelDiscovery();
        //bluetoothAdapter.cancelDiscovery();


       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant

                return;
            }
            }
*/


        imageView = (ImageView) findViewById(R.id.colorWheel);
        button = (Button) findViewById(R.id.tempView);

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache(true);

        brightness_bar = (SeekBar) findViewById(R.id.brightness_bar);

        brightness_bar.setOnSeekBarChangeListener(new Brightness_Listener());


        mode_group = (RadioGroup) findViewById(R.id.mode_group);


        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int x = (int) event.getX();
                int y = (int) event.getY();

                if ((event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) && ((event.getX() >= 0) && (event.getY() >= 0))) {
                    try {
                        bitmap = imageView.getDrawingCache();
                        int pixel = bitmap.getPixel(x, y);

                        int r = Color.red(pixel);
                        int g = Color.green(pixel);
                        int b = Color.blue(pixel);
                        //String rgbCode = Integer.toString(r) + "," + Integer.toString(g) + "," + Integer.toString(b);
                        //byte x_ = Byte.decode(rgbCode);
                        //byte x_ = Byte.decode("100");

                        if (!(r == g && g == b && b == 0)) {
                            brightness_bar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.rgb(r, g, b), PorterDuff.Mode.MULTIPLY));
                            brightness_bar.getThumb().setColorFilter(new PorterDuffColorFilter(Color.rgb(r, g, b), PorterDuff.Mode.SRC_ATOP));


                            //button.setBackgroundColor(Color.rgb(r, g, b));
                            if (connected == true) {
                                String col_dat = "(!" + Integer.toString(r) + "," + Integer.toString(g) + "," + Integer.toString(b) + ")";
                                bluetoothService.write(col_dat);
                                //t.setText("!" + Integer.toString(r) + ",!" + Integer.toString(g) + "," + Integer.toString(b));
                            }
                        }
                    } catch (Exception e) {

                    }
                }

                return true;
            }
        });
    }

    //create a listener for brightness bar changes
    private class Brightness_Listener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            // Log the progress
            Log.d("DEBUG", "Progress is: "+progress);
            //set textView's text
            int radio_id = mode_group.getCheckedRadioButtonId();
            RadioButton active_btn = (RadioButton) mode_group.getChildAt(radio_id);
            active_btn.

            bluetoothService.write("#" + Float.toString(seekBar.getProgress()) + ")");
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}

    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address


                if (deviceName.equals(bluetooth_device)){
                    //the target bluetooth device has been found

                    final_device = device;

                    ConnectThread c = new ConnectThread(device, bluetoothAdapter);
                    c.start();

                    //bluetoothService = new MyBluetoothService(t, c.getSocket());

                }

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private BluetoothAdapter bluetoothAdapter;
        private UUID MY_UUID;

        public ConnectThread(BluetoothDevice device, BluetoothAdapter mybluetoothAdapter) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            bluetoothAdapter = mybluetoothAdapter;
            MY_UUID = UUID.fromString(getResources().getString(R.string.MY_UUID));

            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                //tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                tmp = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

                //inputStream = socket.getInputStream();
                //outputStream = socket.getOutputStream();
            } catch (IOException e) {
                //Log.e(TAG, "Socket's create() method failed", e);

            }
            mmSocket = tmp;
        }

        public BluetoothSocket getSocket(){
            return mmSocket;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.

            bluetoothAdapter.cancelDiscovery();
            for (int i = 0; i < 10; i++) {
                try {
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.

                    mmSocket.connect();
                    break;
                } catch (IOException connectException) {
                    // Unable to connect; close the socket and return.

                    try {
                        mmSocket.close();
                    } catch (IOException closeException) {
                        //Log.e(TAG, "Could not close the client socket", closeException);

                    }
                    if (i == 9) {
                        return;
                    }
                }
            }
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(mmSocket);

            bluetoothService = new MyBluetoothService(mmSocket);
            connected = true;

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }




}