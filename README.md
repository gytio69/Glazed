# SpawnerFarm - Meteor Client Addon

**SpawnerFarm** is a comprehensive Minecraft Meteor Client addon that automates skeleton spawner farming with advanced enemy evasion capabilities. This addon integrates with Baritone for intelligent pathfinding and provides a complete farming solution.

## 🎯 Features

- **Automatic Spawner Detection**: Uses Baritone to automatically find nearby spawners or accepts manual coordinates
- **Silk Touch Verification**: Automatically detects and selects Silk Touch pickaxes from inventory
- **Shift-Mining for 64 Spawners**: Holds shift while mining to get 64 spawners per break (server plugin compatibility)
- **Automated Storage**: Automatically stores collected spawners in nearby Ender Chests
- **Enemy Player Evasion**: Detects nearby enemy players and triggers emergency evasion protocol
- **Complete Baritone Integration**: Uses Baritone for all pathfinding and movement
- **Configurable Settings**: Extensive GUI settings for customization

## 📋 Requirements

- **Minecraft**: 1.21.4
- **Fabric Loader**: 0.16.9 or newer
- **Fabric API**: 0.110.5+1.21.4 or compatible
- **Meteor Client**: 0.5.8-SNAPSHOT or compatible
- **Baritone**: 1.10.2 or compatible
- **Java**: 21 or newer

## 🛠️ Installation Instructions (For Non-Coders)

### Option 1: Use Pre-compiled JAR (If Available)

1. Download the latest `spawnerfarm-1.0.0.jar` file from releases
2. Place it in your Minecraft `.minecraft/mods` folder
3. Ensure you have all required dependencies installed
4. Launch Minecraft with Fabric and Meteor Client
5. Open Meteor Client GUI (Right Shift by default)
6. Find "Spawner Farm" category and configure the module

### Option 2: Compile from Source

#### Step 1: Install Required Software

1. **Install Java 21**:
   - Download from [Oracle](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) or [OpenJDK](https://openjdk.org/projects/jdk/21/)
   - Make sure `java -version` shows version 21

2. **Install Git** (if not already installed):
   - Download from [git-scm.com](https://git-scm.com/downloads)

#### Step 2: Download the Source Code

1. Open Command Prompt/Terminal
2. Navigate to where you want to download the project:
   ```bash
   cd C:\Users\YourName\Desktop
   ```
3. Clone or download this project to your computer

#### Step 3: Compile the Mod

1. Open Command Prompt/Terminal in the project folder
2. On Windows, run:
   ```bash
   gradlew.bat build
   ```
3. On macOS/Linux, run:
   ```bash
   ./gradlew build
   ```

4. Wait for compilation to complete (this may take 5-10 minutes on first run)

#### Step 4: Find Your Compiled JAR

After successful compilation, find your mod file at:
```
build/libs/spawnerfarm-1.0.0.jar
```

#### Step 5: Install the Mod

1. Copy `spawnerfarm-1.0.0.jar` to your Minecraft `.minecraft/mods` folder
2. Make sure you have Meteor Client and Baritone installed
3. Launch Minecraft with Fabric
4. The addon will be automatically loaded with Meteor Client

## ⚙️ Configuration

### Basic Setup

1. Open Meteor Client GUI (Right Shift by default)
2. Navigate to "Spawner Farm" category
3. Click on "Spawner Farm" module to configure:

### Settings Overview

#### Coordinates Tab
- **Manual Coordinates**: Toggle to use manual spawner coordinates
- **Spawner X/Y/Z**: Manual spawner position (only visible when manual mode enabled)

#### General Tab
- **Target Spawners**: Number of spawners to collect (0 = unlimited)
- **Ender Chest X/Y/Z**: Position of your Ender Chest for storage

#### Evasion Tab
- **Enable Evasion**: Toggle automatic enemy player evasion
- **Threat Range**: Distance in blocks to consider players a threat (default: 20)

### Usage Instructions

1. **Place an Ender Chest** near your spawner area
2. **Configure coordinates** in the module settings:
   - Either enable manual coordinates and enter spawner position
   - Or leave manual mode off for automatic detection
3. **Set Ender Chest coordinates** to match your chest location
4. **Ensure you have a Silk Touch pickaxe** in your inventory
5. **Enable the module** by clicking it in the Meteor GUI or pressing the assigned key

The module will automatically:
- Find/path to the spawner
- Verify your tools
- Mine spawners with shift for 64-stacks
- Store them in the Ender Chest
- Monitor for enemy players and evade if necessary

## 🔧 Troubleshooting

### Common Issues

**"No Silk Touch pickaxe found!"**
- Make sure you have a pickaxe with Silk Touch enchantment in your inventory
- The addon will automatically move it to your hotbar if needed

**"Failed to path to spawner"**
- Check that Baritone is properly installed and working
- Verify spawner coordinates are correct
- Make sure there's a valid path to the spawner

**"No spawners found nearby!"**
- Enable manual coordinates and input exact spawner position
- Make sure you're within reasonable distance of spawners
- Check that the spawner actually exists at the location

**Module won't enable**
- Ensure all dependencies are installed (Meteor Client, Baritone, Fabric API)
- Check Minecraft version compatibility
- Look for error messages in the console

### Compatibility Notes

- This addon requires specific server plugins that make spawners:
  - Breakable with Silk Touch
  - Instantly go to inventory (no drops)
  - Stackable up to 10,000
  - Yield 64 spawners when mined while sneaking

- If your server doesn't have these plugins, the addon may not work as expected

## 🚨 Safety Features

### Enemy Player Evasion

The addon includes comprehensive evasion features:

1. **Continuous Monitoring**: Scans for enemy players every second
2. **Threat Detection**: Configurable range for threat detection
3. **Emergency Protocol**: When triggered:
   - Immediately cancels all Baritone processes
   - Attempts to store current spawners in Ender Chest
   - Disconnects from the server for safety
   - Disables the module

### Risk Mitigation

- Always test the addon in single-player or safe environments first
- Configure threat range appropriately for your server
- Ensure Ender Chest is placed securely
- Keep emergency disconnect in mind

## 📝 Development Notes

This addon is built using:
- **Fabric Mod Development Kit** for Minecraft integration
- **Meteor Client Addon API** for module framework and GUI
- **Baritone API** for pathfinding and automation
- **Gradle** for build management

### Project Structure
```
src/main/java/com/spawnerfarm/
├── SpawnerFarmAddon.java          # Main addon class
└── modules/
    └── SpawnerFarm.java           # Core farming module

src/main/resources/
├── fabric.mod.json                # Mod metadata
├── spawnerfarm.accesswidener      # Access wideners
└── assets/spawnerfarm/
    └── icon.png                   # Mod icon
```

## 📜 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## ⚠️ Disclaimer

This addon is for educational purposes. Please ensure compliance with your server's rules and Minecraft's Terms of Service. The developers are not responsible for any consequences resulting from the use of this addon.

---

**Support**: For issues or questions, please check the troubleshooting section above or consult the Meteor Client community.



