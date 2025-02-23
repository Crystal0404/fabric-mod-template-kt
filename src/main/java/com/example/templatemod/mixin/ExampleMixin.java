/*
 * fabric-mod-template-kt © 2025 by Crystal0404 and contributors is licensed under CC BY 4.0.
 * To view a copy of this license, visit https://creativecommons.org/licenses/by/4.0/
 */

package com.example.templatemod.mixin;

import com.example.templatemod.TemplateMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class ExampleMixin {
    @Inject(
            method = "loadWorld",
            at = @At("HEAD")
    )
    private void init(CallbackInfo ci) {
        // This code is injected into the start of MinecraftServer.loadWorld()V
        TemplateMod.LOGGER.info("Hello World");
    }
}
