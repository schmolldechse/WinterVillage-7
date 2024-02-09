package de.voldechse.wintervillage.potterwars.spell.type;

public enum SpellType {

    BUILDING("Bauen"),
    EXPLOSION("Explosion"),
    ENTITY_DAMAGE("Spielerschaden");

    private String spellType;

    SpellType(String spellType) {
        this.spellType = spellType;
    }

    public String getSpellType() {
        return spellType;
    }
}
