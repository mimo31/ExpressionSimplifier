package com.github.mimo31.expressionsimplifier.algorithms;

import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MMultiplication;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MPower;

/**
 * Created by Viktor on 12/20/2015.
 */
public abstract class MFunction extends MathExpression {

    public enum FunctionType {
        ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, POWER
    }

    public final MathExpression[] params;

    protected MFunction(MathExpression param1, MathExpression param2) {
        this.params = new MathExpression[]{ param1, param2 };
    }

    protected MFunction(MathExpression[] params) {
        this.params = params;
    }

    public PolyTerm getPolyTerm() {
        if (this instanceof MMultiplication) {
            PolyTerm polyTerm = new PolyTerm();
            for (int i = 0; i < this.params.length; i++) {
                if (this.params[i] instanceof MInteger) {
                    polyTerm.multiple *= ((MInteger)this.params[i]).value;
                }
                else if (this.params[i] instanceof MVariable) {
                    MVariable var = (MVariable)this.params[i];
                    char name = var.name;
                    boolean found = false;
                    for (int j = 0; j < polyTerm.factors.size(); j++) {
                        if (polyTerm.factors.get(j).variable == name) {
                            polyTerm.factors.get(j).power += 1;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        polyTerm.factors.add(new TermFactor(name, 1));
                    }
                }
                else if (this.params[i] instanceof MPower) {
                    MFunction func = (MFunction)this.params[i];
                    if (func.params[0] instanceof MVariable && func.params[1] instanceof MInteger) {
                        MVariable var = (MVariable)func.params[0];
                        char name = var.name;
                        int power = ((MInteger)func.params[1]).value;
                        boolean found = false;
                        for (int j = 0; j < polyTerm.factors.size(); j++) {
                            if (polyTerm.factors.get(j).variable == name) {
                                polyTerm.factors.get(j).power += power;
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            polyTerm.factors.add(new TermFactor(name, power));
                        }
                    }
                    else {
                        return null;
                    }
                }
                else {
                    return null;
                }
            }
            return polyTerm;
        }
        else if (this instanceof MPower) {
            if (this.params[0] instanceof MVariable && this.params[1] instanceof MInteger) {
                PolyTerm polyTerm = new PolyTerm();
                polyTerm.factors.add(new TermFactor(((MVariable) this.params[0]).name, ((MInteger) this.params[1]).value));
                return polyTerm;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }
}
