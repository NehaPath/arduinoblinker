package com.example.arduinoblinker;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Service;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbAccessory;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import java.io.IOException;
import android.os.CountDownTimer;

@SuppressLint("NewApi")


public class Lightblinker extends Activity implements CompoundButton.OnCheckedChangeListener {

    // TAG is used to debug in Android logcat console
    private static final String TAG = "ArduinoAccessory";
    private static final String ACTION_USB_PERMISSION =
            "com.example.arduinoblinker.action.USB_PERMISSION";
    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;
    private ToggleButton buttonLED;
    private ToggleButton t; //CHECK IF THIS WORKS
    private int clear = 'X';
    private Button clearButton; //testing
    TextView text;
    boolean stopWorker; //testing
    byte[] readBuffer; //testing
    Thread workerThread; //testing


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
                            (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
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
                        (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                {
                    if (accessory != null &&
                            accessory.equals(mAccessory)) {
                        closeAccessory();
                    }
                }
            }
        }
    };

    boolean audio = false;
    boolean background = false;
    // ToggleButton t;
    RelativeLayout r;
    TextToSpeech t1;

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
        // setContentView(R.layout.activity_lightblinker); //original code
        // buttonLED = (ToggleButton) findViewById(R.id.toggleButtonLED); //original code

        // calculator code
        setContentView(R.layout.activity_main);
        t = (ToggleButton) findViewById(R.id.toggleButton2);
        r = (RelativeLayout) findViewById(R.id.layout);
        clearButton = (Button) findViewById(R.id.button_clear); //testing
        //clearButton.setOnClickListener(this); //testingRemove
        r.setBackgroundColor(Color.rgb(0, 0, 156));

        // Initialize TextToSeech Method
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });

  /*      CountDownTimer aCounter = new CountDownTimer(100000 , 1000) {
            public void onTick(long millisUntilFinished) {
                text.setText("Seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
               text.setText("Finished")
        };
        aCounter.start();
*/
      }


    public void changestate(View v) {
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    audio = true; //audio is on

                } else {
                    // The toggle is disabled
                    audio = false;
                }
            }
        });

    }

    ArrayList<String> arrayList = new ArrayList<String>();
    String string = "";
    String string1 = "";
    String string_sound = "";
    String toSpeak1 = "";

    public void button_sound(View v) {

        Button button = (Button) v;
        string_sound = (String) button.getText().toString();

        final MediaPlayer mp = new MediaPlayer();
        // Noise effect every time button is pressed
        if (mp.isPlaying()) {
            mp.stop();
        }


        try {
            mp.reset();
            AssetFileDescriptor afd;
            if (!string_sound.contains("clear")) {
                afd = getAssets().openFd("button_click.mp3");
            } else {
                afd = getAssets().openFd("trash_sound.mp3");
            }
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
    }

    public void onClick1(View v) {

        // Make button noise if clicked and true
        if (audio == true) {
            button_sound(v);
        }


        TextView textView_commands = (TextView) findViewById(R.id.textView_commands);

        Button button = (Button) v;

        string = (String) button.getText().toString();

        if (!string.contains("+") && !string.contains("-") && !string.contains("/") && !string.contains("*")) {
            string1 = string1 + string;

            if (arrayList.size() > 0) {

                arrayList.remove((arrayList.size() - 1));
            }
            arrayList.add(string1);

            numbers(v, string); // calling for arduino data later on

        } else {
// why is this here twice?
            arrayList.add(string);
            arrayList.add(string);
            string1 = "";
        }


        textView_commands.setText(textView_commands.getText().toString() + string);
//        textView_commands.setText(arrayList.toString());


        //toSpeak1 = textView_commands.getText().toString();
    }


    public void onClick(View v) {

        TextView textView_results = (TextView) findViewById(R.id.textView_results);

        String toSpeak = "";


        float calc_result = 0;
        int calc_size = arrayList.size();


        while (calc_size != 1) {


            if (calc_size > 3) {

                // If second operation is * or /, perform this first
                if (arrayList.get(3).contains("*") || arrayList.get(3).contains("/")) {

                    if (arrayList.get(3).contains("*")) {
                        calc_result = Float.parseFloat(arrayList.get(2)) * Float.parseFloat(arrayList.get(4));
                    }

                    if (arrayList.get(3).contains("/")) {
                        calc_result = Float.parseFloat(arrayList.get(2)) / Float.parseFloat(arrayList.get(4));
                    }

                    arrayList.remove(2);
                    arrayList.remove(2);
                    arrayList.remove(2);
                    arrayList.add(2, Float.toString(calc_result));
                    calc_size = arrayList.size();


                } else {

                    // Else just perform first operation
                    if (arrayList.get(1).contains("+")) {
                        calc_result = Float.parseFloat(arrayList.get(0)) + Float.parseFloat(arrayList.get(2));
                    }

                    if (arrayList.get(1).contains("-")) {
                        calc_result = Float.parseFloat(arrayList.get(0)) - Float.parseFloat(arrayList.get(2));
                    }

                    if (arrayList.get(1).contains("*")) {
                        calc_result = Float.parseFloat(arrayList.get(0)) * Float.parseFloat(arrayList.get(2));
                    }

                    if (arrayList.get(1).contains("/")) {
                        calc_result = Float.parseFloat(arrayList.get(0)) / Float.parseFloat(arrayList.get(2));
                    }

                    arrayList.remove(0);
                    arrayList.remove(0);
                    arrayList.remove(0);
                    arrayList.add(0, Float.toString(calc_result));
                    calc_size = arrayList.size();

                }

            }

            // if array size is less then 3 there is only 1 operation to perform
            else {
                if (arrayList.get(1).contains("+")) {
                    calc_result = Float.parseFloat(arrayList.get(0)) + Float.parseFloat(arrayList.get(2));
                }

                if (arrayList.get(1).contains("-")) {
                    calc_result = Float.parseFloat(arrayList.get(0)) - Float.parseFloat(arrayList.get(2));
                }

                if (arrayList.get(1).contains("*")) {
                    calc_result = Float.parseFloat(arrayList.get(0)) * Float.parseFloat(arrayList.get(2));
                }

                if (arrayList.get(1).contains("/")) {
                    calc_result = Float.parseFloat(arrayList.get(0)) / Float.parseFloat(arrayList.get(2));
                }

                arrayList.remove(0);
                arrayList.remove(0);
                arrayList.remove(0);
                arrayList.add(0, Float.toString(calc_result));
                calc_size = arrayList.size();
            }

        }

        textView_results.setText(Float.toString(calc_result));

        toSpeak = textView_results.getText().toString();


        // Speak the result if sound is on
        if (audio == true) {


            toSpeak = toSpeak1 + " equals " + toSpeak;
            toSpeak = toSpeak.replace("+", " plus ");
            toSpeak = toSpeak.replace("*", " times ");
            toSpeak = toSpeak.replace("/", " divided by ");
            toSpeak = toSpeak.replace("-", " minus ");

            Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            textView_results.setText(Float.toString(calc_result));

            checkChar(calc_result);
        }


    }




public void checkChar(float calc_result) {
        TextView textView_results = (TextView) findViewById(R.id.textView_results);
        char readChar = startListening();

        if (readChar == 'C') { // If gets X stop
            textView_results.setText(Float.toString(calc_result));
           textView_results.setText("cleared");

        } else {
            textView_results.setText("button not clicked");
        }
    }



//TextView textView_results = (TextView)findViewById(R.id.textView_results);
    // TextView textView_commands = (TextView)findViewById(R.id.textView_commands);

    public void clear(View v) {
        TextView textView_results = (TextView) findViewById(R.id.textView_results);
        TextView textView_commands = (TextView) findViewById(R.id.textView_commands);

        // Make button noise if clicked and true

        if (audio == true) {
            button_sound(v);
        }

        string1 = "";
        string = "";
        textView_results.setText("0");
        textView_commands.setText("");
        arrayList.clear();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically h
        // andle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            r.setBackgroundColor(Color.rgb(86, 160, 211));

        } else {
            r.setBackgroundColor(Color.rgb(0, 0, 156));

        }

       /* ToggleButton leftToggleButton = (ToggleButton)findViewById(R.id.toggleButton2);
        leftToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    background = true; //background is on
                } else {
                    // The toggle is disabled
                    background = false;
                }

            }
        }); */


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

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

                        mUsbManager.requestPermission(accessory, mPermissionIntent);
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

    public void blinkLED(View v) {
        byte[] buffer = new byte[1];
        if (t.isChecked())
            buffer[0] = (byte) 10; // button says on, light is off

        else
            buffer[0] = (byte) 11; // button says off, light is on
        if (mOutputStream != null) {
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
    }


    public void numbers(View v, String s) {
        byte[] numberbyte = new byte[1];
        for (int i = 0; i < 10; i++) {
            if (s.equals(Integer.toString(i))) {
                numberbyte[0] = (byte) i;
            }
        }
        //if (s.equals("0")) {numberbyte[0] = 0;}

        if (mOutputStream != null) {
            try {
                mOutputStream.write(numberbyte);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
    }

    public char startListening() {

        while (true) {
            if (mInputStream != null) {
                try {

                    if (mInputStream.read() == clear) {

                        return 'C';
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return 'A';
        }


    }


}