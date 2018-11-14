# Guilds
[![Paetron](https://img.shields.io/badge/Patreon-subscribe-ff69b4.svg?style=for-the-badge)](https://www.patreon.com/GlareMasters)
[![Build Status](https://img.shields.io/badge/build-passing-lightgrey.svg?style=for-the-badge)](https://travis-ci.org/darbyjack/Guilds-Plugin) [![Spigot](https://img.shields.io/badge/Spigot-Project%20Page-orange.svg?style=for-the-badge)](https://www.spigotmc.org/resources/guilds.48920/) [![Jenkins](https://img.shields.io/badge/Jenkins-Development%20Builds-blue.svg?style=for-the-badge)](https://ci.glaremasters.me/job/Guilds/) [![Discord](https://img.shields.io/discord/272126301010264064.svg?style=for-the-badge)](https://glaremasters.me/discord) [![Minecraft](https://img.shields.io/badge/Minecraft-1.7--1.13.2-red.svg?style=for-the-badge)]()

# About
The Guilds Plugin was created to offer a RPG type system to servers. It was inspired by players like you who enjoy creating a fun and exciting environment for your server. Guilds allows players to join interactive groups of other players, forge their own communities on your server, and compete with other guilds for dominance and control.

# Useful Links

- [Official Project Page](https://www.spigotmc.org/resources/guilds.48920/)

# Maven Usage

	<repositories>
		<repository>
		    <id>glares-repo</id>
		    <url>https://ci.glaremasters.me/plugin/repository/everything/</url>
		</repository>
	</repositories>
  
  	<dependency>
	    <groupId>me.glaremasters</groupId>
	    <artifactId>Guilds</artifactId>
	    <version>LATEST</version>
	</dependency>
  
  # Gradle Usage
  
  	allprojects {
		repositories {
			...
			maven { url 'https://ci.glaremasters.me/plugin/repository/everything/' }
		}
	}
  
  	dependencies {
	        compile 'me.glaremasters:Guilds:+'
	}
