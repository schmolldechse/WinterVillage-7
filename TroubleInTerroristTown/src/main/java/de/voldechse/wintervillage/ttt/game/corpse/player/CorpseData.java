package de.voldechse.wintervillage.ttt.game.corpse.player;

import com.mojang.authlib.properties.Property;
import de.voldechse.wintervillage.library.document.Document;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;

public class CorpseData {

    private Location location;
    private Property property;
    private ServerPlayer corpse;

    private boolean identified;
    private int savedShopPoints, entityId;
    private Document document;

    public CorpseData(Location location, Property property, Document document) {
        this.location = location;
        this.property = property;

        this.identified = false;
        this.savedShopPoints = 0;

        this.document = document;
    }

    public boolean isIdentified() {
        return identified;
    }

    public void setIdentified(boolean identified) {
        this.identified = identified;
    }

    public int getSavedShopPoints() {
        return savedShopPoints;
    }

    public void setSavedShopPoints(int savedShopPoints) {
        this.savedShopPoints = savedShopPoints;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Location getLocation() {
        return location;
    }

    public Property getProperty() {
        return property;
    }

    public ServerPlayer getCorpse() {
        return corpse;
    }

    public void setCorpse(ServerPlayer corpse) {
        this.corpse = corpse;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public String toString() {
        return "Corpse{" +
                "location=" + location +
                ",property=" + property +
                ",entityId=" + corpse.getId() +
                ",identified=" + identified +
                ",savedPoints=" + savedShopPoints +
                ",document=" + document.toString() + "}";
    }
}