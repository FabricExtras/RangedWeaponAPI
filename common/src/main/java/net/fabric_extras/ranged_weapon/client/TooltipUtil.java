package net.fabric_extras.ranged_weapon.client;

import com.ibm.icu.text.DecimalFormat;
import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

import java.util.List;

public class TooltipUtil {
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
            var line = lines.get(i);
            var content = line.getContent();
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
        var item = itemStack.getItem();
        double pullTime = 0;
//        if (item instanceof CustomRangedWeapon customBow) {
//            pullTime = customBow.getRangedWeaponConfig().pull_time();
//        }
        // Don't calculate haste for now, it would be inconsistent with showing damage without modifiers
//        var player = MinecraftClient.getInstance().player;
//        if (player != null && pullTime > 0) {
//            var haste = player.getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
//            pullTime /= EntityAttributes_RangedWeapon.HASTE.asMultiplier(haste);
//        }
        return (int) pullTime;
    }

    private static String formattedNumber(float number) {
        DecimalFormat formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(1);
        return formatter.format(number);
    }
}
