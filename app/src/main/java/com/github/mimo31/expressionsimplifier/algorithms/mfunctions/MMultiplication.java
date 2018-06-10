package com.github.mimo31.expressionsimplifier.algorithms.mfunctions;

import android.support.annotation.NonNull;

import com.github.mimo31.expressionsimplifier.algorithms.MFunction;
import com.github.mimo31.expressionsimplifier.algorithms.MInteger;
import com.github.mimo31.expressionsimplifier.algorithms.MVariable;
import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;
import com.github.mimo31.expressionsimplifier.algorithms.Powered;
import com.github.mimo31.expressionsimplifier.algorithms.Transformations;
import com.github.mimo31.expressionsimplifier.algorithms.Transformations.Fraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viktor on 12/23/2015.
 */
public final class MMultiplication extends MFunction{

    public MMultiplication(MathExpression multiplicand, MathExpression multiplier) {
        super(multiplicand, multiplier);
    }

    public MMultiplication(MathExpression[] multiplicands) {
        super(multiplicands);
        if (multiplicands.length < 2) {
            throw new IllegalArgumentException("There must be at least two multiplicands passed as arguments.");
        }
    }

    public Transformations.AdditionTerm asAdditionTerm() {
        Fraction count = new Fraction(1);
        List<MathExpression> otherFactors = new ArrayList<MathExpression>();
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MInteger) {
                count.doMultiply(new Fraction(((MInteger) this.params[i]).value));
            }
            else if (isFraction(this.params[i])) {
                MDivision asDivision = (MDivision) this.params[i];
                count.doMultiply(Fraction.getFraction(((MInteger) asDivision.params[0]).value, ((MInteger) asDivision.params[1]).value));
            }
            else {
                otherFactors.add(this.params[i]);
            }
        }
        if (otherFactors.size() == 1) {
            return new Transformations.AdditionTerm(otherFactors.get(0), count);
        }
        return new Transformations.AdditionTerm(new MMultiplication(otherFactors.toArray(new MathExpression[otherFactors.size()])), count);
    }

    private static boolean isFraction(MathExpression expression) {
        if (expression instanceof MDivision) {
            MDivision asDivision = (MDivision) expression;
            if (asDivision.params[0] instanceof MInteger && asDivision.params[1] instanceof MInteger) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String[] paramsStrings = new String[this.params.length];
        boolean[] numeralSignInserting = new boolean[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            paramsStrings[i] = this.params[i].toString();
            if (this.params[i] instanceof MFunction) {
                if (this.params[i] instanceof MAddition || this.params[i] instanceof MSubtraction) {
                    paramsStrings[i] = "(" + paramsStrings[i] + ")";
                    numeralSignInserting[i] = false;
                }
                else {
                    numeralSignInserting[i] = true;
                }
            }
            else if (this.params[i] instanceof MInteger) {
                numeralSignInserting[i] = true;
                if (((MInteger)this.params[i]).value == -1 && i == 0 && paramsStrings.length > 1) {
                    paramsStrings[i] = "-";
                }
            }
            else {
                numeralSignInserting[i] = false;
            }
        }
        StringBuilder builder = new StringBuilder();
        if (paramsStrings[0].equals("-1")) {
            paramsStrings[0] = "-";
            numeralSignInserting[0] = false;
        }
        builder.append(paramsStrings[0]);
        for (int i = 1; i < paramsStrings.length; i++) {
            if (numeralSignInserting[i - 1] && numeralSignInserting[i]) {
                builder.append(" * ");
            }
            builder.append(paramsStrings[i]);
        }
        return builder.toString();
    }
}
