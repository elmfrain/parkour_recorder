This directory contains Python3 scripts that can be utilized to aid with development.

This readme me file contains all the help information for each script. You may refer
to this file for help information or pass the -h, --help flag when executing a script
to print out help information for that script.

If adding help information for a new script, you must use the following format:
  
  Script: <script_name>
    <help_info>
  EndScript

or else the -h, --help functionality for that script may not work.

Script: json_to_lang.py
  Usage: <json_file> 
  
  Description: Converts .json language files into .lang. This is useful when
  you are porting from newer versions of Minecraft (e.g. MC1.16.5) to older
  versions such as MC1.12.2 (versions that still uses .lang files).
  
  How to Use: This script looks into the following directory:
    (Repository DIR)/
      src/
        main/
          resources/
            assets/
              parkour_recorder/
                lang/
                
  The user must put the .json file in that directory first before using this
  script. Once the .json file is in the directory, the user passes the name of
  the file as a parameter to the script. Once executed, the script saves the
  converted file in the same directory.
  
  Example: :$python3 json_to_lang.py en_us.json
EndScript

Script: lang_to_json.py
  Usage: <lang_file>

  Description: Converts .lang language files into .json. This is useful when
  you are porting from older versions of Minecraft (e.g. MC1.12.2) to newer
  versions such as MC1.18.1 (versions that now uses .json files).

  How to Use: This script looks into the following directory:
    (Repository DIR)/
      src/
        main/
          resources/
            assets/
              parkour_recorder/
                lang/

  The user must put the .lang file in that directory first before using this
  script. Once the .lang file is in the directory, the user passes the name of
  the file as the parameter when executing the script. Once executed, the script
  saves the converted file in the same directory.
EndScript

Script: save_version.py
  Usage: <mod_version> <mc_version>

  Description: Saves the specified version semantic to all files that stores
  the mod's version automatically.

  How to Use: This script will open the following files:
    -ParkourRecorderMod.java
    -build.gradle
    -mcmod.info or mods.toml (for newer versions of Minecraft)

  These files will be modified to replace the version semantics they store.
  The user must pass two parameters to the script. The first parameter
  <mod_version> accepts the mod version, and the second parameter <mc_version> 
  accepts the Minecraft version the mod is built for.

  Example: :$python3 save_version.py 1.1.2.0 1.12.2
EndScript

Script: update_changelog.py
  Usage: --no parameters--

  Description: Updates the local changelog file from prmod.elmfer.com.

  How to Use: This script will modify this file:
    (Repository DIR)/
      src/
        main/
          resources/
            assets/
              parkour_recorder/
                changelog.txt
  
  The user may run this script with no parameters. On execution, the script will
  fetch the online version of the changelog and update it locally. However, it
  will also add an "Offline" flag to it.

  The local changelog file is only used when the mod cannot fetch the online
  version of the changelog. Thus, the local "Offline" flag signals that the mod user is 
  viewing the local version of the changelog.
EndScript