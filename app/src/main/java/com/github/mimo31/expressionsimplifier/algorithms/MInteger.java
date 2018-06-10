package com.github.mimo31.expressionsimplifier.algorithms;


import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;

/**
 * Created by Viktor on 12/20/2015.
 */
public final class MInteger extends MathExpression {

    public final int value;

    public MInteger(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public MathExpression simplify() {
        return this;
    }
}
