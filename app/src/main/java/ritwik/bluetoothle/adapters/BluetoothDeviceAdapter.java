package ritwik.bluetoothle.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ritwik.bluetoothle.R;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.BluetoothDeviceViewHolder> {
	private List<BluetoothDevice> mDeviceList;
	private Context mContext;
	private BluetoothDeviceListener mListener;

	public BluetoothDeviceAdapter ( List<BluetoothDevice> mDeviceList, Context mContext, BluetoothDeviceListener mListener ) {
		this.mDeviceList = mDeviceList;
		this.mContext = mContext;
		this.mListener = mListener;
	}

	@Override public BluetoothDeviceViewHolder onCreateViewHolder ( ViewGroup parent, int viewType ) {
		View view = LayoutInflater.from ( mContext ).inflate ( R.layout.list_bluetooth_devices, parent, false );
		return new BluetoothDeviceViewHolder ( view );
	}

	@Override public int getItemCount () {
		return mDeviceList.size ();
	}

	@Override public void onBindViewHolder ( BluetoothDeviceViewHolder holder, int position ) {
		String deviceName = mDeviceList.get ( position ).getName ();
		if ( null == deviceName ) deviceName = "Unknown Device";

		holder.mDeviceName.setText ( deviceName );
		holder.mDeviceMACAddress.setText ( mDeviceList.get ( position ).getAddress () );
	}

	class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		CardView mDeviceCard;
		TextView mDeviceName, mDeviceMACAddress;

		BluetoothDeviceViewHolder ( View itemView ) {
			super ( itemView );
			initializeView ( itemView );
		}

		private void initializeView ( View view ) {
			mDeviceCard = (CardView) view.findViewById ( R.id.paired_device_card );
			mDeviceName = (TextView) view.findViewById ( R.id.paired_device_name );
			mDeviceMACAddress = (TextView) view.findViewById ( R.id.paired_device_mac_address );
			// Set on-Click Listener
			mDeviceCard.setOnClickListener ( BluetoothDeviceViewHolder.this );
		}

		@Override public void onClick ( View view ) {
			mListener.onBluetoothDeviceSelected ( mDeviceList.get ( getAdapterPosition () ) );
		}
	}

	public void updateDeviceList ( List<BluetoothDevice> deviceList ) {
		mDeviceList = deviceList;
		notifyDataSetChanged ();
	}

	public void clearDeviceList () {
		mDeviceList.clear ();
		notifyDataSetChanged ();
	}

	public interface BluetoothDeviceListener {
		void onBluetoothDeviceSelected ( BluetoothDevice device );
	}
}