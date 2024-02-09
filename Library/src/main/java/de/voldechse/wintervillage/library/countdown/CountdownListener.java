package de.voldechse.wintervillage.library.countdown;

public interface CountdownListener {

    void start();

    void stop();

    void second(int v0);

    void sleep();
}