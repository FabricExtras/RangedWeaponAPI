package net.fabric_extras.ranged_weapon.mixin.client;

import net.fabric_extras.ranged_weapon.api.AttributeModifierIDs;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "appendAttributeModifierTooltip", at = @At("HEAD"), cancellable = true)
    private void customFormattedAttributes_RWA(
            Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier,
            CallbackInfo ci) {
        if (player != null) {
            if (attribute == EntityAttributes_RangedWeapon.DAMAGE.entry
                    && modifier.idMatches(AttributeModifierIDs.WEAPON_DAMAGE_ID)
                    && modifier.operation().equals(EntityAttributeModifier.Operation.ADD_VALUE)) {
                addGreenText(textConsumer, attribute, modifier, modifier.value());
                ci.cancel();
            }
            if (attribute == EntityAttributes_RangedWeapon.PULL_TIME.entry
                    && modifier.idMatches(AttributeModifierIDs.WEAPON_PULL_TIME_ID)
                    && modifier.operation().equals(EntityAttributeModifier.Operation.ADD_VALUE)) {
                var value = modifier.value() + player.getAttributeBaseValue(EntityAttributes_RangedWeapon.PULL_TIME.entry);
                addGreenText(textConsumer, attribute, modifier, value);
                ci.cancel();
            }
        }
    }


    private void addGreenText(Consumer<Text> textConsumer, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, double decimalValue) {
        textConsumer.accept(
                ScreenTexts.space()
                        .append(
                                Text.translatable(
                                        "attribute.modifier.equals." + modifier.operation().getId(),
                                        AttributeModifiersComponent.DECIMAL_FORMAT.format(decimalValue),
                                        Text.translatable(attribute.value().getTranslationKey())
                                )
                        )
                        .formatted(Formatting.DARK_GREEN)
        );
    }
}
