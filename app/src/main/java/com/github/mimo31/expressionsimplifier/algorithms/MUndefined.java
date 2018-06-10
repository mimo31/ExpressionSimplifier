package com.github.mimo31.expressionsimplifier.algorithms;

import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;

/**
 * Created by Viktor on 12/23/2015.
 */
public final class MUndefined extends MathExpression {

    @Override
    public String toString() {
        return "[Undefined]";
    }

    @Override
    public MathExpression simplify() {
        return this;
    }
}
