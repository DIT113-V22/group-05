#include <vector>

#include <MQTT.h>
#include <WiFi.h>
#ifdef __SMCE__
#include <OV767X.h>
#endif

#include <Smartcar.h>

MQTTClient mqtt;
WiFiClient net;


// This is for the toggle button, to activate the safety features
// This is changed to false to sync with the app better
bool safetyFeatures = false;

//used to register time
int twoSeconds = 0;//right assures every two seconds

//stopZoneAutoBreak
bool canDrive = true;
bool driveForwards = true;
bool drivebackwards = true;
int speedGate;//check if it's positive or negative speed
int theSpeed;//let's speed actually be registered

//incomingAvoidanceThreshold
bool activeAvoidance = false;

//registerCollision
bool collision = false;
int timeForCollision = 0;

int angleBeforeUpdate;//needed for figure out angle change degree
int angleAfterUpdate;

int angleChangeDegree;


//controls which sensors active during the loops for the simulator car
int loopControl = 0;


const char ssid[] = "***";
const char pass[] = "****";

ArduinoRuntime arduinoRuntime;
BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);
 
GY50 gyroscope(arduinoRuntime, 37);
const auto pulsesPerMeter = 600;
DirectionlessOdometer leftOdometer(
    arduinoRuntime, smartcarlib::pins::v2::leftOdometerPin, []() { leftOdometer.update(); },
    pulsesPerMeter);
DirectionlessOdometer rightOdometer(
    arduinoRuntime, smartcarlib::pins::v2::rightOdometerPin, []() { rightOdometer.update();
}, pulsesPerMeter);
 
SmartCar car(arduinoRuntime, control, gyroscope, leftOdometer, rightOdometer);

const auto oneSecond = 1UL;

//start of ultra sensor//
const auto triggerPin1 = 10;
const auto echoPin1 = 1;
const auto triggerPin2 = 11;
const auto echoPin2 = 2;
const auto triggerPin3 = 15;
const auto echoPin3 = 3;
#ifdef __SMCE__ // Four simulator
const auto triggerPin = 6;
const auto echoPin = 7;
const auto mqttBrokerUrl = "127.0.0.1";
#else // for car
const auto triggerPin = 33;
const auto echoPin = 32;
const auto mqttBrokerUrl = "192.168.0.40";
#endif

int speed;
const auto maxfrontUltDis = 150;
SR04 frontUlt(arduinoRuntime, triggerPin, echoPin, maxfrontUltDis);

const auto maxfrontUltDis1 = 100;
SR04 leftUlt(arduinoRuntime, triggerPin1, echoPin1, maxfrontUltDis1);

const auto maxfrontUltDis2 = 100;
SR04 rightUlt(arduinoRuntime, triggerPin2, echoPin2, maxfrontUltDis2);

const auto maxfrontUltDis3 = 150;
SR04 backUlt(arduinoRuntime, triggerPin3, echoPin3, maxfrontUltDis3);
//end of ultra sensor//

//Camera
std::vector<char> frameBuffer;

//Inizialize variables for sens0or data
int frontUltDis;
int leftUltDis;
int rightUltDis;
int backUltDis;

int gyroscopeAngle;

void setup()
{
    Serial.begin(9600);
#ifdef __SMCE__
    Camera.begin(QVGA, RGB888, 15);
    frameBuffer.resize(Camera.width() * Camera.height() * Camera.bytesPerPixel());
#endif

    WiFi.begin(ssid, pass);
    mqtt.begin(mqttBrokerUrl, 1883, net);

    Serial.println("Connecting to WiFi...");
    auto wifiStatus = WiFi.status();
    while (wifiStatus != WL_CONNECTED && wifiStatus != WL_NO_SHIELD)
    {
        Serial.println(wifiStatus);
        Serial.print(".");
        delay(1000);
        wifiStatus = WiFi.status();
    }

    Serial.println("Connecting to MQTT broker");
    while (!mqtt.connect("arduino", "public", "public"))
    {
        Serial.print(".");
        delay(1000);
    }

    mqtt.subscribe("/smartcar/connectionLost", 1);
    mqtt.subscribe("/smartcar/control/#", 1);
    mqtt.subscribe("/smartcar/safetysystem", 1);
    mqtt.onMessage([](String topic, String message)
    {
     //this is going to be common out because it's going to appear on the android side but the code remains in case someone needed for reference
//         //if statements controls the intake of information from the joystick forward and backwards
//         speedGate = message.toInt();
//         if (driveForwards && speedGate >= 0){
//             theSpeed = speedGate;
//         } 
//         if (drivebackwards && speedGate <= 0){
//             theSpeed = speedGate;
//         }
//             //Serial.println(theSpeed);//shows the speed
        
        if (topic == "/smartcar/control/throttle") {
            car.setSpeed(message.toInt());
        } else if (topic == "/smartcar/control/steering") {
            car.setAngle(message.toInt());//this code needs to be switched out if the above is to be uncommented <<car.setAngle(theSpeed);>>
        } else if (topic == "/smartcar/safetysystem") {
            if (message == "false"){  //Update the boolean depending on the message received from app
                safetyFeatures = false;
            }else{
                safetyFeatures = true;
            }
        } else {
        Serial.println(topic + " " + message);
        } 
    });
}

