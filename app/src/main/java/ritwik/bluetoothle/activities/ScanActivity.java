package ritwik.bluetoothle.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ritwik.bluetoothle.R;
import ritwik.bluetoothle.adapters.BluetoothDeviceAdapter;
import ritwik.bluetoothle.callbacks.LEScanCallback;
import ritwik.bluetoothle.callbacks.ScanCallback;
import ritwik.bluetoothle.utilities.BluetoothNameResolver;
import ritwik.bluetoothle.utilities.ConstantMethods;
import ritwik.bluetoothle.utilities.GATTConstants;

public class ScanActivity
		extends AppCompatActivity
		implements View.OnClickListener,
		           BluetoothDeviceAdapter.BluetoothDeviceListener,
		           LEScanCallback.LEScanListener,
		           ScanCallback.ScanListener {
	private BluetoothAdapter mBluetoothAdapter;
	private ScanCallback mScanCallback;
	private BluetoothDeviceAdapter mAdapter;
	private List<BluetoothDevice> mDevices = new ArrayList<> ();
	private BluetoothGatt mGatt = null;
	private List<BluetoothGattService> mServices;
	private BluetoothGatt mBluetoothGatt;

	private TextView mTextScan;

	private int mBytesToSentLeft = 1600;
	private int mBytesWritten = 0;
	private int mChuksSent = 0;

	private long mStartTime;

	private static final byte[] dummy_data = {
			0x00,0x00,0x00,0x00,0x00,
			0x00,0x00,0x00,0x00,0x00,
			0x00,0x00,0x00,0x00,0x00,
			0x00,0x00,0x00,0x00,0x00
	};

	@Override protected void onCreate ( Bundle savedInstanceState ) {
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.activity_scan );
		initializeViews ();
		checkBluetoothLEInDevice ();
	}

	private void initializeViews () {
		RecyclerView devices = ( RecyclerView ) findViewById ( R.id.recycler_view_bluetooth_devices );
		devices.setLayoutManager ( new LinearLayoutManager ( ScanActivity.this ) );
		mAdapter = new BluetoothDeviceAdapter ( mDevices, ScanActivity.this, ScanActivity.this );
		devices.setAdapter ( mAdapter );
		RelativeLayout scan = ( RelativeLayout ) findViewById ( R.id.relative_layout_scan_devices );
		mTextScan = (TextView) findViewById ( R.id.text_view_scan );
		scan.setOnClickListener ( ScanActivity.this );
	}

	/**
	 * Check whether Bluetooth LE is supported by the Android Device or not.
	 */
	private void checkBluetoothLEInDevice () {
		if ( ! getPackageManager ().hasSystemFeature ( PackageManager.FEATURE_BLUETOOTH_LE ) ) showToastBluetoothNotSupported ();
		else {
			final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService( Context.BLUETOOTH_SERVICE);
			mBluetoothAdapter = bluetoothManager.getAdapter(); // Initialized Bluetooth Adapter.
			if ( mBluetoothAdapter == null ) showToastBluetoothNotSupported (); // Checks if Bluetooth is supported on the device.
			else if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
				mScanCallback = new ScanCallback ( ScanActivity.this ); // Instantiate Scan Callback Class.
			}
		}
	}

	/**
	 * Shows the Toast message that BLE is not supported.
	 */
	private void showToastBluetoothNotSupported () {
		ConstantMethods.showToastMessage ( ScanActivity.this, R.string.bluetooth_not_supported );
		finish();
	}

	@Override public void onClick ( View view ) {
		switch ( view.getId () ) {
			case R.id.relative_layout_scan_devices :
				// Toggle Start or Stop Scanning of Bluetooth LE Devices.
				if ( mTextScan.getText ().toString ().equals ( getResources ().getString ( R.string.scan_devices ) ) ) startScan ();
				else stopScan ();
				break;
		}
	}

	private void startScan () {
		mTextScan.setText ( getResources ().getString ( R.string.stop_scanning ) );
		if ( mScanCallback != null )
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
				// Start Bluetooth LE Device Discovery.
				mBluetoothAdapter.getBluetoothLeScanner ().startScan ( null, new ScanSettings.Builder ().build (), mScanCallback );
				mAdapter.clearDeviceList ();
				android.util.Log.e ( "BLE", "Scan Started" );
			}
		else ConstantMethods.showToastMessage ( ScanActivity.this, "Null Bluetooth Adapter" );
	}

	private void stopScan () {
		mTextScan.setText ( getResources ().getString ( R.string.scan_devices ) );
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
			// Stop Bluetooth LE Device Discovery.
			mBluetoothAdapter.getBluetoothLeScanner ().stopScan ( mScanCallback );
			android.util.Log.e ( "BLE", "Scan Stopped" );
		}
	}

	/**
	 * Connects to a Bluetooth LE Peripheral Device.
	 * Also reads the Characteristic of a Service.
	 * @param device Bluetooth LE Device we wish to connect.
	 */
	private void connectDevice ( BluetoothDevice device ) {
		android.util.Log.e ( "BLE", "connectDevice()" );
		android.util.Log.e ( "Address", device.getAddress () );
		// Connects to the Bluetooth LE Peripheral Device.
		device.connectGatt ( ScanActivity.this, false, mGattCallback );
		// Request for a Characteristic's value.
		if ( mBluetoothGatt == null )
			android.util.Log.e ( "Bluetooth GATT", "null" );
		else {
			android.util.Log.e ( "Bluetooth GATT", "NOT null" );
			BluetoothGattCharacteristic batteryLevel = new BluetoothGattCharacteristic ( UUID.fromString ( "00002a19-0000-1000-8000-00805f9b34fb" ), BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ );
			mBluetoothGatt.readCharacteristic ( batteryLevel );
		}
	}

	@Override public void onBluetoothDeviceSelected ( BluetoothDevice device ) {
		connectDevice ( device );
	}

	@Override public void onLEDeviceFound ( BluetoothDevice device, int RSSI, byte[] scanRecord ) {
		String scanRecords = "Not Found";
		if ( scanRecord != null ) new String ( scanRecord );
		String deviceName = device.getName ();
		if ( deviceName == null ) deviceName = "Unknown";
		android.util.Log.e ( "Scan Record", scanRecords );
		android.util.Log.e ( "Device", "Found" );
		android.util.Log.e ( "Name", deviceName );
		android.util.Log.e ( "Address", device.getAddress () + "\n" );
		android.util.Log.d ( "-------", "---------" );
		mDevices.add ( device );
		mAdapter.updateDeviceList ( mDevices );
	}

	@Override public void onScanResult ( int callbackType, ScanResult result ) {
		Log.e ( "BLE", "onScanResult()" );
		/*
		 * Bluetooth LE Devices advertise themselves some data,
		 * such as Manufacture Related data ( Optional ), Device UUID, Device MAC Address, etc.
		 * Scan Results store all this information of a device.
		 * From the Scan Results, we can filter device that can be connected based on Manufacturer Specific data provided by them.
		 * This data has to be advertised by Manufacturers in order for us to perform filtering of Devices.
		 */
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
			ScanRecord scanRecord = result.getScanRecord ();
			if ( scanRecord == null ) android.util.Log.e ( "Scan Record", "null" );
			else android.util.Log.e ( "Scan Record", scanRecord.toString () );
		}

		// Add Found Devices to Recycler View.
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
			if ( ! mAdapter.checkDevice ( result.getDevice () ) ) {
				mDevices.add ( result.getDevice () );
				mAdapter.updateDeviceList ( mDevices );
			}
		}
	}

	@Override public void onBatchScanResults ( List<ScanResult> results ) {
		android.util.Log.e ( "BLE", "onBatchScanResult()" );
	}

	@Override public void onScanFailed ( int errorCode ) {
		android.util.Log.e ( "BLE", "onScanFailed()" );
	}

	/**
	 * GATT Callback.
	 */
	private BluetoothGattCallback mGattCallback =
			new BluetoothGattCallback () {
				@Override public void onConnectionStateChange ( final BluetoothGatt gatt, int status, int newState ) {
					super.onConnectionStateChange ( gatt, status, newState );
					android.util.Log.e ( "Ble", "onServicesDiscovered()" );
					/*
					 * Invokes when there is a change of connection state from the Bluetooth LE device.
					 */

					// Switch between the various states of connection of BLE Device.
					switch ( newState ) {
						case BluetoothProfile.STATE_CONNECTING :
							android.util.Log.e ( "New State", "Connecting" );
							break;
						case BluetoothProfile.STATE_CONNECTED :
							startServicesDiscovery ( gatt );
							break;
						case BluetoothProfile.STATE_DISCONNECTING :
							android.util.Log.e ( "New State", "Disconnecting" );
							break;
						case BluetoothProfile.STATE_DISCONNECTED :
							android.util.Log.e ( "New State", "Disconnected" );
							break;
					}
				}

				@Override public void onServicesDiscovered ( BluetoothGatt gatt, int status ) {
					/*
					 * Invokes when Services of the connected Bluetooth LE device are found.
					 */
					android.util.Log.e ( "Ble", "onServicesDiscovered()" );
					super.onServicesDiscovered ( gatt, status );
					mBluetoothGatt = gatt;
					getSupportedServices ();
				}

				@Override public void onCharacteristicRead (
						BluetoothGatt gatt,
						BluetoothGattCharacteristic characteristic,
						int status
				) {
					super.onCharacteristicRead ( gatt, characteristic, status );
					// Switch between the various states of connection of BLE Device.
					android.util.Log.e ( "Bluetooth", "onCharacteristic()" );
					switch ( status ) {
						case BluetoothGatt.GATT_SUCCESS :
							onCharacteristicsRead ( characteristic );
							break;
						case BluetoothGatt.GATT_FAILURE :
							android.util.Log.e ( "Characteristic Read", "Failure" );
							break;
						case BluetoothGatt.GATT_READ_NOT_PERMITTED :
							android.util.Log.e ( "Characteristic Read", "Read Not Permitted" );
							break;
						case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION :
							android.util.Log.e ( "Characteristic Read", "Insufficient Authentication" );
							break;
						case BluetoothGatt.GATT_CONNECTION_CONGESTED :
							android.util.Log.e ( "Characteristic Read", "Connection Congestion" );
							break;
					}
				}

				@Override public void onCharacteristicWrite (
						BluetoothGatt gatt,
						BluetoothGattCharacteristic characteristic,
						int status
				) {
					super.onCharacteristicWrite ( gatt, characteristic, status );
					android.util.Log.e ( "BLE", "onCharacteristicWrite()" );

					// Switch between the various states of connection of BLE Device.
					switch ( status ) {
						case BluetoothGatt.GATT_SUCCESS :
							onCharacteristicWritten ( characteristic );
							break;
						case BluetoothGatt.GATT_FAILURE :
							android.util.Log.e ( "Characteristic Write", "Failure" );
							break;
						case BluetoothGatt.GATT_CONNECTION_CONGESTED :
							android.util.Log.e ( "Characteristic Write", "Connection Congested" );
							break;
						case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION :
							android.util.Log.e ( "Characteristic Write", "Insufficient Authentication" );
							break;
						case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION :
							android.util.Log.e ( "Characteristic Write", "Insufficient Encryption" );
							break;
						case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH :
							android.util.Log.e ( "Characteristic Write", "Invalid Attribute Length" );
							break;
					}
				}

				@Override public void onCharacteristicChanged ( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic ) {
					super.onCharacteristicChanged ( gatt, characteristic );
					android.util.Log.e ( "BLE", "onCharacteristicWrite()" );
				}
	};

	/**
	 * Starts the Service Discovery from a connected BLE Device.
	 * @param gatt Bluetooth GATT.
	 */
	private void startServicesDiscovery ( final BluetoothGatt gatt ) {
		new Handler ( Looper.getMainLooper() )
				.post ( new Runnable() {
					       @Override public void run() {
						       mGatt = gatt;
						       mGatt.discoverServices ();
					       }
				       }
				);
	}

	/**
	 * Get the List of Services presented by the connected BLE device.
	 */
	private void getSupportedServices () {
		android.util.Log.e ( "Method", "getSupportedServices ()" );
		// Clear all the Services for previous BLE device.
		if ( mServices != null && mServices.size () > 0 ) mServices.clear ();
		// Check Whether GATT instance is null or not, as it will give us the services of connected BLE device.
		if ( mGatt != null ) {
			mServices = mGatt.getServices ();
			android.util.Log.e ( "Service", String.valueOf ( mServices.size () ) );
		}

		android.util.Log.d ( "--------", "--------" );
		processServices ();
	}

	/**
	 * Process the List of Services.
	 */
	private void processServices () {
		android.util.Log.e ( "Method", "processServices ()" );
		for ( BluetoothGattService service : mServices ) {
			switch ( service.getUuid ().toString () ) {
				case GATTConstants.SERVICE_BATTERY :
					viewBatteryServiceCharacteristics ( service );
					break;
				case GATTConstants.SERVICE_FILE :
					viewFileServiceCharacteristics ( service );
					break;
			}
		}
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Views and reads the Characteristics for Service : Battery Service.
	 * @param service Bluetooth GATT Service.
	 */
	private void viewBatteryServiceCharacteristics ( BluetoothGattService service ) {
		android.util.Log.e ( "Method", "viewBatteryServiceCharacteristics ()" );
		List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics ();
		for ( BluetoothGattCharacteristic characteristic : characteristics ) {
			String charUUID = characteristic.getUuid ().toString ();
			switch ( charUUID ) {
				case GATTConstants.CHARACTERISTIC_BATTERY_LEVEL :
					readCharacteristic ( characteristic );
					break;
				default : android.util.Log.e ( "Battery Service", "Desired Service Not Found" );
			}
		}
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Views and reads the Characteristics for Service : File Service.
	 * @param service Bluetooth GATT Service.
	 */
	private void viewFileServiceCharacteristics ( BluetoothGattService service ) {
		android.util.Log.e ( "Method", "viewFileServiceCharacteristics ()" );
		List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics ();
		for ( BluetoothGattCharacteristic characteristic : characteristics ) {
			String charUUID = characteristic.getUuid ().toString ();
			switch ( charUUID ) {
				case GATTConstants.CHARACTERISTIC_FILE_TRANSFER :
					readFileTransferCharacteristic ( characteristic );
					break;
				case GATTConstants.CHARACTERISTIC_LOCATION :
					readLocationCharacteristic ( characteristic );
					break;
				default : android.util.Log.e ( "File Transfer", "Desired Service Not Found" );
			}
		}
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Makes the read request for Characteristic : File Transfer in the Peripheral.
	 * @param characteristic Bluetooth GATT Characteristic.
	 */
	private void readFileTransferCharacteristic ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "readFileTransferCharacteristic ()" );
		characteristic.setValue ( new byte[] {0x01} );
		readCharacteristic ( characteristic );
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Makes the read request for Characteristic : Location in the Peripheral.
	 * @param characteristic Bluetooth GATT Characteristic.
	 */
	private void readLocationCharacteristic ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "readLocationCharacteristic ()" );
		readCharacteristic ( characteristic );
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Reads and sets some value for Characteristic : Battery Level.
	 * @param characteristic Bluetooth GATT Characteristic.
	 */
	private void onBatteryLevelCharacteristicRead ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "onBatteryLevelCharacteristicRead ()" );
		String charName = BluetoothNameResolver.resolveCharacteristicName ( characteristic.getUuid ().toString () );
		android.util.Log.e ( "Characteristic Name", charName );
		characteristic.setValue ( new byte[] {0x01} );
		android.util.Log.e ( "Battery Level", "Set" );
		android.util.Log.d ( "--------", "--------" );
		writeCharacteristic ( characteristic );
	}

	/**
	 * Reads and sets some value for Characteristic : File Transfer.
	 * @param characteristic Bluetooth GATT Characteristic.
	 */
	private void onFileTransferCharacteristicRead ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "onFileTransferCharacteristicRead ()" );
		String charName = BluetoothNameResolver.resolveCharacteristicName ( characteristic.getUuid ().toString () );
		android.util.Log.e ( "Characteristic Name", charName );
		characteristic.setValue ( dummy_data );
		android.util.Log.e ( "File Transfer", "Ready" );
		mStartTime = System.currentTimeMillis ();
		android.util.Log.d ( "--------", "--------" );
		writeCharacteristic ( characteristic );
	}

	/**
	 * Reads the File Transfer Characteristic for any data present.
	 * @param characteristic Bluetooth GATT Characteristic.
	 */
	private void onLocationCharacteristicRead ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "onLocationCharacteristicRead ()" );
		String charName = BluetoothNameResolver.resolveCharacteristicName ( characteristic.getUuid ().toString () );
		android.util.Log.e ( "Characteristic Name", charName );
		byte [] value = characteristic.getValue ();
		if ( value == null )
			android.util.Log.e ( "Value", "null" );
		else {
			android.util.Log.e ( "Byte Length", String.valueOf ( value.length ) );
			String location = new String ( value, Charset.forName ( "UTF-8" ) );
			if ( location == null )
				 android.util.Log.e ( "Location", "Null" );
			else {
				android.util.Log.e ( "Location", location );
				/*ConstantMethods.showToastMessage ( ScanActivity.this, location );*/
			}
		}
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Makes a request to the Connected Bluetooth LE device for reading a particular Characteristic.
	 * @param characteristic Characteristic to be read.
	 */
	private void readCharacteristic ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "readCharacteristic ()" );
		String charName = BluetoothNameResolver.resolveCharacteristicName ( characteristic.getUuid ().toString () );
		boolean readChar = mBluetoothGatt.readCharacteristic ( characteristic );
		if ( readChar ) android.util.Log.e ( "Read", charName );
		else {
			android.util.Log.e ( "Not Read", charName );
			if ( mBluetoothGatt.discoverServices () ) android.util.Log.e ( "Services", "Discovered Again" );
			else android.util.Log.e ( "Services", "Not Discovered" );
		}
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Writes a characteristic to the connected BLE device.
	 * @param characteristic Bluetooth GATT Characteristic.
	 */
	private void writeCharacteristic ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "writeCharacteristic ()" );
		String charName = BluetoothNameResolver.resolveCharacteristicName ( characteristic.getUuid ().toString () );
		android.util.Log.e ( "Write", charName );
		// Writes the a value to the Characteristic of Bluetooth LE device.
		boolean writeChar = mBluetoothGatt.writeCharacteristic ( characteristic );
		if ( writeChar ) {
			android.util.Log.e ( "Written", charName );
		} else {
			android.util.Log.e ( "Not Written", charName );
			if ( mBluetoothGatt.discoverServices () ) android.util.Log.e ( "Services", "Discovered Again" );
			else android.util.Log.e ( "Services", "Not Discovered" );
		}
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Invokes when a characteristic from Bluetooth LE device is read.
	 * From here we can read the value from a Characteristic of Bluetooth LE device.
	 * @param characteristic Characteristic containing its value fetched from Bluetooth LE device.
	 */
	private void onCharacteristicsRead ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "onCharacteristicsRead ()" );
		// Check whether characteristic instance is null or not
		if ( characteristic == null ) android.util.Log.e ( "Characteristic", "Null" );
		else {
			android.util.Log.e ( "Characteristic UUID", characteristic.getUuid ().toString () );
			switch ( characteristic.getUuid ().toString () ) {
				case GATTConstants.CHARACTERISTIC_BATTERY_LEVEL :
					android.util.Log.e ( "Read", "Battery Level" );
					onBatteryLevelCharacteristicRead ( characteristic );
					break;
				case GATTConstants.CHARACTERISTIC_FILE_TRANSFER :
					android.util.Log.e ( "Read", "File Transfer" );
					onFileTransferCharacteristicRead ( characteristic );
					break;
				case GATTConstants.CHARACTERISTIC_LOCATION :
					android.util.Log.e ( "Read", "Location" );
					onLocationCharacteristicRead ( characteristic );
					break;
				default : android.util.Log.e ( "Characteristic", "Not Found" );
			}
		}
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Invokes when a characteristic has been written in the Peripheral.
	 * @param characteristic Bluetooth GATT Characteristic.
	 */
	private void onCharacteristicWritten ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "onCharacteristicWritten ()" );
		switch ( characteristic.getUuid ().toString () ) {
			case GATTConstants.CHARACTERISTIC_BATTERY_LEVEL :
				onWriteBatteryLevel ( characteristic );
				break;
			case GATTConstants.CHARACTERISTIC_FILE_TRANSFER :
				onWriteFileTransfer ( characteristic );
				break;
			case GATTConstants.CHARACTERISTIC_LOCATION :
				onWriteLocation ( characteristic );
				break;
			default : android.util.Log.e ( "Characteristic Read", "Desired Characteristic not Found" );
		}
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Handles the events when Data has been written in the Peripheral for the characteristic : Battery Level.
	 * @param characteristic Bluetooth GATT Characteristic.
	 */
	private void onWriteBatteryLevel ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "onWriteBatteryLevel ()" );
		String charName = BluetoothNameResolver.resolveCharacteristicName ( characteristic.getUuid ().toString () );
		android.util.Log.e ( "Characteristic Name", charName );
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Handles the events when Data has been written in the Peripheral for the characteristic : File Transfer.
	 * @param characteristic Bluetooth GATT Characteristic.
	 */
	private void onWriteFileTransfer ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "onWriteFileTransfer ()" );
		mBytesWritten += dummy_data.length;
		mChuksSent++;
		android.util.Log.e ( "DATA", dummy_data.length + " bytes received by device." );
		android.util.Log.e ( "DATA", mChuksSent + " chunks" );
		android.util.Log.e ( "DATA", mBytesWritten  + " bytes." );
		mBytesToSentLeft -= dummy_data.length;
		if ( mBytesToSentLeft > 0 ) {
			writeCharacteristic ( characteristic );
		} else {
			long endTIme = System.currentTimeMillis();
			android.util.Log.e ( "DATA", String.format ( Locale.ENGLISH, "Finished, total time: %d", ( endTIme - mStartTime ) * 1000 ) );
		}
		android.util.Log.d ( "--------", "--------" );
	}

	/**
	 * Handles the events when Location has been written in the Peripheral for the characteristic : Location.
	 * @param characteristic Bluetooth GATT Characteristic.
	 */
	private void onWriteLocation ( BluetoothGattCharacteristic characteristic ) {
		android.util.Log.e ( "Method", "onWriteLocation ()" );
		String charName = BluetoothNameResolver.resolveCharacteristicName ( characteristic.getUuid ().toString () );
		android.util.Log.e ( "Characteristic Name", charName );
		android.util.Log.d ( "--------", "--------" );
	}
}