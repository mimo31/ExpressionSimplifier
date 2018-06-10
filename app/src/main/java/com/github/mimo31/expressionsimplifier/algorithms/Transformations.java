package com.github.mimo31.expressionsimplifier.algorithms;

import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MAddition;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MDivision;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MMultiplication;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MPower;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MSubtraction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viktor on 1/16/2016.
 */
public class Transformations {

    public interface Transformation {

        MathExpression transformation(MathExpression expression);

    }

    public static final Transformation mergeAdditionAndSubtraction = new Transformation() {
        @Override
        public MathExpression transformation(MathExpression expression) {
            if (expression instanceof MAddition || expression instanceof MSubtraction) {
                MFunction asFunction = (MFunction) expression;
                List<AdditionTerm> terms = new ArrayList<AdditionTerm>();
                Fraction fractionSum = new Fraction(0);
                InverseOperatorLists plusMinus = InverseOperatorLists.getLists(asFunction, MAddition.class, MSubtraction.class);
                for (int i = 0; i < plusMinus.firstTypeList.size(); i++) {
                    plusMinus.firstTypeList.set(i, plusMinus.firstTypeList.get(i).simplify());
                }
                for (int i = 0; i < plusMinus.secondTypeList.size(); i++) {
                    plusMinus.secondTypeList.set(i, plusMinus.secondTypeList.get(i).simplify());
                }
                for (int i = 0; i < plusMinus.firstTypeList.size(); i++) {
                    if (plusMinus.firstTypeList.get(i) instanceof MInteger) {
                        fractionSum = fractionSum.getAdd(new Fraction(((MInteger) plusMinus.firstTypeList.get(i)).value));
                    } else if (plusMinus.firstTypeList.get(i) instanceof MVariable) {
                        addAdditionTerm(terms, new AdditionTerm(plusMinus.firstTypeList.get(i), new Fraction(1)));
                    } else if (plusMinus.firstTypeList.get(i) instanceof MFunction) {
                        if (plusMinus.firstTypeList.get(i) instanceof MDivision) {
                            MDivision asDivision = (MDivision) plusMinus.firstTypeList.get(i);
                            if (asDivision.params[1] instanceof MInteger) {
                                if (asDivision.params[0] instanceof MInteger) {
                                    fractionSum = fractionSum.getAdd(Fraction.getFraction(((MInteger) asDivision.params[0]).value, ((MInteger) asDivision.params[1]).value));
                                } else {
                                    if (asDivision.params[0] instanceof MMultiplication) {
                                        AdditionTerm term = ((MMultiplication) asDivision.params[0]).asAdditionTerm();
                                        Fraction denominator = new Fraction(((MInteger) asDivision.params[1]).value);
                                        denominator.doInverse();
                                        term.count.doMultiply(denominator);
                                        addAdditionTerm(terms, term);
                                    } else {
                                        Fraction denominator = new Fraction(((MInteger) asDivision.params[1]).value);
                                        denominator.doInverse();
                                        addAdditionTerm(terms, new AdditionTerm(asDivision.params[0], denominator));
                                    }
                                }
                            } else {
                                addAdditionTerm(terms, new AdditionTerm(plusMinus.firstTypeList.get(i), new Fraction(1)));
                            }
                        } else if (plusMinus.firstTypeList.get(i) instanceof MMultiplication) {
                            addAdditionTerm(terms, ((MMultiplication) plusMinus.firstTypeList.get(i)).asAdditionTerm());
                        } else {
                            addAdditionTerm(terms, new AdditionTerm(plusMinus.firstTypeList.get(i), new Fraction(1)));
                        }
                    } else {
                        addAdditionTerm(terms, new AdditionTerm(plusMinus.firstTypeList.get(i), new Fraction(1)));
                    }
                }
                for (int i = 0; i < plusMinus.secondTypeList.size(); i++) {
                    MathExpression currentEntry = plusMinus.secondTypeList.get(i);
                    if (currentEntry instanceof MInteger) {
                        fractionSum = fractionSum.getAdd(new Fraction(-((MInteger) currentEntry).value));
                    }
                    else if (currentEntry instanceof MVariable) {
                        addAdditionTerm(terms, new AdditionTerm(currentEntry, new Fraction(-1)));
                    }
                    else if (currentEntry instanceof MFunction) {
                        if (currentEntry instanceof MDivision) {
                            MDivision asDivision = (MDivision) currentEntry;
                            if (asDivision.params[1] instanceof MInteger) {
                                if (asDivision.params[0] instanceof MInteger) {
                                    fractionSum = fractionSum.getAdd(Fraction.getFraction(-((MInteger) asDivision.params[0]).value, ((MInteger) asDivision.params[1]).value));
                                }
                                else {
                                    if (asDivision.params[0] instanceof MMultiplication) {
                                        AdditionTerm term = ((MMultiplication) asDivision.params[0]).asAdditionTerm();
                                        Fraction denominator = new Fraction(((MInteger) asDivision.params[1]).value);
                                        denominator.doInverse();
                                        term.count.doMultiply(denominator);
                                        term.count.switchSign();
                                        addAdditionTerm(terms, term);
                                    }
                                    else {
                                        Fraction denominator = new Fraction(((MInteger) asDivision.params[1]).value);
                                        denominator.doInverse();
                                        denominator.switchSign();
                                        addAdditionTerm(terms, new AdditionTerm(asDivision.params[0], denominator));
                                    }
                                }
                            }
                            else {
                                addAdditionTerm(terms, new AdditionTerm(currentEntry, new Fraction(-1)));
                            }
                        }
                        else if (currentEntry instanceof MMultiplication) {
                            AdditionTerm additionTerm = ((MMultiplication) currentEntry).asAdditionTerm();
                            additionTerm.count.switchSign();
                            addAdditionTerm(terms, additionTerm);
                        }
                        else {
                            addAdditionTerm(terms, new AdditionTerm(currentEntry, new Fraction(-1)));
                        }
                    }
                    else {
                        addAdditionTerm(terms, new AdditionTerm(currentEntry, new Fraction(-1)));
                    }
                }
                List<MathExpression> expressionsToAdd = new ArrayList<MathExpression>();
                List<MathExpression> expressionsToSubtract = new ArrayList<MathExpression>();
                for (int i = 0; i < terms.size(); i++) {
                    AdditionTerm currentTerm = terms.get(i);
                    if (!currentTerm.count.isZero) {
                        if (currentTerm.count.isNegative) {
                            if (currentTerm.count.factors.size() == 0) {
                                expressionsToSubtract.add(currentTerm.value);
                            }
                            else {
                                currentTerm.count.switchSign();
                                expressionsToSubtract.add(new MMultiplication(currentTerm.count.asMathExpression(), currentTerm.value));
                            }
                        }
                        else {
                            if (currentTerm.count.factors.size() == 0) {
                                expressionsToAdd.add(currentTerm.value);
                            }
                            else {
                                expressionsToAdd.add(new MMultiplication(currentTerm.count.asMathExpression(), currentTerm.value));
                            }
                        }
                    }
                }
                if (!fractionSum.isNegative && !fractionSum.isZero) {
                    expressionsToAdd.add(fractionSum.asMathExpression());
                }
                MathExpression expressionToReturn;
                if (expressionsToAdd.size() == 0) {
                    expressionToReturn = new MInteger(0);
                }
                else if (expressionsToAdd.size() == 1) {
                    expressionToReturn = expressionsToAdd.get(0);
                }
                else {
                    expressionToReturn = new MAddition(expressionsToAdd.toArray(new MathExpression[expressionsToAdd.size()]));
                }
                for (int i = 0; i < expressionsToSubtract.size(); i++) {
                    expressionToReturn = new MSubtraction(expressionToReturn, expressionsToSubtract.get(i));
                }
                if (fractionSum.isNegative && !fractionSum.isZero) {
                    fractionSum.switchSign();
                    expressionToReturn = new MSubtraction(expressionToReturn, fractionSum.asMathExpression());
                }
                return expressionToReturn;
            }
            return expression;
        }
    };

