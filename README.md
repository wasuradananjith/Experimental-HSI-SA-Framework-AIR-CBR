# Experimental Human-Swarm Interaction-Situation Awareness Framework - User Study 1

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-green.svg)]()
[![Language: Java](https://img.shields.io/badge/Language-Java-orange.svg)]()

## Overview
The repository consists of two main components:
1. **The SA Monitoring Android App Code:** An application that allows an operator to guide a robotic swarm.
2. **The Robotic Simulation Environment (CoppeliaSim):** The simulated robotic swarm backend, located in the `Tasks` folder, which communicates with the Android application to execute the human-swarm collaboration scenarios.

---

## 🌿 Branch Information: `user-study-1-code`
You are currently viewing the code for **User Study 1**. 
This branch contains the exact experimental setup used to conduct the first user study we conducted in the set of user studies we conducted. The scenario involves a single-target search task complicated by three dynamic hazard profiles:
1. **Distributed Hazards**
2. **Moving Hazards**
3. **Spreading Hazards** 

---

## Repository Structure & Architecture
This repository is organized to separate the Android HSI interface from the robotic simulation backend, while providing all necessary configuration files to replicate the study.

* `app/`: Contains the Android Studio project (Java). It utilizes **Chaquopy** to embed Python scripts natively, enabling direct communication with the CoppeliaSim backend via the ZeroMQ (ZMQ) Remote API.
* `Tasks/`: Contains the CoppeliaSim environment scenes and simulation backend.
   * `Tasks/.../zmqRemoteApi/`: The local ZeroMQ Remote API dependencies required by CoppeliaSim to receive and process the commands sent from the tablet.
* `Emulator Skin/`: Contains the exact Samsung Galaxy Tab A9+ hardware profile used in our study, allowing researchers to spin up an identical Android Virtual Device (AVD).
* `platform-tools/`: Bundled ADB tools provided for convenience to easily establish the USB port-forwarding connection.

---

## Hardware & Software Specifications
To ensure strict reproducibility, this experiment was developed and validated using the following environment:
* **Robotic Simulator:** CoppeliaSim Edu 4.4.0
* **IDE:** Android Studio Electric Eel | 2022.1.1 Patch 2
* **Testing Hardware:** Samsung Galaxy Tab A9+ 11" Wi-Fi 64GB (Graphite). 
  * *Note: If you do not have this physical hardware, we have provided the exact device profile in the `Emulator Skin/` folder so you can replicate the display dimensions perfectly in the Android Studio Emulator.*
* **Connection Protocol:** USB Debugging via Android Debug Bridge (ADB) to utilize the ZeroMQ (ZMQ) Remote API.

---

## Step-by-Step Setup Guide

### Step 1: Robot Environment Simulation Startup
The simulated robot environment is handled by CoppeliaSim.
1. Navigate to the `Tasks` folder in this repository.
2. Inside, you will find two folders: `Attempt 1` and `Attempt 2`. 
3. Open either folder and launch the specific hazard/danger type file you wish to test using **CoppeliaSim Edu 4.4.0**. Do not start the simulation yet. The correctly loaded simulation in CoppeliaSim should be as below in the screenshot.

<img src="./Screenshots/1 CoppeliaSim.png" alt="CoppeliaSim Robots Environment Screenshot">

### Step 2: Android App Startup
The Android application is installed directly to the testing tablet via USB debugging.
1. Enable Developer Options and **USB Debugging** on your Samsung tablet. *(If you are unsure how to do this, follow the [Official Android USB Debugging Guide](https://developer.android.com/studio/debug/dev-options)).*
2. Connect the tablet to your computer using a USB-C to USB-C cable.
3. Open this project in **Android Studio Electric Eel**.
4. Deploy the app to the tablet by selecting your device and clicking "Run" (Shift + F10). *(See the [Android Studio Deployment Guide](https://developer.android.com/studio/run) for detailed instructions).* The device should show the below screen after when the App is sucessfully deployed and started.
<img src="./Screenshots/2 Android App Startup Screen.jpg" alt="Android App Startup Screen">

### Step 3: Connect the App to the Robot Environment
We use a direct USB-tethered connection (rather than Wi-Fi) to ensure zero latency between the tablet and the CoppeliaSim backend. 
1. Ensure the tablet remains connected to the PC via the USB-C cable with USB Debugging turned on.
2. Open a terminal or command prompt and navigate to your Android SDK `platform-tools` folder (or the `platform-tools` folder in this repository).
3. Execute the following ADB reverse port-forwarding command:
   ```bash
   adb reverse tcp:23000 tcp:23000
   ```
Note: Port 23000 is the default port for the ZeroMQ (ZMQ) Remote API in modern versions of CoppeliaSim, which establishes the communication bridge between the simulation and the Android App. If it is correctly setup, the port 23000 should be printed as shown in the below screenshot.

<img src="./Screenshots/3 abd reverese.jpg" alt="ABD Reverse Command Output">

### Step 4: Start the Task
1. Open the newly installed app on the Android tablet.
2. On the initial launch screen (shown in Step 2), click "Display Swarm". This will open the main task activity interface as shown in the below screenshot.
<img src="./Screenshots/4 Swarm Activity Screen.jpg" alt="Swarm Activity Screen">
3. Tap "Start" on the tablet.
The Android app will automatically send the command over the ZMQ API to start the CoppeliaSim simulation on your PC. During the task, the tablet interface will automatically pause the swarm and prompt the operator with Situation Awareness (SA) questions as described in our methodology.

---
## Academic Citation
If you use this interface framework or code in your research, please cite our related publications:

W. D. Wattearachchi., E. Lakshika, K. Kasmarik, & M. Barlow, "Designing Effective Human-Swarm Interaction Interfaces: Insights from a User Study on Task Performance", 2025 IEEE International Conference on Systems, Man, and Cybernetics (SMC), Vienna, Austria, doi: 10.1109/SMC58881.2025.11343025.

W. D. Wattearachchi., E. Lakshika, K. Kasmarik, & M. Barlow, "Understanding Human Situation Awareness in One-to-Many Human-Robot Interaction Scenarios", Australasian Joint Conference on Artificial Intelligence (AJCAI) 2025, Canberra, Australia, doi: 10.1007/978-981-95-4972-6_37.

(Placeholder for THRI Journal Paper - Update when published)
Wattearachchi, W. D., et al. "A Study on Human-Swarm Interaction: A Framework for Assessing Situation Awareness and Task Performance."

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
