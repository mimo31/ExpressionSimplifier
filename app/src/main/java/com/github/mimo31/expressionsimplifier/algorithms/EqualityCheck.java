package com.github.mimo31.expressionsimplifier.algorithms;

import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MAddition;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MMultiplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viktor on 1/16/2016.
 */
public class EqualityCheck {

    public static boolean sureEqual(MathExpression m1, MathExpression m2) {
        if (m1 instanceof MInteger) {
            if (m2 instanceof MInteger) {
                if (((MInteger) m1).value == ((MInteger) m2).value) {
                    return true;
                }
            }
            return false;
        }
        if (m1 instanceof MVariable) {
            if (m2 instanceof MVariable) {
                if (((MVariable) m1).name == ((MVariable) m2).name) {
                    return true;
                }
            }
            return false;
        }
        if (m1 instanceof MFunction) {
            if (m2 instanceof MFunction) {
                if (m1.getClass().equals(m2.getClass())) {
                    MathExpression[] firstParams = ((MFunction) m1).params;
                    MathExpression[] secondParams = ((MFunction) m2).params;
                    if (firstParams.length == secondParams.length) {
                        boolean inequalityFound = false;
                        for (int i = 0; i < firstParams.length; i++) {
                            if (!sureEqual(firstParams[i], secondParams[i])) {
                                inequalityFound = true;
                                break;
                            }
                        }
                        if (!inequalityFound) {
                            return true;
                        }
                    }
                }
            }
        }
        if ((m1 instanceof MAddition && m2 instanceof MAddition) || (m1 instanceof MMultiplication && m2 instanceof MMultiplication)) {
            MathExpression[] firstParams = ((MFunction) m1).params;
            MathExpression[] secondParams = ((MFunction) m2).params;
            if (firstParams.length == secondParams.length) {
                List<MathExpression> list1 = new ArrayList<MathExpression>(Arrays.asList(firstParams));
                List<MathExpression> list2 = new ArrayList<MathExpression>(Arrays.asList(secondParams));
                boolean unpairedFound = false;
                while (list1.size() != 0) {
                    MathExpression currentExpression = list1.get(0);
                    boolean found = false;
                    for (int i = 0; i < list2.size(); i++) {
                        if (sureEqual(currentExpression, list2.get(i))) {
                            found = true;
                            list1.remove(0);
                            list2.remove(i);
                            break;
                        }
                    }
                    if (!found) {
                        unpairedFound = true;
                        break;
                    }
                }
                if (!unpairedFound) {
                    return true;
                }
            }
        }
        return false;
    }

}
