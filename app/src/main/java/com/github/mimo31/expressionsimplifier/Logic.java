package com.github.mimo31.expressionsimplifier;

import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;

/**
 * Encapsulates the main logical structures for handling entered expressions.
 *
 * Provides methods to detect and analyse the expressions.
 */
public class Logic
{
    /**
     * Carries out all required actions to correctly parse, analyse, and process the entered input.
     *
     * @param input the entered input string
     * @param activity the MainActivity to send the results to
     */
    public static void processInput(String input, MainActivity activity)
    {
        // comparison to results from before Remake2
        try
        {
            activity.pushOutput("You would have got " + MathExpression.getMathExpression(input).simplify().toString());
        }
        catch (Exception e)
        {
            activity.pushOutput("Internal error: " + e.getMessage());
        }

        // empty input check
        if (input.length() == 0)
        {
            activity.pushOutput("No input.");
            return;
        }

        // parsing the input
        Object expression = Parsing.tryParse(input, activity);

        // check for error in parsing
        if (expression == null)
            return;

        activity.pushOutput("Input interpretation:\n" + Printing.print(expression));
    }


}
