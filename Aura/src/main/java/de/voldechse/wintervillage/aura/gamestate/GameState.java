package de.voldechse.wintervillage.aura.gamestate;

import de.voldechse.wintervillage.library.countdown.Countdown;

public abstract class GameState {

    public abstract void startCountdown();

    public abstract void endCountdown();

    public abstract Countdown getCountdown();

    public abstract Types getGameStatePhase();
}