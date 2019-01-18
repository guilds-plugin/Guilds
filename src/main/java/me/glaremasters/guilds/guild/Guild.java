/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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

package me.glaremasters.guilds.guild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Guild {

    public enum Status {
        Public("Public"),
        Private("Private");

        Status(String s) {
        }
    }

    private final UUID id;
    private String name, prefix;
    private GuildMember guildMaster;

    private Location home;
    private Inventory inventory;
    private String textureUrl;
    private Status status;
    private GuildTier tier;
    private double balance = 0;

    private List<GuildMember> members = new ArrayList<>();
    private List<UUID> invitedMembers = new ArrayList<>();

    private List<String> allies = new ArrayList<>();
    private List<String> pendingAllies = new ArrayList<>();

    /**
     * Get a member in the guild
     * @param uuid the uuid of the member
     * @return the member which was found
     */
    public GuildMember getMember(UUID uuid) {
        return members.stream().filter(m -> m.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public void addMember(GuildMember guildMember){
        if (members.contains(guildMember)) return;
        members.add(guildMember);
    }

    public void removeMember(GuildMember guildMember){
        members.remove(guildMember);
    }

    public void removeMember(OfflinePlayer player){
        removeMember(getMember(player.getUniqueId()));
    }

    public void removeAlly(Guild guild){
        allies.remove(guild.getName());
    }

    public void addAlly(Guild guild) {
        allies.add(guild.getName());
    }

    public void addPendingAlly(Guild guild){
        pendingAllies.add(guild.getName());
    }

    public void removePendingAlly(Guild guild){
        pendingAllies.remove(guild.getName());
    }

    public int getSize() {
        return members.size();
    }
}

