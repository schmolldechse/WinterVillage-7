package de.voldechse.wintervillage.ttt.gamestate;

public enum Types {

    LOBBY("TTT_LOBBY-PHASE", "§0[§aLobby§0]"),
    PREPARING_START("TTT_PREPARE_START", "§0[§cIngame§0]"),
    INGAME("TTT-INGAME", "§0[§cIngame§0]"),
    RESTART("TTT_RESTART-SERVER", "§0[§4Restart§0]");

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
