package com.github.mimo31.expressionsimplifier;

import com.github.mimo31.expressionsimplifier.expressionStructure.ElemOperation;
import com.github.mimo31.expressionsimplifier.expressionStructure.MInteger;
import com.github.mimo31.expressionsimplifier.expressionStructure.MOperation;
import com.github.mimo31.expressionsimplifier.expressionStructure.MVariable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Contains only static methods for parsing user input into a mathematical expression.
 *
 * The only public method is tryParse which just parses the whole string. There are other method which are internal, i.e. private.
 */
public class Parsing
{

    /**
     * Parses the input string to an object of the corresponding mathematical expression.
     *
     * If the input string is malformed, errors are sent through the pushOutput method of the specified MainActivity and this method returns null.
     *
     * @param input the input string to be parsed
     * @param displayer displayer to display potential errors
     * @return the parsed mathematical expression (that is of class MInteger, MOperation, or MVariable) or null when input is malformed
     */
    public static Object tryParse(String input, TextDisplayer displayer)
    {
        ParseToken[] tokens = tokenize(input, displayer);

        if (tokens == null)
            return null;

        if (resolveDecimals(tokens, input, displayer))
            return null;

        if (resolveMixed(tokens, input, displayer))
            return null;

        tokens = condense(tokens);

        if (tokens.length == 0)
        {
            displayer.display("No input.");
            return null;
        }

        if (assignBrackets(tokens, input, displayer))
            return null;

        Object expression = resolveOperators(tokens, 0, tokens.length, input, displayer);

        return expression;
    }

