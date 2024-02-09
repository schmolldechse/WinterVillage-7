package de.voldechse.wintervillage.masterbuilders.gamestate;

public enum Types {

    LOBBY("WV-MB_LOBBY-PHASE", "§aLobby"),
    VOTING_THEME("WV-MB_VOTING-THEME", "§cVoting"),
    BUILDING_PHASE("WV-MB_BUILDING-PHASE", "§cIngame"),
    VOTING_BUILDINGS("WV-MB_VOTING-BUILDINGS", "§cIngame"),
    RESTART("WV-MB_RESTART-SERVER", "§4Restart");

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