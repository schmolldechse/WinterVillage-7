package de.voldechse.wintervillage.aura.gamestate;

public enum Types {

    LOBBY("AURA_LOBBY-PHASE", "§0[§aLobby§0]"),
    PREPARING_START("AURA_PREPARE_START", "§0[§cIngame§0]"),
    INGAME("AURA-INGAME", "§0[§cIngame§0]"),
    RESTART("AURA_RESTART-SERVER", "§0[§4Restart§0]");

    private String phaseName, cloudConvertion;

    Types(String phaseName, String cloudConvertion) {
        this.phaseName = phaseName;
        this.cloudConvertion = cloudConvertion;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }
}
