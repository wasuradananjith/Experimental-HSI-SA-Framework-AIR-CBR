# Experimental Human-Swarm Interaction-Situation Awareness Framework

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-green.svg)]()
[![Language: Java](https://img.shields.io/badge/Language-Java-orange.svg)]()

## Overview
The framework consists of two main components designed to work in tandem:
1. **The SA Monitoring Android App Code:** An application that allows an operator to guide a robotic swarm.
2. **The Robotic Simulation Environment (CoppeliaSim):** The simulated robotic swarm backend, located in the `Tasks` folder, which communicates with the Android application to execute the human-swarm collaboration scenarios.

---

## 🌿 Repository Structure & Branches
To ensure strict reproducibility for our published user studies, the code, configuration files, and step-by-step setup instructions for each specific experiment are isolated in their own branches. 

Please navigate to the relevant branch for the study you wish to review or replicate:

*   [`user-study-1-code`](https://github.com/wasuradananjith/experimental-hsi-sa-framework/tree/user-study-1-code) - Contains the code, CoppeliaSim environments, and full setup guide for **User Study 1** (Single-target search task with distributed, moving, and spreading hazard profiles).
*   *(Future studies will be added as independent branches).*

----
## Academic Citation
If you use this interface framework or code in your research, please cite our related publications:

W. D. Wattearachchi., E. Lakshika, K. Kasmarik, & M. Barlow, "Designing Effective Human-Swarm Interaction Interfaces: Insights from a User Study on Task Performance", 2025 IEEE International Conference on Systems, Man, and Cybernetics (SMC), Vienna, Austria, doi: 10.1109/SMC58881.2025.11343025.
W. D. Wattearachchi., E. Lakshika, K. Kasmarik, & M. Barlow, "Understanding Human Situation Awareness in One-to-Many Human-Robot Interaction Scenarios", Australasian Joint Conference on Artificial Intelligence (AJCAI) 2025, Canberra, Australia, doi: 10.1007/978-981-95-4972-6_37.

(Placeholder for THRI Journal Paper - Update when published)
Wattearachchi, W. D., et al. "A Study on Human-Swarm Interaction: A Framework for Assessing Situation Awareness and Task Performance."

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.