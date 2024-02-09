package de.voldechse.wintervillage.aura.gamestate;

import de.voldechse.wintervillage.aura.gamestate.list.GameStateIngame;
import de.voldechse.wintervillage.aura.gamestate.list.GameStateLobby;
import de.voldechse.wintervillage.aura.gamestate.list.GameStatePrepareStart;
import de.voldechse.wintervillage.aura.gamestate.list.GameStateRestart;

import java.util.ArrayList;
import java.util.List;

public class GameStateManager {

    private int gameStatePointer;

    private final List<GameState> gameStates = new ArrayList<>();

    public GameStateManager() {
        this.gameStatePointer = 0;
        this.gameStates.add(new GameStateLobby());
        this.gameStates.add(new GameStatePrepareStart());
        this.gameStates.add(new GameStateIngame());
        this.gameStates.add(new GameStateRestart());

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
        this.gameStatePointer = this.gameStates.size() - 1;
        this.currentGameState().startCountdown();
    }

    public GameState currentGameState() {
        return this.gameStates.get(this.gameStatePointer);
    }
}