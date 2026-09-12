package net.fabric_extras.ranged_weapon.client;

import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

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
///    own base damage rather than a bonus;
/// 3. the `ranged_weapon:pull_time` line is re-rendered as an absolute figure (and inserted when vanilla
///    left it out) — see {@link #showAbsolutePullTime}.
public class TooltipHelper {
    public static void updateTooltipText(ItemStack itemStack, List<Text> lines) {
        if (itemStack.getItem() instanceof CustomRangedWeapon weapon) {
            mergeAttributeLines_MainHandOffHand(lines);
            replaceAttributeLines_BlueWithGreen(lines);
            showAbsolutePullTime(weapon, lines);
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
            // `ItemStack#getTooltip` emits an empty separator line before EVERY equipment-slot header.
            // The off-hand header is gone now, so its separator is left dangling at the end of the merged
            // block. Removing it here — rather than trimming a trailing empty line off the whole tooltip,
            // as this used to do — is order independent: by the time this mixin runs at `getTooltip` RETURN
            // another contributor (Spell Engine's spell block, on Forge always, on Fabric depending on
            // mixin order) may already have appended its own separator plus lines, in which case the
            // dangling empty is no longer last and two blank lines end up stacked.
            var danglingSeparator = offHandLine - 1;
            if (danglingSeparator >= 0 && tooltip.get(danglingSeparator).getString().isEmpty()) {
                tooltip.remove(danglingSeparator);
            }
            for (var offhandAttribute : offHandAttributes) {
                if (mainHandAttributes.contains(offhandAttribute)) {
                    tooltip.remove(tooltip.lastIndexOf(offhandAttribute));
                }
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
                tooltip.set(i, absoluteAttributeLine(attributeValue, attributeTranslationKey));
            }
        }
    }

    /// Pull time is an absolute stat, not a bonus: `ranged_weapon:pull_time` has a base value of 1.0 and a
    /// weapon's `RangedConfig#pull_time_bonus` is an offset on top of it, so a `+0.25 sec Pull Time` line
    /// reads as nonsense and a 0 offset makes vanilla drop the line entirely (`ItemStack#getTooltip` only
    /// prints a modifier when `d > 0` or `d < 0`).
    ///
    /// This renders the effective figure instead — exactly what `RangedConfig#pullTimeTicks` hands to
    /// `CrossbowItem#getPullTime` and what `BowItemMixin` derives the bow's pull progress from — in the
    /// same dark green "equals" form vanilla uses for an item's own Attack Damage / Attack Speed, and
    /// always shows it for a bow or crossbow.
    private static void showAbsolutePullTime(CustomRangedWeapon weapon, List<Text> tooltip) {
        var pullTimeTicks = weapon.getRangedWeaponConfig().pullTimeTicks();
        var translationKey = EntityAttributes_RangedWeapon.PULL_TIME.translationKey;
        var line = absoluteAttributeLine(pullTimeTicks / 20.0, translationKey);

        var existing = indexOfAttributeLine(tooltip, translationKey);
        if (existing >= 0) {
            tooltip.set(existing, line);
            return;
        }
        // A zero offset means vanilla never emitted the line. Anchor the insert to the damage line so the
        // block keeps the multimap's damage → pull time → velocity order, and fall back to the end of the
        // attribute block. If there is no attribute block at all (`HideFlags` hides the modifiers), nothing
        // is inserted — an attribute line outside its section would be worse than a missing one.
        var anchor = indexOfAttributeLine(tooltip, EntityAttributes_RangedWeapon.DAMAGE.translationKey);
        if (anchor < 0) {
            anchor = lastAttributeBlockLine(tooltip);
        }
        if (anchor >= 0) {
            tooltip.add(anchor + 1, line);
        }
    }

    /// The `<value> <Attribute Name>` form vanilla uses for an item's own base attributes — copied from
    /// the `bl` branch of `ItemStack#getTooltip`: a leading space, the `attribute.modifier.equals.0` key,
    /// `MODIFIER_FORMAT` (`#.##`) and dark green.
    private static Text absoluteAttributeLine(double value, String attributeTranslationKey) {
        return Text.literal(" ")
                .append(
                        Text.translatable("attribute.modifier.equals." + EntityAttributeModifier.Operation.ADDITION.getId(),
                                MODIFIER_FORMAT.format(value), Text.translatable(attributeTranslationKey))
                )
                .formatted(Formatting.DARK_GREEN);
    }

    /// Index of the attribute line naming `attributeTranslationKey`, in any of vanilla's renderings
    /// (`plus` / `take` / `equals`), or -1.
    private static int indexOfAttributeLine(List<Text> tooltip, String attributeTranslationKey) {
        for (int i = 0; i < tooltip.size(); i++) {
            var modifier = attributeModifierContent(tooltip.get(i));
            if (modifier != null && namesAttribute(modifier, attributeTranslationKey)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean namesAttribute(TranslatableTextContent modifier, String attributeTranslationKey) {
        for (var arg : modifier.getArgs()) {
            if (arg instanceof Text attributeText
                    && attributeText.getContent() instanceof TranslatableTextContent attributeTranslatable
                    && attributeTranslatable.getKey().equals(attributeTranslationKey)) {
                return true;
            }
        }
        return false;
    }

    /// Last line of the item's own attribute section — the slot header (`item.modifiers.*`) plus the
    /// unbroken run of modifier lines under it — or -1 if there is no such section.
    ///
    /// The run is cut at the first line that is neither, so a later attribute block contributed by another
    /// mod (Spell Engine renders equipment-set bonuses as `attribute.modifier.*` lines of its own, behind
    /// a blank separator) cannot be mistaken for the tail of this one.
    private static int lastAttributeBlockLine(List<Text> tooltip) {
        int last = -1;
        for (int i = 0; i < tooltip.size(); i++) {
            var line = tooltip.get(i);
            var isBlockLine = hasKeyPrefixed(line, "item.modifiers") || attributeModifierContent(line) != null;
            if (isBlockLine) {
                last = i;
            } else if (last >= 0) {
                break;
            }
        }
        return last;
    }

    /// The `attribute.modifier.*` content of a tooltip line, or null.
    ///
    /// Vanilla's own "equals" lines — and the lines {@link #absoluteAttributeLine} produces — wrap the
    /// translatable inside a `Text.literal(" ")`, so the key can sit either on the line itself
    /// (`plus` / `take`) or on its first sibling.
    @Nullable
    private static TranslatableTextContent attributeModifierContent(Text line) {
        if (line.getContent() instanceof TranslatableTextContent translatable
                && translatable.getKey().startsWith("attribute.modifier")) {
            return translatable;
        }
        for (var sibling : line.getSiblings()) {
            if (sibling.getContent() instanceof TranslatableTextContent translatable
                    && translatable.getKey().startsWith("attribute.modifier")) {
                return translatable;
            }
        }
        return null;
    }

    private static boolean hasKeyPrefixed(Text line, String prefix) {
        return line.getContent() instanceof TranslatableTextContent translatable
                && translatable.getKey().startsWith(prefix);
    }
}
