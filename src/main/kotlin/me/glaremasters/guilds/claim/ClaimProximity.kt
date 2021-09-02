package me.glaremasters.guilds.claim

import co.aikar.commands.ACFBukkitUtil
import com.cryptomorin.xseries.XBlock
import com.cryptomorin.xseries.XMaterial
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.Guild
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.region.IWrappedRegion

object ClaimProximity {

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
                    val claim = ifIsAndGetGuildsRegion(guilds, tempRegion)
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
    fun ifIsAndGetGuildsRegion(guilds: Guilds, iWrappedRegion: IWrappedRegion): GuildClaim? {
        for (guild in guilds.guildHandler.guilds) {
            for (claim in guild.claimedLand) {
                if (claim.name.toString() == iWrappedRegion.id.toString()) {
                    return claim
                }
            }
        }
        return null
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
}