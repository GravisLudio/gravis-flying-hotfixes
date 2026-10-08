package com.gravisludio.flyinghotfixes.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client entrypoint. Fabric only loads it on a client, which is what keeps the Flywheel classes
 * that {@link FlywheelShutdownFix} touches out of a dedicated server.
 */
public class GravisFlyingHotfixesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FlywheelShutdownFix.register();
    }
}
