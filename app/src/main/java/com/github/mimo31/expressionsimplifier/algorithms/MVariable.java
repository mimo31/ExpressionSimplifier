package com.github.mimo31.expressionsimplifier.algorithms;

import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;

/**
 * Created by Viktor on 12/20/2015.
 */
public final class MVariable extends MathExpression {

    public final char name;

    public MVariable(char name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return Character.toString(name);
    }

    @Override
    public MathExpression simplify() {
        return this;
    }
}
