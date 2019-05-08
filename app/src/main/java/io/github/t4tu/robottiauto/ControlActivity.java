package io.github.t4tu.robottiauto;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;

public class ControlActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        TextView speedText = findViewById(R.id.speed_text);
        speedText.setText("Nopeus: 100%");

        Intent intent = getIntent();
        String address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : devices) {
            if (device.getAddress().equals(address)) {
                bluetoothDevice = device;
                new BluetoothConnectionTask().execute();
                return;
            }
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            bluetoothSocket.close();
        }
        catch (IOException e) { }
    }

    private void init() {

        ImageButton forward = findViewById(R.id.arrow_forward);
        ImageButton backward = findViewById(R.id.arrow_backward);
        ImageButton left = findViewById(R.id.arrow_left);
        ImageButton right = findViewById(R.id.arrow_right);

        forward.setOnTouchListener(new TouchListener('1'));
        backward.setOnTouchListener(new TouchListener('2'));
        left.setOnTouchListener(new TouchListener('3'));
        right.setOnTouchListener(new TouchListener('4'));

        SeekBar speedBar = findViewById(R.id.speed_bar);

        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int speed = seekBar.getProgress();
                TextView speedText = findViewById(R.id.speed_text);
                int speedPercentage = (int) (speed / 255f * 100);
                speedText.setText("Nopeus: " + speedPercentage + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int speed = seekBar.getProgress();
                sendBluetoothCommand('5');
                sendBluetoothData(speed);
                sendBluetoothCommand('6');
                sendBluetoothData(speed);
            }
        });
    }

    private void sendBluetoothCommand(char command) {
        TextView status = findViewById(R.id.status);
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.getOutputStream().write(command);
                status.setText(R.string.connected);
                status.setTextColor(getResources().getColor(R.color.green, null));
                return;
            }
            catch (IOException e) { }
        }
        status.setText(R.string.not_connected);
        status.setTextColor(getResources().getColor(R.color.red, null));
    }

    private void sendBluetoothData(int data) {
        TextView status = findViewById(R.id.status);
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.getOutputStream().write(data);
                status.setText(R.string.connected);
                status.setTextColor(getResources().getColor(R.color.green, null));
                return;
            }
            catch (IOException e) { }
        }
        status.setText(R.string.not_connected);
        status.setTextColor(getResources().getColor(R.color.red, null));
    }

    private class TouchListener implements View.OnTouchListener {

        private char command;

        private TouchListener(char command) {
            this.command = command;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ImageButton button = (ImageButton) view;
                button.setImageResource(R.drawable.arrow2);
                sendBluetoothCommand(command);
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                ImageButton button = (ImageButton) view;
                button.setImageResource(R.drawable.arrow);
                sendBluetoothCommand('0');
            }
            return true;
        }
    }

    private class BluetoothConnectionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            TextView status = findViewById(R.id.status);
            status.setText(R.string.connecting);
            status.setTextColor(getResources().getColor(R.color.yellow, null));
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(MainActivity.BLUETOOTH_UUID);
                bluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();
                return true;
            }
            catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView status = findViewById(R.id.status);
            if (result) {
                status.setText(R.string.connected);
                status.setTextColor(getResources().getColor(R.color.green, null));
                init();
            }
            else {
                status.setText(R.string.not_connected);
                status.setTextColor(getResources().getColor(R.color.red, null));
            }
        }
    }
}
