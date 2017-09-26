package ritwik.bluetoothle.callbacks;

import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

/**
 * Callback Class for Bluetooth LE Scan.
 */
@RequiresApi (api = Build.VERSION_CODES.LOLLIPOP)
public class ScanCallback extends android.bluetooth.le.ScanCallback {
	private ScanListener mListener;

	public ScanCallback ( ScanListener mListener ) { this.mListener = mListener; }

	/**
	 * Invokes when a nearby Bluetooth LE device is discovered.
	 */
	@Override public void onScanResult ( int callbackType, ScanResult result ) {
		super.onScanResult ( callbackType, result );
		android.util.Log.e ( "ScanCallback", "onScanResult()" );
		mListener.onScanResult ( callbackType, result );
	}

	@Override public void onBatchScanResults ( List<ScanResult> results ) {
		super.onBatchScanResults ( results );
		android.util.Log.e ( "ScanCallback", "onBatchScanResults()" );
		mListener.onBatchScanResults ( results );
	}

	/**
	 * Invokes when a Bluetooth LE scan Failed.
	 * @param errorCode Refer Android BLE.
	 */
	@Override public void onScanFailed ( int errorCode ) {
		super.onScanFailed ( errorCode );
		android.util.Log.e ( "ScanCallback", "onScanFailed()" );
		mListener.onScanFailed ( errorCode );
	}

	public interface ScanListener {
		void onScanResult ( int callbackType, ScanResult result );
		void onBatchScanResults ( List<ScanResult> results );
		void onScanFailed ( int errorCode );
	}
}