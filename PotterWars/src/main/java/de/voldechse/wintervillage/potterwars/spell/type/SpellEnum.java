package de.voldechse.wintervillage.potterwars.spell.type;

import org.bukkit.Material;

public enum SpellEnum {

    EXPELLIARMUS(75, "§eExpelliarmus", "§rEntwaffnen des Spielers", Material.RED_MUSHROOM, SpellType.ENTITY_DAMAGE),
    CRUCIO(155, "§aCrucio", "§rVergiftung für 7 Sekunden", Material.LEGACY_GREEN_RECORD, SpellType.ENTITY_DAMAGE),
    STUPOR(140, "§cStupor", "§rVerbesserter Pfeil Schaden", Material.ANVIL, SpellType.ENTITY_DAMAGE),
    INCENDIO(225, "§6Incendio", "§rAnzünden von Gegnern", Material.FLINT_AND_STEEL, SpellType.ENTITY_DAMAGE),
    PETRIFICUS_TOTALUS(130, "§8Petrificus Totalus", "§rVersteinern der Gegner", Material.STONE, SpellType.ENTITY_DAMAGE),
    BAUEN(2, "§eBauen", "§rPlatzieren von Blöcken ist out!", Material.SANDSTONE, SpellType.BUILDING),
    BOMBADA(275, "§4Bombada", "§rExplosionsschaden", Material.TNT, SpellType.EXPLOSION),
    PROTEGO(250, "§5Protego", "§rSchutzschild", Material.ENDER_EYE, SpellType.BUILDING);

    private int levelRequired;

    private String spellName;
    private String spellInformation;
    private Material spellItem;
    private SpellType spellType;

    SpellEnum(int levelRequired, String spellName, String spellInformation, Material spellItem, SpellType spellType) {
        this.levelRequired = levelRequired;
        this.spellName = spellName;
        this.spellInformation = spellInformation;
        this.spellItem = spellItem;
        this.spellType = spellType;
    }

    public int getLevelRequired() {
        return levelRequired;
    }

    public String getSpellName() {
        return spellName;
    }

    public String getSpellInformation() {
        return spellInformation;
    }

    public Material getSpellItem() {
        return spellItem;
    }

    public SpellType getSpellType() {
        return spellType;
    }
}