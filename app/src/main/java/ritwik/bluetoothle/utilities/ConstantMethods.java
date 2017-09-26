package ritwik.bluetoothle.utilities;

import android.content.Context;
import android.widget.Toast;

public class ConstantMethods {
	/**
	 * Shows the Toast Message.
	 * @param context Context of the invocation.
	 * @param message String as a message to be displayed in Toast.
	 */
	public static void showToastMessage ( Context context, String message ) {
		Toast.makeText ( context, message, Toast.LENGTH_SHORT ).show ();
	}

	public static void showToastMessage ( Context context, int resourceID ) {
		Toast.makeText ( context, resourceID, Toast.LENGTH_SHORT ).show ();
	}
}