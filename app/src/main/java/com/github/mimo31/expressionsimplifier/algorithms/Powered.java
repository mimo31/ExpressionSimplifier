package com.github.mimo31.expressionsimplifier.algorithms;

import com.github.mimo31.expressionsimplifier.algorithms.MInteger;
import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MAddition;

/**
 * Created by Viktor on 12/22/2015.
 */
public class Powered {

    public final char variable;
    public MathExpression power;

    public Powered(char variable, MathExpression power) {
        this.variable = variable;
        this.power = power;
    }

    public void addToPower(MathExpression expression) {
        this.power = new MAddition(this.power, expression);
    }

    public void add1ToPower() {
        this.power = new MAddition(this.power, new MInteger(1));
    }
}
