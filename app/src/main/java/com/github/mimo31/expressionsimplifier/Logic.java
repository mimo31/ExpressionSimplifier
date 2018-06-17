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
     * @param displayer the displayer to send the results to
     */
    public static void processInput(String input, TextDisplayer displayer)
    {
        // comparison to results from before Remake2
        try
        {
            displayer.display("You would have got " + MathExpression.getMathExpression(input).simplify().toString());
        }
        catch (Exception e)
        {
            displayer.display("Internal error: " + e.getMessage());
        }

        // empty input check
        if (input.length() == 0)
        {
            displayer.display("No input.");
            return;
        }

        // parsing the input
        Object expression = Parsing.tryParse(input, displayer);

        // check for error in parsing
        if (expression == null)
            return;

        displayer.display("Input interpretation:\n" + Printing.print(expression));
    }


}
