package ritwik.bluetoothle.callbacks;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class LEScanCallback implements BluetoothAdapter.LeScanCallback {
	private LEScanListener mListener;

	public LEScanCallback ( LEScanListener mListener ) { this.mListener = mListener; }

	@Override public void onLeScan ( BluetoothDevice bluetoothDevice, int RSSI, byte[] scanRecord ) {
		mListener.onLEDeviceFound ( bluetoothDevice, RSSI, scanRecord );
	}

	public interface LEScanListener {
		void onLEDeviceFound ( BluetoothDevice device, int RSSI, byte[] scanRecord );
	}
}
