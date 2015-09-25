package com.example.arduinoblinker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbAccessory;
@SuppressLint("NewApi")

public class Lightblinker extends Activity {
        // TAG is used to debug in Android logcat console
        private static final String TAG = "ArduinoAccessory";
        private static final String ACTION_USB_PERMISSION =
                "com.example.arduinoblinker.action.USB_PERMISSION";
        private UsbManager mUsbManager;
        private PendingIntent mPermissionIntent;
        private boolean mPermissionRequestPending;
        private ToggleButton buttonLED;
        UsbAccessory mAccessory;
        ParcelFileDescriptor mFileDescriptor;
        FileInputStream mInputStream;
        FileOutputStream mOutputStream;
        private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        UsbAccessory accessory =
                                (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                        if
                                (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            openAccessory(accessory);
                        } else {
                            Log.d(TAG, "permission denied for accessory "
                                            + accessory);

                        }
                        mPermissionRequestPending = false;
                    }
                } else if
                        (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                    UsbAccessory accessory =
                            (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY); {
                        if (accessory != null &&
                                accessory.equals(mAccessory)) {
                            closeAccessory();
                        }
                    }
                }
            }
        };
        @SuppressWarnings("deprecation")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new
                    Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
            registerReceiver(mUsbReceiver, filter);
            if (getLastNonConfigurationInstance() != null) {
                mAccessory = (UsbAccessory)
                        getLastNonConfigurationInstance();
                openAccessory(mAccessory);
            }
            setContentView(R.layout.activity_lightblinker);
            buttonLED = (ToggleButton) findViewById(R.id.toggleButtonLED);

        }
        @SuppressWarnings("deprecation")
        @Override
        public Object onRetainNonConfigurationInstance() {
            if (mAccessory != null) {
                return mAccessory;
            } else {
                return super.onRetainNonConfigurationInstance();
            }
        }
        @Override
        public void onResume() {
            super.onResume();
            if (mInputStream != null && mOutputStream != null) {
                return;
            }
            UsbAccessory[] accessories = mUsbManager.getAccessoryList();
            UsbAccessory accessory = (accessories == null ? null :
                    accessories[0]);
            if (accessory != null) {
                if (mUsbManager.hasPermission(accessory)) {
                    openAccessory(accessory);
                } else {
                    synchronized (mUsbReceiver) {
                        if (!mPermissionRequestPending) {

                            mUsbManager.requestPermission(accessory,mPermissionIntent);
                            mPermissionRequestPending = true;
                        }
                    }
                }
            } else {
                Log.d(TAG, "mAccessory is null");
            }
        }
        @Override
        public void onPause() {
            super.onPause();
            closeAccessory();
        }
        @Override
        public void onDestroy() {
            unregisterReceiver(mUsbReceiver);
            super.onDestroy();
        }
        private void openAccessory(UsbAccessory mAccessory2) {
            mFileDescriptor = mUsbManager.openAccessory(mAccessory2);
            if (mFileDescriptor != null) {
                mAccessory = mAccessory2;
                FileDescriptor fd = mFileDescriptor.getFileDescriptor();
                mInputStream = new FileInputStream(fd);
                mOutputStream = new FileOutputStream(fd);
                Log.d(TAG, "accessory opened");
            } else {
                Log.d(TAG, "accessory open fail");
            }
        }
        private void closeAccessory() {
            try {
                if (mFileDescriptor != null) {
                    mFileDescriptor.close();
                }
            } catch (IOException e) {
            } finally {
                mFileDescriptor = null;
                mAccessory = null;
            }
        }
        public void blinkLED(View v){
            byte[] buffer = new byte[1];
            if(buttonLED.isChecked())
                buffer[0]=(byte)0; // button says on, light is off
            else
                buffer[0]=(byte)1; // button says off, light is on
            if (mOutputStream != null) {
                try {
                    mOutputStream.write(buffer);
                } catch (IOException e) {
                    Log.e(TAG, "write failed", e);
                }
            }
        }
    }