    public static final Transformation simplifyMultiplicationAndDivision = new Transformation() {
        @Override
        public MathExpression transformation(MathExpression expression) {
            if (expression instanceof MMultiplication || expression instanceof MDivision) {
                MFunction asFunction = (MFunction) expression;
                InverseOperatorLists mulDivLists = InverseOperatorLists.getLists(asFunction, MMultiplication.class, MDivision.class);
                Fraction numberProduct = new Fraction(1);
                List<MultiplicationTerm> terms = new ArrayList<MultiplicationTerm>();
                for (int i = 0; i < mulDivLists.firstTypeList.size(); i++) {
                    mulDivLists.firstTypeList.set(i, mulDivLists.firstTypeList.get(i).simplify());
                }
                for (int i = 0; i < mulDivLists.secondTypeList.size(); i++) {
                    mulDivLists.secondTypeList.set(i, mulDivLists.secondTypeList.get(i).simplify());
                }
                for (int i = 0; i < mulDivLists.firstTypeList.size(); i++) {
                    MathExpression currentExpression = mulDivLists.firstTypeList.get(i);
                    if (currentExpression instanceof MInteger) {
                        numberProduct.doMultiply(new Fraction(((MInteger) currentExpression).value));
                    }
                    else if (currentExpression instanceof MPower) {
                        MPower asPower = (MPower) currentExpression;
                        if (asPower.params[0] instanceof MInteger && asPower.params[1] instanceof MInteger) {
                            Fraction currentFactor = new Fraction(((MInteger) asPower.params[0]).value);
                            currentFactor.doPower(((MInteger) asPower.params[1]).value);
                            numberProduct.doMultiply(currentFactor);
                        }
                        else {
                            addMultiplicationTerm(terms, new MultiplicationTerm(asPower.params[0], asPower.params[1]));
                        }
                    }
                    else if (isANegativeInteger(currentExpression)) {
                        numberProduct.doMultiply(new Fraction(-((MInteger) ((MSubtraction) currentExpression).params[1]).value));
                    }
                    else {
                        addMultiplicationTerm(terms, new MultiplicationTerm(currentExpression, new MInteger(1)));
                    }
                }
                for (int i = 0; i < mulDivLists.secondTypeList.size(); i++) {
                    MathExpression currentExpression = mulDivLists.secondTypeList.get(i);
                    if (currentExpression instanceof MInteger) {
                        int value = ((MInteger) currentExpression).value;
                        if (value == 0) {
                            return new MUndefined();
                        }
                        Fraction asFraction = new Fraction(value);
                        asFraction.doInverse();
                        numberProduct.doMultiply(asFraction);
                    }
                    else if (currentExpression instanceof MPower) {
                        MPower asPower = (MPower) currentExpression;
                        if (asPower.params[0] instanceof MInteger && asPower.params[1] instanceof MInteger) {
                            Fraction currentFactor = new Fraction(((MInteger) asPower.params[0]).value);
                            currentFactor.doPower(((MInteger) asPower.params[1]).value);
                            currentFactor.doInverse();
                            numberProduct.doMultiply(currentFactor);
                        }
                        else {
                            addMultiplicationTerm(terms, new MultiplicationTerm(asPower.params[0], new MMultiplication(asPower.params[1], new MInteger(-1))));
                        }
                    }
                    else if (isANegativeInteger(currentExpression)) {
                        Fraction factor = new Fraction(-((MInteger) ((MSubtraction) currentExpression).params[1]).value);
                        factor.doInverse();
                        numberProduct.doMultiply(factor);
                    }
                    else {
                        addMultiplicationTerm(terms, new MultiplicationTerm(currentExpression, new MInteger(-1)));
                    }
                }
                for (int i = 0; i < terms.size(); i++) {
                    MultiplicationTerm currentTerm = terms.get(i);
                    currentTerm.count = currentTerm.count.simplify();
                }
                if (numberProduct.isZero) {
                    return new MInteger(0);
                }
                List<MathExpression> expressionsToMultiply  = new ArrayList<MathExpression>();
                List<MathExpression> expressionsToDivide = new ArrayList<MathExpression>();
                if (numberProduct.factors.size() != 0 || numberProduct.isNegative) {
                    expressionsToMultiply.add(numberProduct.asMathExpression());
                }
                for (int i = 0; i < terms.size(); i++) {
                    MultiplicationTerm currentTerm = terms.get(i);
                    if (currentTerm.count instanceof MInteger) {
                        int power = ((MInteger) currentTerm.count).value;
                        if (power > 0) {
                            if (power == 1) {
                                expressionsToMultiply.add(currentTerm.value);
                            }
                            else {
                                expressionsToMultiply.add(new MPower(currentTerm.value, currentTerm.count));
                            }
                        }
                        else if (power < 0) {
                            if (power == -1) {
                                expressionsToDivide.add(currentTerm.value);
                            }
                            else {
                                expressionsToDivide.add(new MPower(currentTerm.value, new MInteger(-power)));
                            }
                        }
                    }
                    else {
                        expressionsToMultiply.add(new MPower(currentTerm.value, currentTerm.count));
                    }
                }
                MathExpression expressionToReturn;
                if (expressionsToMultiply.size() == 0) {
                    expressionToReturn = new MInteger(1);
                }
                else if (expressionsToMultiply.size() == 1) {
                    expressionToReturn = expressionsToMultiply.get(0);
                }
                else {
                    expressionToReturn = new MMultiplication(expressionsToMultiply.toArray(new MathExpression[expressionsToMultiply.size()]));
                }
                for (int i = 0; i < expressionsToDivide.size(); i++) {
                    expressionToReturn = new MDivision(expressionToReturn, expressionsToDivide.get(i));
                }
                return expressionToReturn;
            }
            return expression;
        }
    };

