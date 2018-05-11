The EMC (Easy Minecraft Client) Framework installer 
===================

This is an installer for the [EMC framework](https://github.com/Moudoux/EMC)

Using the installer
-------------------

To install EMC simply [download the latest version of this installer](https://github.com/Moudoux/EMC-Installer/releases).
Run it with `java -jar Installer.jar`, after that open your Minecraft launcher and select `release 1.12.2-EMC`.

Making sure the installation was successful
-------------------

You can type `.version` to check what EMC version you are running, you can type `.mods` to see what client is loaded.

Installing a mod
-------------------

To install a mod made for [EMC framework](https://github.com/Moudoux/EMC) simply drag and drop the mod jar file in 
`.minecraft/libraries/EMC/`.

Building and bundling mods
-------------------

Compile the installer or grab the latest release, open it with any archive manager,
open the EMC.json file and set the name field to whatever you want, finally add all the mods you 
want to bundle in the `mods` array. Now drag the mod jar files you want to bundle into the `assets` dir inside the installer.

License
-------------------

EMC Installer is licensed under GPL-3.0
