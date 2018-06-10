package com.github.mimo31.expressionsimplifier.algorithms;

/**
 * Created by Viktor on 12/22/2015.
 */
public class TermFactor {

    public int power;
    public final char variable;

    public TermFactor(char variable, int power) {
        this.variable = variable;
        this.power = power;
    }
}
