# Module 2 Final Assignment: Pi Server

###### Renske Schram

## Introduction

With this repository, a Raspberry Pi can be used as a wireless storage medium, with which reliable file transfer using the UDP protocol can be achieved. Installation is done according to the [setup guide](pi_setup/setup.md). The Wi-Fi network is used to communicate between a laptop (the Client) and the Raspberry Pi (the Server).


## Elements

The application consists of two main parts:

 - The Server: storage application on the Raspberry Pi.
 - The Client: an application which connects to the Pi, running from a laptop.


# Raspberry Pi Usage Guide

## Image

You will be provided with a Raspberry Pi 5 starter kit. [setup.md](pi_setup/setup.md) contains the description on how to setup the Pi. Before reading any further, follow the steps in that guide to set it up correctly for this assignment.

To connect to the Pi you can use any SSH client (`ssh pi@[IP address of pi]`).
The default credentials are:
 - Username: `pi`
 - Password: `raspberry`

It's recommended to shutdown the Pi before disconnecting power
Just unplugging power may cause a corrupted filesystem.
You can shutdown the Pi with the following command:

`sudo shutdown -h now`

## Networking

Besides the physical ethernet port, the Raspberry Pi is configured to start a Wi-Fi Access point on startup.
This network can be used to connect the Pi to your laptop without the need of switches/cables.

**Important:** you may need to disable the Nedap security policy of you computer's VPN client.

### Physical ethernet

The physical adapter is configured to use DHCP to obtain an IP address.

### Wi-Fi

The name of the Wi-Fi network is unique for each participant.
The raspberry Pi is configured to start a DHCP server on the Wi-Fi interface.
Your computer will automatically get an IP address from the Pi (range 172.16.1.2–172.16.1.100).

 - SSID: `nu-pi-$name`
 - Security: `WPA2`
 - Wi-Fi password: `nedap1234`
 - Static IP address: `172.16.1.1`

We recommend you to change the Wi-Fi password once you're connected to the Pi. This can be done by editing the following file:

`sudo nano /etc/hostapd/hostapd.conf`

Replace the password on the following line:

`wpa_passphrase=nedap1234`


## Java Client & Server setup

This `RenskeSchram/nu_module_II` repo on GitHub contains a setup of the Gradle project in the repo root. Using the ./gradlew deploy command on the Raspberry Pi, the Server can be started (in Main the desired port number can be adjusted, default set at 8080).

On the Client side, the Client TUI class can be run to start the Client application. Starting the Client, you will be asked for the IP adress and port number on the Pi you want to connect to. When started, the application will show the five possible commands to be executed (not capital sensitive):

        Client TUI commands:
              SEND <src_dir> <dst_dir>.... send file 
              GET  <src_dir> <dst_dir>.... get file
              LIST <src_dir> ............. get filenames stored directory 
              DISCONNECT ................. disconnect and stop 
              HELP ....................... help (this menu)

The home directory on both the Client and server side can be adjusted in the variables CLIENT_HOME and SERVER_HOME (default at respectivaly "example_files/" and "home/pi/", make sure these are set at correct and existing folders).
