package com.github.mimo31.expressionsimplifier;

import com.github.mimo31.expressionsimplifier.algorithms.MathExpression;
import com.github.mimo31.expressionsimplifier.expressionStructure.ElemOperation;
import com.github.mimo31.expressionsimplifier.expressionStructure.MInteger;
import com.github.mimo31.expressionsimplifier.expressionStructure.MOperation;
import com.github.mimo31.expressionsimplifier.expressionStructure.MVariable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Stack;

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
        Object expression = tryParse(input, activity);
    }



    public static Object tryParse(String input, MainActivity activity)
    {
        ParseToken[] tokens = tokenize(input, activity);

        if (tokens == null)
            return null;

        if (resolveDecimals(tokens, input, activity))
            return null;

        if (resolveMixed(tokens, input, activity))
            return null;

        tokens = condense(tokens);

        if (assignBrackets(tokens, input, activity))
            return null;

        Object expression = resolveOperators(tokens, 0, tokens.length, input, activity);

        return expression;
    }

    private static Object resolveOperators(ParseToken[] tokens, int startIndex, int endIndex, String input, MainActivity activity)
    {
        boolean startingWMinus = false;
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
                int originalIndex = tokens[startIndex].originalStartIndex;
                String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(),originalIndex + 4));
                activity.pushOutput("Parsing error.\nIllegal syntax. Expression starting with an operator near \"" + usage + "\".");
                return null;
            }
        }
        if (tokens[endIndex - 1].getClass() == ParseSymbol.class)
        {
            int originalIndex = tokens[endIndex - 1].originalStartIndex;
            String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(),originalIndex + 4));
            activity.pushOutput("Parsing error.\nIllegal syntax. Expression ending with an operator near \"" + usage + "\".");
            return null;
        }
        for (int i = startIndex; i < endIndex; i++)
        {
            ParseToken token = tokens[i];
            if (token.getClass() != ParseSymbol.class)
            {
                valuesCount++;
                if (i != startIndex && tokens[i - 1].getClass() != ParseSymbol.class)
                {
                    boolean canRightMultiply;
                    if (token.getClass() == ParseOpenBracket.class)
                    {
                        canRightMultiply = true;
                    }
                    else
                    {
                        canRightMultiply = token.multipliableFromLeft;
                    }
                    if (!canRightMultiply || !tokens[i - 1].multipliableFromRight)
                    {
                        int originalIndex = token.originalStartIndex;
                        String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(),originalIndex + 4));
                        activity.pushOutput("Parsing error.\nIllegal syntax. No operator specified near \"" + usage + "\".");
                        return null;
                    }
                }
                if (token.getClass() == ParseOpenBracket.class)
                {
                    i = ((ParseOpenBracket)token).closingIndex;
                }
            }
            if (token.getClass() == ParseSymbol.class && i != 0 && tokens[i - 1].getClass() == ParseSymbol.class)
            {
                int originalIndex = token.originalStartIndex;
                String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(),originalIndex + 4));
                activity.pushOutput("Parsing error.\nIllegal syntax. Two successive operators near \"" + usage + "\".");
                return null;
            }
        }
        Object[] values = new Object[valuesCount];
        ElemOperation[] operations = new ElemOperation[valuesCount];
        int nextValuesIndex = 0;
        if (startingWMinus)
        {
            values[0] = new MInteger(BigInteger.ZERO);
        }
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
                    value = resolveOperators(tokens, i + 1, closingIndex, input, activity);
                    skipToIndex = closingIndex;
                }
                values[nextValuesIndex] = value;
                if (i != startIndex && tokens[i - 1].getClass() != ParseSymbol.class)
                {
                    operations[nextValuesIndex] = ElemOperation.MULTIPLY;
                }
                if (skipToIndex != -1)
                {
                    i = skipToIndex;
                }
                nextValuesIndex++;
            }
        }
        for (int i = valuesCount - 2; i >= 0; i--)
        {
            if (operations[i] == ElemOperation.POWER)
            {
                Object arg1 = values[i];
                Object arg2 = values[i + 1];
                int realArg1Index;
                if (arg1.getClass() == Integer.class)
                {
                    realArg1Index = ((Integer)arg1).intValue();
                    arg1 = values[((Integer)arg1).intValue()];
                }
                else
                {
                    realArg1Index = i;
                }
                values[realArg1Index] = new MOperation(ElemOperation.POWER, new Object[]{ arg1, arg2 });
                values[i + 1] = new Integer(realArg1Index);
                // to be continued here: implement MExpression building (power, mult & div, add & subt) by condensing the values array and placing pointers in it
            }
        }
        for (int i = 0; i < valuesCount - 1; i++)
        {
            if (operations[i] == ElemOperation.MULTIPLY || operations[i] == ElemOperation.DIVIDE)
            {
                Object arg1 = values[i];
                Object arg2 = values[i + 1];
                int realArg1Index;
                if (arg1.getClass() == Integer.class)
                {
                    realArg1Index = ((Integer)arg1).intValue();
                    arg1 = values[((Integer)arg1).intValue()];
                }
                else
                {
                    realArg1Index = i;
                }
                values[realArg1Index] = new MOperation(operations[i], new Object[]{ arg1, arg2 });
                values[i + 1] = new Integer(realArg1Index);
            }
        }
        for (int i = 0; i < valuesCount - 1; i++)
        {
            if (operations[i] == ElemOperation.ADD || operations[i] == ElemOperation.SUBTRACT)
            {
                Object arg1 = values[i];
                Object arg2 = values[i + 1];
                int realArg1Index;
                if (arg1.getClass() == Integer.class)
                {
                    realArg1Index = ((Integer)arg1).intValue();
                    arg1 = values[((Integer)arg1).intValue()];
                }
                else
                {
                    realArg1Index = i;
                }
                values[realArg1Index] = new MOperation(operations[i], new Object[]{ arg1, arg2 });
                values[i + 1] = new Integer(realArg1Index);
            }
        }
        return values[0];
    }

    private static ParseToken[] condense(ParseToken[] tokens)
    {
        int usefulCount = 0;
        for (ParseToken token : tokens)
        {
            if (token != null && (token.getClass() != ParseSymbol.class || ((ParseSymbol)token).type != ParseSymbolType.SPACE))
                usefulCount++;
        }
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

    private static boolean resolveMixed(ParseToken[] tokens, String input, MainActivity activity)
    {

        int lastNumSpacesIndex = -1;
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
                        activity.pushOutput("Parsing error.\nIllegal syntax. Two decimal sequences without an operator or another form used near \"" + usage + "\".");
                        return true;
                    }
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
                    tokens[lastNumSpacesIndex] = new ParseMExpression();
                    for (int j = lastNumSpacesIndex + 1; j <= divArg2Index; j++)
                    {
                        tokens[j] = null;
                    }
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

    private static boolean resolveDecimals(ParseToken[] tokens, String input, MainActivity activity)
    {
        for (int i = 0; i < tokens.length; i++)
        {
            ParseToken token = tokens[i];

            if (token == null)
                continue;

            if (token.getClass() == ParseSymbol.class && ((ParseSymbol)token).type == ParseSymbolType.DECIMAL_DOT)
            {
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
                    activity.pushOutput("Parsing error.\nDecimal dot used in \"" + usage + "\" must be followed by a decimal sequence.");
                    return true;
                }

                ParseNumberSequence followNumberSequence = (ParseNumberSequence)tokens[numbersFollowingIndex];

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
                        numbersPrecedingIndex = j;
                }

                MInteger arg1 = new MInteger(followNumberSequence.value);
                MInteger arg2 = new MInteger(BigInteger.TEN.pow(followNumberSequence.charLength));
                MOperation division = new MOperation(ElemOperation.DIVIDE, new Object[]{arg1, arg2});
                for (int j = i + 1; j <= numbersFollowingIndex; j++)
                {
                    tokens[j] = null;
                }

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

    private static boolean assignBrackets(ParseToken[] tokens, String input, MainActivity activity)
    {
        Stack<Integer> openBrackIndexes = new Stack<Integer>();

        for (int i = 0; i < tokens.length; i++)
        {
            if (tokens[i].getClass() == ParseOpenBracket.class)
            {
                openBrackIndexes.add(new Integer(i));
            }
            else if (tokens[i].getClass() == ParseCloseBracket.class)
            {
                if (openBrackIndexes.size() == 0)
                {
                    int originalIndex = tokens[i].originalStartIndex;
                    String usage = input.substring(Math.max(0, originalIndex - 3), Math.min(input.length(), originalIndex + 4));
                    activity.pushOutput("Parsing error.\nExtra closing bracket used in \"" + usage + "\".");
                    return true;
                }

                int openBrackIndex = openBrackIndexes.pop().intValue();
                ParseOpenBracket openBracket = (ParseOpenBracket)tokens[openBrackIndex];
                openBracket.closingIndex = i;

                ParseCloseBracket closeBracket = (ParseCloseBracket)tokens[i];
                closeBracket.openingIndex = openBrackIndex;
            }
        }

        if (openBrackIndexes.size() != 0)
        {
            int firstBrackIndex = tokens[openBrackIndexes.peek().intValue()].originalStartIndex;
            String usage = input.substring(Math.max(0, firstBrackIndex - 3), Math.min(input.length(), firstBrackIndex + 4));
            String message;
            if (openBrackIndexes.size() == 1)
            {
                message = "Parsing error.\n 1 extra closing bracket used in \"" + usage + "\".";
            }
            else
            {
                message = "Parsing error.\n " + openBrackIndexes.size() + " extra closing brackets. One of them used in \"" + usage + "\".";
            }
            activity.pushOutput(message);
            return true;
        }
        return false;
    }

    private static ParseToken[] tokenize(String input, MainActivity activity)
    {
        ArrayList<ParseToken> tokens = new ArrayList<ParseToken>();
        boolean numberSequence = false;
        int sequenceStart = -1;
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
                    if (('a' <= curchar && curchar <= 'z') || ('A' <= curchar && 'Z' <= curchar))
                    {
                        tokens.add(new ParseVariable(curchar, i));
                        break;
                    }
                    else
                    {
                        String usage = input.substring(Math.max(0, i - 3), Math.min(input.length(), i + 4));
                        activity.pushOutput("Parsing error.\nCharater \"" + curchar + "\" used in \"" + usage + "\" is illegal.");
                        return null;
                    }
            }
        }
        if (numberSequence)
        {
            BigInteger value = new BigInteger(input.substring(sequenceStart, input.length()));
            ParseNumberSequence obj = new ParseNumberSequence(value, input.length() - sequenceStart);
            obj.originalStartIndex = sequenceStart;
            obj.originalEndIndex = input.length();
            tokens.add(obj);
        }
        if (lastCharStar)
        {
            tokens.add(new ParseSymbol(ParseSymbolType.MULTIPLY, input.length() - 1));
        }
        ParseToken[] newTokens = new ParseToken[tokens.size()];
        tokens.toArray(newTokens);
        return newTokens;
    }

    private static abstract class ParseToken
    {
        int originalStartIndex = 0;
        int originalEndIndex = 0;
        boolean multipliableFromLeft = false;
        boolean multipliableFromRight = false;
    }

    private static class ParseOpenBracket extends ParseToken
    {
        int closingIndex = -1;
    }

    private static class ParseCloseBracket extends ParseToken
    {
        int openingIndex = -1;
    }

    private enum ParseSymbolType
    {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, SPACE, DECIMAL_DOT
    }

    private static class ParseSymbol extends ParseToken
    {
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

    private static class ParseNumberSequence extends ParseToken
    {
        BigInteger value;
        int charLength;

        ParseNumberSequence(BigInteger value, int charLength)
        {
            this.value = value;
            this.charLength = charLength;
            super.multipliableFromLeft = false;
            super.multipliableFromRight = true;
        }
    }

    private static class ParseVariable extends ParseToken
    {
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

    private static class ParseMExpression extends ParseToken
    {
        Object expression;
    }
}
