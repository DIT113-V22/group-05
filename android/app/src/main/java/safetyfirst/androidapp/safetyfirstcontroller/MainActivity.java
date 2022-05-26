package safetyfirst.androidapp.safetyfirstcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Timer;
import java.util.TimerTask;

import safetyfirst.androidapp.safetyfirstcontroller.Data.DataBaseHelper;
import safetyfirst.androidapp.safetyfirstcontroller.MailBot.MailSender;
import safetyfirst.androidapp.safetyfirstcontroller.fragments.ContactsFragment;
import safetyfirst.androidapp.safetyfirstcontroller.fragments.HomeFragment;
import safetyfirst.androidapp.safetyfirstcontroller.fragments.LoginFragment;
import safetyfirst.androidapp.safetyfirstcontroller.fragments.ProfileFragment;
import safetyfirst.androidapp.safetyfirstcontroller.fragments.RegisterFragment;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener, NavigationView.OnNavigationItemSelectedListener {

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

    private boolean drivebackwards;
    private boolean driveforwards;

    //Variables for the seekbar
    //Button submitButton;
    //SeekBar simpleSeekBar;
    //private static int adjust;

    // variables for contact dialogue popup
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText newContactPopupFirstname, newContactPopupLastname, newContactPopupMobile, newContactPopupEmail;
    private Button newContactPopupCancel, newContactPopupSave;
    ListView contactList;

    //Crash popup
    private Button iAmOk;
    private AlertDialog dialogCrashPopup;

    private double currentSpeed;

    private DrawerLayout drawerLayout;

    FirebaseAuth firebaseAuth;

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
        safetTtoggleButton();
    }

    public void safetTtoggleButton(){
        //This is the toggle button object to create the on and off switch for the automatic stopping features
        ToggleButton toggle = findViewById(R.id.toggleButton1);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {//Publish a message depending on which value the button has
                if (b) {
                    Toast.makeText(getApplicationContext(), "Safety system enabled", Toast.LENGTH_SHORT).show();
                    mMqttClient.publish(SAFETY_SYSTEMS, "true", QOS, null);
                } else {
                    Toast.makeText(getApplicationContext(), "Safety system disabled", Toast.LENGTH_SHORT).show();
                    mMqttClient.publish(SAFETY_SYSTEMS, "false", QOS, null);
                }
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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

            case R.id.login:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new LoginFragment()).commit();
                break;

            case R.id.registerUser:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new RegisterFragment()).commit();
                break;
            case R.id.logOut:
                FirebaseAuth.getInstance().signOut();
                hideItemDefault();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void hideItemLogged() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.profile).setVisible(true);
        nav_Menu.findItem(R.id.login).setVisible(false);
        nav_Menu.findItem(R.id.registerUser).setVisible(false);
        nav_Menu.findItem(R.id.logOut).setVisible(true);

    }

    public void hideItemDefault() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.profile).setVisible(false);
        nav_Menu.findItem(R.id.logOut).setVisible(false);
        nav_Menu.findItem(R.id.login).setVisible(true);
        nav_Menu.findItem(R.id.registerUser).setVisible(true);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            hideItemLogged();
        } else {
            hideItemDefault();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                    mMqttClient.subscribe("/smartcar/safetysystem/#", QOS, null);
                    mMqttClient.subscribe("/smartcar/speedometer", QOS, null);

                    mMqttClient.publish(SAFETY_SYSTEMS, "true", QOS, null);//Publish once connected to make sure the car and the app has the same value upon start
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
                    } else if (topic.equals("/smartcar/safetysystem")) {
                        ToggleButton toggle = findViewById(R.id.toggleButton1);
                        if (message.toString().equals("true")){ //sync the toggle button to the car.
                            toggle.setChecked(true);
                        }else if (message.toString().equals("false")){
                            toggle.setChecked(false);
                        }
                    } else if (topic.equals("/smartcar/safetysystem/driveforwards")) {
                        if (message.toString().equals("true")){
                            drivebackwards = true;
                        }else if (message.toString().equals("false")){
                            drivebackwards = false;
                        }
                    } else if (topic.equals("/smartcar/safetysystem/drivebackwards")) {
                        if (message.toString().equals("true")){
                            driveforwards = true;
                        }else if (message.toString().equals("false")){
                            driveforwards = false;
                        }
                    } else if (topic.equals("/smartcar/speedometer")) {
                        TextView speedometer = (TextView)findViewById(R.id.speedometer);

                        double speedMS = Double.parseDouble(message.toString());
                        double speedKMH = Math.round((speedMS * 3.6)*10.0)/10.0;

                        speedometer.setText(Double.toString(speedKMH));
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
        if (movingForwards) {
            mMqttClient.publish(THROTTLE_CONTROL, Integer.toString(throttleSpeed), QOS, null);
        } else {
            mMqttClient.publish(THROTTLE_CONTROL, Integer.toString(-throttleSpeed), QOS, null);
        }
        mMqttClient.publish(STEERING_CONTROL, Integer.toString(steeringAngle), QOS, null);
    }

    //Mqtt connection status icon
    public void mqttConnectionStatus(boolean isConnected) {
        if (!isConnected) {
            findViewById(R.id.imageView_no_connection).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView_connected).setVisibility(View.GONE);
        } else {
            findViewById(R.id.imageView_connected).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView_no_connection).setVisibility(View.GONE);
        }
    }

    //When the joystick has been moved the coordinates will be sent to this method and the attributes xPercent and yPercent will store them
    //I multiple yPercent by 100, as the coordinates received were from 1.0 - 0.0. Now its 100 - 0. Which makes it easier to work with.

    @Override
    public void onJoystickMoved(float xPercent, float yPercent, int id) {
        //When the joystick has been moved the coordinates will be sent to this method and the attributes xPercent and yPercent will store them
        //I multiple yPercent by 100, as the coordinates received were from 1.0 - 0.0. Now its 100 - 0. Which makes it easier to work with.
        System.out.println(driveforwards + " " + drivebackwards);
        xPercent = xPercent * 100;
        yPercent = (-yPercent) * 100;
        System.out.println(xPercent + yPercent);

        //Here it will publish the yPercent and xPercent as ThrottleSpeed and SteeringAngle to the smartCar
        //If statement to avoid sending messages if the car has detected an obstacle
        if(yPercent <= 0 && driveforwards){
            mMqttClient.publish(THROTTLE_CONTROL, Integer.toString((int) yPercent), QOS, null);
            mMqttClient.publish(STEERING_CONTROL, Integer.toString((int) xPercent), QOS, null);
        }else if(yPercent >= 0 && drivebackwards){
            mMqttClient.publish(THROTTLE_CONTROL, Integer.toString((int) yPercent), QOS, null);
            mMqttClient.publish(STEERING_CONTROL, Integer.toString((int) xPercent), QOS, null);
        }
      }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.first_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu3) {
            //call emergency services
            callEmergencyContact();

        }
        if (id == R.id.menu4) {
            //send message to emergency services
            sendMessageEmergencyContact();
        }

        if (id == R.id.menu5) {
            //send message to emergency services
            crashPopup();
        }

        return super.onOptionsItemSelected(item);
    }
    public void sendMessageEmergencyContact() {

        DataBaseHelper phone_number_data = new DataBaseHelper(this);

        try {
            String query = "SELECT CONTACT_PHONE_NUMBER FROM CONTACT_TABLE ORDER BY CONTACT_PHONE_NUMBER DESC LIMIT 1";
            SQLiteDatabase dbs = phone_number_data.getReadableDatabase();
            Cursor result = dbs.rawQuery(query, null);
            result.moveToFirst();
            int phoneNumber = result.getInt(result.getColumnIndexOrThrow("CONTACT_PHONE_NUMBER"));
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("sms:" + phoneNumber));
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "No emergency contact added", Toast.LENGTH_SHORT).show();
        }

    }

    public void callEmergencyContact() {

        DataBaseHelper phone_number_data = new DataBaseHelper(this);

        try {
            String query = "SELECT CONTACT_PHONE_NUMBER FROM CONTACT_TABLE ORDER BY CONTACT_PHONE_NUMBER DESC LIMIT 1";
            SQLiteDatabase dbs = phone_number_data.getReadableDatabase();
            Cursor result = dbs.rawQuery(query, null);
            result.moveToFirst();
            int phoneNumber = result.getInt(result.getColumnIndexOrThrow("CONTACT_PHONE_NUMBER"));
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "No emergency contact added", Toast.LENGTH_SHORT).show();
        }

    }


   //Ready to be implemented when we have crash detection

   public void crashPopup(){
       dialogBuilder = new AlertDialog.Builder(this);
       final View crashPopupView = getLayoutInflater().inflate(R.layout.crash_popup, null);
       dialogBuilder.setView(crashPopupView);
       dialogCrashPopup = dialogBuilder.create();
       dialogCrashPopup.show();

       //I am ok button
       iAmOk = (Button) crashPopupView.findViewById(R.id.button_iAmOk);

       //Countdown timer for message
       Timer timer = new Timer();
       TimerTask timerTaskObj = new TimerTask() {
           public void run() {
               //perform your action here
                   new Thread(new Runnable() {
                       @Override
                       public void run() {
                           try {
                               MailSender sender = new MailSender("safetyfirst.emergencyservices@gmail.com",
                                       "Safetyfirst123");
                               sender.sendMail("Accident detected", "Send help immediately to the drivers location.",
                                       "safetyfirst.emergencyservices@gmail.com", "gusvalkfe@student.gu.se");

                           } catch (Exception e) {
                               Log.e("SendMail", e.getMessage(), e);
                           }

                       }

                   }).start();
               }
       };
       iAmOk.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               timer.cancel();//stop the time
               dialogCrashPopup.dismiss();
           }
       });
       timer.schedule(timerTaskObj, 15000);
   }
}



