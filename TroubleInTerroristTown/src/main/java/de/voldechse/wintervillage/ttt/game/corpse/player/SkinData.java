package de.voldechse.wintervillage.ttt.game.corpse.player;

public class SkinData {

    private String value, signature;

    public SkinData(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return "SkinData{" +
                "value=" + value +
                ",signature=" + signature + "}";
    }
}