package me.glaremasters.guilds.extension;

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.StringProvider;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import me.glaremasters.guilds.Guilds;

import java.util.UUID;

@PluginInfo(name = "Guilds", iconName = "flask", iconFamily = Family.SOLID, color = Color.GREEN)
public final class GuildsExtension implements DataExtension {
    public GuildsExtension() {
    }

    @StringProvider(text = "Guild", iconName = "flag")
    public String guild(final UUID uuid) {
        return Guilds.getApi().getGuildHandler().getGuildByPlayerId(uuid).getName();
    }
}
