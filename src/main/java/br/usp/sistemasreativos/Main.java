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
        setCTL(ctx, getCTL(ctx.expr()));
    }

    @Override
    public void exitParenExpr(CTLParser.ParenExprContext ctx) {
        StringBuilder buf = new StringBuilder();

        buf.append('(');
        buf.append(getCTL(ctx.expr()));
        buf.append(')');

        setCTL(ctx, buf.toString());
    }

    @Override
    public void exitParen(CTLParser.ParenContext ctx) {
        setCTL(ctx, getCTL(ctx.parenExpr()));
    }

    @Override
    public void exitOr(CTLParser.OrContext ctx) {
        StringBuilder buf = new StringBuilder();

        String a = getCTL(ctx.expr(0));
        String b = getCTL(ctx.expr(1));

        buf.append(a);
        buf.append('|');
        buf.append(b);

        setCTL(ctx, buf.toString());
    }

    @Override
    public void exitAnd(CTLParser.AndContext ctx) {
        StringBuilder buf = new StringBuilder();

        String a = getCTL(ctx.expr(0));
        String b = getCTL(ctx.expr(1));

        buf.append('!');
        buf.append(a);
        buf.append('|');
        buf.append('!');
        buf.append(b);

        setCTL(ctx, buf.toString());
    }

    @Override
    public void exitBiconditional(CTLParser.BiconditionalContext ctx) {
        StringBuilder buf = new StringBuilder();

        String a = getCTL(ctx.expr(0));
        String b = getCTL(ctx.expr(1));

        buf.append("!(!(!");
        buf.append(a);
        buf.append('|');
        buf.append(b);
        buf.append(")|!(!");
        buf.append(b);
        buf.append('|');
        buf.append(a);
        buf.append("))");

        setCTL(ctx, buf.toString());
    }

    @Override
    public void exitImplies(CTLParser.ImpliesContext ctx) {
        StringBuilder buf = new StringBuilder();

        String a = getCTL(ctx.expr(0));
        String b = getCTL(ctx.expr(1));

        buf.append('!');
        buf.append(a);
        buf.append('|');
        buf.append(b);

        setCTL(ctx, buf.toString());
    }

    @Override
    public void exitBinaryCTL(CTLParser.BinaryCTLContext ctx) {
        String op = ctx.CTLOpBinary().getText();
        if(op.compareTo("AU") == 0) {
            StringBuilder buf = new StringBuilder();

            String q = getCTL(ctx.expr(0));
            String notq = '!' + q;

            buf.append("!(EG("); buf.append(notq); buf.append(")|EU(");
            buf.append(notq);
            buf.append(",");
            buf.append("!(");
            buf.append(q);
            buf.append("|");
            buf.append(getCTL(ctx.expr(1)));
            buf.append(")))");

            setCTL(ctx, buf.toString());
        }
        else
            setCTL(ctx, ctx.getText());
    }

    private void convertEG(CTLParser.UnaryCTLContext ctx, String prop) {
        StringBuilder buf = new StringBuilder();

        buf.append("!AF(!");
        buf.append(prop);
        buf.append(")");

        setCTL(ctx, buf.toString());
    }

    private void convertEF(CTLParser.UnaryCTLContext ctx, String prop) {
        StringBuilder buf = new StringBuilder();

        buf.append("EU(true, ");
        buf.append(prop);
        buf.append(")");

        setCTL(ctx, buf.toString());
    }

    private void convertExistsUnaryCTLNode(CTLParser.UnaryCTLContext ctx, String op, String prop) {
        switch(op.charAt(1)) {
            case 'X':
                setCTL(ctx, prop);
            break;
            case 'G':
                convertEG(ctx, prop);
            break;
            case 'F':
                convertEF(ctx, prop);
            break;
        }
    }

    private void convertAX(CTLParser.UnaryCTLContext ctx, String prop) {
        StringBuilder buf = new StringBuilder();

        buf.append("!EX(!");
        buf.append(prop);
        buf.append(')');

        setCTL(ctx, buf.toString());
    }

    private void convertAG(CTLParser.UnaryCTLContext ctx, String prop) {
        StringBuilder buf = new StringBuilder();

        buf.append("!EU(true, !");
        buf.append(prop);
        buf.append(')');

        setCTL(ctx, buf.toString());
    }

    private void convertAllUnaryCTLNode(CTLParser.UnaryCTLContext ctx, String op, String prop) {
        switch(op.charAt(1)) {
            case 'X':
                convertAX(ctx, prop);
                break;
            case 'G':
                convertAG(ctx, prop);
                break;
            case 'F':
                setCTL(ctx, prop);
                break;
        }
    }

    @Override
    public void exitUnaryCTL(CTLParser.UnaryCTLContext ctx) {
        String op = ctx.CTLOpUnary().getText();
        String prop = getCTL(ctx.parenExpr());
        switch(op.charAt(0)) {
            case 'E':
                convertExistsUnaryCTLNode(ctx, op, prop);
            break;
            case 'A':
                convertAllUnaryCTLNode(ctx, op, prop);
            break;
        }
    }

    @Override
    public void exitCTL(CTLParser.CTLContext ctx) {
        setCTL(ctx, getCTL(ctx.ctlExpr()));
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
