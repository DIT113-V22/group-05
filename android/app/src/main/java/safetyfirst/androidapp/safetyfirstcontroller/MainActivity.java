package safetyfirst.androidapp.safetyfirstcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import safetyfirst.androidapp.safetyfirstcontroller.fragments.ContactsFragment;
import safetyfirst.androidapp.safetyfirstcontroller.fragments.HomeFragment;
import safetyfirst.androidapp.safetyfirstcontroller.fragments.ProfileFragment;
import safetyfirst.androidapp.safetyfirstcontroller.fragments.RegisterFragment;
import safetyfirst.androidapp.safetyfirstcontroller.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener, NavigationView.OnNavigationItemSelectedListener{

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

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Instantiate the joystick
        JoystickView joystick = new JoystickView(this);
        setContentView(R.layout.activity_main);

        mMqttClient = new MqttClient(getApplicationContext(), MQTT_SERVER, TAG);
        mCameraView = findViewById(R.id.imageView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        connectToMqttBroker();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;

            case R.id.profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
                break;

            case R.id.contacts:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ContactsFragment()).commit();
                break;

            case R.id.settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SettingsFragment()).commit();
                break;

            case R.id.registerUser:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new RegisterFragment()).commit();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }


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
            findViewById(R.id.imageView_no_connection).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView_connected).setVisibility(View.GONE);
        }else{
            findViewById(R.id.imageView_connected).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView_no_connection).setVisibility(View.GONE);
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

