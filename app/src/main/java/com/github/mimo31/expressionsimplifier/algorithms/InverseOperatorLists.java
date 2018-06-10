package com.github.mimo31.expressionsimplifier.algorithms;

import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MSubtraction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viktor on 1/17/2016.
 */
public class InverseOperatorLists {

    public final List<MathExpression> firstTypeList;
    public final List<MathExpression> secondTypeList;

    public InverseOperatorLists(List<MathExpression> firstTypeList, List<MathExpression> secondTypeList) {
        this.firstTypeList = firstTypeList;
        this.secondTypeList = secondTypeList;
    }

    public static InverseOperatorLists getLists(MFunction fExpression, Class firstOperatorType, Class inverseOperatorType) {
        List<MathExpression> firstList = new ArrayList<MathExpression>();
        List<MathExpression> inverseList = new ArrayList<MathExpression>();
        if (firstOperatorType.isInstance(fExpression)) {
            for (int i = 0; i < fExpression.params.length; i++) {
                if (fExpression.params[i] instanceof MFunction) {
                    InverseOperatorLists paramPlusesMinuses = getLists((MFunction) fExpression.params[i], firstOperatorType, inverseOperatorType);
                    firstList.addAll(paramPlusesMinuses.firstTypeList);
                    inverseList.addAll(paramPlusesMinuses.secondTypeList);
                }
                else {
                    firstList.add(fExpression.params[i]);
                }
            }
        }
        else if (inverseOperatorType.isInstance(fExpression)){
            if (fExpression.params[0] instanceof MFunction) {
                InverseOperatorLists parPlusMinus = getLists((MFunction) fExpression.params[0], firstOperatorType, inverseOperatorType);
                firstList.addAll(parPlusMinus.firstTypeList);
                inverseList.addAll(parPlusMinus.secondTypeList);
            }
            else {
                firstList.add(fExpression.params[0]);
            }
            if (fExpression.params[1] instanceof MFunction) {
                InverseOperatorLists parPlusMinus = getLists((MFunction)fExpression.params[1], firstOperatorType, inverseOperatorType);
                firstList.addAll(parPlusMinus.secondTypeList);
                inverseList.addAll(parPlusMinus.firstTypeList);
            }
            else {
                inverseList.add(fExpression.params[1]);
            }
        }
        else {
            firstList.add(fExpression);
        }
        return new InverseOperatorLists(firstList, inverseList);
    }
}
