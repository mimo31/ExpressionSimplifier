package com.github.mimo31.expressionsimplifier.algorithms;

import android.nfc.FormatException;

import java.util.ArrayList;
import java.util.List;

import com.github.mimo31.expressionsimplifier.algorithms.MFunction.FunctionType;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MAddition;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MDivision;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MMultiplication;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MPower;
import com.github.mimo31.expressionsimplifier.algorithms.mfunctions.MSubtraction;

/**
 * Created by Viktor on 12/20/2015.
 */
public abstract class MathExpression {

    public static final char[] alphabet = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public MathExpression simplify() {
        return Transformations.safeTransformation.transformation(this);
    }

    protected List<MathExpression> getFactors() {
        List<MathExpression> factors = new ArrayList<MathExpression>();
        if (this instanceof MMultiplication) {
            MMultiplication function = (MMultiplication) this;
            for (int i = 0; i < function.params.length; i++) {
                factors.addAll(function.params[i].getFactors());
            }
        }
        else {
            factors.add(this);
        }
        return factors;
    }

    public static MathExpression getMathExpression(String text) throws FormatException{
        //Null check
        if (text == null) {
            throw new FormatException("The text is null.");
        }

        //Format the text
        text = text.trim().replace(" ", "").replace(System.getProperty("line.separator"), "").replace("**", "^");

        //Empty check
        if (text.length() == 0) {
            throw new FormatException("There is no expression.");
        }

        //Char check
        for (int i = 0; i < text.length(); i++) {
            if (!isCharValid(text.charAt(i))) {
                throw new FormatException("An unexpected character \"" + text.charAt(i) + "\".");
            }
        }

        //Brackets check
        int bracketsOpened = 0;
        for (int i = 0; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            if (currentChar == '(') {
                bracketsOpened++;
            }
            else if (currentChar == ')') {
                bracketsOpened--;
            }
            if (bracketsOpened < 0) {
                throw new FormatException("There is an unexpected closing of a bracket.");
            }
        }
        if (bracketsOpened > 0) {
            throw new FormatException("There are some not closed brackets.");
        }

        //First char check
        char firstChar = text.charAt(0);
        if (firstChar == '+' || firstChar == '*' || firstChar == '/' || firstChar == '^') {
            throw new FormatException("There is " + Character.toString(firstChar) + " at the beginning of the expression.");
        }

        //Last char check
        char lastChar = text.charAt(text.length() - 1);
        if (lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/' || lastChar == '^' || lastChar == '.') {
            throw new FormatException("There is " + Character.toString(lastChar) + " at the end of the expression.");
        }

        //Before operator check
        for (int i = 1; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            char charBefore = text.charAt(i - 1);
            if ((currentChar == '+' || currentChar == '-' || currentChar == '*' || currentChar == '/' || currentChar == '^') && !isAlphabetic(charBefore) && !Character.isDigit(charBefore) && charBefore != ')') {
                throw new FormatException("There is a " + charBefore + " before a " + currentChar + " sign.");
            }
        }

        //Decimal dots check
        int lastDot = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '.') {
                if (lastDot != -1) {
                    String textBetween = text.substring(lastDot + 1, i);
                    boolean nonDigitFound = false;
                    for (int j = 0; j < textBetween.length(); j++) {
                        if (!Character.isDigit(textBetween.charAt(j))) {
                            nonDigitFound = true;
                            break;
                        }
                    }
                    if (!nonDigitFound) {
                        throw new FormatException("There are two decimal dots in one number.");
                    }
                }
                lastDot = i;
            }
        }

        return safeMathExpression(text);
    }

    private static MathExpression safeMathExpression(String s) {
        if (s.charAt(0) == '-') {
            s = "0" + s;
        }
        List<FunctionType> operators = new ArrayList<FunctionType>();
        List<MathExpression> expressions = new ArrayList<MathExpression>();
        while (s.length() != 0) {
            if (s.startsWith("(")) {
                int openedBrackets = 1;
                int index = 0;
                while (openedBrackets != 0) {
                    index++;
                    if (s.charAt(index) == '(') {
                        openedBrackets++;
                    } else if (s.charAt(index) == ')') {
                        openedBrackets--;
                    }
                }
                String inBracketsText = s.substring(1, index);
                if (s.length() == index + 1) {
                    s = "";
                }
                else {
                    s = s.substring(index + 1);
                }
                expressions.add(safeMathExpression(inBracketsText));
                if (s.length() != 0) {
                    char nextChar = s.charAt(0);
                    if (nextChar == '(' || isAlphabetic(nextChar) || Character.isDigit(nextChar)) {
                        operators.add(FunctionType.MULTIPLICATION);
                    }
                }
            }
            else if (s.startsWith("+")) {
                operators.add(FunctionType.ADDITION);
                s = s.substring(1);
            }
            else if (s.startsWith("-")) {
                operators.add(FunctionType.SUBTRACTION);
                s = s.substring(1);
            }
            else if (s.startsWith("*")) {
                operators.add(FunctionType.MULTIPLICATION);
                s = s.substring(1);
            }
            else if (s.startsWith("/")) {
                operators.add(FunctionType.DIVISION);
                s = s.substring(1);
            }
            else if (s.startsWith("^")) {
                operators.add(FunctionType.POWER);
                s = s.substring(1);
            }
            else if (isAlphabetic(s.charAt(0))) {
                expressions.add(new MVariable(s.charAt(0)));
                s = s.substring(1);
                if (s.length() != 0) {
                    char nextChar = s.charAt(0);
                    if (nextChar == '(' || isAlphabetic(nextChar) || Character.isDigit(nextChar)) {
                        operators.add(FunctionType.MULTIPLICATION);
                    }
                }
            }
            else {
                int index = 0;
                while (s.length() > index && (Character.isDigit(s.charAt(index)) || s.charAt(index) == '.')) {
                    index++;
                }
                String numberString = s.substring(0, index);
                boolean hasDot = false;
                int dotAt = 0;
                for (int i = 0; i < numberString.length(); i++) {
                    if (numberString.charAt(i) == '.') {
                        hasDot = true;
                        dotAt = i;
                        break;
                    }
                }
                if (hasDot) {
                    int decimalPlaces = numberString.length() - dotAt - 1;
                    String numberWithoutDot = numberString.replace(".", "");
                    expressions.add(new MDivision(new MInteger(Integer.parseInt(numberWithoutDot)), new MInteger((int) Math.pow(10, decimalPlaces))));
                }
                else {
                    expressions.add(new MInteger(Integer.parseInt(numberString)));
                }
                s = s.substring(index);
                if (s.length() != 0) {
                    char nextChar = s.charAt(0);
                    if (nextChar == '(' || isAlphabetic(nextChar) || Character.isDigit(nextChar)) {
                        operators.add(FunctionType.MULTIPLICATION);
                    }
                }
            }
        }

        //Power resolving
        for (int i = 0; i < operators.size(); i++) {
            if (operators.get(i) == FunctionType.POWER) {
                expressions.add(i, new MPower(expressions.get(i), expressions.get(i + 1)));
                expressions.remove(i + 1);
                expressions.remove(i + 1);
                operators.remove(i);
                i--;
            }
        }

        //Multiplication-Division resolving
        for (int i = 0; i < operators.size(); i++) {
            if (operators.get(i) == FunctionType.MULTIPLICATION || operators.get(i) == FunctionType.DIVISION) {
                MathExpression resultingMathExpression;
                if (operators.get(i) == FunctionType.MULTIPLICATION) {
                    resultingMathExpression = new MMultiplication(expressions.get(i), expressions.get(i + 1));
                }
                else {
                    resultingMathExpression = new MDivision(expressions.get(i), expressions.get(i + 1));
                }
                expressions.add(i, resultingMathExpression);
                expressions.remove(i + 1);
                expressions.remove(i + 1);
                operators.remove(i);
                i--;
            }
        }

        //Addition-Subtraction resolving
        while (operators.size() != 0) {
            MathExpression resultingMathExpression;
            if (operators.get(0) == FunctionType.ADDITION) {
                resultingMathExpression = new MAddition(expressions.get(0), expressions.get(1));
            }
            else {
                resultingMathExpression = new MSubtraction(expressions.get(0), expressions.get(1));
            }
            expressions.add(0, resultingMathExpression);
            expressions.remove(1);
            expressions.remove(1);
            operators.remove(0);
        }

        return expressions.get(0);
    }

    private static boolean isCharValid(char c) {
        return isAlphabetic(c) || Character.isDigit(c) || c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')' || c == '^' || c == '.';
    }

    private static boolean isAlphabetic(char c) {
        char lowerCase = Character.toLowerCase(c);
        for (int i = 0; i < alphabet.length; i++) {
            if (alphabet[i] == lowerCase) {
                return true;
            }
        }
        return false;
    }
}
