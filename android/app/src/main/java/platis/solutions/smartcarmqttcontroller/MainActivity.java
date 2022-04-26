package platis.solutions.smartcarmqttcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "SmartcarMqttController";
    private static final String EXTERNAL_MQTT_BROKER = "aerostun.dev";
    private static final String LOCALHOST = "10.0.2.2";
    private static final String MQTT_SERVER = "tcp://" + LOCALHOST + ":1883";
    private static final String THROTTLE_CONTROL = "/smartcar/control/throttle";
    private static final String STEERING_CONTROL = "/smartcar/control/steering";
    private static int MOVEMENT_SPEED = 40;
    private static final int IDLE_SPEED = 0;
    private static final int STRAIGHT_ANGLE = 0;
    private static final int STEERING_ANGLE = 50;
    private static final int QOS = 1;
    private static final int IMAGE_WIDTH = 320;
    private static final int IMAGE_HEIGHT = 240;

    private MqttClient mMqttClient;
    private boolean isConnected = false;
    private ImageView mCameraView;

    //Variables for the seekbar
    Button submitButton;
    SeekBar simpleSeekBar;
    private static int adjust;
    int throttleSpeed = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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


        mMqttClient = new MqttClient(getApplicationContext(), MQTT_SERVER, TAG);
        mCameraView = findViewById(R.id.imageView);

        connectToMqttBroker();

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
                            final byte r = payload[3 * ci];
                            final byte g = payload[3 * ci + 1];
                            final byte b = payload[3 * ci + 2];
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

    void drive(int throttleSpeed, int steeringAngle, String actionDescription) {
        if (!isConnected) {

            final String notConnected = "Not connected (yet)";
            Log.e(TAG, notConnected);
            Toast.makeText(getApplicationContext(), notConnected, Toast.LENGTH_SHORT).show();

            return;
        }
        if(MOVEMENT_SPEED < 0) {
            MOVEMENT_SPEED = 0 - adjust;
        }else{
            MOVEMENT_SPEED = adjust;
        }
        throttleSpeed = MOVEMENT_SPEED;
        Log.i(TAG, actionDescription);
        mMqttClient.publish(THROTTLE_CONTROL, Integer.toString(throttleSpeed), QOS, null);
        mMqttClient.publish(STEERING_CONTROL, Integer.toString(steeringAngle), QOS, null);
        MOVEMENT_SPEED = 0;
    }

    public void moveForward(View view) {
        drive(MOVEMENT_SPEED, STRAIGHT_ANGLE, "Moving forward");
    }
    public void adjustSpeed(View view){
        drive(MOVEMENT_SPEED-adjust, STRAIGHT_ANGLE, "Adjust speed");
    }

    public void moveForwardLeft(View view) {
        drive(MOVEMENT_SPEED, -STEERING_ANGLE, "Moving forward left");
    }

    public void stop(View view) {
        drive(IDLE_SPEED, STRAIGHT_ANGLE, "Stopping");
    }

    public void moveForwardRight(View view) {
        drive(MOVEMENT_SPEED, STEERING_ANGLE, "Moving forward left");
    }

    public void moveBackward(View view) {
        drive(MOVEMENT_SPEED = -1, STRAIGHT_ANGLE, "Moving backward");
    }


    //This listener will activate if there are any changed made in the SeekBar
    public void onProgressChanged (SeekBar seekBar, int progresValue, boolean fromUser) {

    }
    //Whenever a user touches or drags the seekbar then this method will will automatically be called
    public void onStartTrackingTouch(SeekBar seekBar) {

    }
    //Whenever a user stops touching the seekbar then this method will be called
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    //Testing how the seekBar works
    /*
    // GEt the maximum value of the seekbar
    void getMax(){
        //Gauge Seekbar variables
        SeekBar simpleSeekBar = (SeekBar) findViewById(R.id.simpleSeekBar); // initiate the Seek bar

        int maxValue=simpleSeekBar.getMax(); // get maximum value of the Seek bar
    }

    void getProgres(){
        SeekBar simpleSeekBar=(SeekBar)findViewById(R.id.simpleSeekBar); // initiate the Seek bar

        int seekBarValue= simpleSeekBar.getProgress(); // get progress value from the Seek bar
    }

     */

    public void mqttConnectionStatus(boolean isConnected){

        if(!isConnected){
            findViewById(R.id.imageView_no_connection).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView_connected).setVisibility(View.GONE);
        }else{
            findViewById(R.id.imageView_connected).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView_no_connection).setVisibility(View.GONE);
        }


    }
}