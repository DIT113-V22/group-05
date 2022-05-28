[![Arduino CI](https://github.com/DIT113-V22/group-05/actions/workflows/arduino-build.yml/badge.svg?branch=master&event=push)](https://github.com/DIT113-V22/group-05/actions/workflows/arduino-build.yml)
[![Android CI](https://github.com/DIT113-V22/group-05/actions/workflows/android.yml/badge.svg?branch=master&event=push)](https://github.com/DIT113-V22/group-05/actions/workflows/android.yml)
***


### Contents
- [What?](#what)
- [Why?](#why)
- [How?](#how)
- [System installation](#system-installation)
- [Dependencies](#dependencies)
- [The Team](#the-team)
- [License](#license)

***

# Group 5
#### WHAT:
##### Minimum viable product:
1. When the car is moving towards a static object, it should automatically stop to avoid collision.
2. When the car is standing still and another car is heading towards it. The car that is standing still will automatically move out of the way to avoid collision.
3. Buttons for cruise control, overriding safety features, turning engine off and on. Will be implemented in the app. 
4. Automatically calling/messaging emergency services in case of a major collision.
 
##### Extra (if time allows it):
5. Following another car
6. Staying within 2 white lines (lane keeping)
7. Autonomous parking 

*** 

#### WHY:
The idea is to permit the car to recognize obstacles and other SmartCars. This would be the first step towards self-driving cars, guide and assist with parking, prevent car collisions, thus decreasing accidents. 
According to the World Health Organization, road traffic injuries caused an estimated 1.35 million deaths worldwide in 2016. That is, one person is killed every 25 seconds. 
Many people lose their lives due to driving accidents and by creating these features to be universally available to car companies make it less likely for people to die in car accidents
By implementing this in a miniature car we allow us to get a rough idea of how we can implement it in a larger version.

***

#### HOW:
Learning more about the emulator in general, and using github to keep track and document our progress. We would break into teams to divide up the workload. We would then bring our work back together to ensure the code still works together and for testing purposes.
Accessing sensors (for example: Infrared, Ultrasonic sensor, gyroscope, camera), would allow the car to scan its environment and register obstacles in its surroundings. If the sensors would pick up an object getting closer it would move out of its path. 
We will develop an app using Android Studio and connect it to the Godot Emulator to be able to control the car in the virtual environment. We would then aim to implement support to control the physical car with the developed app.

List of items we would need: 
* Godot Editor 
* SMCE Godot emulator
* Arduino IDE
* Android Studio 
* Github
* Blender

***

### System Installation

Installation guidance are provided in detail on the Wiki pages below.

- [Installing the software for users](https://github.com/DIT113-V22/group-05/wiki/Installation-and-Setup-guide).

- [Installing the software for developers](https://github.com/DIT113-V22/group-05/wiki/Tools-for-developers).

***

### Dependencies

- [Arduino IDE](https://www.arduino.cc/en/software) or - [Visual Studio Code](https://code.visualstudio.com/)
- [SMCE-gd](https://github.com/ItJustWorksTM/smce-gd)
- [Android Studio](https://developer.android.com/studio)
- [Mosquitto Broker](https://mosquitto.org/)

***

### The Team
* [Albin Karlsson](https://github.com/AlbinKarlsson)
* [Erik Lindmaa](https://github.com/Lindet94)
* [Felix Valkama](https://github.com/Valkama1)
* [Liam Axelrod](https://github.com/liamaxelrod)
* [Sepehr Moradian](https://github.com/sepehrmoradian)
* [Ossian Ålund](https://github.com/o55ian)

***

### License
MIT © Group-05

The source code for the repository is licensed under the MIT license, refer to [LICENSE](https://github.com/DIT113-V22/group-05/wiki/License) file in the repository.

***
