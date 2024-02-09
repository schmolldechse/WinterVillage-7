package de.voldechse.wintervillage.library.gradient;

import com.google.common.collect.ImmutableMap;
import de.voldechse.wintervillage.library.gradient.patterns.GradientPattern;
import de.voldechse.wintervillage.library.gradient.patterns.IPattern;
import de.voldechse.wintervillage.library.gradient.patterns.RainbowPattern;
import de.voldechse.wintervillage.library.gradient.patterns.SolidPattern;
import net.md_5.bungee.api.ChatColor;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Gradient {

    private static final List<String> SPECIAL_COLORS = Arrays.asList("&l", "&n", "&o", "&k", "&m", "§l", "§n", "§o", "§k", "§m");

    private static final Map<Color, ChatColor> COLORS = ImmutableMap.<Color, ChatColor>builder()
            .put(new Color(0), ChatColor.getByChar('0'))
            .put(new Color(170), ChatColor.getByChar('1'))
            .put(new Color(43520), ChatColor.getByChar('2'))
            .put(new Color(43690), ChatColor.getByChar('3'))
            .put(new Color(11141120), ChatColor.getByChar('4'))
            .put(new Color(11141290), ChatColor.getByChar('5'))
            .put(new Color(16755200), ChatColor.getByChar('6'))
            .put(new Color(11184810), ChatColor.getByChar('7'))
            .put(new Color(5592405), ChatColor.getByChar('8'))
            .put(new Color(5592575), ChatColor.getByChar('9'))
            .put(new Color(5635925), ChatColor.getByChar('a'))
            .put(new Color(5636095), ChatColor.getByChar('b'))
            .put(new Color(16733525), ChatColor.getByChar('c'))
            .put(new Color(16733695), ChatColor.getByChar('d'))
            .put(new Color(16777045), ChatColor.getByChar('e'))
            .put(new Color(16777215), ChatColor.getByChar('f')).build();

    private static final List<IPattern> PATTERNS = Arrays.asList(new GradientPattern(), new SolidPattern(), new RainbowPattern());

    @Nonnull
    public static String process(@Nonnull String string) {
        for (IPattern pattern : PATTERNS) {
            string = pattern.process(string);
        }

        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }

    @Nonnull
    public static List<String> process(@Nonnull Collection<String> strings) {
        return strings.stream()
                .map(Gradient::process)
                .collect(Collectors.toList());
    }

    @Nonnull
    public static String color(@Nonnull String string, @Nonnull Color color) {
        return ChatColor.of(color) + string;
    }

    @Nonnull
    public static String color(@Nonnull String string, @Nonnull Color start, @Nonnull Color end) {
        String originalString = string;

        ChatColor[] colors = createGradient(start, end, withoutSpecialChar(string).length());
        return apply(originalString, colors);
    }

    @Nonnull
    public static String rainbow(@Nonnull String string, float saturation) {
        String originalString = string;

        ChatColor[] colors = createRainbow(withoutSpecialChar(string).length(), saturation);
        return apply(originalString, colors);
    }

    @Nonnull
    public static ChatColor getColor(@Nonnull String string) {
        return ChatColor.of(new Color(Integer.parseInt(string, 16)));
    }

    @Nonnull
    public static String stripColorFormatting(@Nonnull String string) {
        return string.replaceAll("<#[0-9A-F]{6}>|[&§][a-f0-9lnokm]|<[/]?[A-Z]{5,8}(:[0-9A-F]{6})?[0-9]*>", "");
    }

    @Nonnull
    private static String apply(@Nonnull String source, ChatColor[] colors) {
        StringBuilder specialColors = new StringBuilder();
        StringBuilder stringBuilder = new StringBuilder();
        String[] characters = source.split("");
        int outIndex = 0;
        for (int i = 0; i < characters.length; i++) {
            if (characters[i].equals("&") || characters[i].equals("§")) {
                if (i + 1 < characters.length) {
                    if (characters[i + 1].equals("r")) {
                        specialColors.setLength(0);
                    } else {
                        specialColors.append(characters[i]);
                        specialColors.append(characters[i + 1]);
                    }
                    i++;
                } else
                    stringBuilder.append(colors[outIndex++]).append(specialColors).append(characters[i]);
            } else
                stringBuilder.append(colors[outIndex++]).append(specialColors).append(characters[i]);
        }
        return stringBuilder.toString();
    }

    @Nonnull
    private static String withoutSpecialChar(@Nonnull String source) {
        String workingString = source;
        for (String color : SPECIAL_COLORS) {
            if (workingString.contains(color)) {
                workingString = workingString.replace(color, "");
            }
        }
        return workingString;
    }

    @Nonnull
    private static ChatColor[] createRainbow(int step, float saturation) {
        ChatColor[] colors = new ChatColor[step];
        double colorStep = (1.00 / step);

        for (int i = 0; i < step; i++) {
            Color color = Color.getHSBColor((float) (colorStep * i), saturation, saturation);
            colors[i] = getClosestColor(color);
        }

        return colors;
    }

    @Nonnull
    private static ChatColor[] createGradient(@Nonnull Color start, @Nonnull Color end, int step) {
        ChatColor[] colors = new ChatColor[step];
        int stepR = Math.abs(start.getRed() - end.getRed()) / (step - 1);
        int stepG = Math.abs(start.getGreen() - end.getGreen()) / (step - 1);
        int stepB = Math.abs(start.getBlue() - end.getBlue()) / (step - 1);
        int[] direction = new int[]{
                start.getRed() < end.getRed() ? +1 : -1,
                start.getGreen() < end.getGreen() ? +1 : -1,
                start.getBlue() < end.getBlue() ? +1 : -1
        };

        for (int i = 0; i < step; i++) {
            Color color = new Color(start.getRed() + ((stepR * i) * direction[0]), start.getGreen() + ((stepG * i) * direction[1]), start.getBlue() + ((stepB * i) * direction[2]));
            colors[i] = ChatColor.of(color);
        }

        return colors;
    }

    @Nonnull
    private static ChatColor getClosestColor(Color color) {
        Color nearestColor = null;
        double nearestDistance = Integer.MAX_VALUE;

        for (Color constantColor : COLORS.keySet()) {
            double distance = Math.pow(color.getRed() - constantColor.getRed(), 2) + Math.pow(color.getGreen() - constantColor.getGreen(), 2) + Math.pow(color.getBlue() - constantColor.getBlue(), 2);
            if (nearestDistance > distance) {
                nearestColor = constantColor;
                nearestDistance = distance;
            }
        }
        return COLORS.get(nearestColor);
    }
}