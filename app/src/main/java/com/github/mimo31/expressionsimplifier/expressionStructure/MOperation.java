package com.github.mimo31.expressionsimplifier.expressionStructure;

public class MOperation
{
    public ElemOperation type;
    public Object[] arguments;

    public MOperation(ElemOperation type, Object[] arguments)
    {
        this.type = type;
        this.arguments = arguments;
    }
}
