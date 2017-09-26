package ritwik.bluetoothle.callbacks;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;

public class GATTCallback extends BluetoothGattCallback {
	private GATTListener mListener;

	public GATTCallback ( GATTListener mListener ) { this.mListener = mListener; }

	@Override public void onConnectionStateChange ( BluetoothGatt gatt, int status, int newState ) {
		super.onConnectionStateChange ( gatt, status, newState );
		mListener.onConnectionStateChange ( gatt, status, newState );
	}

	@Override public void onServicesDiscovered ( BluetoothGatt gatt, int status ) {
		super.onServicesDiscovered ( gatt, status );
		mListener.onServicesDiscovered ( gatt, status );
	}

	@Override public void onCharacteristicRead ( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status ) {
		super.onCharacteristicRead ( gatt, characteristic, status );
		mListener.onCharacteristicRead ( gatt, characteristic, status );
	}

	@Override public void onCharacteristicWrite ( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status ) {
		super.onCharacteristicWrite ( gatt, characteristic, status );
		mListener.onCharacteristicWrite ( gatt, characteristic, status );
	}

	@Override public void onCharacteristicChanged ( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic ) {
		super.onCharacteristicChanged ( gatt, characteristic );
		mListener.onCharacteristicChanged ( gatt, characteristic );
	}

	public interface GATTListener {
		void onConnectionStateChange ( BluetoothGatt gatt, int status, int newState );
		void onServicesDiscovered ( BluetoothGatt gatt, int status );
		void onCharacteristicRead ( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status );
		void onCharacteristicWrite ( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status );
		void onCharacteristicChanged ( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic );
	}
}