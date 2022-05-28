![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
![C++](https://img.shields.io/badge/c++-%2300599C.svg?style=for-the-badge&logo=c%2B%2B&logoColor=white)
![Arduino](https://img.shields.io/badge/-Arduino-00979D?style=for-the-badge&logo=Arduino&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Godot Engine](https://img.shields.io/badge/GODOT-%23FFFFFF.svg?style=for-the-badge&logo=godot-engine)
![Blender](https://img.shields.io/badge/blender-%23F5792A.svg?style=for-the-badge&logo=blender&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=for-the-badge&logo=Firebase&logoColor=white)

[![Arduino CI](https://github.com/DIT113-V22/group-05/actions/workflows/arduino-build.yml/badge.svg?branch=master&event=push)](https://github.com/DIT113-V22/group-05/actions/workflows/arduino-build.yml)
[![Android CI](https://github.com/DIT113-V22/group-05/actions/workflows/android.yml/badge.svg?branch=master&event=push)](https://github.com/DIT113-V22/group-05/actions/workflows/android.yml)

***

# Group 5 - Safety First 🚘

## Table of Contents
* [What is our goal?](#what-is-our-goal)
* [Why are we embarking on this project?](#why-are-we-embarking-on-this-project)
* [How are we going to achieve our goal?](#how-are-we-going-to-achieve-our-goal)
* [Technologies](#technologies)
* [Software Architecture](#software-architecture)
* [Installation & Setup](#installation--setup)
* [Dependencies](#dependencies)
* [The Team](#the-team)
* [License](#license)

***

### What is our goal?
The penultimate goal for us is to have safety features that aid a driver of a motor vehicle, whether
it be in motion or otherwise. This in turn provides a certain peace of mind and an extra sense of 
safety and comfort whilst on the road. Now, this is just the beginning in a series of advancements 
towards an ultimate goal of truly self-driving and self-aware vehicles.
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

### Why are we embarking on this project?
According to the World Health Organization, road traffic injuries caused an estimated 1.35 million 
deaths worldwide in 2016. That is, one person is killed every 25 seconds. Many people are either 
injured or unfortunately lose their lives due to driving accidents, by creating these features to be
universally available to all car companies, we can help reduce the number of fatal collisions worldwide.

According to a [study](https://www.iihs.org/media/259e5bbd-f859-42a7-bd54-3888f7a2d3ef/e9boUQ/Topics/ADVANCED%20DRIVER%20ASSISTANCE/IIHS-real-world-CA-benefits.pdf) 
by the Insurance Institute for Highway Safety (IIHS), collision avoidance technologies reduced car accidents in various ways, such as:

- Forward collision warning technologies reduced front-to-rear crashes by 27%, and front-to-rear crashes with injuries by 20%.
- Forward collision warning technologies with autobrake reduced front-to-rear crashes by 50% and front-to-rear crashes with injuries by 56%.
- Rear automatic braking reduced backing-up collisions by 78%.

Our overarching project starts of in a risk-free and to the best of our abilities, a controlled environment. 
This can help setup the foundation for working with vehicles in the real world which have a different set of challenges to tackle.

***

### How are we going to achieve our goal?
In brief, first and foremost we will be implementing our planned safety features in three stages. End users are provided an app to control the car in it's virtual environment. it includes a live camera feed and other extended functionality provided by the app such as emergency contacts and all relating functions. Finally, create a modded environment in the emulator for showcasing the project. Any extra planned features will solely depend upon time and resource availability -> Milestones in [our wiki.](https://github.com/DIT113-V22/group-05/wiki/Milestones)

***

### Technologies
* Godot Editor
* Blender
* SMCE Godot emulator
* Arduino (C++)
* Android Studio (Java)
* Git (Version control)
* MQTT
* Firebase

***

### Software Architecture

- [Wikipage describing the general software architecture of the project](https://github.com/DIT113-V22/group-05/wiki/Software-Architecture)

***

### Installation & Setup

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
