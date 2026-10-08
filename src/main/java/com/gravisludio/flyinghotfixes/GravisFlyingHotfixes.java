package com.gravisludio.flyinghotfixes;

import com.gravisludio.flyinghotfixes.fix.SchematicRequirementFixes;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entrypoint. This mod registers no blocks, items, packets or recipes on purpose, so it can
 * be installed on only the client or only the server: the fixes that need a side live in that
 * side's entrypoint ({@link com.gravisludio.flyinghotfixes.client.GravisFlyingHotfixesClient}) or
 * in a mixin config with its own environment.
 */
public class GravisFlyingHotfixes implements ModInitializer {
    public static final String MOD_ID = "gravis_flying_hotfixes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SchematicRequirementFixes.register();
    }
}
