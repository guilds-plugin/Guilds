/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.glaremasters.guilds.guild

import com.google.common.collect.Sets

/**
 * A data class representing a role in a guild.
 *
 * @property name A string representing the name of the role.
 * @property node A string representing the node of the role.
 * @property level An integer representing the level of the role.
 *
 * @constructor Creates an instance of GuildRole.
 */
data class GuildRole(
    @Transient val name: String,
    @Transient val node: String,
    val level: Int
) {
    @Transient
    private val perms: MutableSet<GuildRolePerm> = Sets.newHashSet()

    /**
     * Gets the set of permissions associated with the role.
     *
     * @return A set of GuildRolePerm objects representing the role's permissions.
     */
    fun getPerms(): Set<GuildRolePerm> {
        return perms
    }

    /**
     * Adds a permission to the set of permissions associated with the role.
     *
     * @param perm The GuildRolePerm object to be added to the role's permissions.
     */
    @Synchronized
    fun addPerm(perm: GuildRolePerm) {
        perms.add(perm)
    }

    /**
     * Removes a permission from the set of permissions associated with the role.
     *
     * @param perm The GuildRolePerm object to be removed from the role's permissions.
     */
    @Synchronized
    fun removePerm(perm: GuildRolePerm) {
        perms.remove(perm)
    }

    /**
     * Checks whether the role has a specific permission.
     *
     * @param perm The GuildRolePerm object representing the permission to check.
     *
     * @return A boolean indicating whether the role has the specified permission or not.
     */
    @Synchronized
    fun hasPerm(perm: GuildRolePerm): Boolean {
        return perms.contains(perm)
    }
}
