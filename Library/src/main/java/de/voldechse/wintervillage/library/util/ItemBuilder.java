/* Copyright 2016 Acquized
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.voldechse.wintervillage.library.util;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * ItemBuilder - API Class to create a {@link org.bukkit.inventory.ItemStack} with just one line of Code
 * @version 1.8
 * @author Acquized
 * @contributor Kev575
 */
public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;
    private Material material = Material.STONE;
    private int amount = 1;
    private short damage = 0;
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private String displayname;
    private List<String> lore = new ArrayList<>();
    private List<ItemFlag> flags = new ArrayList<>();

    private boolean unbreakable;

    /**
     * Initalizes the ItemBuilder with {@link org.bukkit.Material}
     */
    public ItemBuilder(Material material) {
        if (material == null) material = Material.AIR;
        this.item = new ItemStack(material);
        this.material = material;
        this.unbreakable = false;
    }

    /**
     * Initalizes the ItemBuilder with {@link org.bukkit.Material} and Amount
     */
    public ItemBuilder(Material material, int amount) {
        if (material == null) material = Material.AIR;
        if (((amount > material.getMaxStackSize()) || amount <= 0)) amount = 1;
        this.amount = amount;
        this.item = new ItemStack(material, amount);
        this.material = material;
        this.unbreakable = false;
    }

    /**
     * Initalizes the ItemBuilder with {@link org.bukkit.Material}, Amount and Displayname
     */
    public ItemBuilder(Material material, int amount, String displayname) {
        if (material == null) material = Material.AIR;
        Objects.requireNonNull(displayname, "Displayname is null.");
        this.item = new ItemStack(material, amount);
        this.material = material;
        if (((amount > material.getMaxStackSize()) || amount <= 0)) amount = 1;
        this.amount = amount;
        this.displayname = displayname;
        this.unbreakable = false;
    }

    /**
     * Initalizes the ItemBuilder with {@link org.bukkit.Material} and Displayname
     */
    public ItemBuilder(Material material, String displayname) {
        if (material == null) material = Material.AIR;
        Objects.requireNonNull(displayname, "The Displayname is null.");
        this.item = new ItemStack(material);
        this.material = material;
        this.displayname = displayname;
        this.unbreakable = false;
    }

    /**
     * Initalizes the ItemBuilder with a {@link org.bukkit.inventory.ItemStack}
     */
    public ItemBuilder(ItemStack item) {
        Objects.requireNonNull(item, "The Item is null.");
        this.item = item;
        if (item.hasItemMeta())
            this.meta = item.getItemMeta();
        this.material = item.getType();
        this.amount = item.getAmount();
        this.damage = item.getDurability();
        this.enchantments = item.getEnchantments();
        if (item.hasItemMeta())
            this.displayname = item.getItemMeta().getDisplayName();
        if (item.hasItemMeta())
            this.lore = item.getItemMeta().getLore();
        if (item.hasItemMeta())
            for (ItemFlag f : item.getItemMeta().getItemFlags()) {
                flags.add(f);
            }
        this.unbreakable = false;
    }

    /**
     * Initalizes the ItemBuilder with an already existing {@link de.voldechse.wintervillage.library.util.ItemBuilder}
     *
     * @deprecated Use the already initalized {@code ItemBuilder} Instance to improve performance
     */
    @Deprecated
    public ItemBuilder(ItemBuilder builder) {
        Objects.requireNonNull(builder, "The ItemBuilder is null.");
        this.item = builder.item;
        this.meta = builder.meta;
        this.material = builder.material;
        this.amount = builder.amount;
        this.damage = builder.damage;
        this.enchantments = builder.enchantments;
        this.displayname = builder.displayname;
        this.lore = builder.lore;
        this.flags = builder.flags;
        this.unbreakable = builder.unbreakable;
    }

    /**
     * Sets the Amount of the ItemStack
     *
     * @param amount Amount for the ItemStack
     */
    public ItemBuilder amount(int amount) {
        if (((amount > material.getMaxStackSize()) || amount <= 0)) amount = 1;
        this.amount = amount;
        return this;
    }

    /**
     * Sets the Damage of the ItemStack
     *
     * @param damage Damage for the ItemStack
     * @deprecated Use {@code ItemBuilder#durability}
     */
    @Deprecated
    public ItemBuilder damage(short damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Sets the Durability (Damage) of the ItemStack
     *
     * @param damage Damage for the ItemStack
     */
    public ItemBuilder durability(short damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Sets the {@link org.bukkit.Material} of the ItemStack
     *
     * @param material Material for the ItemStack
     */
    public ItemBuilder material(Material material) {
        Objects.requireNonNull(material, "The Material is null.");
        this.material = material;
        return this;
    }

    /**
     * Sets the {@link org.bukkit.inventory.meta.ItemMeta} of the ItemStack
     *
     * @param meta Meta for the ItemStack
     */
    public ItemBuilder meta(ItemMeta meta) {
        Objects.requireNonNull(meta, "The Meta is null.");
        this.meta = meta;
        return this;
    }

    /**
     * Adds a {@link org.bukkit.enchantments.Enchantment} to the ItemStack
     *
     * @param enchant Enchantment for the ItemStack
     * @param level   Level of the Enchantment
     */
    public ItemBuilder enchant(Enchantment enchant, int level) {
        Objects.requireNonNull(enchant, "The Enchantment is null.");
        enchantments.put(enchant, level);
        return this;
    }

    /**
     * Adds a list of {@link org.bukkit.enchantments.Enchantment} to the ItemStack
     *
     * @param enchantments Map containing Enchantment and Level for the ItemStack
     */
    public ItemBuilder enchant(Map<Enchantment, Integer> enchantments) {
        Objects.requireNonNull(enchantments, "The Enchantments are null.");
        this.enchantments = enchantments;
        return this;
    }

    /**
     * Sets the Displayname of the ItemStack
     *
     * @param displayname Displayname for the ItemStack
     */
    public ItemBuilder displayname(String displayname) {
        Objects.requireNonNull(displayname, "The Displayname is null.");
        this.displayname = displayname;
        return this;
    }

    /**
     * Adds a Line to the Lore of the ItemStack
     *
     * @param line Line of the Lore for the ItemStack
     */
    public ItemBuilder lore(String line) {
        Objects.requireNonNull(line, "The Line is null.");
        lore.add(line);
        return this;
    }

    /**
     * Sets the Lore of the ItemStack
     *
     * @param lore List containing String as Lines for the ItemStack Lore
     */
    public ItemBuilder lore(List<String> lore) {
        Objects.requireNonNull(lore, "The Lores are null.");
        this.lore = lore;
        return this;
    }

    /**
     * Adds one or more Lines to the Lore of the ItemStack
     *
     * @param lines One or more Strings for the ItemStack Lore
     * @deprecated Use {@code ItemBuilder#lore}
     */
    @Deprecated
    public ItemBuilder lores(String... lines) {
        Objects.requireNonNull(lines, "The Lines are null.");
        for (String line : lines) {
            lore(line);
        }
        return this;
    }

    /**
     * Adds one or more Lines to the Lore of the ItemStack
     *
     * @param lines One or more Strings for the ItemStack Lore
     */
    public ItemBuilder lore(String... lines) {
        Objects.requireNonNull(lines, "The Lines are null.");
        for (String line : lines) {
            lore(line);
        }
        return this;
    }

    /**
     * Adds a String at a specified position in the Lore of the ItemStack
     *
     * @param line  Line of the Lore for the ItemStack
     * @param index Position in the Lore for the ItemStack
     */
    public ItemBuilder lore(String line, int index) {
        Objects.requireNonNull(line, "The Line is null.");
        lore.set(index, line);
        return this;
    }

    /**
     * Adds a {@link org.bukkit.inventory.ItemFlag} to the ItemStack
     *
     * @param flag ItemFlag for the ItemStack
     */
    public ItemBuilder flag(ItemFlag flag) {
        Objects.requireNonNull(flag, "The Flag is null.");
        flags.add(flag);
        return this;
    }

    /**
     * Adds more than one {@link org.bukkit.inventory.ItemFlag} to the ItemStack
     *
     * @param flags List containing all ItemFlags
     */
    public ItemBuilder flag(List<ItemFlag> flags) {
        Objects.requireNonNull(flags, "The Flags are null.");
        this.flags = flags;
        return this;
    }

    /**
     * Makes or removes the Unbreakable Flag from the ItemStack
     *
     * @param unbreakable If it should be unbreakable
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    /**
     * Makes the ItemStack Glow like it had a Enchantment
     */
    public ItemBuilder glow() {
        enchant(material != Material.BOW ? Enchantment.ARROW_INFINITE : Enchantment.LUCK, 10);
        flag(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    /**
     * Returns the Displayname
     */
    public String getDisplayname() {
        return displayname;
    }

    /**
     * Returns the Amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Returns all Enchantments
     */
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    /**
     * Returns the Damage
     *
     * @deprecated Use {@code ItemBuilder#getDurability}
     */
    @Deprecated
    public short getDamage() {
        return damage;
    }

    /**
     * Returns the Durability
     */
    public short getDurability() {
        return damage;
    }

    /**
     * Returns the Lores
     */
    public List<String> getLores() {
        return lore;
    }

    /**
     * Returns all ItemFlags
     */
    public List<ItemFlag> getFlags() {
        return flags;
    }

    /**
     * Returns the Material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Returns the ItemMeta
     */
    public ItemMeta getMeta() {
        return meta;
    }

    /**
     * Returns all Lores
     *
     * @deprecated Use {@code ItemBuilder#getLores}
     */
    @Deprecated
    public List<String> getLore() {
        return lore;
    }

    /**
     * Converts the ItemBuilder to a JsonItemBuilder
     *
     * @return The ItemBuilder as JSON String
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

    /**
     * Converts the ItemBuilder to a JsonItemBuilder
     *
     * @param builder Which ItemBuilder should be converted
     * @return The ItemBuilder as JSON String
     */
    public static String toJson(ItemBuilder builder) {
        return new Gson().toJson(builder);
    }

    /**
     * Converts the JsonItemBuilder back to a ItemBuilder
     *
     * @param json Which JsonItemBuilder should be converted
     */
    public static ItemBuilder fromJson(String json) {
        return new Gson().fromJson(json, ItemBuilder.class);
    }

    /**
     * Converts the ItemBuilder to a {@link org.bukkit.inventory.ItemStack}
     */
    public ItemStack build() {
        item.setType(material);
        item.setAmount(amount);
        item.setDurability(damage);
        meta = item.getItemMeta();

        if (this.unbreakable) meta.setUnbreakable(this.unbreakable);

        if (enchantments.size() > 0)
            enchantments.forEach((enchantment, integer) -> meta.addEnchant(enchantment, integer, true));
        if (displayname != null)
            meta.setDisplayName(displayname);

        if (lore.size() > 0)
            meta.setLore(lore);

        if (flags.size() > 0) {
            for (ItemFlag f : flags) {
                meta.addItemFlags(f);
            }
        }
        item.setItemMeta(meta);
        return item;
    }
}