# Guilds
[![Paetron](https://img.shields.io/badge/Patreon-subscribe-ff69b4.svg?style=for-the-badge)](https://www.patreon.com/GlareMasters)
[![Build Status](https://img.shields.io/badge/build-passing-lightgrey.svg?style=for-the-badge)](https://travis-ci.org/darbyjack/Guilds-Plugin) [![Spigot](https://img.shields.io/badge/Spigot-Project%20Page-orange.svg?style=for-the-badge)](https://www.spigotmc.org/resources/guilds.48920/) [![Discord](https://img.shields.io/discord/272126301010264064.svg?style=for-the-badge)](https://glaremasters.me/discord) [![Minecraft](https://img.shields.io/badge/Minecraft-1.8--1.13.2-red.svg?style=for-the-badge)]()

# About
The Guilds Plugin was created to offer a RPG type system to servers. It was inspired by players like you who enjoy creating a fun and exciting environment for your server. Guilds allows players to join interactive groups of other players, forge their own communities on your server, and compete with other guilds for dominance and control.

# Maven Usage

	<repositories>
		<repository>
		    <id>guilds</id>
		    <url>https://repo.glaremasters.me/repository/public/</url>
		</repository>
	</repositories>
  
  	<dependency>
	    <groupId>me.glaremasters</groupId>
	    <artifactId>guilds</artifactId>
	    <version>3.4-RELEASE</version>
	</dependency>
  
  # Gradle Usage
  
  	allprojects {
		repositories {
			...
			maven { url 'https://repo.glaremasters.me/repository/public/' }
		}
	}
  
  	dependencies {
	        compile 'me.glaremasters:guilds:3.4-RELEASE'
	}
