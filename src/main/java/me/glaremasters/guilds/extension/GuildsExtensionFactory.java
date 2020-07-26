package me.glaremasters.guilds.extension;

import com.djrapitops.plan.extension.DataExtension;

import java.util.Optional;

public class GuildsExtensionFactory {

    private boolean isAvailable() {
        try {
            Class.forName("me.glaremasters.guilds.Guilds");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public Optional<DataExtension> createExtension() {
        if (isAvailable()) {
            return Optional.of(new GuildsExtension());
        }
        return Optional.empty();
    }
}
