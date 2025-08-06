# SpawnerFarm Addon - Complete Setup Guide for Non-Coders

This guide will walk you through **every step** needed to compile and use the SpawnerFarm addon, even if you have never coded before.

## 📋 What You Will Need

- **Windows, macOS, or Linux computer**
- **Internet connection** (to download software)
- **About 30-60 minutes** of time
- **At least 4GB of free disk space**

## 🛠️ Step 1: Install Required Software

### Java 21 Installation

SpawnerFarm requires Java 21 to compile. Here's how to install it:

#### Windows:
1. Go to https://adoptium.net/
2. Click **"Download"** for **Temurin 21 (LTS)**
3. Choose **Windows** and **x64** (or **aarch64** for ARM processors)
4. Download and run the `.msi` installer
5. Follow the installation wizard (accept all defaults)
6. **Test installation**: Open Command Prompt and type `java -version`
   - You should see something like `openjdk version "21.0.x"`

#### macOS:
1. Open Terminal (press `Cmd + Space`, type "Terminal")
2. Install Homebrew if you don't have it:
   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```
3. Install Java 21:
   ```bash
   brew install openjdk@21
   ```
4. Test: `java -version`

#### Linux (Ubuntu/Debian):
```bash
sudo apt update
sudo apt install openjdk-21-jdk
java -version
```

### Git Installation (Optional but Recommended)

#### Windows:
1. Download from https://git-scm.com/download/win
2. Run the installer with default settings

#### macOS:
```bash
brew install git
```

#### Linux:
```bash
sudo apt install git
```

## 📁 Step 2: Download the Source Code

You have two options:

### Option A: Using Git (Recommended)
```bash
git clone [YOUR_REPOSITORY_URL] spawnerfarm-addon
cd spawnerfarm-addon
```

### Option B: Download ZIP
1. Go to the GitHub repository page
2. Click the green **"Code"** button
3. Click **"Download ZIP"**
4. Extract to a folder called `spawnerfarm-addon`
5. Open Terminal/Command Prompt in that folder

## 🔧 Step 3: Compile the Addon

### Windows:
```bash
cd spawnerfarm-addon
gradlew.bat build
```

### macOS/Linux:
```bash
cd spawnerfarm-addon
chmod +x gradlew
./gradlew build
```

**⏰ This will take 5-15 minutes on first run** as it downloads dependencies.

### If You Get Errors:

**Java Version Error:**
- Make sure you installed Java 21 correctly
- Run `java -version` to verify

**Permission Denied (Linux/macOS):**
```bash
chmod +x gradlew
```

**Network/Download Errors:**
- Check your internet connection
- Try again - sometimes repositories are temporarily down

**Dependency Errors:**
- The build might fail if Meteor Client versions don't match
- This is normal - see troubleshooting section below

## 📦 Step 4: Find Your Compiled JAR

After successful compilation, your addon will be located at:

```
build/libs/spawnerfarm-1.0.0.jar
```

This is the file you need!

## 🎮 Step 5: Install and Use the Addon

### Prerequisites:
1. **Minecraft Java Edition 1.21.1**
2. **Fabric Loader 0.16.0+** - Download from https://fabricmc.net/
3. **Meteor Client** - Download from https://meteorclient.com/
4. **Baritone** - Usually included with Meteor Client

### Installation:
1. Copy `spawnerfarm-1.0.0.jar` to your `.minecraft/mods` folder
2. Make sure Meteor Client and Fabric API are also in the mods folder
3. Launch Minecraft with the Fabric profile
4. Open Meteor Client GUI (Right Shift by default)
5. Look for "Spawner Farm" in the modules list

### Configuration:
1. **Set Spawner Coordinates**: Either enable auto-detection or manually input X, Y, Z
2. **Set Ender Chest Coordinates**: Input the location of your storage chest
3. **Configure Evasion Settings**: Set threat range and enable/disable evasion
4. **Ensure you have a Silk Touch pickaxe** in your inventory

### Usage:
1. Place an Ender Chest near your spawner farm
2. Configure the addon settings in Meteor Client GUI
3. Enable the "Spawner Farm" module
4. The addon will automatically:
   - Find or navigate to the spawner
   - Mine spawners with shift for 64-stacks
   - Store them in the Ender Chest
   - Monitor for enemy players and disconnect if detected

## 🚨 Troubleshooting Common Issues

### Build Fails - "Could not resolve meteordevelopment:meteor-client"

This happens when exact version dependencies don't match. **This is common and expected.**

**Solution 1 - Use Pre-built Dependencies:**
1. Download Meteor Client 0.5.8 from https://meteorclient.com/
2. Put the downloaded jar in a `libs` folder in your project
3. Update `build.gradle.kts` dependencies section:
   ```kotlin
   dependencies {
       // ... other dependencies ...
       implementation(files("libs/meteor-client-0.5.8.jar"))
   }
   ```

**Solution 2 - Use Different Versions:**
Update the versions in `build.gradle.kts`:
```kotlin
// Try these versions instead:
minecraft("com.mojang:minecraft:1.21")
mappings("net.fabricmc:yarn:1.21+build.9:v2")
modImplementation("meteordevelopment:meteor-client:0.5.7-1.20.6-SNAPSHOT")
```

**Solution 3 - Manual Compilation (Advanced):**
1. Clone Meteor Client separately: `git clone https://github.com/MeteorDevelopment/meteor-client`
2. Compile it: `./gradlew build`
3. Use the compiled jar as a dependency

