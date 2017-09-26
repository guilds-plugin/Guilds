# Guilds
[![Paetron](https://img.shields.io/badge/Patreon-subscribe-lightblue.svg)](https://www.patreon.com/GlareMasters)
[![Build Status](https://travis-ci.org/darbyjack/Guilds-Plugin.svg?branch=dev%2F2.0)](https://travis-ci.org/darbyjack/Guilds-Plugin) [![Spigot](https://img.shields.io/badge/Spigot-Project%20Page-orange.svg)](https://www.spigotmc.org/resources/guilds.46962/) [![Jenkins](https://img.shields.io/badge/Jenkins-Development%20Builds-blue.svg)](https://ci.glaremasters.me/job/Guilds/) [![Discord](https://img.shields.io/discord/272126301010264064.svg)](https://glaremasters.me/discord) [![Minecraft](https://img.shields.io/badge/Minecraft-1.8--1.12.2-red.svg)]()

# About
The Guilds Plugin was created to offer a RPG type system to servers. It was inspired by players like you who enjoy creating a fun and exciting environment for your server. Guilds allows players to join interactive groups of other players, forge their own communities on your server, and compete with other guilds for dominance and control.

# Useful Links

- [Official Project Page](https://www.spigotmc.org/resources/guilds.46962/)
- [Permissions](https://glaremasters.me/wiki/permissions/)
- [Commands](https://glaremasters.me/wiki/commands/)
- [Core Features](https://glaremasters.me/wiki/features/)

# Importing project

1. Clone the project
2. Import as a Gradle project in IntelliJ or whatever IDE you use.
3. Make a run task (in IntelliJ) (Run > Edit Configuration) 
4. Gradle project should be Guilds-Plugin and Tasks should be "clean build"

# Maven Usage

	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
  
  	<dependency>
	    <groupId>com.github.darbyjack</groupId>
	    <artifactId>Guilds-Plugin</artifactId>
	    <version>master-SNAPSHOT</version>
	</dependency>
  
  # Gradle Usage
  
  	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  	dependencies {
	        compile 'com.github.darbyjack:Guilds-Plugin:master-SNAPSHOT'
	}

