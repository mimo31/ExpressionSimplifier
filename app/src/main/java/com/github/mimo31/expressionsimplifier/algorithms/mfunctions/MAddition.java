package com.github.mimo31.expressionsimplifier.algorithms.mfunctions;

import com.github.mimo31.expressionsimplifier.algorithms.MFunction;
import com.github.mimo31.expressionsimplifier.algorithms.MInteger;
import com.github.mimo31.expressionsimplifier.algorithms.MVariable;
import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;
import com.github.mimo31.expressionsimplifier.algorithms.PolyTerm;
import com.github.mimo31.expressionsimplifier.algorithms.TermFactor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viktor on 12/23/2015.
 */
public final class MAddition extends MFunction{

    public MAddition(MathExpression addend1, MathExpression addend2) {
        super(addend1, addend2);
    }

    public MAddition(MathExpression[] addends) {
        super(addends);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.params[0].toString());
        for (int i = 1; i < this.params.length; i++) {
            builder.append(" + ");
            builder.append(this.params[i].toString());
        }
        return builder.toString();
    }
}
