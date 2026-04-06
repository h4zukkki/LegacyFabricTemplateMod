package net.legacyfabric.example

import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object ExampleMod : ModInitializer {

    const val MOD_ID = "modid"

    val logger: Logger = LogManager.getLogger(MOD_ID)

    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        logger.info("Hello fabric world!")
    }
}