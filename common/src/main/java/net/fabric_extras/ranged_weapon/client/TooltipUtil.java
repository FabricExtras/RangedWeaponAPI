package net.fabric_extras.ranged_weapon.client;

import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.util.List;

public class TooltipUtil {
    /// Appends a `x.y sec Pull Time` line after the weapon's attribute block.
    /// Not wired up by default (2.3.4 left it dormant too); kept as public API for consumers that want it.
    public static void addPullTime(ItemStack itemStack, List<Text> lines) {
        var pullTime = readablePullTime(itemStack);
        if (pullTime > 0) {
            int lastAttributeLine = getLastAttributeLine(lines);

            if (lastAttributeLine > 0) {
                lines.add(lastAttributeLine + 1,
                        Text.literal(" ").append(
                                Text.translatable("item.ranged_weapon.pull_time", formattedNumber(pullTime / 20F))
                                        .formatted(Formatting.DARK_GREEN)
                        )
                );
            }
        }
    }

    private static int getLastAttributeLine(List<Text> lines) {
        int lastAttributeLine = -1;
        var attributePrefix = "attribute.modifier";
        var handPrefix = "item.modifiers";
        for (int i = 0; i < lines.size(); i++) {
            var content = lines.get(i).getContent();
            // Is this a line like "+1 Something"
            if (content instanceof TranslatableTextContent translatableText) {
                var key = translatableText.getKey();
                if (key.startsWith(attributePrefix) || key.startsWith(handPrefix)) {
                    lastAttributeLine = i;
                }
            }
        }
        return lastAttributeLine;
    }

    private static int readablePullTime(ItemStack itemStack) {
        if (itemStack.getItem() instanceof CustomRangedWeapon weapon) {
            return weapon.getRangedWeaponConfig().pullTimeTicks();
        }
        return 0;
    }

    private static String formattedNumber(float number) {
        DecimalFormat formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(1);
        return formatter.format(number);
    }
}
