package me.glaremasters.guilds.database.databases.mysql;

class Query {

  static final String CREATE_TABLE_GUILDS =
      "CREATE TABLE IF NOT EXISTS guilds (" + "name VARCHAR(255) NOT NULL, "
          + "prefix VARCHAR(255) NOT NULL, " + "PRIMARY KEY (name))";
  static final String CREATE_TABLE_ALLIES =
          "CREATE TABLE IF NOT EXISTS guild_allies(" + "name VARCHAR(255) NOT NULL, " +
                  "guildName VARCHAR(255) NOT NULL, " +
                  "PRIMARY KEY(name), " +
                  "FOREIGN KEY(guildName) REFERENCES guilds(name))";
  static final String CREATE_TABLE_MEMBERS =
      "CREATE TABLE IF NOT EXISTS guild_members ( " + "uuid VARCHAR(36) NOT NULL, "
          + "guild VARCHAR(255) NOT NULL, " + "role INTEGER NOT NULL, " + "PRIMARY KEY (uuid)," +
              "FOREIGN KEY(guild) REFERENCES guilds(name))";
  static final String CREATE_TABLE_INVITED_MEMBERS =
      "CREATE TABLE IF NOT EXISTS invited_members ( " + "uuid VARCHAR(36) NOT NULL, "
          + "guild VARCHAR(255) NOT NULL," +
              "FOREIGN KEY (guild) REFERENCES guilds(name)," +
              "PRIMARY KEY (uuid))";

  static final String CREATE_GUILD = "INSERT INTO guilds (name, prefix) VALUES(?, ?)";
  static final String REMOVE_GUILD = "DELETE FROM guilds WHERE name=?";

  static final String UPDATE_PREFIX = "UPDATE guilds SET prefix=? WHERE name=?";

  static final String ADD_MEMBER =
      "INSERT INTO guild_members (uuid, guild, role) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE guild=VALUES(guild), role=VALUES(role)";
  static final String REMOVE_MEMBER = "DELETE FROM guild_members WHERE uuid=?";

  static final String ADD_INVITED_MEMBER =
      "INSERT INTO invited_members (uuid, guild) VALUES(?, ?)";
  static final String REMOVE_INVITED_MEMBER = "DELETE FROM invited_members WHERE uuid=?";

  static final String ADD_ALLY =
          "INSERT INTO guild_allies(name, guildName) VALUES(?, ?)";
  static final String REMOVE_ALLY = "DELETE FROM guild_allies WHERE name=?";
  static final String FIND_ALLY =
          "SELECT name FROM guild_allies WHERE guildName=?";

  static final String GET_GUILDS = "SELECT name, prefix FROM guilds";
  static final String GET_GUILD_MEMBERS = "SELECT uuid, role FROM guild_members WHERE guild=?";

}
