package org.vfast.backrooms.mixins;

import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServerLoader.class)
public abstract class WorldOpenFlowsMixins {
    @Shadow
    protected abstract void openWorldLoadBundledResourcePack(LevelStorage.Session worldAccess, SaveLoader worldStem, ResourcePackManager packRepository, Runnable onCancel);

    @Inject(method = "openWorldCheckWorldStemCompatibility", at = @At(value = "HEAD"))
    private void removeExperimental(LevelStorage.Session worldAccess, SaveLoader worldStem, ResourcePackManager packRepository, Runnable onCancel, CallbackInfo ci) {
        this.openWorldLoadBundledResourcePack(worldAccess, worldStem, packRepository, onCancel);
    }
}
