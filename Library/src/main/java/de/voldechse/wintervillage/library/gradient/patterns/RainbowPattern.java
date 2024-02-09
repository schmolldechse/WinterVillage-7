package de.voldechse.wintervillage.library.gradient.patterns;

import de.voldechse.wintervillage.library.gradient.Gradient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RainbowPattern implements IPattern {

    Pattern pattern = Pattern.compile("<RAINBOW([0-9]{1,3})>(.*?)</RAINBOW>");

    public String process(String string) {
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String saturation = matcher.group(1);
            String content = matcher.group(2);
            string = string.replace(matcher.group(), Gradient.rainbow(content, Float.parseFloat(saturation)));
        }
        return string;
    }
}