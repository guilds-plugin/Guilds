package me.glaremasters.guilds.arena

import co.aikar.commands.ACFBukkitUtil
import java.util.UUID
import org.bukkit.Location

/**
 * Represents an Arena with unique id and name.
 *
 * The generated `equals` and `hashCode` cover `challenger`, `defender` and `inUse`, all of which
 * change while a war runs. Arenas are looked up by name through [ArenaHandler] rather than by hash,
 * so that is harmless here, but an arena must never be used as a key in a hash based collection:
 * reserving one would change its hash while it was stored, and it would become unreachable. The same
 * trap is what [me.glaremasters.guilds.guild.GuildChallenge] used to fall into.
 *
 * @property id the unique id of the arena.
 * @property name the name of the arena.
 * @property challenger the challenger location string representation, can be null.
 * @property defender the defender location string representation, can be null.
 * @property inUse boolean flag indicating whether the arena is currently in use.
 */
data class Arena(
    val id: UUID,
    val name: String,
    var challenger: String?,
    var defender: String?,
    @Transient var inUse: Boolean
) {

    /**
     * Constructs a new Arena with id and name.
     *
     * @param id the unique id of the arena.
     * @param name the name of the arena.
     */
    constructor(id: UUID, name: String) : this(id, name, null, null, false)

    /**
     * Returns the Location object for the challenger.
     *
     * @return the Location object for the challenger, null if the string representation is null.
     */
    val challengerLoc: Location?
        get() = ACFBukkitUtil.stringToLocation(challenger)

    /**
     * Returns the Location object for the defender.
     *
     * @return the Location object for the defender, null if the string representation is null.
     */
    val defenderLoc: Location?
        get() = ACFBukkitUtil.stringToLocation(defender)
}
