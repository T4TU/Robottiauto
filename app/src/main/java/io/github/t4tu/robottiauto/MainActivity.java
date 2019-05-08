package io.github.t4tu.robottiauto;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_ADDRESS = "BLUETOOTH_ADDRESS";
    public static final UUID BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private PairedDeviceListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!enableBluetooth()) {
            finish();
            return;
        }

        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

        listAdapter = new PairedDeviceListAdapter(devices);

        ListView listView = findViewById(R.id.paired_device_list);
        listView.setAdapter(listAdapter);
    }

    public void updatePairedDeviceList(View view) {
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        listAdapter.setDevices(devices);
        listAdapter.notifyDataSetChanged();
    }

    public void selectPairedDevice(View view) {
        Intent intent = new Intent(this, ControlActivity.class);
        TextView deviceAddress = view.findViewById(R.id.device_address);
        String address = deviceAddress.getText().toString();
        intent.putExtra(EXTRA_ADDRESS, address);
        startActivity(intent);
        finish();
    }

    private boolean enableBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, 1);
        }
        return true;
    }

    private class PairedDeviceListAdapter extends BaseAdapter {

        private Set<BluetoothDevice> devices;

        public PairedDeviceListAdapter(Set<BluetoothDevice> devices) {
            this.devices = devices;
        }

        public Set<BluetoothDevice> getDevices() {
            return devices;
        }

        public void setDevices(Set<BluetoothDevice> devices) {
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.paired_device_list_layout, null);
            }
            BluetoothDevice device = devices.toArray(new BluetoothDevice[devices.size()])[i];
            TextView deviceName = view.findViewById(R.id.device_name);
            deviceName.setText(device.getName());
            TextView deviceAddress = view.findViewById(R.id.device_address);
            deviceAddress.setText(device.getAddress());
            return view;
        }
    }
}
