package fi.dy.masa.malilib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft
{
    @Inject(method = "init", at = @At("RETURN"))
    private void onInitComplete(CallbackInfo ci)
    {
        // Register all mod handlers
        InitializationHandler.getInstance().onGameInitDone();
    }

    @Inject(method = "shutdown()V", at = @At("RETURN"))
    private void onTick(CallbackInfo ci)
    {
        ConfigManager.getInstance().saveAllConfigs();
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;processKeyBinds()V", shift = Shift.AFTER))
    private void onPostKeyboardInput(CallbackInfo ci)
    {
        KeybindMulti.reCheckPressedKeys();
    }
}
