package de.davidvogt.hkbmod.network;

import de.davidvogt.hkbmod.HkbMod;

public class ModNetworking {
    // Temporarily disabled networking due to API changes in Forge 57.0.3
    // TODO: Implement proper networking for multiplayer support

    public static void register() {
        HkbMod.LOGGER.info("Research system networking temporarily disabled");
    }

    // Placeholder methods for compatibility
    public static void send(Object packet, Object distributor) {
        // No-op for now
    }
}