package com.github.mimo31.expressionsimplifier;

import com.github.mimo31.expressionsimplifier.expressionStructure.ElemOperation;
import com.github.mimo31.expressionsimplifier.expressionStructure.MInteger;
import com.github.mimo31.expressionsimplifier.expressionStructure.MOperation;
import com.github.mimo31.expressionsimplifier.expressionStructure.MVariable;

import java.math.BigInteger;

public class Printing
{

    public static String print(Object expression)
    {
        return getExpressionString(expression, new ExtraPrintInfo());
    }

    private static String getExpressionString(Object expression, ExtraPrintInfo extraInfoOut)
    {
        Class c = expression.getClass();
        if (c == MInteger.class)
        {
            extraInfoOut.atomic = true;
            extraInfoOut.negated = false;
            extraInfoOut.leftMultipliable = false;
            extraInfoOut.rightMultipliable = true;
            return ((MInteger)expression).value.toString();
        }
        else if (c == MVariable.class)
        {
            extraInfoOut.atomic = true;
            extraInfoOut.negated = false;
            extraInfoOut.leftMultipliable = true;
            extraInfoOut.rightMultipliable = true;
            return ((MVariable)expression).name;
        }
        ElemOperation operation = ((MOperation)expression).type;
        Object[] args = ((MOperation)expression).arguments;
        extraInfoOut.operation = operation;
        if (operation == ElemOperation.ADD)
        {
            ExtraPrintInfo arg1Info = new ExtraPrintInfo();
            ExtraPrintInfo arg2Info = new ExtraPrintInfo();
            String arg1Text = getExpressionString(args[0], arg1Info);
            String arg2Text = getExpressionString(args[1], arg2Info);
            extraInfoOut.atomic = false;
            extraInfoOut.negated = arg1Info.negated;
            extraInfoOut.leftMultipliable = false;
            extraInfoOut.rightMultipliable = false;
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
                extraInfoOut.leftMultipliable = false;
                if (arg2Info.negated || (!arg2Info.atomic && (arg2Info.operation == ElemOperation.ADD || arg2Info.operation == ElemOperation.SUBTRACT)))
                {
                    extraInfoOut.rightMultipliable = true;
                    return "-(" + arg2Text + ")";
                }
                else
                {
                    extraInfoOut.rightMultipliable = arg2Info.rightMultipliable;
                    return "-" + arg2Text;
                }
            }
            extraInfoOut.atomic = false;
            ExtraPrintInfo arg1Info = new ExtraPrintInfo();
            ExtraPrintInfo arg2Info = new ExtraPrintInfo();
            String arg1Text = getExpressionString(args[0], arg1Info);
            String arg2Text = getExpressionString(args[1], arg2Info);
            extraInfoOut.negated = arg1Info.negated;
            extraInfoOut.leftMultipliable = false;
            extraInfoOut.rightMultipliable = false;
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
            extraInfoOut.leftMultipliable = arg1Info.leftMultipliable || bracketsOnArg1;
            extraInfoOut.rightMultipliable = arg2Info.rightMultipliable || bracketsOnArg2;
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
            if ((arg1Info.rightMultipliable || bracketsOnArg1) && (arg2Info.leftMultipliable || bracketsOnArg2))
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
            extraInfoOut.leftMultipliable = arg1Info.leftMultipliable || bracketsOnArg1;
            extraInfoOut.rightMultipliable = false;
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
            extraInfoOut.leftMultipliable = arg1Info.leftMultipliable || bracketsOnArg1;
            extraInfoOut.rightMultipliable = false;
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

    private static class ExtraPrintInfo
    {
        boolean atomic = false;
        ElemOperation operation;
        boolean negated;
        boolean leftMultipliable;
        boolean rightMultipliable;
    }
}
