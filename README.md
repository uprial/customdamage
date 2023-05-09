![CustomDamage Logo](images/customdamage-logo.jpg)

## Warning: I froze this plugin due to lack of popularity.

## Compatibility

Tested on Spigot-1.19.

## Dependencies

* [NashornJs](https://www.spigotmc.org/resources/nashornjs-provider-and-cli.91204/)

## Introduction

This is a fully customizable Minecraft (Bukkit) plugin that allows you to control damage taken by entities.

## Features

* Creeper does more damage by an explosion.
* The more a player dies, the more damage he/she will receive in the long-term.
* The more a player kills monsters, the more damage he/she will make in the long-term.
* There is also a short-term increase of incoming damage based on a rate of monster kills to death to cut the player domination.

#### You can configure:
* A filter of damage sources, targets and causes
* A formula of how to calculate a damage
* Dependencies from a player statistics

#### You can solve the following problems:
* Increase the damage taken by a specific entity, thus make some enemies more dangerous naturally
* Change damage depending on a players' kills or deaths, thus making a game more difficult for experienced players and easy for newbies without mechanics changes

## Commands

* `customdamage reload`        - reload config from disk
* `customdamage info`          - show damage modifiers
* `customdamage info @player`  - show damage modifiers of @player

## Permissions

* Access to 'reload' command:
`customdamage.reload` (default: op)

* Access to 'info @player' command:
`customdamage.info` (default: op)

## Configuration
[Default configuration file](src/main/resources/config.yml)

## Author
I will be happy to add some features or fix bugs. My mail: uprial@gmail.com.

## Useful links
* [Project on GitHub](https://github.com/uprial/customdamage/)
* [Project on Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customdamage/)
* [Project on Spigot](https://www.spigotmc.org/resources/customdamage.68712/)

## Related projects
* CustomCreatures: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customcreatures/), [GitHub](https://github.com/uprial/customcreatures), [Spigot](https://www.spigotmc.org/resources/customcreatures.68711/)
* CustomNukes: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customnukes/), [GitHub](https://github.com/uprial/customnukes), [Spigot](https://www.spigotmc.org/resources/customnukes.68710/)
* CustomRecipes: [Bukkit Dev](https://dev.bukkit.org/projects/custom-recipes), [GitHub](https://github.com/uprial/customrecipes/), [Spigot](https://www.spigotmc.org/resources/customrecipes.89435/)
* CustomVillage: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customvillage/), [GitHub](https://github.com/uprial/customvillage/), [Spigot](https://www.spigotmc.org/resources/customvillage.69170/)
* NastyIllusioner: [Bukkit Dev](https://legacy.curseforge.com/minecraft/bukkit-plugins/nastyillusioner), [GitHub](https://github.com/uprial/nastyillusioner), [Spigot](https://www.spigotmc.org/resources/nastyillusioner.109715/)
* RespawnLimiter: [Bukkit Dev](https://www.curseforge.com/minecraft/bukkit-plugins/respawn-limiter), [GitHub](https://github.com/uprial/respawnlimiter/), [Spigot](https://www.spigotmc.org/resources/respawnlimiter.106469/)
* TakeAim: [Bukkit Dev](https://dev.bukkit.org/projects/takeaim), [GitHub](https://github.com/uprial/takeaim), [Spigot](https://www.spigotmc.org/resources/takeaim.68713/)