    public static final Transformation findUndefinedSpots = new Transformation() {
        @Override
        public MathExpression transformation(MathExpression expression) {
            if (expression instanceof MFunction) {
                MFunction asFunction = (MFunction) expression;
                for (int i = 0; i < asFunction.params.length; i++) {
                    asFunction.params[i] = findUndefinedSpots.transformation(asFunction.params[i]);
                    if (asFunction.params[i] instanceof MUndefined) {
                        return new MUndefined();
                    }
                }
                if (asFunction instanceof MDivision) {
                    if (asFunction.params[0] instanceof MInteger) {
                        if (((MInteger) asFunction.params[0]).value == 0) {
                            return new MUndefined();
                        }
                    }
                }
                else if (asFunction instanceof MPower) {
                    if (asFunction.params[0] instanceof MInteger && asFunction.params[1] instanceof MInteger) {
                        if (((MInteger) asFunction.params[0]).value == 0 && ((MInteger) asFunction.params[1]).value == 0) {
                            return new MUndefined();
                        }
                    }
                }
            }
            return expression;
        }
    };

    public static final Transformation resolveBasicPowers = new Transformation() {
        @Override
        public MathExpression transformation(MathExpression expression) {
            if (expression instanceof MPower) {
                MPower asPower = (MPower) expression;
                asPower.params[0] = asPower.params[0].simplify();
                asPower.params[1] = asPower.params[1].simplify();
                if (asPower.params[0] instanceof MPower) {
                    MPower baseAsPower = (MPower) asPower.params[0];
                    asPower.params[0] = baseAsPower.params[0];
                    asPower.params[1] = new MMultiplication(baseAsPower.params[1], asPower.params[1]).simplify();
                }
                if (asPower.params[0] instanceof MInteger) {
                    int base = ((MInteger) asPower.params[0]).value;
                    if (base == 0) {
                        if (asPower.params[1] instanceof MInteger) {
                            int exponent = ((MInteger) asPower.params[1]).value;
                            if (exponent == 0) {
                                return new MUndefined();
                            }
                        }
                        return new MInteger(0);
                    }
                    else if (base == 1) {
                        return  new MInteger(1);
                    }
                    else if (base == -1) {
                        if (asPower.params[1] instanceof MInteger) {
                            int exponent = ((MInteger) asPower.params[1]).value;
                            return Math.abs(exponent) % 2 == 0 ? new MInteger(1) : new MInteger(-1);
                        }
                    }
                }
                else if (asPower.params[1] instanceof MInteger) {
                    int exponent = ((MInteger) asPower.params[1]).value;
                    if (exponent == 0) {
                        return new MInteger(1);
                    }
                }
            }
            return expression;
        }
    };

