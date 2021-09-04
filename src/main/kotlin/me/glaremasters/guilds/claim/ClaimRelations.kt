package me.glaremasters.guilds.claim

import ch.jalu.configme.SettingsManager
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.ClaimSettings
import me.glaremasters.guilds.guild.Guild
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.region.IWrappedRegion

object ClaimRelations {

    @JvmStatic
    fun getMap(wrapper: WorldGuardWrapper, player: Player, guilds: Guilds): List<ArrayList<GuildClaim?>> {

        val chunkArray = ArrayList<ArrayList<GuildClaim?>>()
        val bottomChunk = getBottomChunk(player)

        outer@for (i in 0..4) {
            val row = ArrayList<GuildClaim?>()
            inner@for (j in 0..8) {
                var haveAdded = false
                val chunk = getChunkFromBottomCorner(player, bottomChunk, i, j)

                val locBoundOne = getBottomCorner(player, chunk)
                val locBoundTwo = getTopCorner(player, chunk)

                val tempRegions = wrapper.getRegions(locBoundOne, locBoundTwo)

                for (tempRegion in tempRegions) {
                    val claim = ClaimUtils.getGuildsRegion(guilds, tempRegion)
                    if (claim != null) {
                        row.add(claim)
                        haveAdded = true
                    }
                }
                if (!haveAdded) {
                    row.add(null)
                }
            }
            chunkArray.add(row)
        }
        return chunkArray.reversed()
    }




    @JvmStatic
    fun getBottomChunk(player: Player): Chunk {

        val world = player.world
        val chunk = player.location.chunk
        val baseX = chunk.x
        val baseZ = chunk.z
        val rotation = (player.location.yaw + 225)

        return when {
            rotation <= 90 -> {
                world.getChunkAt(baseX - 4, baseZ + 2)
            }
            rotation <= 180 -> {
                world.getChunkAt(baseX - 2, baseZ - 4)
            }
            rotation <= 270 -> {
                world.getChunkAt(baseX + 4, baseZ - 2)
            }
            rotation <= 360 -> {
                world.getChunkAt(baseX + 2, baseZ + 4)
            }
            else -> {
                world.getChunkAt(baseX - 4, baseZ + 2)
            }
        }
    }

    @JvmStatic
    fun getChunkFromBottomCorner(player: Player, chunk: Chunk, x: Int, z: Int):Chunk {

        val rotation = (player.location.yaw + 225)

        return when {
            rotation <= 90 -> {
                player.world.getChunkAt(chunk.x+z, chunk.z-x)
            }
            rotation <= 180 -> {
                player.world.getChunkAt(chunk.x+x, chunk.z+z)
            }
            rotation <= 270 -> {
                player.world.getChunkAt(chunk.x-z, chunk.z+x)
            }
            rotation <= 360 -> {
                player.world.getChunkAt(chunk.x-x, chunk.z-z)

            }
            else -> {
                player.world.getChunkAt(chunk.x+z, chunk.z-x)
            }
        }
    }

    @JvmStatic
    fun getBottomCorner(player: Player, chunk: Chunk): Location {
        return chunk.getBlock(0, player.world.minHeight, 0).location
    }

    @JvmStatic
    fun getTopCorner(player: Player, chunk: Chunk): Location {
        return chunk.getBlock(15, player.world.maxHeight, 15).location
    }










    @JvmStatic
    fun isInProximity(wrapper: WorldGuardWrapper, player: Player, settingsManager: SettingsManager, guild: Guild, guilds: Guilds): Boolean {

        if (settingsManager.getProperty(ClaimSettings.CLAIM_PROXIMITY) <= 0) {
            return false
        }
        else {
            val chunk = player.location.chunk
            val proximity = settingsManager.getProperty(ClaimSettings.CLAIM_PROXIMITY)

            val bottomChunk = player.world.getChunkAt(chunk.x-proximity, chunk.z-proximity)
            val topChunk = player.world.getChunkAt(chunk.x+proximity, chunk.z+proximity)

            val bottomLocation = bottomChunk.getBlock(0, player.world.minHeight, 0).location
            val topLocation = topChunk.getBlock(0, player.world.maxHeight, 0).location

            val regions = wrapper.getRegions(bottomLocation, topLocation)

            if (!guild.claimedLand.isNullOrEmpty()) {
                return !setContainsOnlyGuildClaims(regions, guild, guilds)
            }
            else {
                return !wrapper.getRegions(bottomLocation, topLocation).isNullOrEmpty()
            }
        }
    }

    @JvmStatic
    fun setContainsOnlyGuildClaims(regions: MutableSet<IWrappedRegion>, guild: Guild, guilds: Guilds): Boolean {
        for (region in regions) {
            val claim = ClaimUtils.getGuildsRegion(guilds, region)
            if (claim != null) {
                if (!claim.getGuild(guilds.guildHandler).equals(guild)) {
                    return false
                }
            } else {
                return false
            }
        }
        return true
    }









    @JvmStatic
    fun isAdjacent(wrapper: WorldGuardWrapper, player: Player, settingsManager: SettingsManager, guild: Guild): Boolean {
        if (settingsManager.getProperty(ClaimSettings.CLAIM_ADJACENT)) {
            val chunk = player.location.chunk

            val bottomChunk = player.world.getChunkAt(chunk.x-1, chunk.z-1)
            val topChunk = player.world.getChunkAt(chunk.x+1, chunk.z+1)

            val bottomLocation = bottomChunk.getBlock(0, player.world.minHeight, 0).location
            val topLocation = topChunk.getBlock(0, player.world.maxHeight, 0).location

            val regions = wrapper.getRegions(bottomLocation, topLocation)

            for (region in regions) {
                for (claim in guild.claimedLand) {
                    if (claim.name.toString().equals(region.id.toString())) {
                        return true
                    }
                }
            }
            if (guild.claimedLand.isNullOrEmpty()) {
                return true
            }
            return false
        }
        else {
            return true
        }
    }
}