void loop()
{
    
    if (mqtt.connected())
    {
        
        //////////////////////////////  start of read sensory input //////////////////////////////
        loopControl = loopControl + 1;
        if (loopControl == 1)
        {
            frontUltDis = frontUlt.getDistance();
        }
        else if (loopControl == 2)
        {
            leftUltDis = leftUlt.getDistance();
        }
        else if (loopControl == 3)
        {
            rightUltDis = rightUlt.getDistance();
        }
        else if (loopControl == 4)
        {
            backUltDis = backUlt.getDistance();
        }
        else if (loopControl == 5)
        {
            angleBeforeUpdate = gyroscopeAngle;
            gyroscope.update();
        }
        else if (loopControl == 6)
        {
            gyroscopeAngle = gyroscope.getHeading();
            angleAfterUpdate = gyroscopeAngle;
            
        }
        else if (loopControl == 7)
        {
        
            if (gyroscopeAngle < 330 && gyroscopeAngle > 30){
                angleChangeDegree = angleAfterUpdate - angleBeforeUpdate;
            } 

            loopControl = 0;
        }
        
        twoSeconds = twoSeconds + 1;   
        if (twoSeconds >= 75)//this can use better calculation but this is about two seconds
        {
            twoSeconds = 0;
        }
        
        //Prince out for different ultra sensors and the control
        //this is for testing removed for final product or, comment out
        
        if (true /* true / false */){
            // Serial.print("F sen: ");
            // Serial.println(frontUltDis);
            // Serial.print("L sen: ");
            // Serial.println(leftUltDis);
            // Serial.print("R sen: ");
            // Serial.println(rightUltDis);
            // Serial.print("B sen: ");
            // Serial.println(backUltDis);


            // Serial.print("gyroscopeAngle: ");
            // Serial.println(gyroscopeAngle);

            Serial.print("angleCha: ");
            Serial.println(angleChangeDegree);

            Serial.print("angleCha: ");
            Serial.println(angleChangeDegree);
            

            
            if (collision){
                Serial.println("not safe");
                Serial.println("not safe"); 
                Serial.println("not safe");
                Serial.println("not safe");
                Serial.println("not safe"); 
                Serial.println("not safe");
                Serial.println("not safe");
                Serial.println("not safe"); 
                Serial.println("not safe"); 
            } else {
                // Serial.print("safe");
                // Serial.print("safe");
                // Serial.print("safe");
            }

            // Serial.print("speed: ");
            // Serial.println(car.getSpeed());
            

            // Serial.print("loop: ");
            // Serial.println(loopControl);
        }
        //Serial.println(twoSeconds); //shows the integers for the two second interval
        
        
        
        
        


        
        //////////////////////////////  and of read sensory input //////////////////////////////

        mqtt.loop();

        const auto currentTime = millis();
#ifdef __SMCE__
        static auto previousFrame = 0UL;
        if (currentTime - previousFrame >= 40) // 40 basically being a latency here, 
                                               //larger number meaning fewer camera frames published per second
        {
            previousFrame = currentTime;
            Camera.readFrame(frameBuffer.data());
            mqtt.publish("/smartcar/camera", frameBuffer.data(), frameBuffer.size(),
                         false, 0);
        }
#endif
        if (safetyFeatures){// check if the safety system is enabled
                            //safetyFeatures && frontUltDis <= 150 || safetyFeatures && backUltDis <= 100
                            //Also check if sensors are in range to avoid going through all checks if they aren't
            registerCollision(frontUltDis, leftUltDis, rightUltDis, backUltDis, angleChangeDegree, twoSeconds);
            // if (!activeAvoidance){
            //     stopZoneAutoBreak(frontUltDis, backUltDis);  
            // }
            //incomingAvoidanceThreshold(frontUltDis, backUltDis);
        } else {
                driveForwards = true;
                drivebackwards = true;
        }

#ifdef __SMCE__
        // Avoid over-using the CPU if we are running in the emulator
        delay(1);
#endif
    }
    else
    {
        lastWill();
        // Avoid over-using the CPU if we are running in the emulator
        delay(1);
    }
}

// This method will be called when the connection breaks from the broker
void lastWill()
{
    if (speed > 10)
    { // Car slows down if speed is greater than 10
        smoothStop();
    }
    else
    {
        car.setSpeed(0); // Car just stops if speed is lower than 10
    }
}

