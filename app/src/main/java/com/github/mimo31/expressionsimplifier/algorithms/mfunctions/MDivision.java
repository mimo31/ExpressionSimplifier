package com.github.mimo31.expressionsimplifier.algorithms.mfunctions;

import com.github.mimo31.expressionsimplifier.algorithms.MFunction;
import com.github.mimo31.expressionsimplifier.algorithms.MInteger;
import com.github.mimo31.expressionsimplifier.algorithms.MUndefined;
import com.github.mimo31.expressionsimplifier.algorithms.MVariable;
import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;
import com.github.mimo31.expressionsimplifier.algorithms.PolyTerm;

/**
 * Created by Viktor on 12/23/2015.
 */
public final class MDivision extends MFunction{

    public MDivision(MathExpression dividend, MathExpression divisor) {
        super(dividend, divisor);
    }

    @Override
    public String toString() {
        String firstParam = this.params[0].toString();
        if (this.params[0] instanceof MAddition || this.params[0] instanceof MSubtraction) {
            firstParam = "(" + firstParam + ")";
        }
        String secondParam = this.params[1].toString();
        if (this.params[1] instanceof MFunction) {
            if (!(this.params[1] instanceof MPower)) {
                secondParam = "(" + secondParam + ")";
            }
        }
        return firstParam + " / " + secondParam;
    }
}