    /**
     * Builds a mathematical object out of a correctly formatted array of tokens in the interval of indices [startIndex, endIndex).
     * Also requires the original input string to potentially send well-annotated errors to the activity.
     *
     * The array of tokens should contain no null entries, no decimal dots, no spaces.
     * Therefore, it can only contain number sequences, already built mathematical expressions, operators (+, -, *, /), and variables.
     * It is not required that there is the multiplication operator between elements which can be multiplied without it. (i.e. { ..., variable x, variable y, ...} is fine)
     *
     * This method is recursive: it calls itself when there are brackets among the tokens.
     *
     * @param tokens the array of tokens, shall not be of length 0
     * @param startIndex the first index of the array of tokens to be dealt with (inclusive)
     * @param endIndex the one after the last index of the array of tokens to be dealt with (i.e. endIndex itself is exclusive)
     * @param input the very original string the user entered
     * @param displayer the displayer to send errors to
     * @return the mathematical object built from the specified subarray of the tokens array or null if the array is malformed
     */
    private static Object resolveOperators(ParseToken[] tokens, int startIndex, int endIndex, String input, TextDisplayer displayer)
    {
        if (startIndex == endIndex)
        {
            // probably an empty bracket

            int originalIndex = tokens[startIndex].originalStartIndex;
            String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(),originalIndex + 4));
            displayer.display("Parsing error.\nIllegal syntax. Empty expression near \"" + usage + "\".");
            return null;
        }

        // whether the whole expression starts with a minus sign
        boolean startingWMinus = false;

        // the number of non operator elements in the specifed interval: 8x + 6 has valuesCount = 3, x^x has valuesCount = 2
        // a top-level bracket counts as one value element: (t - (2 + 5)) - 5 has valuesCount = 2
        int valuesCount = 0;

        if (tokens[startIndex].getClass() == ParseSymbol.class)
        {
            if (((ParseSymbol)tokens[startIndex]).type == ParseSymbolType.SUBTRACT)
            {
                valuesCount++;
                startingWMinus = true;
            }
            else
            {
                // the interval starts with an operator (something that is not a minus sign)

                int originalIndex = tokens[startIndex].originalStartIndex;
                String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(),originalIndex + 4));
                displayer.display("Parsing error.\nIllegal syntax. Expression starting with an operator near \"" + usage + "\".");
                return null;
            }
        }

        if (tokens[endIndex - 1].getClass() == ParseSymbol.class)
        {
            // the interval ends with an operator

            int originalIndex = tokens[endIndex - 1].originalStartIndex;
            String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(),originalIndex + 4));
            displayer.display("Parsing error.\nIllegal syntax. Expression ending with an operator near \"" + usage + "\".");
            return null;
        }

        // calculate the right value for valuesCount and check that the value elements without an operator in between can be multiplied together
        for (int i = startIndex; i < endIndex; i++)
        {
            ParseToken token = tokens[i];

            if (token.getClass() != ParseSymbol.class)
            {
                valuesCount++;

                // if there is not operator between the current and the previous element
                if (i != startIndex && tokens[i - 1].getClass() != ParseSymbol.class)
                {
                    // whether the current element can be multiplied from the left
                    boolean canRightMultiply = token.getClass() == ParseOpenBracket.class || token.multipliableFromLeft;

                    if (!canRightMultiply || !tokens[i - 1].multipliableFromRight)
                    {
                        // the elements cannot be multiplied together

                        int originalIndex = token.originalStartIndex;
                        String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(),originalIndex + 4));
                        displayer.display("Parsing error.\nIllegal syntax. No operator specified near \"" + usage + "\".");
                        return null;
                    }
                }

                // if this is a opening bracket, skip to the closing bracket
                if (token.getClass() == ParseOpenBracket.class)
                {
                    i = ((ParseOpenBracket)token).closingIndex;
                }
            }

            // if both this and the previous token are an operator -- error
            else if (token.getClass() == ParseSymbol.class && i != 0 && tokens[i - 1].getClass() == ParseSymbol.class)
            {
                int originalIndex = token.originalStartIndex;
                String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(),originalIndex + 4));
                displayer.display("Parsing error.\nIllegal syntax. Two successive operators near \"" + usage + "\".");
                return null;
            }
        }

        // the tokens from interval shall be divided into value elements and operator elements

        // holds the value elements of the interval
        Object[] values = new Object[valuesCount];

        // hold the operators between the respective value elements (even if multiplication sign was omitted, it is required in this array)
        ElemOperation[] operations = new ElemOperation[valuesCount - 1];

        // the next index of the values array to be used
        int nextValuesIndex = 0;

        if (startingWMinus)
        {
            // the interval staring with a minus sign is formally interpreted as 0 - ...
            // the 0 is added to the values array here, the minus is then read from the interval as if it were a classical subtract operator
            values[0] = new MInteger(BigInteger.ZERO);
            nextValuesIndex = 1;
        }

        // populating the values and operations arrays (if an operator is missing, multiply is now always inserted
        for (int i = startIndex; i < endIndex; i++)
        {
            ParseToken token = tokens[i];
            if (token.getClass() == ParseSymbol.class)
            {
                ParseSymbolType symType = ((ParseSymbol)token).type;
                ElemOperation operation;
                switch (symType)
                {
                    case ADD:
                        operation = ElemOperation.ADD;
                        break;
                    case SUBTRACT:
                        operation = ElemOperation.SUBTRACT;
                        break;
                    case DIVIDE:
                        operation = ElemOperation.DIVIDE;
                        break;
                    case POWER:
                        operation = ElemOperation.POWER;
                        break;

                    default:
                        operation = ElemOperation.MULTIPLY;
                        break;
                }
                operations[nextValuesIndex - 1] = operation;
            }
            else
            {
                int skipToIndex = -1;
                Object value;
                if (token.getClass() == ParseMExpression.class)
                {
                    value = ((ParseMExpression)token).expression;
                }
                else if (token.getClass() == ParseVariable.class)
                {
                    value = new MVariable(String.valueOf(((ParseVariable)token).name));
                }
                else if (token.getClass() == ParseNumberSequence.class)
                {
                    value = new MInteger(((ParseNumberSequence)token).value);
                }
                else
                {
                    int closingIndex = ((ParseOpenBracket)token).closingIndex;
                    value = resolveOperators(tokens, i + 1, closingIndex, input, displayer);

                    if (value == null)
                        return null;

                    skipToIndex = closingIndex;
                }
                values[nextValuesIndex] = value;
                if (i != startIndex && tokens[i - 1].getClass() != ParseSymbol.class)
                {
                    operations[nextValuesIndex - 1] = ElemOperation.MULTIPLY;
                }
                if (skipToIndex != -1)
                {
                    i = skipToIndex;
                }
                nextValuesIndex++;
            }
        }

        // the operators and values arrays are about to be read
        // new mathmatical expression are about to be built form adjacent elements of the values array and the new slot will be occupied by an Integer object pointing to the index with the result

        // for each index i of the values array, this is the one after index of the last index that should point to that index i
        int[] valueScopeEnd = new int[valuesCount];

        // at first, only the index itself refers to itself
        for (int i = 0; i < valuesCount; i++)
        {
            valueScopeEnd[i] = i + 1;
        }

        // all power operations are found and built
        // in reverse order because x ^ y ^ z = x ^ (y ^ z)
        for (int i = valuesCount - 2; i >= 0; i--)
        {
            if (operations[i] == ElemOperation.POWER)
            {
                Object arg1 = values[i];
                Object arg2 = values[i + 1];
                int realArg1Index;
                int realArg2Index;
                if (arg1.getClass() == Integer.class)
                {
                    realArg1Index = ((Integer)arg1).intValue();
                    arg1 = values[realArg1Index];
                }
                else
                {
                    realArg1Index = i;
                }
                if (arg2.getClass() == Integer.class)
                {
                    realArg2Index = ((Integer)arg2).intValue();
                    arg2 = values[realArg2Index];
                }
                else
                {
                    realArg2Index = i + 1;
                }
                values[realArg1Index] = new MOperation(ElemOperation.POWER, new Object[]{ arg1, arg2 });
                values[valueScopeEnd[realArg2Index] - 1] = new Integer(realArg1Index);
                valueScopeEnd[realArg1Index] = valueScopeEnd[realArg2Index];
            }
        }

        // all multiply and divide operations are found and built
        for (int i = 0; i < valuesCount - 1; i++)
        {
            if (operations[i] == ElemOperation.MULTIPLY || operations[i] == ElemOperation.DIVIDE)
            {
                Object arg1 = values[i];
                Object arg2 = values[i + 1];
                int realArg1Index;
                int realArg2Index;
                if (arg1.getClass() == Integer.class)
                {
                    realArg1Index = ((Integer)arg1).intValue();
                    arg1 = values[realArg1Index];
                }
                else
                {
                    realArg1Index = i;
                }
                if (arg2.getClass() == Integer.class)
                {
                    realArg2Index = ((Integer)arg2).intValue();
                    arg2 = values[realArg2Index];
                }
                else
                {
                    realArg2Index = i + 1;
                }
                values[realArg1Index] = new MOperation(operations[i], new Object[]{ arg1, arg2 });
                values[valueScopeEnd[realArg2Index] - 1] = new Integer(realArg1Index);
                valueScopeEnd[realArg1Index] = valueScopeEnd[realArg2Index];
            }
        }

        // all add and subtract operations are found and built
        for (int i = 0; i < valuesCount - 1; i++)
        {
            if (operations[i] == ElemOperation.ADD || operations[i] == ElemOperation.SUBTRACT)
            {
                Object arg1 = values[i];
                Object arg2 = values[i + 1];
                int realArg1Index;
                int realArg2Index;
                if (arg1.getClass() == Integer.class)
                {
                    realArg1Index = ((Integer)arg1).intValue();
                    arg1 = values[realArg1Index];
                }
                else
                {
                    realArg1Index = i;
                }
                if (arg2.getClass() == Integer.class)
                {
                    realArg2Index = ((Integer)arg2).intValue();
                    arg2 = values[realArg2Index];
                }
                else
                {
                    realArg2Index = i + 1;
                }
                values[realArg1Index] = new MOperation(operations[i], new Object[]{ arg1, arg2 });
                values[valueScopeEnd[realArg2Index] - 1] = new Integer(realArg1Index);
                valueScopeEnd[realArg1Index] = valueScopeEnd[realArg2Index];
            }
        }

        // the final mathematical expression ends up at index 0
        return values[0];
    }

    /**
     * Removes all null entries and space elements from a token array.
     *
     * The array argument remains unchanged, new array is created as the result.
     *
     * @param tokens the array of tokens
     * @return a newly created array of length at most that of the specified array
     */
    private static ParseToken[] condense(ParseToken[] tokens)
    {
        // the number of non-null, non-space elements
        int usefulCount = 0;

        // calculate usefulCount
        for (ParseToken token : tokens)
        {
            if (token != null && (token.getClass() != ParseSymbol.class || ((ParseSymbol)token).type != ParseSymbolType.SPACE))
                usefulCount++;
        }

        // populate the new array
        ParseToken[] newTokens = new ParseToken[usefulCount];
        int nextIndex = 0;
        for (ParseToken token : tokens)
        {
            if (token != null && (token.getClass() != ParseSymbol.class || ((ParseSymbol)token).type != ParseSymbolType.SPACE))
            {
                newTokens[nextIndex] = token;
                nextIndex++;
            }
        }

        return newTokens;
    }

    /**
     * In an array of tokens, replaces tokens in a mixed number structure with a built mathematical expression.
     * The newly freed indices are populated with null.
     *
     * Replaces token sequences "x {}y{}/{}z" where {} is a sequence of only space characters of variable length,
     *  x, y, z are number sequences with a mathematical expression of x + y / z.
     *
     * @param tokens the array of tokens
     * @param input the very original string of input
     * @param displayer the displayer to send error messages to
     * @return whether an error has occurred (true: an error has occurred, false: no error occurred)
     */
    private static boolean resolveMixed(ParseToken[] tokens, String input, TextDisplayer displayer)
    {
        // the index of a number sequence that is from the current index separated only by space elements
        // -1 if there is no such index
        // potentially corresponds to the whole part of the mixed number
        int lastNumSpacesIndex = -1;

        // iterate through the array of tokens
        // if there are two number sequences separated only by spaces, it is either a malformed input or a beginning of a mixed number
        // so if that happens, it is check whether {}/{}z follows
        for (int i = 0; i < tokens.length - 2; i++)
        {
            ParseToken token = tokens[i];

            if (token == null)
                continue;

            if (lastNumSpacesIndex != -1)
            {
                if (token.getClass() == ParseSymbol.class && ((ParseSymbol)token).type == ParseSymbolType.SPACE)
                {
                    continue;
                }
                else if (token.getClass() == ParseNumberSequence.class)
                {
                    // the index of a division sign which is separated form the current index only by spaces
                    // -1 if no such exists
                    int divSignIndex = -1;

                    for (int j = i + 1; j < tokens.length; j++)
                    {
                        ParseToken token2 = tokens[j];
                        if (token2 == null || (token2.getClass() == ParseSymbol.class && ((ParseSymbol)token2).type == ParseSymbolType.SPACE))
                            continue;

                        if (token2.getClass() == ParseSymbol.class && ((ParseSymbol)token2).type == ParseSymbolType.DIVIDE)
                        {
                            divSignIndex = j;
                        }

                        break;
                    }

                    // the index of the last number sequence in the mixed number format
                    // a number sequence separated from the division sign only by spaces
                    // -1 if no such number sequence exists
                    int divArg2Index = -1;

                    if (divSignIndex != -1)
                    {
                        for (int j = divSignIndex + 1; j < tokens.length; j++)
                        {
                            ParseToken token2 = tokens[j];
                            if (token2 == null || (token2.getClass() == ParseSymbol.class && ((ParseSymbol)token2).type == ParseSymbolType.SPACE))
                                continue;

                            if (token2.getClass() == ParseNumberSequence.class)
                            {
                                divArg2Index = j;
                            }

                            break;
                        }
                    }

                    if (divSignIndex == -1 || divArg2Index == -1)
                    {
                        int originalIndex = token.originalEndIndex;
                        String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(),originalIndex + 4));
                        displayer.display("Parsing error.\nIllegal syntax. Two decimal sequences without an operator or another form used near \"" + usage + "\".");
                        return true;
                    }

                    // build the mathematical expression for the mixed number
                    MInteger divArg1 = new MInteger(((ParseNumberSequence)token).value);
                    MInteger divArg2 = new MInteger(((ParseNumberSequence)tokens[divArg2Index]).value);
                    MOperation division = new MOperation(ElemOperation.DIVIDE, new Object[]{ divArg1, divArg2 });
                    MInteger addArg1 = new MInteger(((ParseNumberSequence)tokens[lastNumSpacesIndex]).value);
                    MOperation addition = new MOperation(ElemOperation.ADD, new Object[]{ addArg1, division });
                    ParseMExpression parseAddition = new ParseMExpression();
                    parseAddition.expression = addition;
                    parseAddition.originalStartIndex = tokens[lastNumSpacesIndex].originalStartIndex;
                    parseAddition.originalEndIndex = tokens[divArg2Index].originalEndIndex;
                    parseAddition.multipliableFromLeft = false;
                    parseAddition.multipliableFromRight = false;

                    tokens[lastNumSpacesIndex] = parseAddition;

                    // populate the indices formerly occupied by part of the mixed number with null entries
                    for (int j = lastNumSpacesIndex + 1; j <= divArg2Index; j++)
                    {
                        tokens[j] = null;
                    }

                    // skip to the end of the mixed number
                    i = divArg2Index;
                }
                else
                {
                    lastNumSpacesIndex = -1;
                }
            }
            else
            {
                if (token.getClass() == ParseNumberSequence.class)
                {
                    lastNumSpacesIndex = i;
                }
            }
        }

        return false;
    }

    /**
     * In an array of tokens, replaces tokens in a decimal number structure with a built mathematical expression.
     * The newly freed indices are populated with null.
     *
     * Replaces token sequences "x{}.{}y" where {} is a sequence of only space characters of variable length and
     *  x, y are number sequences with a mathematical expression of x + y / 10 ^ (character length of y).
     * Also replaces ".{}y" with y / 10 ^ (character length of y) if there is no preceding number sequence.
     *
     * @param tokens the array of tokens
     * @param input the very original string of input
     * @param displayer the displayer to send error messages to
     * @return whether an error has occurred (true: an error has occurred, false: no error occurred)
     */
    private static boolean resolveDecimals(ParseToken[] tokens, String input, TextDisplayer displayer)
    {
        // simply check for a decimal dot token
        for (int i = 0; i < tokens.length; i++)
        {
            ParseToken token = tokens[i];

            if (token == null)
                continue;

            // decimal dot check
            if (token.getClass() == ParseSymbol.class && ((ParseSymbol)token).type == ParseSymbolType.DECIMAL_DOT)
            {
                // the index of a number sequence which follows the decimal dot and is separated from it only by spaces
                // -1 if no such exists
                int numbersFollowingIndex = -1;

                for (int j = i + 1; j < tokens.length; j++)
                {
                    if (tokens[j] == null)
                        break;

                    if (tokens[j].getClass() == ParseSymbol.class && ((ParseSymbol)token).type == ParseSymbolType.SPACE)
                    {
                        continue;
                    }
                    if (tokens[j].getClass() == ParseNumberSequence.class)
                        numbersFollowingIndex = j;

                    break;
                }

                if (numbersFollowingIndex == -1)
                {
                    int originalIndex = token.originalStartIndex;
                    String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(),originalIndex + 4));
                    displayer.display("Parsing error.\nDecimal dot used in \"" + usage + "\" must be followed by a decimal sequence.");
                    return true;
                }

                // the number sequence representing the y in the decimal format (the number sequence after the decimal dot)
                ParseNumberSequence followNumberSequence = (ParseNumberSequence)tokens[numbersFollowingIndex];

                // the index of a number sequence which precedes the decimal dot and is separated from it only by spaces
                // -1 if no such exists
                int numbersPrecedingIndex = -1;

                for (int j = i - 1; j >= 0; j--)
                {
                    if (tokens[j] == null)
                        break;

                    if (tokens[j].getClass() == ParseSymbol.class && ((ParseSymbol)token).type == ParseSymbolType.SPACE)
                    {
                        continue;
                    }
                    if (tokens[j].getClass() == ParseNumberSequence.class)
                    {
                        numbersPrecedingIndex = j;
                        break;
                    }
                }

                // mathematical expressions for the part after the decimal for & sweeping the according indices with null entries
                MInteger arg1 = new MInteger(followNumberSequence.value);
                MInteger arg2 = new MInteger(BigInteger.TEN.pow(followNumberSequence.charLength));
                MOperation division = new MOperation(ElemOperation.DIVIDE, new Object[]{arg1, arg2});
                for (int j = i + 1; j <= numbersFollowingIndex; j++)
                {
                    tokens[j] = null;
                }

                // building and inserting the complete mathematical expressions
                if (numbersPrecedingIndex == -1)
                {
                    ParseMExpression parseDivision = new ParseMExpression();
                    parseDivision.expression = division;
                    parseDivision.multipliableFromLeft = false;
                    parseDivision.multipliableFromRight = true;
                    parseDivision.originalStartIndex = token.originalStartIndex;
                    parseDivision.originalEndIndex = followNumberSequence.originalEndIndex;
                    tokens[i] = parseDivision;
                }
                else
                {
                    ParseNumberSequence precedeNumberSequence = (ParseNumberSequence)tokens[numbersPrecedingIndex];
                    MInteger intarg = new MInteger(precedeNumberSequence.value);
                    MOperation addition = new MOperation(ElemOperation.ADD, new Object[]{intarg, division});
                    ParseMExpression parseAddition = new ParseMExpression();
                    parseAddition.expression = addition;
                    parseAddition.multipliableFromLeft = false;
                    parseAddition.multipliableFromRight = true;
                    parseAddition.originalStartIndex = precedeNumberSequence.originalStartIndex;
                    parseAddition.originalEndIndex = followNumberSequence.originalEndIndex;
                    tokens[numbersPrecedingIndex] = parseAddition;
                    for (int j = numbersPrecedingIndex + 1; j <= i; j++)
                    {
                        tokens[j] = null;
                    }
                }

                i = numbersFollowingIndex;
            }
        }

        return false;
    }

    /**
     * Pairs up brackets in an array of tokens. That is, assigns an index of the closing bracket to every opening bracket and vice versa.
     *
     * Uses the openingIndex and closingIndex properties of the ParseOpenBracket and ParseCloseBracket classes.
     *
     * Runs is O(n), uses a stack data structure.
     *
     * @param tokens the array of tokens
     * @param input the very original string of input
     * @param displayer the displayer to send error messages to
     * @return whether an error has occurred (true: an error has occurred, false: no error occurred)
     */
    private static boolean assignBrackets(ParseToken[] tokens, String input, TextDisplayer displayer)
    {
        // indexes of opening brackets that have been encountered and are prepared to be assigned to a closing bracket
        Stack<Integer> openBrackIndexes = new Stack<Integer>();

        // assign the indices to the brackets
        for (int i = 0; i < tokens.length; i++)
        {
            if (tokens[i].getClass() == ParseOpenBracket.class)
            {
                // new opening bracket added to the stack

                openBrackIndexes.add(new Integer(i));
            }
            else if (tokens[i].getClass() == ParseCloseBracket.class)
            {
                // new closing bracket to be paired up with the opening bracket at the top of the stack

                if (openBrackIndexes.size() == 0)
                {
                    // no opening brackets in the stack

                    int originalIndex = tokens[i].originalStartIndex;
                    String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(), originalIndex + 4));
                    displayer.display("Parsing error.\nExtra closing bracket used in \"" + usage + "\".");
                    return true;
                }

                // assign the brackets

                int openBrackIndex = openBrackIndexes.pop().intValue();
                ParseOpenBracket openBracket = (ParseOpenBracket)tokens[openBrackIndex];
                openBracket.closingIndex = i;

                ParseCloseBracket closeBracket = (ParseCloseBracket)tokens[i];
                closeBracket.openingIndex = openBrackIndex;
            }
        }

        if (openBrackIndexes.size() != 0)
        {
            // some extra opening brackets remaining - error

            int firstBrackIndex = tokens[openBrackIndexes.peek().intValue()].originalStartIndex;
            String usage = input.substring(Math.max(0, firstBrackIndex - 3), Math.min(input.length(), firstBrackIndex + 4));
            String message;

            // decide whether to use singular of plural
            if (openBrackIndexes.size() == 1)
            {
                message = "Parsing error.\n 1 extra closing bracket used in \"" + usage + "\".";
            }
            else
            {
                message = "Parsing error.\n " + openBrackIndexes.size() + " extra closing brackets. One of them used in \"" + usage + "\".";
            }

            displayer.display(message);

            return true;
        }

        return false;
    }

    /**
     * Translates the input string into an array of tokens (see the ParseToken class and its subclasses).
     *
     * every alphabetical character translates to a variable token
     * a sequence of digits translates to a number sequence
     * brackets, spaces, operators +, -, /, ^, decimal dots translate to the respective tokens character by character
     * ** translates to the power (^) token
     * * not preceded or followed by another * translates to the multiply token
     *
     * @param input the input string to be parsed
     * @param displayer the displayer to send error messages to
     * @return the parsed array of tokens or null if input is malformed
     */
    private static ParseToken[] tokenize(String input, TextDisplayer displayer)
    {
        // the list of already parse tokens
        ArrayList<ParseToken> tokens = new ArrayList<ParseToken>();

        // whether a sequence of digits was being read up to to the current index
        boolean numberSequence = false;

        // the first index of the sequence of digits, -1 if none was being read
        int sequenceStart = -1;

        // whether the last character was a '*'
        boolean lastCharStar = false;

        for (int i = 0; i < input.length(); i++)
        {
            char curchar = input.charAt(i);

            if (Character.isDigit(curchar))
            {
                if (numberSequence)
                {
                    continue;
                }
                else
                {
                    if (lastCharStar)
                    {
                        tokens.add(new ParseSymbol(ParseSymbolType.MULTIPLY, i));
                        lastCharStar = false;
                    }
                    numberSequence = true;
                    sequenceStart = i;
                    continue;
                }
            }
            if (numberSequence)
            {
                BigInteger value = new BigInteger(input.substring(sequenceStart, i));
                ParseNumberSequence obj = new ParseNumberSequence(value, i - sequenceStart);
                obj.originalStartIndex = sequenceStart;
                obj.originalEndIndex = i;
                tokens.add(obj);
                numberSequence = false;
                sequenceStart = -1;
            }
            if (curchar == '*')
            {
                if (lastCharStar)
                {
                    tokens.add(new ParseSymbol(ParseSymbolType.POWER, i - 1, i + 1));
                }
                lastCharStar = !lastCharStar;
                continue;
            }
            else if (lastCharStar)
            {
                tokens.add(new ParseSymbol(ParseSymbolType.MULTIPLY, i - 1));
                lastCharStar = false;
            }
            switch (curchar)
            {
                case '(':
                    tokens.add(new ParseOpenBracket());
                    break;
                case ')':
                    tokens.add(new ParseCloseBracket());
                    break;
                case '+':
                    tokens.add(new ParseSymbol(ParseSymbolType.ADD, i));
                    break;
                case '-':
                    tokens.add(new ParseSymbol(ParseSymbolType.SUBTRACT, i));
                    break;
                case '/':
                    tokens.add(new ParseSymbol(ParseSymbolType.DIVIDE, i));
                    break;
                case '^':
                    tokens.add(new ParseSymbol(ParseSymbolType.POWER, i));
                    break;
                case ' ':
                    tokens.add(new ParseSymbol(ParseSymbolType.SPACE, i));
                    break;
                case '.':
                    tokens.add(new ParseSymbol(ParseSymbolType.DECIMAL_DOT, i));
                    break;

                default:
                    if (('a' <= curchar && curchar <= 'z') || ('A' <= curchar && curchar <= 'Z'))
                    {
                        tokens.add(new ParseVariable(curchar, i));
                        break;
                    }
                    else
                    {
                        // an unknown character has been encountered

                        String usage = input.substring(Math.max(0, i - 3), Math.min(input.length(), i + 4));
                        displayer.display("Parsing error.\nCharater \"" + curchar + "\" used in \"" + usage + "\" is illegal.");
                        return null;
                    }
            }
        }

        // number sequence was at the end of the input string - parse it and add it to the list
        if (numberSequence)
        {
            BigInteger value = new BigInteger(input.substring(sequenceStart, input.length()));
            ParseNumberSequence obj = new ParseNumberSequence(value, input.length() - sequenceStart);
            obj.originalStartIndex = sequenceStart;
            obj.originalEndIndex = input.length();
            tokens.add(obj);
        }

        // the input string ends with a '*' character, add it to the list
        if (lastCharStar)
        {
            tokens.add(new ParseSymbol(ParseSymbolType.MULTIPLY, input.length() - 1));
        }

        // convert to array and return
        ParseToken[] newTokens = new ParseToken[tokens.size()];
        tokens.toArray(newTokens);
        return newTokens;
    }

    /**
     * A universal superclass for a token parsed from the input string.
     * Should be only extended by ParseOpenBracket, ParseCloseBracket, ParseSymbol, ParseNumberSequence, ParseVariable, or ParseMExpression.
     */
    private static abstract class ParseToken
    {
        /**
         * the first index in the very original input string that was occupied by this token
         */
        int originalStartIndex = 0;

        /**
         * the one after the last index in the very original input string that was occupied by this token (this index itself is therefore exclusive)
         */
        int originalEndIndex = 0;

        /**
         * whether this token can be multiplied from the left-hand side by omitting the multiplication sign
         */
        boolean multipliableFromLeft = false;

        /**
         * whether this token can be multiplied from the right-hand side by omitting the multiplication sign
         */
        boolean multipliableFromRight = false;
    }

    /**
     * A token describing the presence of an opening bracket ('(').
     */
    private static class ParseOpenBracket extends ParseToken
    {
        /**
         * the index of the corresponding closing bracket token in an array of tokens
         */
        int closingIndex = -1;
    }

    /**
     * A token describing the presence of a closing bracket (')').
     */
    private static class ParseCloseBracket extends ParseToken
    {
        /**
         * the index of the corresponding opening bracket token in an array of tokens
         */
        int openingIndex = -1;
    }

    /**
     * A type of a (typically) one character token.
     */
    private enum ParseSymbolType
    {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, SPACE, DECIMAL_DOT
    }

    /**
     * A token describing the presence of a short symbol. (usually just one character long)
     */
    private static class ParseSymbol extends ParseToken
    {
        /**
         * describes what symbol was used
         */
        ParseSymbolType type;

        ParseSymbol(ParseSymbolType type, int index)
        {
            this.type = type;
            super.originalStartIndex = index;
            super.originalEndIndex = index + 1;
        }

        ParseSymbol(ParseSymbolType type, int startIndex, int endIndex)
        {
            this.type = type;
            this.originalStartIndex = startIndex;
            this.originalEndIndex = endIndex;
        }
    }

    /**
     * A token describing the presence of a number sequence.
     */
    private static class ParseNumberSequence extends ParseToken
    {
        /**
         * the exact value of the sequence
         */
        BigInteger value;

        /**
         * the number of characters of the original sequence
         * important when constructing decimal numbers -- .0002 is not the same as .2
         */
        int charLength;

        ParseNumberSequence(BigInteger value, int charLength)
        {
            this.value = value;
            this.charLength = charLength;
            super.multipliableFromLeft = false;
            super.multipliableFromRight = true;
        }
    }

    /**
     * A token describing the presence of a mathematical variable.
     */
    private static class ParseVariable extends ParseToken
    {
        /**
         * the name of the variable based on what character the user entered
         */
        char name;

        ParseVariable(char name, int index)
        {
            this.name = name;
            super.originalStartIndex = index;
            super.originalEndIndex = index + 1;
            super.multipliableFromLeft = true;
            super.multipliableFromRight = true;
        }
    }

    /**
     * A token describing the presence of a mathematical expression which has already been parsed from simpler tokens.
     */
    private static class ParseMExpression extends ParseToken
    {
        /**
         * the parsed mathematical expression
         */
        Object expression;
    }
}
