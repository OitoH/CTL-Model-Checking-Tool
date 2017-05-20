package br.usp.sistemasreativos;

import br.usp.sistemasreativos.grammar.CTLBaseListener;
import br.usp.sistemasreativos.grammar.CTLLexer;
import br.usp.sistemasreativos.grammar.CTLParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NormalizedCTLEmitter extends CTLBaseListener {
    ParseTreeProperty<String> ctl = new ParseTreeProperty<String>();

    String getCTL(ParseTree ctx) {
        return ctl.get(ctx);
    }

    void setCTL(ParseTree ctx, String s) {
        ctl.put(ctx, s);
    }

    private static String wrap(String str) {
        return "(" + str + ")";
    }

    @Override
    public void exitProperty(CTLParser.PropertyContext ctx) {
        setCTL(ctx, ctx.getText());
    }

    @Override
    public void exitNot(CTLParser.NotContext ctx) {
        setCTL(ctx, "!" + wrap(getCTL(ctx.expr())));
    }

    @Override
    public void exitParenExpr(CTLParser.ParenExprContext ctx) {
        setCTL(ctx, wrap(getCTL(ctx.expr())));
    }

    @Override
    public void exitParen(CTLParser.ParenContext ctx) {
        setCTL(ctx, getCTL(ctx.parenExpr()));
    }

    @Override
    public void exitOr(CTLParser.OrContext ctx) {
        StringBuilder buf = new StringBuilder();

        String a = wrap(getCTL(ctx.expr(0)));
        String b = wrap(getCTL(ctx.expr(1)));

        buf.append(a);
        buf.append('|');
        buf.append(b);

        setCTL(ctx, buf.toString());
    }

    @Override
    public void exitAnd(CTLParser.AndContext ctx) {
        StringBuilder buf = new StringBuilder();

        String a = wrap(getCTL(ctx.expr(0)));
        String b = wrap(getCTL(ctx.expr(1)));

        buf.append("!(!");
        buf.append(a);
        buf.append("|!");
        buf.append(b);
        buf.append(')');

        setCTL(ctx, buf.toString());
    }

    @Override
    public void exitBiconditional(CTLParser.BiconditionalContext ctx) {
        StringBuilder buf = new StringBuilder();

        String a = wrap(getCTL(ctx.expr(0)));
        String b = wrap(getCTL(ctx.expr(1)));

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

        String a = wrap(getCTL(ctx.expr(0)));
        String b = wrap(getCTL(ctx.expr(1)));

        buf.append('!');
        buf.append(a);
        buf.append('|');
        buf.append(b);

        setCTL(ctx, buf.toString());
    }

    @Override
    public void exitBinaryCTL(CTLParser.BinaryCTLContext ctx) {
        StringBuilder buf = new StringBuilder();
        String p = getCTL(ctx.expr(0));
        String q = getCTL(ctx.expr(1));

        String op = ctx.CTLOpBinary().getText();
        if (op.compareTo("AU") == 0) {
            q = wrap(q);
            String notq = '!' + q;

            buf.append("!(EG(");
            buf.append(notq);
            buf.append(")|EU(");
            buf.append(notq);
            buf.append(",");
            buf.append("!(");
            buf.append(q);
            buf.append("|");
            buf.append(wrap(p));
            buf.append(")))");

            setCTL(ctx, buf.toString());
        } else {
            buf.append("EU(");
            buf.append(p);
            buf.append(",");
            buf.append(q);
            buf.append(")");

            setCTL(ctx, buf.toString());
        }
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
        switch (op.charAt(1)) {
            case 'X':
                setCTL(ctx, "EX" + prop);
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
        switch (op.charAt(1)) {
            case 'X':
                convertAX(ctx, prop);
                break;
            case 'G':
                convertAG(ctx, prop);
                break;
            case 'F':
                setCTL(ctx, "AF" + prop);
                break;
        }
    }

    @Override
    public void exitUnaryCTL(CTLParser.UnaryCTLContext ctx) {
        String op = ctx.CTLOpUnary().getText();
        String prop = getCTL(ctx.parenExpr());
        switch (op.charAt(0)) {
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

    public static void testAppMain(String[] args) throws IOException {
        String inputfile = null;
        if (args.length > 0) inputfile = args[0];
        InputStream is = (inputfile != null) ? new FileInputStream(inputfile) : System.in;

        ANTLRInputStream input = new ANTLRInputStream(is);

        CTLLexer lexer = new CTLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        CTLParser parser = new CTLParser(tokens);
        parser.setBuildParseTree(true);
        ParseTree tree = parser.expr();

        System.out.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();
        NormalizedCTLEmitter converter = new NormalizedCTLEmitter();
        walker.walk(converter, tree);

        System.out.println("\n---------------------------------------------------\n");
        System.out.println(converter.getCTL(tree));
    }
}
