package net.legacyfabric.example.mixin.a;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.screen.TitleScreen;

@Mixin(TitleScreen.class)
public class ExampleAMixin {

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo ci) {
        System.out.println("This line is printed by an example mod mixin A!");
    }
}