package com.github.mimo31.expressionsimplifier.algorithms;

import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MMultiplication;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MPower;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viktor on 12/22/2015.
 */
public class PolyTerm {

    public final List<TermFactor> factors;
    public int multiple;

    public PolyTerm() {
        this.factors = new ArrayList<TermFactor>();
        this.multiple = 1;
    }

    public PolyTerm(char variable) {
        this.multiple = 1;
        this.factors = new ArrayList<TermFactor>();
        this.factors.add(new TermFactor(variable, 1));
    }

    public PolyTerm(List<TermFactor> factors) {
        this.factors = factors;
        this.multiple = 1;
    }

    public MathExpression getMathExpression() {
        List<MathExpression> finalFactors = new ArrayList<MathExpression>();
        if (this.multiple != 1) {
            finalFactors.add(new MInteger(this.multiple));
        }
        for (int i = 0; i < this.factors.size(); i++) {
            if (this.factors.get(i).power == 1) {
                finalFactors.add(new MVariable(this.factors.get(i).variable));
            }
            else if (this.factors.get(i).power != 0) {
                finalFactors.add(new MPower(new MVariable(this.factors.get(i).variable), new MInteger(this.factors.get(i).power)));
            }
        }
        if (finalFactors.size() == 0) {
            return new MInteger(1);
        }
        else if (finalFactors.size() == 1) {
            return finalFactors.get(0);
        }
        else {
            return new MMultiplication(finalFactors.toArray(new MathExpression[finalFactors.size()]));
        }
    }

    public boolean sameFactors(PolyTerm term) {
        if (term.factors.size() != this.factors.size()) {
            return false;
        }
        for (int i = 0; i < term.factors.size(); i++) {
            boolean found = false;
            for (int j = 0; j < this.factors.size(); j++) {
                if (term.factors.get(i).variable == this.factors.get(j).variable) {
                    found = true;
                    if (term.factors.get(i).power != this.factors.get(j).power) {
                        return false;
                    }
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
