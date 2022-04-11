#include <Smartcar.h>

//Engine constants
const int fSpeed   = 30;  // 30% of the full speed forward
const int bSpeed   = -30; // 30% of the full speed backward
const int lDegrees = -75; // degrees to turn left
const int rDegrees = 75;  // degrees to turn right

//ultrasonic sensor constants
const int TRIGGER_PIN           = 6; // D6
const int ECHO_PIN              = 7; // D7
const unsigned int MAX_DISTANCE = 100;

//Car setup
ArduinoRuntime arduinoRuntime;
BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);

//ultrasonic sensor setup
SR04 front(arduinoRuntime, TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);

SimpleCar car(control);


void handleInput() { // handle serial input if there is any
    if (Serial.available()) {
        char input = Serial.read(); // read everything that has been received so far and log down
                                    // the last entry
        switch (input) {
        case 'a': // rotate counter-clockwise going forward
            car.setSpeed(fSpeed);
            car.setAngle(lDegrees);
            break;
        case 's': // turn clock-wise
            car.setSpeed(fSpeed);
            car.setAngle(rDegrees);
            break;
        case 'w': // go ahead
            car.setSpeed(fSpeed);
            car.setAngle(0);
            break;
        case 'd': // go back
            car.setSpeed(bSpeed);
            car.setAngle(0);
            break;
        default: // if you receive something that you don't know, just stop
            car.setSpeed(0);
            car.setAngle(0);
        }
    }
}

void setup() {
    Serial.begin(9600);
}

void loop() {
    handleInput();

    auto temp = front.getDistance(); //Temp variable to compare in order to decide whether to stop or not

    if (temp < 50 && temp != 0) 
    //car will not move unless we have the second statement 
    //because sensor sends 0 when nothing is in reach
    {
        car.setSpeed(0);
    }
    Serial.println(front.getDistance());
    delay(100); 
}