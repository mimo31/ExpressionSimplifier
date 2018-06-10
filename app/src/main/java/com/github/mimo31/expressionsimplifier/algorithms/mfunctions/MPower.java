package com.github.mimo31.expressionsimplifier.algorithms.mfunctions;

import com.github.mimo31.expressionsimplifier.algorithms.MFunction;
import com.github.mimo31.expressionsimplifier.algorithms.MInteger;
import com.github.mimo31.expressionsimplifier.algorithms.MUndefined;
import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;

/**
 * Created by Viktor on 12/23/2015.
 */
public final class MPower extends MFunction {

    public MPower(MathExpression base, MathExpression exponent) {
        super(base, exponent);
    }

    @Override
    public String toString() {
        String firstParam = this.params[0].toString();
        if (this.params[0] instanceof MFunction) {
            firstParam = "(" + firstParam + ")";
        }
        String secondParam = this.params[1].toString();
        if (this.params[1] instanceof MFunction) {
            secondParam = "(" + secondParam + ")";
        }
        return firstParam + " ^ " + secondParam;
    }
}
