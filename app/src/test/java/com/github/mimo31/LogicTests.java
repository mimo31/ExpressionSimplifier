package com.github.mimo31;

import com.github.mimo31.expressionsimplifier.Logic;
import com.github.mimo31.expressionsimplifier.TextDisplayer;

import org.junit.Test;

import java.util.Random;

/**
 * This class contains unit test methods that test whether the logical mechanism for dealing with mathematical expressions is not throwing exceptions.
 */
public class LogicTests
{

    /**
     * Tests three simple expressions.
     */
    @Test
    public void testBasicOperations()
    {
        TextDisplayer displayer = System.out::println;
        Logic.processInput("4 - x", displayer);
        Logic.processInput("15er", displayer);
        Logic.processInput("t^2-41", displayer);
    }

    /**
     * Tests three malformed input strings.
     */
    @Test
    public void testMalformedInput()
    {
        TextDisplayer displayer = System.out::println;
        Logic.processInput("(xx-8", displayer);
        Logic.processInput("()()", displayer);
        Logic.processInput("(8*5/4*x)/2.", displayer);
    }

    /**
     * Tests many randomly generated input strings of length [0, 20).
     */
    @Test
    public void heavyRandomTest()
    {
        TextDisplayer displayer = System.out::println;
        Random r = new Random();
        for (int i = 0; i < 1000; i++)
        {
            int len = r.nextInt(20);
            char[] sequence = new char[len];
            for (int j = 0; j < len; j++)
            {
                sequence[j] = (char) (r.nextInt(127 - 32) + 32);
            }
            Logic.processInput(String.valueOf(sequence), displayer);
        }
    }

    /**
     * Tests many randomly generated input strings of length [0, 20) with the restriction that none of the characters are necessarily illegal..
     */
    @Test
    public void heavyRandomCharRestrictedRandomTest()
    {
        TextDisplayer displayer = System.out::println;
        Random r = new Random();
        for (int i = 0; i < 1000; i++)
        {
            int len = r.nextInt(20);
            char[] sequence = new char[len];
            for (int j = 0; j < len; j++)
            {
                char c = (char) (r.nextInt(127 - 32) + 32);
                if (c == ' ' || (40 <= c && c < 58) || c == '^' || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z'))
                {
                    sequence[j] = c;
                }
                else
                {
                    j--;
                }
            }
            Logic.processInput(String.valueOf(sequence), displayer);
        }
    }
}
