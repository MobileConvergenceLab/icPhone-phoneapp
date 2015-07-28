# icPhone & Phone app

&nbsp;

## What is icPhone?
__icPhone__ is Information-Centric Networking based communication mobile-device. Its hardware is Single Board Computer called Raspberry Pi and includes 2.8 TFT LCD touchscreen. The __icPhone__ is based on CCN(Content-Centric Networking) architecture that is among ICN architecture. This device has Group chatting and PTT(Push To Talk; walkie-talkie) application which operate over the CCN architecture using open source project implementing CCN, called [CCNx](http://ccnx.org). CCNx is one of the CCN projects that are studied in Palo Alto Research Center(PARC).

&nbsp;


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


## Run CCNx
1. Start daemon
	
	`$ ccndstart`

2. Tunneling
	
	`$ ccndc add ccnx:/ tcp ***.***.***.***`	

3. Run app
	
	`$ ccnchat ccnx:/room_name`


## Import the project (IDE : Eclipse)
(1) Download eclipse IDE from [https://www.eclipse.org](https://www.eclipse.org)
![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/1_eclipse_download.png)

(2) Unzip eclipse file & Execute eclipse

(3) Import the project(CCNTalk or CCNVoice)
![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/2_import_the_project.png)
![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/3_select_our_project.png)

(4) Configure build path
![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/4_configure_build_path.png)

(5) Add external library
![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/5_add_external_library.png)

&nbsp;&nbsp;&nbsp;&nbsp;

##Hardware Component of icPhone
![](https://github.com/MobileConvergenceLab/icPhone-phoneapp/raw/master/img/deployment_of_hardware.png)

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;



