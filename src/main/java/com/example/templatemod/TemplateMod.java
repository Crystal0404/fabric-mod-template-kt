/*
 * fabric-mod-template-kt © 2025 by Crystal0404 and contributors is licensed under CC BY 4.0.
 * To view a copy of this license, visit https://creativecommons.org/licenses/by/4.0/
 */

package com.example.templatemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMod implements ModInitializer {
    public static final String MOD_NAME = "TemplateMod";
    public static final String MOD_ID = "template_mod";
    public static String version;

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        version = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
        //# if MC >= 12104
        LOGGER.info("Hello World");
        //#else
        //$$ LOGGER.info("Hello Minecraft");
        //#endif
    }
}
