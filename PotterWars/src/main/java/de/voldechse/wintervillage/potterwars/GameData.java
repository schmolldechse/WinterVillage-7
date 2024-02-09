package de.voldechse.wintervillage.potterwars;

public class GameData {

    private final String mapName, mapBuilder;

    public GameData(String mapName, String mapBuilder) {
        this.mapName = mapName;
        this.mapBuilder = mapBuilder;
    }

    public String getMapName() {
        return mapName;
    }

    public String getMapBuilder() {
        return mapBuilder;
    }
}
