package de.voldechse.wintervillage.library.gradient.patterns;

import de.voldechse.wintervillage.library.gradient.Gradient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolidPattern implements IPattern {

    Pattern pattern = Pattern.compile("<SOLID:([0-9A-Fa-f]{6})>|#\\{([0-9A-Fa-f]{6})}");

    public String process(String string) {
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String color = matcher.group(1);
            if (color == null) color = matcher.group(2);

            string = string.replace(matcher.group(), Gradient.getColor(color) + "");
        }
        return string;
    }
}