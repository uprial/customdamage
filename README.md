## Compatibility

Tested on Spigot-1.14.2.

## Introduction

This is fully customizable Minecraft (Bukkit) plugin that allows you to control a damage taken by entities.

## Features

* Creeper does more damage by an explosion.
* The more a player dies, the more damage he/she will receive in the long-term.
* The more a player kills monsters, the more damage he/she will make in the long-term.
* There is also a short-term increase of incoming damage based on a rate of monster kills to death to cut the player domination.

#### You can configure:
* Filter of damage sources, targets and causes
* A formula of how to calculate a damage
* Dependencies from a player statistics

#### You can solve the following problems:
* Increase a damage taken by a specific entity, thus make some enemies more
dangerous naturally
* Change a damage depending on a players' kills or deaths, thus make a game more
difficult for experienced players and easy for newbies without mechanics changes

## Commands

* `customdamage reload`        - reload config from disk
* `customdamage info`          - show damage modifiers
* `customdamage info @player`  - show damage modifiers of @player;

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

## Related projects
* CustomNukes: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customnukes/), [GitHub](https://github.com/uprial/customnukes)
* CustomCreatures: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customcreatures/), [GitHub](https://github.com/uprial/customcreatures)
