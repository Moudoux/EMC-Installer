The EMC (Easy Minecraft Client) Framework installer
===================

This is a installer for the [EMC framework](https://github.com/Moudoux/EMC)

Using the installer
-------------------

To install EMC simply [download the latest version of this installer](https://github.com/Moudoux/EMC-Installer/releases).
Run it with `java -jar Installer.jar`, after that open your Minecraft launcher and select `release EMC_1.12`.

Making sure the installation was successful
-------------------

You can type `.version` to check what EMC version you are running, you can type `.cinfo` to see what client is loaded.

Installing a mod
-------------------

To install a mod made for [EMC framework](https://github.com/Moudoux/EMC) simply drag and drop the `Client.jar` file in 
`.minecraft/versions/EMC_1.12/`. The client must be named `Client.jar`.

Building
-------------------

Compile the code with all dependencies, open the jar, create a folder called `assets`, in the assets add your
patch file, and your `Client.jar` (Optional)

Making the patch file
-------------------

To make the patch file, download the latest version of this installer, then run 

`java -jar Installer.jar --gen <Original Minecraft jar> <Minecraft jar with EMC installed> <Patch file>`

License
-------------------

EMC Installer is licensed under GPL-3.0