    public static boolean isANegativeInteger(MathExpression expression) {
        if (expression instanceof MSubtraction) {
            MSubtraction asSubtraction = (MSubtraction) expression;
            if (asSubtraction.params[0] instanceof MInteger && asSubtraction.params[1] instanceof MInteger) {
                if (((MInteger) asSubtraction.params[0]).value == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addAdditionTerm(List<AdditionTerm> terms, AdditionTerm newTerm) {
        boolean found = false;
        for (int i = 0; i < terms.size(); i++) {
            if (EqualityCheck.sureEqual(terms.get(i).value, newTerm.value)) {
                terms.get(i).count = terms.get(i).count.getAdd(newTerm.count);
                found = true;
                break;
            }
        }
        if (!found) {
            terms.add(newTerm);
        }
    }

    public static class AdditionTerm {

        MathExpression value;
        Fraction count;

        public AdditionTerm(MathExpression value, Fraction count) {
            this.value = value;
            this.count = count;
        }
    }

    public static void addMultiplicationTerm(List<MultiplicationTerm> terms, MultiplicationTerm newTerm) {
        boolean found = false;
        for (int i = 0; i < terms.size(); i++) {
            MultiplicationTerm currentTerm = terms.get(i);
            if (EqualityCheck.sureEqual(currentTerm.value, newTerm.value)) {
                currentTerm.count = new MAddition(currentTerm.count, newTerm.count);
                found = true;
                break;
            }
        }
        if (!found) {
            terms.add(newTerm);
        }
    }

    public static class MultiplicationTerm {

        MathExpression value;
        MathExpression count;

        public MultiplicationTerm(MathExpression value, MathExpression count) {
            this.value = value;
            this.count = count;
        }

    }

    public static class Fraction {

        List<Factor> factors = new ArrayList<Factor>();
        boolean isNegative = false;
        boolean isZero = false;

        public Fraction getAdd(Fraction f) {
            if (f.isZero) {
                return this;
            }
            else if (this.isZero) {
                return f;
            }
            f = f.clone();
            Fraction f2 = this.clone();
            List<Factor> factoredOut = new ArrayList<Factor>();
            for (int i = 0; i < f2.factors.size(); i++) {
                int factorSearching = f2.factors.get(i).value;
                for (int j = 0; j < f.factors.size(); j++) {
                    if (factorSearching == f.factors.get(j).value) {
                        int powerToFactorOut;
                        int firstPower = f2.factors.get(i).power;
                        int secondPower = f.factors.get(j).power;
                        if ((firstPower < 0) != (secondPower < 0)) {
                            break;
                        }
                        if (Math.abs(firstPower) < Math.abs(secondPower)) {
                            powerToFactorOut = firstPower;
                        }
                        else {
                            powerToFactorOut = secondPower;
                        }
                        factoredOut.add(new Factor(factorSearching, powerToFactorOut));
                        firstPower -= powerToFactorOut;
                        secondPower -= powerToFactorOut;
                        if (firstPower == 0) {
                            f2.factors.remove(i);
                            i--;
                        }
                        else {
                            f2.factors.get(i).power = firstPower;
                        }
                        if (secondPower == 0) {
                            f.factors.remove(j);
                        }
                        else {
                            f.factors.get(j).power = secondPower;
                        }
                        break;
                    }
                }
            }
            for (int i = 0; i < f2.factors.size(); i++) {
                if (f2.factors.get(i).power < 0) {
                    addFactor(f2.factors.get(i), factoredOut);
                    f.addFactor(f2.factors.get(i).getInverse());
                    f2.factors.remove(i);
                    i--;
                }
            }
            for (int i = 0; i < f.factors.size(); i++) {
                if (f.factors.get(i).power < 0) {
                    addFactor(f.factors.get(i), factoredOut);
                    f2.addFactor(f.factors.get(i).getInverse());
                    f.factors.remove(i);
                    i--;
                }
            }
            Fraction sumFraction = new Fraction(f2.getValue() + f.getValue());
            sumFraction.addFactors(factoredOut);
            return sumFraction;
        }

        public void doPower(int power) {
            for (int i = 0; i < this.factors.size(); i++) {
                this.factors.get(i).power *= power;
            }
        }

        public static void addFactor(Factor f, List<Factor> factors) {
            boolean found = false;
            for (int i = 0; i < factors.size(); i++) {
                if (factors.get(i).value == f.value) {
                    factors.get(i).power += f.power;
                    if (factors.get(i).power == 0) {
                        factors.remove(i);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                factors.add(f);
            }
        }

        public MathExpression asMathExpression() {
            if (this.isZero) {
                return new MInteger(0);
            }
            int nominator;
            if (this.isNegative) {
                nominator = -1;
            }
            else {
                nominator = 1;
            }
            int denominator = 1;
            for (int i = 0; i < this.factors.size(); i++) {
                if (this.factors.get(i).power < 0) {
                    for (int j = 0; j > this.factors.get(i).power; j--) {
                        denominator *= this.factors.get(i).value;
                    }
                }
                else {
                    for (int j = 0; j < this.factors.get(i).power; j++) {
                        nominator *= this.factors.get(i).value;
                    }
                }
            }
            if (denominator == 1) {
                return new MInteger(nominator);
            }
            return new MDivision(new MInteger(nominator), new MInteger(denominator));
        }

        public void switchSign() {
            this.isNegative = !this.isNegative;
        }

        private int getValue() {
            int product = 1;
            for (int i = 0; i < this.factors.size(); i++) {
                int value = this.factors.get(i).value;
                int power = this.factors.get(i).power;
                for (int j = 0; j < power; j++) {
                    product *= value;
                }
            }
            if (this.isNegative) {
                product *= -1;
            }
            return product;
        }

        private void addFactor(Factor f) {
            if (!this.isZero) {
                addFactor(f, this.factors);
            }
        }

        private void addFactors(List<Factor> factors) {
            if (!this.isZero) {
                for (int i = 0; i < factors.size(); i++) {
                    this.addFactor(factors.get(i));
                }
            }
        }

        public void doMultiply(Fraction f) {
            if (f.isZero || this.isZero) {
                this.isZero = true;
            }
            else {
                for (int i = 0; i < f.factors.size(); i++) {
                    int factorToAdd = f.factors.get(i).value;
                    int exponent = f.factors.get(i).power;
                    boolean found = false;
                    for (int j = 0; j < this.factors.size(); j++) {
                        if (this.factors.get(j).value == factorToAdd) {
                            this.factors.get(j).power += exponent;
                            if (this.factors.get(j).power == 0) {
                                this.factors.remove(j);
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        this.factors.add(f.factors.get(i).clone());
                    }
                }
                this.isNegative = this.isNegative != f.isNegative;
            }
        }

        public Fraction getMultiply(Fraction f) {
            Fraction newFraction = this.clone();
            newFraction.doMultiply(f);
            return newFraction;
        }

        public Fraction clone() {
            List<Factor> newFactors = new ArrayList<Factor>(this.factors.size());
            for (int i = 0; i < this.factors.size(); i++) {
                newFactors.add(this.factors.get(i).clone());
            }
            Fraction newFraction = new Fraction();
            newFraction.factors = newFactors;
            newFraction.isNegative = this.isNegative;
            return newFraction;
        }

        private Fraction() {

        }

        public Fraction(int number) {
            if (number == 0) {
                this.isZero = true;
            }
            else {
                if (number < 0) {
                    this.isNegative = true;
                    number *= -1;
                }
                int dividend = 2;
                while (number != 1) {
                    if (number % dividend == 0) {
                        boolean found = false;
                        for (int i = 0; i < this.factors.size(); i++) {
                            if (this.factors.get(i).value == dividend) {
                                this.factors.get(i).power++;
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Factor newFactor = new Factor(dividend, 1);
                            this.factors.add(newFactor);
                        }
                        number /= dividend;
                    }
                    else {
                        if (dividend == 2) {
                            dividend++;
                        }
                        else {
                            dividend += 2;
                        }
                    }
                }
            }
        }

        public static Fraction getFraction(int nominator, int denominator) {
            Fraction nominatorFraction = new Fraction(nominator);
            Fraction denominatorFraction = new Fraction(denominator);
            denominatorFraction.doInverse();
            nominatorFraction.doMultiply(denominatorFraction);
            return nominatorFraction;
        }

        public void doInverse() {
            for (int i = 0; i < this.factors.size(); i++) {
                this.factors.get(i).power *= -1;
            }
        }

        public Fraction getInverse() {
            Fraction newFraction = this.clone();
            newFraction.doInverse();
            return newFraction;
        }
    }

    public static class Factor {
        int power;
        int value;

        public Factor(int value, int power) {
            this.value = value;
            this.power = power;
        }

        public Factor clone() {
            return new Factor(this.value, this.power);
        }

        public Factor getInverse() {
            return new Factor(this.value, -this.power);
        }
    }

    private static final Transformation[] safeTransformations = new Transformation[] { resolveBasicPowers, mergeAdditionAndSubtraction, simplifyMultiplicationAndDivision };

    public static final Transformation safeTransformation = new Transformation() {
        @Override
        public MathExpression transformation(MathExpression expression) {
            expression = findUndefinedSpots.transformation(expression);
            for (int i = 0; i < safeTransformations.length; i++) {
                expression = safeTransformations[i].transformation(expression);
            }
            expression = findUndefinedSpots.transformation(expression);
            return expression;
        }
    };
}
