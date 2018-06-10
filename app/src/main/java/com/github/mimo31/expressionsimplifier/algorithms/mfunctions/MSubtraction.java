package com.github.mimo31.expressionsimplifier.algorithms.mfunctions;

import com.github.mimo31.expressionsimplifier.algorithms.MFunction;
import com.github.mimo31.expressionsimplifier.algorithms.MInteger;
import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;

/**
 * Created by Viktor on 12/23/2015.
 */
public final class MSubtraction extends MFunction{

    public MSubtraction(MathExpression minuend, MathExpression subtrahend) {
        super(minuend, subtrahend);
    }

    @Override
    public String toString() {
        String firstParam = this.params[0].toString();
        String secondParam = this.params[1].toString();
        if (this.params[0] instanceof MInteger) {
            if (((MInteger) this.params[0]).value == 0) {
                return "-" + secondParam;
            }
        }
        if (this.params[1] instanceof MAddition || this.params[1] instanceof MSubtraction) {
            secondParam = "(" + secondParam + ")";
        }
        return firstParam + " - " + secondParam;
    }
}
