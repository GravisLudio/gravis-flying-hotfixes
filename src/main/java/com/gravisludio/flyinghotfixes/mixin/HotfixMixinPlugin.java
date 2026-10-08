package com.gravisludio.flyinghotfixes.mixin;

import com.gravisludio.flyinghotfixes.GravisFlyingHotfixes;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * A hotfix mod must never be the reason the game does not start. If a later Create Fly renames or
 * removes a class one of these fixes targets, that fix is skipped (and logged) instead of failing
 * the mixin apply. Methods that move inside a class are covered by {@code defaultRequire: 0} in the
 * mixin configs.
 */
public class HotfixMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        try {
            MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
            return true;
        } catch (ClassNotFoundException | IOException e) {
            GravisFlyingHotfixes.LOGGER.warn("Skipping {}: Create Fly has no class {} (probably already fixed or renamed there).",
                    mixinClassName, targetClassName);
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
