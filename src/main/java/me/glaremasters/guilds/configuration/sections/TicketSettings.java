package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 2:29 PM
 */
public class TicketSettings implements SettingsHolder {

    @Comment("Do you want to enable guild upgrade tickets?")
    public static final Property<Boolean> TICKET_ENABLED =
            newProperty("tickets.enabled", true);

    @Comment("What do you want the name of the upgrade ticket to be?")
    public static final Property<String> TICKET_NAME =
            newProperty("tickets.name", "&bGuild Upgrade Ticket");

    @Comment("What do you want the lore of the ticket to be?")
    public static final Property<List<String>> TICKET_LORE =
            newListProperty("tickets.lore", "&dRight click this ticket to upgrade your guild tier!");

    @Comment("What do you want the material of the ticket to be?")
    public static final Property<String> TICKET_MATERIAL =
            newProperty("tickets.material", "PAPER");





    private TicketSettings() {
    }
}
