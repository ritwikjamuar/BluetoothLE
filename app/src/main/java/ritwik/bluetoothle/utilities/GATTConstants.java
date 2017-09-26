package ritwik.bluetoothle.utilities;

public interface GATTConstants {
	// Battery Service and it's Characteristics.
	String SERVICE_BATTERY = "0000180f-0000-1000-8000-00805f9b34fb";
	String CHARACTERISTIC_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";

	// File Service and it's Characteristics.
	String SERVICE_FILE = "2b86cda0-d897-443e-b232-36b18e5f06cb";
	String CHARACTERISTIC_FILE_TRANSFER = "769549ac-c2b1-439c-8c74-8c6764330c6e";
	String CHARACTERISTIC_LOCATION = "ccead0c9-5e48-4dc4-b471-a238770a6a5b";
}