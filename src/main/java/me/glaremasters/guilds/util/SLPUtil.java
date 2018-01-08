package me.glaremasters.guilds.util;

import me.glaremasters.guilds.placeholders.PlaceholdersSRV;
import net.minecrell.serverlistplus.core.ServerListPlusCore;
import net.minecrell.serverlistplus.core.player.PlayerIdentity;
import net.minecrell.serverlistplus.core.replacement.LiteralPlaceholder;
import net.minecrell.serverlistplus.core.replacement.ReplacementManager;
import net.minecrell.serverlistplus.core.status.StatusResponse;
import org.bukkit.Bukkit;

/**
 * Created by GlareMasters on 1/7/2018.
 */
public class SLPUtil {

    public void registerSLP() {

        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%guild_name%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    return this.replace(s,
                            PlaceholdersSRV.getGuild(Bukkit.getOfflinePlayer(identity.getName())));

                } else // Use the method below if player is unknown
                {
                    return super.replace(response, s);
                }
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                return "???";
            }
        });
        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%guild_master%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    return this.replace(s,
                            PlaceholdersSRV
                                    .getGuildMaster(Bukkit.getOfflinePlayer(identity.getName())));

                } else // Use the method below if player is unknown
                {
                    return super.replace(response, s);
                }
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                return "???";
            }
        });
        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%guild_member_count%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    return this.replace(s,
                            PlaceholdersSRV.getGuildMemberCount(Bukkit.getOfflinePlayer(identity.getName())));
                } else // Use the method below if player is unknown
                {
                    return super.replace(response, s);
                }
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                return "???";
            }
        });
        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%guild_members_online_count%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    return this.replace(s,
                            PlaceholdersSRV.getGuildMembersOnline(Bukkit.getOfflinePlayer(identity.getName())));
                } else // Use the method below if player is unknown
                {
                    return super.replace(response, s);
                }
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                return "???";
            }
        });
        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%guild_status%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    return this.replace(s,
                            PlaceholdersSRV.getGuildStatus(Bukkit.getOfflinePlayer(identity.getName())));
                } else // Use the method below if player is unknown
                {
                    return super.replace(response, s);
                }
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                return "???";
            }
        });
        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%guild_prefix%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    return this.replace(s,
                            PlaceholdersSRV.getGuildPrefix(Bukkit.getOfflinePlayer(identity.getName())));
                } else // Use the method below if player is unknown
                {
                    return super.replace(response, s);
                }
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                return "???";
            }
        });
        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%guild_role%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    return this.replace(s,
                            PlaceholdersSRV.getGuildRole(Bukkit.getOfflinePlayer(identity.getName())));
                } else // Use the method below if player is unknown
                {
                    return super.replace(response, s);
                }
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                return "???";
            }
        });
        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%guild_tier%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    return this.replace(s,
                            PlaceholdersSRV.getGuildTier(Bukkit.getOfflinePlayer(identity.getName())));
                } else // Use the method below if player is unknown
                {
                    return super.replace(response, s);
                }
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                return "???";
            }
        });
        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%guild_bank_balance%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    return this.replace(s,
                            PlaceholdersSRV.getBankBalance(Bukkit.getOfflinePlayer(identity.getName())));
                } else // Use the method below if player is unknown
                {
                    return super.replace(response, s);
                }
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                return "???";
            }
        });
        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%guild_tier_name%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    return this.replace(s,
                            PlaceholdersSRV.getTierName(Bukkit.getOfflinePlayer(identity.getName())));
                } else // Use the method below if player is unknown
                {
                    return super.replace(response, s);
                }
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                return "???";
            }
        });
        ReplacementManager.getDynamic().add(new LiteralPlaceholder("%guild_members%") {
            @Override
            public String replace(StatusResponse response, String s) {
                PlayerIdentity identity = response.getRequest().getIdentity();
                if (identity != null) {
                    return this.replace(s,
                            PlaceholdersSRV.getGuildMembers(Bukkit.getOfflinePlayer(identity.getName())));
                } else // Use the method below if player is unknown
                {
                    return super.replace(response, s);
                }
            }

            @Override
            public String replace(ServerListPlusCore core, String s) {
                // Unknown player, so let's just replace it with something unknown
                return "???";
            }
        });
    }

}
