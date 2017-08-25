package me.glaremasters.guilds.guild;

import com.google.gson.annotations.Expose;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class Guild implements InventoryHolder {

	@Expose(serialize = false)
	private String name;

	@Expose
	private String prefix;

	@Expose
	private List<GuildMember> members;

	@Expose
	private List<String> allies;

	@Expose
	private List<UUID> invitedMembers;

	@Expose
	private List<String> pendingAllies;
	private Inventory inventory;

	public Guild(String name, UUID master) {
		this(name);
		this.members.add(new GuildMember(master, 0));
	}

	public Guild(String name) {
		this.name = name;
		this.prefix = name.substring(0, Math.min(Main.getInstance().getConfig().getInt("prefix.max-length"), name.length()));
		this.members = new ArrayList<>();
		this.invitedMembers = new ArrayList<>();
		this.allies = new ArrayList<>();
		this.pendingAllies = new ArrayList<>();
	}

	public static Guild getGuild(String name) {
		return Main.getInstance().getGuildHandler().getGuilds().values().stream()
				.filter(guild -> ChatColor.stripColor(guild.getName()).equalsIgnoreCase(name))
				.findFirst().orElse(null);
	}

	public String getName() {
		return name;
	}

	public static boolean areAllies(UUID uuid1, UUID uuid2) {
		Guild guild1 = getGuild(uuid1);
		Guild guild2 = getGuild(uuid2);
		return (guild1 != null && guild2 != null) && guild1.getAllies().contains(guild2.getName());
	}

	public static Guild getGuild(UUID uuid) {
		return Main.getInstance().getGuildHandler().getGuilds().values().stream()
				.filter(guild -> guild.getMembers().stream().anyMatch(member -> member.getUniqueId().equals(uuid)))
				.findFirst().orElse(null);
	}

	public List<String> getAllies() {
		return allies;
	}

	public List<GuildMember> getMembers() {
		return members;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getStatus() {
		return Main.getInstance().guildStatusConfig.getString(name).toLowerCase();
	}

	public double getBankBalance() {
		YamlConfiguration banksConfig = Main.getInstance().guildBanksConfig;
		if (!banksConfig.isSet(name)) {
			banksConfig.set(name, 0);
		}
		return banksConfig.getDouble(name);
	}

	public int getTierCost() {
		if (getTier() >= Main.getInstance().getConfig().getInt("max-number-of-tiers")) {
			return 0;
		}
		return Main.getInstance().getConfig().getInt("tier" + (getTier() + 1) + ".cost");
	}

	public int getTier() {
		YamlConfiguration tiersConfig = Main.getInstance().guildTiersConfig;
		if (!tiersConfig.isSet(name)) {
			tiersConfig.set(name, 1);
		}
		return tiersConfig.getInt(name);
	}

	public int getMaxMembers() {
		return Main.getInstance().getConfig().getInt("tier" + getTier() + ".max-members");
	}

	public double getExpMultiplier() {
		return Main.getInstance().getConfig().getDouble("tier" + getTier() + ".mob-xp-multiplier");
	}

	public double getDamageMultiplier() {
		return Main.getInstance().getConfig().getDouble("tier" + getTier() + ".damage-multiplier");
	}

	public double getMaxBankBalance() {
		return Main.getInstance().getConfig().getDouble("tier" + getTier() + ".max-bank-balance");
	}

	public List<UUID> getInvitedMembers() {
		return invitedMembers;
	}

	public void addMember(UUID uuid, GuildRole role) {
		members.add(new GuildMember(uuid, role.getLevel()));

		Player player = Bukkit.getPlayer(uuid);
		if (player != null && player.isOnline()) {
			Main.getInstance().getScoreboardHandler().show(player);
		}
		updateGuild("An error occurred while adding a member with an UUID of '%s' to guild '%s'", uuid.toString(), name);
	}

	public void updateGuild(String errorMessage, String... params) {
		Main.getInstance().getDatabaseProvider().updateGuild(this, (result, exception) -> {
			if (!result) {
				Main.getInstance().getLogger().log(Level.SEVERE, String.format(errorMessage, (Object[]) params));
				if (exception != null) {
					exception.printStackTrace();
				}
			}
		});
	}

	public void removeMember(UUID uuid) {
		GuildMember member = getMember(uuid);
		if (member == null) {
			return;
		}
		if (member == getGuildMaster()) {
			Main.getInstance().getDatabaseProvider().removeGuild(this, (result, exception) -> {
				if (!result) {
					Main.getInstance().getLogger().log(Level.SEVERE, String.format("An error occurred while removing guild '%s'", name));
					if (exception != null) {
						exception.printStackTrace();
					}
					return;
				}

				Map<String, Guild> guilds = Main.getInstance().getGuildHandler().getGuilds();
				guilds.remove(name);
				Main.getInstance().getGuildHandler().setGuilds(guilds);
				Main.getInstance().getScoreboardHandler().update();
			});
			return;
		}

		members.remove(member);
		updateGuild("An error occurred while removing a member with the UUID of '%s' from guild '%s'", uuid.toString(), name);
	}

	public GuildMember getMember(UUID uuid) {
		return members.stream().filter(member -> member.getUniqueId().equals(uuid))
				.findFirst().orElse(null);
	}

	public GuildMember getGuildMaster() {
		return members.stream().filter(member -> member.getRole() == 0)
				.findFirst().orElse(null);
	}

	public void inviteMember(UUID uuid) {
		invitedMembers.add(uuid);
		updateGuild("An error occurred while inviting a member with the UUID of '%s' to guild '%s'", uuid.toString(), name);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public Inventory getInventory() {
		if (inventory != null) {
			return inventory;
		}
		File vaultf = new File(Main.getInstance().getDataFolder(), "data/vaults/" + name + ".yml");

		if (!vaultf.exists()) {
			vaultf.getParentFile().mkdirs();
			try {
				vaultf.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileConfiguration vault = new YamlConfiguration();
		try {
			vault.load(vaultf);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		inventory = Bukkit.createInventory(this, 54, getName() + "'s " + Main.getInstance().getConfig().getString("gui-name.vault"));
		for (int i = 0; i < inventory.getSize(); i++) {
			if (vault.isSet("items.slot" + i)) {
				inventory.setItem(i, vault.getItemStack("items.slot" + i));
			}
		}
		return inventory;
	}

	public void removeInvitedPlayer(UUID uuid) {
		invitedMembers.remove(uuid);
		updateGuild("An error occurred while removing an invited member member with the UUID of '%s' to guild '%s'", uuid.toString(), name);
	}

	public void sendMessage(String message) {
		members.stream().map(member -> Bukkit.getPlayer(member.getUniqueId())).filter(Objects::nonNull)
				.forEach(player -> Message.sendMessage(player, message));
	}

	public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		members.stream().map(member -> Bukkit.getPlayer(member.getUniqueId())).filter(Objects::nonNull)
				.forEach(player -> player.sendTitle(title, subtitle, fadeIn, stay, fadeOut));
	}

	@Deprecated
	public void sendTitleOld(String title, String subtitle) {
		members.stream().map(member -> Bukkit.getPlayer(member.getUniqueId())).filter(Objects::nonNull)
				.forEach(player -> player.sendTitle(title, subtitle));
	}

	public void updatePrefix(String prefix) {
		setPrefix(prefix);
		updateGuildPrefix("An error occurred while updating prefix to '%s' for guild '%s'", prefix, name);
		Main.getInstance().getScoreboardHandler().update();
	}

	private void updateGuildPrefix(String errorMessage, String... params) {
		Main.getInstance().getDatabaseProvider().updatePrefix(this, (res, ex) -> {
			if (!res) {
				Main.getInstance().getLogger().severe(String.format(errorMessage, (Object[]) params));
				if (ex != null) {
					ex.printStackTrace();
				}
			}
		});
	}

	public void addAlly(Guild targetGuild) {
		allies.add(targetGuild.getName());
		addGuildAlly(targetGuild);
		updateGuild("Something went wrong while adding the ally %s from guild %s", targetGuild.getName(), name);
	}

	private void addGuildAlly(Guild targetGuild) {
		Main.getInstance().getDatabaseProvider().addAlly(this, targetGuild, (res, ex) -> {
			if (!res) {
				ex.printStackTrace();
			}
		});
	}

	public void removeAlly(Guild targetGuild) {
		allies.remove(targetGuild.getName());
		removeGuildAlly(targetGuild);
		updateGuild("Something went wrong while removing the ally %s from guild %s", targetGuild.getName(), name);
	}

	private void removeGuildAlly(Guild targetGuild) {
		Main.getInstance().getDatabaseProvider().removeAlly(this, targetGuild, (res, ex) -> {
			if (!res) {
				ex.printStackTrace();
			}
		});
	}

	public void addPendingAlly(Guild targetGuild) {
		pendingAllies.add(targetGuild.getName());
		updateGuild("Something went wrong while adding pending ally %s from guild %s", targetGuild.getName(), name);
	}

	public void removePendingAlly(Guild targetGuild) {
		pendingAllies.remove(targetGuild.getName());
		updateGuild("Something went wrong while removing pending ally %s from guild %s", targetGuild.getName(), name);
	}

	public List<String> getPendingAllies() {
		return pendingAllies;
	}

}



