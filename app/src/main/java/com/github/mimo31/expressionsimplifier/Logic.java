package com.github.mimo31.expressionsimplifier;

import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;

public class Logic
{
    public static void processInput(String input, MainActivity activity)
    {
        try
        {
            activity.pushOutput("You would have got " + MathExpression.getMathExpression(input).simplify().toString());
        }
        catch (Exception e)
        {
            activity.pushOutput("Internal error: " + e.getMessage());
        }
        if (input.length() == 0)
        {
            activity.pushOutput("No input.");
            return;
        }
        Object expression = Parsing.tryParse(input, activity);

        if (expression == null)
            return;

        activity.pushOutput("Input interpretation:\n" + Printing.print(expression));
    }


}
