package br.usp.sistemasreativos;

import java.lang.System;
import java.util.Iterator;
import java.util.List;

import br.usp.sistemasreativos.grammar.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

class NormalizedCTLEmitter extends CTLBaseListener {
    ParseTreeProperty<String> ctl = new ParseTreeProperty<String>();
    String getCTL(ParseTree ctx) { return ctl.get(ctx); }
    void setCTL(ParseTree ctx, String s) { ctl.put(ctx, s); }

    @Override
    public void exitProperty(CTLParser.PropertyContext ctx) {
        setCTL(ctx, ctx.getText());
    }

    @Override
    public void exitNot(CTLParser.NotContext ctx) {
        setCTL(ctx, ctx.getText());
    }

    @Override
    public void exitParenExpr(CTLParser.ParenExprContext ctx) {
        setCTL(ctx, ctx.getText());
    }

    @Override
    public void exitParen(CTLParser.ParenContext ctx) {
        setCTL(ctx, ctx.getText());
    }

    @Override
    public void exitAnd(CTLParser.AndContext ctx) {
        setCTL(ctx, ctx.getText());
    }

    @Override
    public void exitOr(CTLParser.OrContext ctx) {
        StringBuilder buf = new StringBuilder();

        List<CTLParser.ExprContext> eList = ctx.expr();
        Iterator<CTLParser.ExprContext> it = eList.iterator();

        it.next();
        while(it.hasNext()) {

        }
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
