package de.voldechse.wintervillage.potterwars.gamestate;

public enum Types {

    LOBBY("PW_LOBBY-PHASE", "§0[§aLobby§0]"),
    PREPARING_START("PW_PREPARE_START", "§0[§cIngame§0]"),
    INGAME("PW-INGAME", "§0[§cIngame§0]"),
    OVERTIME("PW-OVERTIME", "§0[§cIngame§0]"),
    RESTART("PW_RESTART-SERVER", "§0[§4Restart§0]");

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