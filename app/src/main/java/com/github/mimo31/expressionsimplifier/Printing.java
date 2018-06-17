package com.github.mimo31.expressionsimplifier;

import com.github.mimo31.expressionsimplifier.expressionStructure.ElemOperation;
import com.github.mimo31.expressionsimplifier.expressionStructure.MInteger;
import com.github.mimo31.expressionsimplifier.expressionStructure.MOperation;
import com.github.mimo31.expressionsimplifier.expressionStructure.MVariable;

import java.math.BigInteger;

public class Printing
{

    /**
     * Returns a string constructed by converting the passed mathematical expression into text.
     *
     * @param expression the mathematical expression to be converted into a string
     * @return the constructed string
     */
    public static String print(Object expression)
    {
        return getExpressionString(expression, new ExtraPrintInfo());
    }

    /**
     * Constructs the string for the passed mathematical expression.
     * Also provides addition information about the expression / string.
     *
     * @param expression the mathematical expression to be converted into a string
     * @param extraInfoOut an object to be used as the storage for the additional information about the expression / string
     * @return the constructed string
     */
    private static String getExpressionString(Object expression, ExtraPrintInfo extraInfoOut)
    {
        Class c = expression.getClass();
        if (c == MInteger.class)
        {
            extraInfoOut.atomic = true;
            extraInfoOut.negated = false;
            extraInfoOut.multipliableFromLeft = false;
            extraInfoOut.multipliableFromRight = true;
            return ((MInteger)expression).value.toString();
        }
        else if (c == MVariable.class)
        {
            extraInfoOut.atomic = true;
            extraInfoOut.negated = false;
            extraInfoOut.multipliableFromLeft = true;
            extraInfoOut.multipliableFromRight = true;
            return ((MVariable)expression).name;
        }
        ElemOperation operation = ((MOperation)expression).type;
        Object[] args = ((MOperation)expression).arguments;
        extraInfoOut.operation = operation;

        // in general, the crucial part is to decide whether brackets are necessary around an argument of an operator
        // for example, if the operation to be parsed is addition, brackets around its arguments are never necessary unless the later argument is negated (i.e. starts with a minus sign)
        // another example: the operation to be parsed is power. Brackets around its arguments are always necessary.
        // in other cases, it depends on what kind of operation is used in the argument - that is found out from the ExtraPrintInfo object of the argument

        if (operation == ElemOperation.ADD)
        {
            ExtraPrintInfo arg1Info = new ExtraPrintInfo();
            ExtraPrintInfo arg2Info = new ExtraPrintInfo();
            String arg1Text = getExpressionString(args[0], arg1Info);
            String arg2Text = getExpressionString(args[1], arg2Info);
            extraInfoOut.atomic = false;
            extraInfoOut.negated = arg1Info.negated;
            extraInfoOut.multipliableFromLeft = false;
            extraInfoOut.multipliableFromRight = false;
            if (arg2Info.negated)
            {
                return arg1Text + " + (" + arg2Text + ")";
            }
            else
            {
                return arg1Text + " + " + arg2Text;
            }
        }
        else if (operation == ElemOperation.SUBTRACT)
        {
            if (args[0].getClass() == MInteger.class && ((MInteger)args[0]).value.equals(BigInteger.ZERO))
            {
                extraInfoOut.negated = true;
                ExtraPrintInfo arg2Info = new ExtraPrintInfo();
                String arg2Text = getExpressionString(args[1], arg2Info);
                extraInfoOut.atomic = arg2Info.atomic;
                extraInfoOut.operation = arg2Info.operation;
                extraInfoOut.multipliableFromLeft = false;
                if (arg2Info.negated || (!arg2Info.atomic && (arg2Info.operation == ElemOperation.ADD || arg2Info.operation == ElemOperation.SUBTRACT)))
                {
                    extraInfoOut.multipliableFromRight = true;
                    return "-(" + arg2Text + ")";
                }
                else
                {
                    extraInfoOut.multipliableFromRight = arg2Info.multipliableFromRight;
                    return "-" + arg2Text;
                }
            }
            extraInfoOut.atomic = false;
            ExtraPrintInfo arg1Info = new ExtraPrintInfo();
            ExtraPrintInfo arg2Info = new ExtraPrintInfo();
            String arg1Text = getExpressionString(args[0], arg1Info);
            String arg2Text = getExpressionString(args[1], arg2Info);
            extraInfoOut.negated = arg1Info.negated;
            extraInfoOut.multipliableFromLeft = false;
            extraInfoOut.multipliableFromRight = false;
            if (arg2Info.negated || (!arg2Info.atomic && (arg2Info.operation == ElemOperation.ADD || arg2Info.operation == ElemOperation.SUBTRACT)))
            {
                return arg1Text + " - (" + arg2Text + ")";
            }
            else
            {
                return arg1Text + " - " + arg2Text;
            }
        }
        else if (operation == ElemOperation.MULTIPLY)
        {
            extraInfoOut.atomic = false;
            ExtraPrintInfo arg1Info = new ExtraPrintInfo();
            ExtraPrintInfo arg2Info = new ExtraPrintInfo();
            String arg1Text = getExpressionString(args[0], arg1Info);
            String arg2Text = getExpressionString(args[1], arg2Info);
            boolean bracketsOnArg1 = !arg1Info.atomic && (arg1Info.operation == ElemOperation.ADD || arg1Info.operation == ElemOperation.SUBTRACT);
            boolean bracketsOnArg2 = (!arg2Info.atomic && (arg2Info.operation == ElemOperation.ADD || arg2Info.operation == ElemOperation.SUBTRACT)) || arg2Info.negated;
            extraInfoOut.negated = arg1Info.negated && !bracketsOnArg1;
            extraInfoOut.multipliableFromLeft = arg1Info.multipliableFromLeft || bracketsOnArg1;
            extraInfoOut.multipliableFromRight = arg2Info.multipliableFromRight || bracketsOnArg2;
            String leftPart;
            String middlePart;
            String rightPart;
            if (bracketsOnArg1)
            {
                leftPart = "(" + arg1Text + ")";
            }
            else
            {
                leftPart = arg1Text;
            }
            if ((arg1Info.multipliableFromRight || bracketsOnArg1) && (arg2Info.multipliableFromLeft || bracketsOnArg2))
            {
                middlePart = "";
            }
            else
            {
                middlePart = " * ";
            }
            if (bracketsOnArg2)
            {
                rightPart = "(" + arg2Text + ")";
            }
            else
            {
                rightPart = arg2Text;
            }
            return leftPart + middlePart + rightPart;
        }
        else if (operation == ElemOperation.DIVIDE)
        {
            extraInfoOut.atomic = false;
            ExtraPrintInfo arg1Info = new ExtraPrintInfo();
            ExtraPrintInfo arg2Info = new ExtraPrintInfo();
            String arg1Text = getExpressionString(args[0], arg1Info);
            String arg2Text = getExpressionString(args[1], arg2Info);
            boolean bracketsOnArg1 = !arg1Info.atomic && (arg1Info.operation == ElemOperation.ADD || arg1Info.operation == ElemOperation.SUBTRACT);
            boolean bracketsOnArg2 = (!arg2Info.atomic &&
                    (arg2Info.operation == ElemOperation.ADD || arg2Info.operation == ElemOperation.SUBTRACT || arg2Info.operation == ElemOperation.MULTIPLY || arg2Info.operation == ElemOperation.DIVIDE)
                    ) || arg2Info.negated;
            extraInfoOut.negated = arg1Info.negated && !bracketsOnArg1;
            extraInfoOut.multipliableFromLeft = arg1Info.multipliableFromLeft || bracketsOnArg1;
            extraInfoOut.multipliableFromRight = false;
            String leftPart;
            String rightPart;
            if (bracketsOnArg1)
            {
                leftPart = "(" + arg1Text + ")";
            }
            else
            {
                leftPart = arg1Text;
            }
            if (bracketsOnArg2)
            {
                rightPart = "(" + arg2Text + ")";
            }
            else
            {
                rightPart = arg2Text;
            }
            return leftPart + " / " + rightPart;
        }
        else if (operation == ElemOperation.POWER)
        {
            extraInfoOut.atomic = false;
            ExtraPrintInfo arg1Info = new ExtraPrintInfo();
            ExtraPrintInfo arg2Info = new ExtraPrintInfo();
            String arg1Text = getExpressionString(args[0], arg1Info);
            String arg2Text = getExpressionString(args[1], arg2Info);
            boolean bracketsOnArg1 = !arg1Info.atomic || arg1Info.negated;
            boolean bracketsOnArg2 = !arg2Info.atomic || arg2Info.negated;
            extraInfoOut.negated = false;
            extraInfoOut.multipliableFromLeft = arg1Info.multipliableFromLeft || bracketsOnArg1;
            extraInfoOut.multipliableFromRight = false;
            String leftPart;
            String rightPart;
            if (bracketsOnArg1)
            {
                leftPart = "(" + arg1Text + ")";
            }
            else
            {
                leftPart = arg1Text;
            }
            if (bracketsOnArg2)
            {
                rightPart = "(" + arg2Text + ")";
            }
            else
            {
                rightPart = arg2Text;
            }
            return leftPart + " ^ " + rightPart;
        }
        return null;
    }

    /**
     * Provides addition information about the mathematical expression and the string that has been constructed from that expression.
     */
    private static class ExtraPrintInfo
    {
        /**
         * whether the expression is only a variable or an integer (both possibly negated) meaning that the expression is not an operation applied on simpler expressions
         */
        boolean atomic = false;

        /**
         * the kind of operation this expression is - meaningful only if !atomic
         */
        ElemOperation operation;

        /**
         * whether the constructed string starts with a minus sign
         */
        boolean negated;

        /**
         * whether multiplication of the expression from the left-hand side is possible just by attaching another expression without a multiplication sign
         */
        boolean multipliableFromLeft;

        /**
         * whether multiplication of the expression from the left-hand side is possible just by attaching another expression without a multiplication sign
         */
        boolean multipliableFromRight;
    }
}
