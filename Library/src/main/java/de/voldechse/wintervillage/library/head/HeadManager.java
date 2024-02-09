package de.voldechse.wintervillage.library.head;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class HeadManager {

    private final char[] characters = new char[] { 'ϧ', 'Ϩ', 'ϩ', 'Ϫ', 'ϫ', 'Ϭ', 'ϭ', 'Ϯ' };
    private final Gson gson = new Gson();

    private final String headsProvider;
    private final LoadingCache<String, Optional<BaseComponent[]>> cache;

    private boolean hasPlayerProfile;

    public HeadManager(int cacheTime, String headProvider) {
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(cacheTime, TimeUnit.SECONDS).build(CacheLoader.from(this::loadHead));
        this.headsProvider = headProvider;

        try {
            hasPlayerProfile = OfflinePlayer.class.getMethod("getPlayerProfile") != null;
        } catch (NoSuchMethodException exception) {
            hasPlayerProfile = false;
        }
    }

    public BaseComponent[] getPlayerHead(String player) throws ExecutionException {
        return cache.get(player).orElse(null);
    }

    public void refreshPlayerHead(String player) {
        cache.refresh(player);
    }

    private Optional<BaseComponent[]> loadHead(String playerName) {
        try {
            Player player = Bukkit.getPlayer(playerName);
            String skinUrl = getSkinUrl(player);
            boolean isSpigotUrl = skinUrl != null;

            URL url = new URL(isSpigotUrl ? skinUrl : String.format(headsProvider, playerName));

            ComponentBuilder builder = new ComponentBuilder();
            BufferedImage headImage = isSpigotUrl ? HeadUtil.getHead(url) : ImageIO.read(url);

            for (int x = 0; x < headImage.getWidth(); x++) {
                for (int y = 0; y < headImage.getHeight(); y++) {
                    builder.append(String.valueOf(characters[y])).color(ChatColor.of(new Color(headImage.getRGB(x, y))));
                }
            }

            return Optional.of(builder.create());
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private String getSkinUrl(Player player) {
        if (player == null) return null;

        if (hasPlayerProfile) {
            URL skinURL = player.getPlayerProfile().getTextures().getSkin();
            return skinURL == null ? null : skinURL.toString();
        }

        try {
            GameProfile gameProfile = ((CraftPlayer) player).getProfile();
            if (gameProfile == null) return null;

            Property property = (Property) gameProfile.getProperties().get("textures");
            if (property == null) return null;

            return gson.fromJson(new String(
                            Base64.getDecoder().decode(property.getValue())), JsonObject.class)
                    .getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .get("url").getAsString();
        } catch (IllegalAccessError exception) {
            return null;
        }
    }
}
