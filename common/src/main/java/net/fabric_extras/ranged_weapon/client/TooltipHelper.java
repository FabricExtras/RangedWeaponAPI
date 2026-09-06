package net.fabric_extras.ranged_weapon.client;

import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.item.ItemStack.MODIFIER_FORMAT;

/// 1.20.1 tooltip formatting for ranged weapons, restored from RangedWeaponAPI 1.1.4.
///
/// 2.3.4 achieved this by cancelling `ItemStack#appendAttributeModifierTooltip` (a 1.21.1-only method)
/// and emitting a green line itself. On 1.20.1 the attribute block is built inline in
/// `ItemStack#getTooltip`, so the finished list is post-processed instead:
/// 1. the weapon's attributes appear under BOTH "When in Main Hand" and "When in Off Hand" (RWA puts its
///    modifiers on both hand slots) — those two sections are merged into one "both hands" section;
/// 2. the `ranged_weapon:damage` line is re-rendered dark green, matching how vanilla paints an item's
///    own base damage rather than a bonus.
public class TooltipHelper {
    public static void updateTooltipText(ItemStack itemStack, List<Text> lines) {
        if (itemStack.getItem() instanceof CustomRangedWeapon) {
            mergeAttributeLines_MainHandOffHand(lines);
            replaceAttributeLines_BlueWithGreen(lines);
        }
    }

    private static void mergeAttributeLines_MainHandOffHand(List<Text> tooltip) {
        List<Text> heldInHandLines = new ArrayList<>();
        List<Text> mainHandAttributes = new ArrayList<>();
        List<Text> offHandAttributes = new ArrayList<>();
        for (Text line : tooltip) {
            var content = line.getContent();
            if (content instanceof TranslatableTextContent translatableText) {
                if (translatableText.getKey().startsWith("item.modifiers")) {
                    heldInHandLines.add(line);
                }
                if (translatableText.getKey().startsWith("attribute.modifier")) {
                    if (heldInHandLines.size() == 1) {
                        mainHandAttributes.add(line);
                    }
                    if (heldInHandLines.size() == 2) {
                        offHandAttributes.add(line);
                    }
                }
            }
        }
        if (heldInHandLines.size() == 2) {
            var mainHandLine = tooltip.indexOf(heldInHandLines.get(0));
            var offHandLine = tooltip.indexOf(heldInHandLines.get(1));
            tooltip.remove(mainHandLine);
            tooltip.add(mainHandLine, Text.translatable("item.modifiers.both_hands").formatted(Formatting.GRAY));
            tooltip.remove(offHandLine);
            for (var offhandAttribute : offHandAttributes) {
                if (mainHandAttributes.contains(offhandAttribute)) {
                    tooltip.remove(tooltip.lastIndexOf(offhandAttribute));
                }
            }

            var lastIndex = tooltip.size() - 1;
            if (lastIndex >= 0 && tooltip.get(lastIndex).getString().isEmpty()) {
                tooltip.remove(lastIndex);
            }
        }
    }

    private static void replaceAttributeLines_BlueWithGreen(List<Text> tooltip) {
        var attributeTranslationKey = EntityAttributes_RangedWeapon.DAMAGE.translationKey;
        for (int i = 0; i < tooltip.size(); i++)  {
            var content = tooltip.get(i).getContent();
            if (!(content instanceof TranslatableTextContent translatable)) {
                continue;
            }
            var isProjectileAttributeLine = false;
            var attributeValue = 0.0;
            if (translatable.getKey().startsWith("attribute.modifier.plus.0")) { // `.0` suffix for addition
                for (var arg : translatable.getArgs()) {
                    if (arg instanceof String string) {
                        try {
                            attributeValue = Double.parseDouble(string);
                        } catch (Exception ignored) { }
                    }
                    if (arg instanceof Text attributeText
                            && attributeText.getContent() instanceof TranslatableTextContent attributeTranslatable
                            && attributeTranslatable.getKey().startsWith(attributeTranslationKey)) {
                        isProjectileAttributeLine = true;
                    }
                }
            }

            if (isProjectileAttributeLine && attributeValue > 0) {
                // The construction of this line is copied from ItemStack.class
                var greenAttributeLine = Text.literal(" ")
                        .append(
                                Text.translatable("attribute.modifier.equals." + EntityAttributeModifier.Operation.ADDITION.getId(),
                                        MODIFIER_FORMAT.format(attributeValue), Text.translatable(attributeTranslationKey))
                        )
                        .formatted(Formatting.DARK_GREEN);
                tooltip.set(i, greenAttributeLine);
            }
        }
    }
}
