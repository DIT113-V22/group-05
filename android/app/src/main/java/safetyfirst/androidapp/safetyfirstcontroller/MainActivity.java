package safetyfirst.androidapp.safetyfirstcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.Objects;

import safetyfirst.androidapp.safetyfirstcontroller.Data.DataBaseHelper;
import safetyfirst.androidapp.safetyfirstcontroller.Model.EmergencyContact;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener{


    private static final String TAG = "SmartcarMqttController";
    private static final String EXTERNAL_MQTT_BROKER = "aerostun.dev";
    private static final String LOCALHOST = "10.0.2.2";
    private static final String MQTT_SERVER = "tcp://" + LOCALHOST + ":1883";
    private static final String THROTTLE_CONTROL = "/smartcar/control/throttle";
    private static final String STEERING_CONTROL = "/smartcar/control/steering";
    private static final String SAFETY_SYSTEMS = "/smartcar/safetysystem";
    private static int movementSpeed = 0;
    //These will be used when we will have the option to use buttons instead of a joystick. So we will keep these as comments for now 
    // private static final int IDLE_SPEED = 0;
    // private static final int STRAIGHT_ANGLE = 0;
    // private static final int STEERING_ANGLE = 50;
    private static final int QOS = 1;
    private static final int IMAGE_WIDTH = 320;
    private static final int IMAGE_HEIGHT = 240;

    private MqttClient mMqttClient;
    private boolean isConnected = false;
    private ImageView mCameraView;
    private static boolean movingForwards = true;

    //Variables for the seekbar
    //Button submitButton;
    //SeekBar simpleSeekBar;
    //private static int adjust;


    // variables for contact dialogue popup
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText newcontactpopup_firstname, newcontactpopup_lastname, newcontactpopup_mobile, newcontactpopup_email;
    private Button newcontactpopup_cancel, newcontactpopup_save;
    ListView lv_contactList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Instantiate the joystick
        JoystickView joystick = new JoystickView(this);
        setContentView(R.layout.activity_main);

        /*
        //On initiate views
        simpleSeekBar = (SeekBar)findViewById(R.id.simpleSeekBar); // initiate the Seekbar

        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    progressChangedValue = progress;
                    adjust = progressChangedValue;
                    drive(adjust, STRAIGHT_ANGLE, "Adjust speed");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MainActivity.this, "Seek bar progress is :" + progressChangedValue,
                        Toast.LENGTH_SHORT).show();
            }
        });

         */

        mMqttClient = new MqttClient(getApplicationContext(), MQTT_SERVER, TAG);
        mCameraView = findViewById(R.id.imageView);
        Objects.requireNonNull(getSupportActionBar()).setTitle("SAFETY FIRST");  // provide compatibility to all the versions
        connectToMqttBroker();

        //This is the toggle button object to create the on and off switch for the automatic stopping features
        ToggleButton toggle = findViewById(R.id.toggleButton1);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {//Publish a message depending on which value the button has
                if (b){
                    Toast.makeText(getApplicationContext(), "Safety system enabled", Toast.LENGTH_SHORT).show();
                    mMqttClient.publish(SAFETY_SYSTEMS, "true", QOS, null);
                }else{
                    Toast.makeText(getApplicationContext(), "Safety system disabled", Toast.LENGTH_SHORT).show();
                    mMqttClient.publish(SAFETY_SYSTEMS, "false", QOS, null);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.first_menu,menu);
       return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.menu1){
            //Contact creation
            createNewContactDialog();
        }
        if(id == R.id.menu2){
            //Viewing contacts
            openContactActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    // Method for opening the popup window for the new emergency contact.
    public void createNewContactDialog(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View contactPopupView = getLayoutInflater().inflate(R.layout.popup, null);
        newcontactpopup_firstname = (EditText) contactPopupView.findViewById(R.id.newcontactpopup_firstname);
        newcontactpopup_lastname = (EditText) contactPopupView.findViewById(R.id.newcontactpopup_lastname);
        newcontactpopup_mobile = (EditText) contactPopupView.findViewById(R.id.newcontactpopup_mobile);
        newcontactpopup_email = (EditText) contactPopupView.findViewById(R.id.newcontactpopup_email);

        newcontactpopup_save = (Button) contactPopupView.findViewById(R.id.saveButton);
        newcontactpopup_cancel = (Button) contactPopupView.findViewById(R.id.cancelButton);


        dialogBuilder.setView(contactPopupView);
        dialog = dialogBuilder.create();
        dialog.show();


        newcontactpopup_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EmergencyContact contactModel;

                //Throws exception if added contact information does not meet requirements.

                try {
                    contactModel = new EmergencyContact(-1, newcontactpopup_firstname.getText().toString(),newcontactpopup_lastname.getText().toString(),Integer.parseInt(newcontactpopup_mobile.getText().toString()),newcontactpopup_email.getText().toString());
                    Toast.makeText(MainActivity.this, contactModel.toString(), Toast.LENGTH_SHORT).show();

                } catch(Exception e){
                    Toast.makeText(MainActivity.this, "Error creating customer", Toast.LENGTH_SHORT).show();
                    contactModel = new EmergencyContact(-1, "error","error",0,"error");
                }

                DataBaseHelper dataBaseHelper = new DataBaseHelper(MainActivity.this);

                boolean success = dataBaseHelper.addOne(contactModel);

                Toast.makeText(MainActivity.this, "Contact Added", Toast.LENGTH_SHORT).show();
                //ShowCustomerOnListView(dataBaseHelper);

                dialog.dismiss();

            }
        });

        newcontactpopup_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //define cancel button
                dialog.dismiss();
            }
        });
    }

    public void openContactActivity(){
        Intent intent = new Intent(this, ContactList.class);
        startActivity(intent);
        lv_contactList = findViewById(R.id.lv_contactList);
    }


    @Override
    protected void onResume() {
        super.onResume();
        connectToMqttBroker();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mMqttClient.disconnect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "Disconnected from broker");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            }
        });
    }

    private void connectToMqttBroker() {
        if (!isConnected) {
            mMqttClient.connect(TAG, "", new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    isConnected = true;

                    mqttConnectionStatus(isConnected);

                    final String successfulConnection = "Connected to MQTT broker";
                    Log.i(TAG, successfulConnection);
                    Toast.makeText(getApplicationContext(), successfulConnection, Toast.LENGTH_SHORT).show();
                    mMqttClient.subscribe("/smartcar/ultrasound/front", QOS, null);
                    mMqttClient.subscribe("/smartcar/camera", QOS, null);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    final String failedConnection = "Failed to connect to MQTT broker";
                    Log.e(TAG, failedConnection);
                    Toast.makeText(getApplicationContext(), failedConnection, Toast.LENGTH_SHORT).show();
                }
            }, new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    isConnected = false;

                    mqttConnectionStatus(isConnected);

                    final String connectionLost = "Connection to MQTT broker lost";

                    Log.w(TAG, connectionLost);
                    Toast.makeText(getApplicationContext(), connectionLost, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if (topic.equals("/smartcar/camera")) {
                        final Bitmap bm = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);

                        final byte[] payload = message.getPayload();
                        final int[] colors = new int[IMAGE_WIDTH * IMAGE_HEIGHT];
                        for (int ci = 0; ci < colors.length; ++ci) {
                            final int r = payload[3 * ci] & 0xFF;
                            final int g = payload[3 * ci + 1] & 0xFF;
                            final int b = payload[3 * ci + 2] & 0xFF;
                            colors[ci] = Color.rgb(r, g, b);
                        }
                        bm.setPixels(colors, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
                        mCameraView.setImageBitmap(bm);
                    } else {
                        Log.i(TAG, "[MQTT] Topic: " + topic + " | Message: " + message.toString());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Message delivered");
                }
            });
        }
        mqttConnectionStatus(isConnected);
    }

    //This method is not being used other than for the speed adjuster, which is irrelevant
    //with the joystick being in place. However, it will be used later on for the option
    //of having buttons later on in the project in the menu bar

    void drive(int throttleSpeed, int steeringAngle, String actionDescription) {
        if (!isConnected) {

            final String notConnected = "Not connected (yet)";
            Log.e(TAG, notConnected);
            Toast.makeText(getApplicationContext(), notConnected, Toast.LENGTH_SHORT).show();

            return;
        }
        //Changing the speed using the adjust variable from the seekbar slider.
        //Adjust is the variable where the seekbar is, add or remove that from the current speed
/*

        if(actionDescription == "Moving backward"){
            movingForwards = false;
            movementSpeed = adjust;
        }else if (actionDescription == "Stopping"){
            movementSpeed = 0;
        }else if (actionDescription == "Moving forward"){
            movingForwards = true;
            movementSpeed = adjust;
        }else if (actionDescription == "Moving forward left"){
            movementSpeed = adjust;
        }else if (actionDescription == "Moving forward right"){
            movementSpeed = adjust;
        }else{
            movementSpeed = adjust;
        }

 */

        //The movementSpeed that has been adjusted will now be added to the throttlespeed
        throttleSpeed = movementSpeed;
        Log.i(TAG, actionDescription);
        if(movingForwards) {
            mMqttClient.publish(THROTTLE_CONTROL, Integer.toString(throttleSpeed), QOS, null);
        }else{
            mMqttClient.publish(THROTTLE_CONTROL, Integer.toString(-throttleSpeed), QOS, null);
        }
        mMqttClient.publish(STEERING_CONTROL, Integer.toString(steeringAngle), QOS, null);
    }




    public void mqttConnectionStatus(boolean isConnected){
        if(!isConnected){
            findViewById(R.id.imageView_red_symbol).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView_green_symbol).setVisibility(View.GONE);
        }else{
            findViewById(R.id.imageView_green_symbol).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView_red_symbol).setVisibility(View.GONE);
        }
    }

//When the joystick has been moved the coordinates will be sent to this method and the attributes xPercent and yPercent will store them
//I multiple yPercent by 100, as the coordinates received were from 1.0 - 0.0. Now its 100 - 0. Which makes it easier to work with.
    @Override
    public void onJoystickMoved(float xPercent, float yPercent, int id) {

        xPercent = xPercent * 80;
        yPercent = (-yPercent) * 100;

        //Here it will publish the yPercent and xPercent as ThrottleSpeed and SteeringAngle to the smartCar
        mMqttClient.publish(THROTTLE_CONTROL, Integer.toString((int) yPercent), QOS, null);
        mMqttClient.publish(STEERING_CONTROL, Integer.toString((int) xPercent), QOS, null);

      }
    }

