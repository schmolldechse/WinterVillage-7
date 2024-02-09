package de.voldechse.wintervillage.library.gradient.patterns;

import de.voldechse.wintervillage.library.gradient.Gradient;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradientPattern implements IPattern {

    Pattern pattern = Pattern.compile("<GRADIENT:([0-9A-Fa-f]{6})>(.*?)</GRADIENT:([0-9A-Fa-f]{6})>");

    /**
     * Applies a gradient pattern to the provided String.
     * Output might be the same as the input if this pattern is not present.
     *
     * @param string The String to which this pattern should be applied to
     * @return The new String with applied pattern
     */
    public String process(String string) {
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String start = matcher.group(1);
            String end = matcher.group(3);
            String content = matcher.group(2);
            string = string.replace(matcher.group(), Gradient.color(content, new Color(Integer.parseInt(start, 16)), new Color(Integer.parseInt(end, 16))));
        }
        return string;
    }
}