// A method for slowing down, can be used in other methods
void smoothStop()
{
    if (speed > 3)
    {
        car.setSpeed(speed * 0.9); // 0.9 is the fraction it will multiple the speed with, hence slowing down
        delay(100);
        speed = speed * 0.9;
    }
    else
    {
        car.setSpeed(0); // then it will come to a complete stop
    }
    car.setSpeed(0);
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                            //all safetyFeatures methods//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// void stopZoneAutoBreak(long frontUltDis, long backUltDis)
// {
//     if (frontUltDis <= 150 && frontUltDis != 0){// stop zone
//         if (canDrive) {// check whether you're in the stop zone
//             car.setSpeed(0);
//             Serial.println("forward stop");
//         }
//         canDrive = false; // so the car can move in the stop zone
//         driveForwards = false;    
//     } else if (backUltDis <= 150 && backUltDis != 0){
//         if (canDrive) {// check whether you're in the stop zone
//             car.setSpeed(0);
//             Serial.println("backward stop");
//         }
//         canDrive = false; // so the car can move in the stop zone
//         drivebackwards = false; 
//     } else {
//         canDrive = true; // so the car will stop again if it hits the stop zone
//         driveForwards = true;
//         drivebackwards = true;
//     }
// }

// //Threshold stands for when the car is too close to a certain obstacle
// //and we use threshold because there are multiple thresholds for when the car is getting too close to an obstacle in has to move backwards or forwards to avoid it
// void incomingAvoidanceThreshold(long frontUltDis, long backUltDis)
// {
//     if (frontUltDis <= 30 && frontUltDis != 0)//forward obstacle threshold 1
//     {
//         car.setSpeed(-90);
//         //Serial.println("backing up level 1");
//         activeAvoidance = true;
//         driveForwards = false;
//         drivebackwards = false;
//     } else if (frontUltDis <= 60 && frontUltDis != 0)//forward obstacle threshold 2
//     {
//         car.setSpeed(-60);
//         //Serial.println("backing up level 2");
//         activeAvoidance = true;
//         driveForwards = false;
//         drivebackwards = false;
//     } else if (frontUltDis <= 90 && frontUltDis != 0)//forward obstacle threshold 3
//     {
//         car.setSpeed(-30);
//         //Serial.println("backing up level 3");
//         activeAvoidance = true;
//         driveForwards = false;
//         drivebackwards = false;
//     } else if (backUltDis <= 30 && backUltDis != 0)//backwards obstacle threshold 1
//     {
//         car.setSpeed(90);
//         //Serial.println("moving forward level 1");
//         activeAvoidance = true;
//         driveForwards = false;
//         drivebackwards = false;
//     } else if (backUltDis <= 60 && backUltDis != 0)//backwards obstacle threshold 2
//     {
//         car.setSpeed(60);
//         //Serial.println("moving forward level 2");
//         activeAvoidance = true;
//         driveForwards = false;
//         drivebackwards = false;
//     } else if (backUltDis <= 90 && backUltDis != 0)//backwards obstacle threshold 3
//     {
//         car.setSpeed(30);
//         //Serial.println("moving forward level 3");
//         activeAvoidance = true;
//         driveForwards = false;
//         drivebackwards = false;
//     } else if (frontUltDis == 0 && backUltDis == 0 && activeAvoidance)
//     {
//         car.setSpeed(0);
//         activeAvoidance = false;
//         driveForwards = true;
//         drivebackwards = true;
//     } 
// }

void registerCollision(long frontUltDis, long leftUltDis, long rightUltDis, long backUltDis, long angleChangeDegree, long twoSeconds){
    bool timecheck1 = false;
    bool timecheck2 = false;
    //0.000001 is necessary because when the car stops it will make a second increase above 20
    if (angleChangeDegree >= 25 && car.getSpeed() > 0.000001 || angleChangeDegree <= -25 && car.getSpeed() > 0.000001){
        collision = true;
    } else {
        collision = false;
    }
    
    //tested and working
    //around 20 is the closest you can get the tree in boxes so I went with 23
    // if (frontUltDis <= 23 && frontUltDis != 0 || leftUltDis <= 23 && leftUltDis != 0 || rightUltDis <= 23 && rightUltDis != 0 || backUltDis <= 23 && backUltDis != 0){
    //     collision = true;
    // } else {
    //     collision = false;
    // }

    // if (frontUltDis <= 10 && frontUltDis != 0 || leftUltDis <= 10 && leftUltDis != 0 || rightUltDis <= 10 && rightUltDis != 0 || backUltDis <= 10 && backUltDis != 0){
    //     if (timecheck1 && timecheck2){
    //         collision = true;
    //     } else if (twoSeconds = 1){
    //         timecheck2 = true;
    //     } else if (twoSeconds = 75){
    //         timecheck1 = true;
    //     }
    // } 
    // else {
    //     timecheck1 = false;
    //     timecheck2 = false;
    //     collision = false;
    // }
    
}