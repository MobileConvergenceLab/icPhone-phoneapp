# icPhone & Phone app

&nbsp;

## What is icPhone?
__icPhone__ is Information-Centric Networking based communication mobile-device. Its hardware is Single Board Computer called Raspberry Pi and includes 2.8 TFT LCD touchscreen. The __icPhone__ is based on CCN(Content-Centric Networking) architecture that is among ICN architecture. This device has Group chatting and PTT(Push To Talk; walkie-talkie) application which operate over the CCN architecture using open source project implementing CCN, called [CCNx](http://ccnx.org). CCNx is one of the CCN projects that are studied in Palo Alto Research Center([PARC](https://www.parc.com/)).

![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/pic_1.png)


&nbsp;&nbsp;


## Install
1. Set prerequisites
	
	`$ sudo rpi-update`

	`$ sudo apt-get update`

2. Install Java JDK & Git
	
	`$ sudo add-apt-repository ppa:webupd8team/java`

	`$ sudo apt-get install oracle-java7-jdk`
	
	`$ sudo apt-get install git-core`

3. Install default package for CCNx
	
	`$ sudo apt-get install libssl-dev expat libexpat1-dev libcrypto++9 libcrypto++-doc libssl1.0.0 libssl1.0.0-dbg ant libpcap-dev libxml2-dev python-software-properties`

4. Download the CCNx source code
	
	`$ wget www.ccnx.org/releases/ccnx-0.8.2.tar.gz`

	`$ tar -zxvf ccnx-0.8.2.tar.gz`

5. Build the CCNx source code
	
	`$ cd ccnx-0.8.2/`

	`$ sudo ./configure`
	
	`$ sudo make`
	
	`$ sudo make install`
    
6. Clone icPhone App from GitHub

	`$ git clone https://github.com/MobileConvergenceLab/icPhone-phoneapp.git`
	
7. Run CCNTalk or CCNVoice application by JAR file
	
	`$ cd icPhone-phoneapp`

	`$ java -jar CCNTalk.jar` or `$ java -jar CCNVoice.jar`

&nbsp;&nbsp;

## Run icPhone & applications

##### (1) Turn on the icPhone
<img src="https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/img_component_desktop.jpg" width="50%">

##### (2) Start CCNx Daemon

`$ ccndstart`

##### (3) Connect each devices by tunneling

`$ ccndc add ccnx:/ tcp ***.***.***.***`	

##### (4) Run applications(CCN Talk or CCN Voice) by double-clicking JAR file

`$ cd icPhone-phoneapp/jar`

`$ java -jar CCNTalk.jar` or `$ java -jar CCNVoice.jar`


##### (5) Enter a room name(chatting room)
<img src="https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/pic_1.png">

##### (6-1) In case of CCN Talk, Enter a message in text field and Press send button
<img src="https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/pic_4.jpg">

##### (6-2) In case of CCN Voice, Record your voice by pressing record button and Press send button. If you want to listen
<img src="https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/pic_2.png">


&nbsp;&nbsp;&nbsp;


## Import the project (IDE : Eclipse)
If you want to modify the code of these apps or to re-build these apps, follow thie procedure
##### (1) Download eclipse IDE from [https://www.eclipse.org](https://www.eclipse.org)
![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/1_eclipse_download.png)

##### (2) Unzip eclipse file & Execute eclipse

##### (3) Import the project(CCNTalk or CCNVoice)
![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/2_import_the_project.png)
![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/3_select_our_project.png)

##### (4) Configure build path
![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/4_configure_build_path.png)

##### (5) Add external library
![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/5_add_external_library.png)

&nbsp;&nbsp;&nbsp;&nbsp;


* * *


##Hardware main component of icPhone
<img src="https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/img_component_all.JPG">

> List of icPhone's components
> 
> (1) Raspberry Pi (Pi B / Pi B+ / Pi2)
> 
> <img src="https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/img_component_pies.JPG">
> 
> (2) 320x240 2.8" TFT LCD + Touch screen (SPI Interface or USB, etc)
> 
> <img src="https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/img_component_touchscreen.jpg">
> 
> (3) USB wireless LAN card(dongle)
> 
> <img src="https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/img_component_dongle.JPG">
> 
> (4) 3.5mm speaker or earphones for walkie-talkie app
> 
> <img src="https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/img_component_mic.jpg">
> 
> (5) USB audio adaptor and mic for recording voice in walkie-talkie app
> 
> <img src="https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/img_component_audio_adapter.JPG">
> 
> <img src="https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/img_component_mic.jpg">




&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;



