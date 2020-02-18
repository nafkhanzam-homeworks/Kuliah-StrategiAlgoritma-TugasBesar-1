# CNA Bot
By:
- Moch. Nafkhan Alzamzami 	13518132
- Ahadi Ihsan Rasyidin		13518006
- Naufal Prima Yoriko 		13518146

---

## Environment requirements

Install the Java SE Development Kit 8 for your environment here: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

Make sure JAVA_HOME system variable is set, Windows 10 tutorial here: https://www.mkyong.com/java/how-to-set-java_home-on-windows-10/

Download and Install Maven here: https://maven.apache.org/download.cgi

---

## Building

#### Windows:
Make your modifications to the starter bot using IntelliJ. Once you are happy with your changes, package your bot by opening up the "Maven Projects" tab on the right side of the screen. From here go to the  "java-sample-bot" > "Lifecycle" group and double-click "Install"
This  will create a .jar file in the folder called "target". The file will be called "java-sample-bot-jar-with-dependencies.jar".

#### Linux:
1. Install Maven
2. Run: `mvn install`

---

## Running

To run the bot, copy the file "java-sample-bot-jar-with-dependencies.jar" to a different location. Then go to the starter-pack and edit the config.json file accordingly.
Then run the "run.bat" file on windows or the "run.sh" file for unix.