package net.rpg_foundation.ranged_weapon.mixin.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.rpg_foundation.ranged_weapon.api.AttributeModifierIDs;
import net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Custom tooltip lines for the weapon's base damage / pull time modifiers (rendered like vanilla attack damage).
 * Since 1.21.5 the per-modifier tooltip line is produced by `AttributeModifiersComponent.Display.Default`.
 */
@Mixin(ItemAttributeModifiers.Display.Default.class)
public class ItemStackMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void customFormattedAttributes_RWA(
            Consumer<Component> textConsumer, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier modifier,
            CallbackInfo ci) {
        if (player != null) {
            if (attribute == EntityAttributes_RangedWeapon.DAMAGE.entry
                    && modifier.is(AttributeModifierIDs.WEAPON_DAMAGE_ID)
                    && modifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
                addGreenText(textConsumer, attribute, modifier, modifier.amount());
                ci.cancel();
            }
            if (attribute == EntityAttributes_RangedWeapon.PULL_TIME.entry
                    && modifier.is(AttributeModifierIDs.WEAPON_PULL_TIME_ID)
                    && modifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
                var value = modifier.amount() + player.getAttributeBaseValue(EntityAttributes_RangedWeapon.PULL_TIME.entry);
                addGreenText(textConsumer, attribute, modifier, value);
                ci.cancel();
            }
        }
    }


    private void addGreenText(Consumer<Component> textConsumer, Holder<Attribute> attribute, AttributeModifier modifier, double decimalValue) {
        textConsumer.accept(
                CommonComponents.space()
                        .append(
                                Component.translatable(
                                        "attribute.modifier.equals." + modifier.operation().id(),
                                        ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(decimalValue),
                                        Component.translatable(attribute.value().getDescriptionId())
                                )
                        )
                        .withStyle(ChatFormatting.DARK_GREEN)
        );
    }
}
