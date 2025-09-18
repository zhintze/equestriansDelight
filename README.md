
Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

Jade Integration:
================
This mod automatically handles Jade conflicts when viewing horses with the monocle equipped.

**How it works:**
- When wearing the monocle and looking at horses, this mod renders a blocking overlay that covers Jade's tooltip area
- Your detailed horse stats display through the monocle without Jade interference
- When not wearing the monocle, Jade functions normally for all entities
- No manual Jade configuration required!

**Benefits:**
- ✅ No conflicting information when using the monocle
- ✅ Jade continues to work normally for other entities
- ✅ Automatic switching based on monocle equipment status
- ✅ Clean, professional overlay without competing displays

Improved Horse Breeding:
========================
This mod includes an enhanced horse breeding system that makes breeding more rewarding and strategic:

**Features:**
- **Removes random third horse** from vanilla calculation for more predictable results
- **Weighted parent selection**: 60% weight to faster parent, 40% to slower parent (configurable)
- **Configurable stat variation**: Default -4% to +8% variation during breeding
- **Optional limit removal**: Remove vanilla speed/jump caps for unlimited breeding potential
- **Works with all mount types**: Horses, donkeys, mules that support breeding

**Configuration Options:**
- Enable/disable improved breeding system
- Adjust parent weight ratios (faster vs slower parent influence)
- Configure stat variation ranges
- Set maximum speed/jump multipliers when limits are removed
- Individual toggles for speed and jump limit removal

**Default Settings:**
- Faster parent weight: 60%
- Stat variation: -4% to +8%
- Speed limit: Removed (3x multiplier cap)
- Jump limit: Removed (2x multiplier cap)

This allows for strategic breeding programs where the best horses can produce even better offspring!

**Debug Commands:**
For server administrators and mod developers, several debug commands are available:
- `/equestriansdelight breeding test` - Creates two test horses with known stats for breeding experiments
- `/equestriansdelight breeding spawn <speed> <jump>` - Spawns a custom horse with specified speed and jump values
- `/equestriansdelight breeding config` - Displays current breeding configuration settings
- `/equestriansdelight breeding stats <horse>` - Shows detailed stats for a targeted horse entity
- `/equestriansdelight breeding debug` - Analyzes nearby horses and shows breeding readiness status

All commands require operator permissions (level 2).

Credits:
========
This mod includes monocle functionality and horse attribute tooltip system inspired by and adapted from the [Horse Expert](https://www.curseforge.com/minecraft/mc-mods/horse-expert) mod by Fuzs. The original Horse Expert mod provides comprehensive horse stat viewing capabilities, and portions of its tooltip rendering and attribute calculation systems have been integrated into this project with modifications for compatibility.

Original Horse Expert mod: https://www.curseforge.com/minecraft/mc-mods/horse-expert

Additional Resources:
==========
Community Documentation: https://docs.neoforged.net/
NeoForged Discord: https://discord.neoforged.net/