### "Module doesn't appear in Meteor Client"

1. Check that the jar is in the mods folder
2. Make sure Meteor Client is also installed
3. Look in the logs for error messages
4. Verify Minecraft version compatibility

### Runtime Errors

**"No Silk Touch pickaxe found":**
- Put a Silk Touch pickaxe in your inventory before enabling the module

**"Failed to path to spawner":**
- Make sure Baritone is installed and working
- Check spawner coordinates are correct
- Ensure there's a valid path to the spawner

**"No spawners found nearby":**
- Use manual coordinates instead of auto-detection
- Verify the spawner exists at the specified location

### Server Compatibility Issues

This addon requires servers with specific plugins that make spawners:
- Breakable with Silk Touch
- Stack up to 10,000 in one slot
- Yield 64 spawners when mined while sneaking
- Go directly to inventory (no item drops)

If your server doesn't have these features, the addon may not work correctly.

## 📞 Getting Help

If you're still having issues:

1. **Check the error logs** in `.minecraft/logs/latest.log`
2. **Verify all dependencies** are correct versions
3. **Test in singleplayer first** to isolate server-related issues
4. **Ask for help** in Minecraft modding communities

## ⚠️ Important Notes

- **This addon is for educational purposes**
- **Always follow your server's rules** about automation
- **Test thoroughly** before using on important servers
- **Use at your own risk** - the developers are not responsible for any consequences
- **Keep backups** of important items

## 🔄 Alternative: Getting a Pre-compiled Version

If compilation continues to fail, you can:

1. **Ask someone else to compile it** for you with their working development environment
2. **Use GitHub Actions** (if set up) to automatically build releases
3. **Join the community** and request a pre-built version

## 📝 File Structure Summary

After setup, your project should look like:
```
spawnerfarm-addon/
├── build/
│   └── libs/
│       └── spawnerfarm-1.0.0.jar  ← This is what you need!
├── src/
│   └── main/
│       ├── java/
│       │   └── com/spawnerfarm/
│       │       ├── SpawnerFarmAddon.java
│       │       └── modules/
│       │           └── SpawnerFarm.java
│       └── resources/
│           ├── fabric.mod.json
│           └── spawnerfarm.accesswidener
├── build.gradle.kts
├── settings.gradle
└── README.md
```

---

**You're now ready to use the SpawnerFarm addon! Remember to always follow server rules and use responsibly.**