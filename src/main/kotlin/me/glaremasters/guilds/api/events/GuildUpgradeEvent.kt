package me.glaremasters.guilds.api.events

import me.glaremasters.guilds.api.events.base.GuildEvent
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildTier
import org.bukkit.entity.Player

class GuildUpgradeEvent(player: Player, guild: Guild, val tier: GuildTier) : GuildEvent(player, guild)
