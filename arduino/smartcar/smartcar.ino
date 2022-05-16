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

//stopZoneAutoBreak
bool canDrive = true;
bool driveForwards = false;
bool drivebackwards = false;

//incomingAvoidanceThreshold
bool activeAvoidance = false;

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

// SimpleCar car(control);

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
const auto maxfrontUltDis = 100;
SR04 frontUlt(arduinoRuntime, triggerPin, echoPin, maxfrontUltDis);

const auto maxfrontUltDis1 = 100;
SR04 leftUlt(arduinoRuntime, triggerPin1, echoPin1, maxfrontUltDis1);

const auto maxfrontUltDis2 = 100;
SR04 rightUlt(arduinoRuntime, triggerPin2, echoPin2, maxfrontUltDis2);

const auto maxfrontUltDis3 = 100;
SR04 backUlt(arduinoRuntime, triggerPin3, echoPin3, maxfrontUltDis3);
//end of ultra sensor//

//Camera
std::vector<char> frameBuffer;

//Inizialize variables for sens0or data
int frontUltDis;
int leftUltDis;
int rightUltDis;
int backUltDis;

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
    // Serial.println(message);
    if (topic == "/smartcar/control/throttle") {
        car.setSpeed(message.toInt());
    } else if (topic == "/smartcar/control/steering") {
        car.setAngle(message.toInt());
    } else if (topic == "/smartcar/safetysystem") {
        if (message == "false"){  //Update the boolean depending on the message received from app
            safetyFeatures = false;
        }else{
            safetyFeatures = true;
        }
    } else {
      Serial.println(topic + " " + message);
    } });
}

void loop()
{
    
    if (mqtt.connected())
    {
        if (driveForwards)
        {
            driveForwards = false;
        }
        else if (drivebackwards)
        {
            drivebackwards = false;
        }
        else
        {
            driveForwards = false;
            drivebackwards = false;
        }
        
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
            loopControl = 0;
        }
        
        //Prince out for different ultra sensors and the control
        Serial.print("F sensor: ");
        Serial.println(frontUltDis);
        Serial.print("L sensor: ");
        Serial.println(leftUltDis);
        Serial.print("R sensor: ");
        Serial.println(rightUltDis);
        Serial.print("B sensor: ");
        Serial.println(backUltDis);
        Serial.print("loop: ");
        Serial.println(loopControl);
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
        if (safetyFeatures) // check if the safety system is enabled
                            //safetyFeatures && frontUltDis <= 150 || safetyFeatures && backIRDis <= 40
                            //Also check if sensors are in range to avoid going through all checks if they aren't
        {
            // if (!activeAvoidance)
            // {
                //  stopZoneAutoBreak(frontUltDis, backUltDis);
            // }
            // incomingAvoidanceThreshold(frontUltDis, backIRDis);
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

// void stopZoneAutoBreak(long frontUltDis, long backIRDis)
// {
//     if (frontUltDis <= 100 && frontUltDis != 0 || backIRDis <= 40 && backIRDis != 0) // stop zone
//     {
//         if (canDrive) // check whether you're in the stop zone
//         {
//             car.setSpeed(0);
//             Serial.println("Emergency stop 1");
//         }
//         canDrive = false; // so the car can move in the stop zone
//     }
//     else
//     {
//         canDrive = true; // so the car will stop again if it hits the stop zone
//     }
// }

// // Threshold stands for when the car is too close to a certain
// // obstacle and we use threshold because there are multiple thresholds
// void incomingAvoidanceThreshold(long frontUltDis, long backIRDis)
// {
//     if (frontUltDis <= 30 && frontUltDis != 0)//forward obstacle threshold 1
//     {
//         car.setSpeed(0);
//         car.setSpeed(-90);
//         Serial.println("backing up level 1");
//         activeAvoidance = true;
//     } else if (frontUltDis <= 60 && frontUltDis != 0)//forward obstacle threshold 2
//     {
//         car.setSpeed(0);
//         car.setSpeed(-60);
//         Serial.println("backing up level 2");
//         activeAvoidance = true;
//     } else if (frontUltDis <= 90 && frontUltDis != 0)//forward obstacle threshold 3
//     {
//         car.setSpeed(0);
//         car.setSpeed(-30);
//         Serial.println("backing up level 3");
//         activeAvoidance = true;
//     } else if (backIRDis <= 10 && backIRDis != 0)//backwards obstacle threshold 1
//     {
//         car.setSpeed(0);
//         car.setSpeed(40);
//         Serial.println("moving forward level 1");
//         activeAvoidance = true;
//     } else if (backIRDis <= 20 && backIRDis != 0)//backwards obstacle threshold 2
//     {
//         car.setSpeed(0);
//         car.setSpeed(30);
//         Serial.println("moving forward level 2");
//         activeAvoidance = true;
//     } else if (backIRDis <= 30 && backIRDis != 0)//backwards obstacle threshold 3
//     {
//         car.setSpeed(0);
//         car.setSpeed(20);
//         Serial.println("moving forward level 3");
//         activeAvoidance = true;
//     } else if (frontUltDis == 0 && backIRDis == 0 && activeAvoidance)
//     {
//         car.setSpeed(0);
//         activeAvoidance = false;
//     } 
// }