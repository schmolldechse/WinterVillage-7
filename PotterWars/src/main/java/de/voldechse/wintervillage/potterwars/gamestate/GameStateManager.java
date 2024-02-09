package de.voldechse.wintervillage.potterwars.gamestate;

import de.voldechse.wintervillage.potterwars.gamestate.list.*;

import java.util.ArrayList;
import java.util.List;

public class GameStateManager {

    private int gameStatePointer;
    private final List<GameState> gameStateList = new ArrayList<GameState>();

    public GameStateManager() {
        this.gameStatePointer = 0;
        this.gameStateList.add(new GameStateLobby());
        this.gameStateList.add(new GameStatePrepareStart());
        this.gameStateList.add(new GameStateIngame());
        this.gameStateList.add(new GameStateOvertime());
        this.gameStateList.add(new GameStateRestart());

        this.currentGameState().startCountdown();
    }

    public void nextGameState() {
        this.gameStatePointer++;
        this.currentGameState().startCountdown();
    }

    public void previousGameState() {
        this.gameStatePointer--;
        this.currentGameState().startCountdown();
    }

    public void setCurrentGameState(int gameStateIndex) {
        this.gameStatePointer = gameStateIndex;
        this.currentGameState().startCountdown();
    }

    public void lastGameState() {
        this.gameStatePointer = this.gameStateList.size() - 1;
        this.currentGameState().startCountdown();
    }

    public GameState currentGameState() {
        return this.gameStateList.get(this.gameStatePointer);
    }
}