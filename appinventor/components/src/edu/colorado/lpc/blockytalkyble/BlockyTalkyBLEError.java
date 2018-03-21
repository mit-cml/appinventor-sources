package edu.colorado.lpc.blockytalkyble;

/**
 * Created by Abbie on 10/14/17.
 */
public enum BlockyTalkyBLEError {

    ERROR_BLUETOOTH_LE_NOT_SUPPORTED(9001,"BluetoothLE is not supported on your phone's hardware!"),
    ERROR_BLUETOOTH_LE_NOT_ENABLED(9002,  "BluetoothLE is not enabled!"),
    ERROR_API_LEVEL_TOO_LOW(9003, "BluetoothLE requires Android 5.0 or newer!"),
    ERROR_NO_DEVICE_SCAN_IN_PROGRESS(9004, "StopScan cannot be called before StartScan! There is no scan currently in progress."),
    ERROR_NOT_CURRENTLY_CONNECTED(9005, "No connected Microbit, so cannot write."),
    ERROR_INDEX_OUT_OF_BOUNDS(9006, "Block %1s attempted to access %2s with an invalid index. Index out of bounds!"),
    ERROR_DEVICE_LIST_EMPTY(9007, "You cannot connect to a device when the device list is empty! Try scanning again."),
    ERROR_INVALID_UUID_CHARACTERS(9008, "%1s UUID string in block %2s contains invalid characters! "
            + "Try typing it in again and rebuilding your app."),
    ERROR_INVALID_UUID_FORMAT(9009, "%1s UUID string in block %2s does not use the proper format! "
            + "Try typing it in again and rebuilding your app."),
    ERROR_ADVERTISEMENTS_NOT_SUPPORTED(9010, "Bluetooth Advertisements not supported!");

    int errorCode;
    String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    BlockyTalkyBLEError(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
