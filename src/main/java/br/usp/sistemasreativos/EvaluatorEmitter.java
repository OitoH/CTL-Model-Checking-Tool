package br.usp.sistemasreativos;

import br.usp.sistemasreativos.grammar.CTLBaseListener;
import br.usp.sistemasreativos.grammar.CTLLexer;
import br.usp.sistemasreativos.grammar.CTLParser;
import com.sun.corba.se.impl.orb.ParserTable;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EvaluatorEmitter extends CTLBaseListener {
    ParseTreeProperty<Integer> label = new ParseTreeProperty<Integer>();
    int nextLabel = 1;

    Integer getLabel(ParseTree ctx) {
        return label.get(ctx);
    }

    void setLabel(ParseTree ctx) {
        label.put(ctx, nextLabel);
        ++nextLabel;
    }

    void setTrueLabel(ParseTree ctx) {
        label.put(ctx, 0);
    }

    void cloneChildLabel(ParseTree ctx, ParseTree child) {
        label.put(ctx, getLabel(child));
    }

    @Override
    public void exitProperty(CTLParser.PropertyContext ctx) {
        if(ctx.getText().contains("true"))
            setTrueLabel(ctx);
        else
            setLabel(ctx);
    }

    @Override
    public void exitParen(CTLParser.ParenContext ctx) {
        cloneChildLabel(ctx, ctx.parenExpr());
    }

    @Override
    public void exitParenExpr(CTLParser.ParenExprContext ctx) {
        cloneChildLabel(ctx, ctx.expr());
    }

    @Override
    public void exitOr(CTLParser.OrContext ctx) {
        setLabel(ctx);
        Integer leftLabel = getLabel(ctx.expr(0));
        Integer rightLabel = getLabel(ctx.expr(1));

        // TODO: Call machine or function
    }

    @Override
    public void exitNot(CTLParser.NotContext ctx) {
        setLabel(ctx);
        Integer childLabel = getLabel(ctx.expr());

        // TODO: Call machine not function
    }

    @Override
    public void exitCTL(CTLParser.CTLContext ctx) {
        cloneChildLabel(ctx, ctx.ctlExpr());
    }

    @Override
    public void exitUnaryCTL(CTLParser.UnaryCTLContext ctx) {
        /* Implementation note: Since the evaluator emitter
        is meant to work upon normalized CTL, this should be either EX or AF */
        Integer childLabel = getLabel(ctx.parenExpr());
        setLabel(ctx);

        if(ctx.CTLOpUnary().getText().charAt(0) == 'A') {
            // TODO: Call AF machine function
        }
        else {
            // TODO: Call EX machine function
        }
    }

    @Override
    public void exitBinaryCTL(CTLParser.BinaryCTLContext ctx) {
        Integer leftLabel = getLabel(ctx.expr(0));
        Integer rightLabel = getLabel(ctx.expr(1));
        setLabel(ctx);

        // TODO: Call EU machine function
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
        ParseTree conversionTree = parser.expr();

        System.out.println(conversionTree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();
        NormalizedCTLEmitter converter = new NormalizedCTLEmitter();
        walker.walk(converter, conversionTree);

        System.out.println("\n---------------------------------------------------\n");
        System.out.println(converter.getCTL(conversionTree));
        System.out.println("\n---------------------------------------------------\n");
        
        input = new ANTLRInputStream(converter.getCTL(conversionTree));
        lexer = new CTLLexer(input);
        tokens = new CommonTokenStream(lexer);
        parser = new CTLParser(tokens);
        parser.setBuildParseTree(true);
        ParseTree labelTree = parser.expr();
        EvaluatorEmitter evaluator = new EvaluatorEmitter();
        walker.walk(evaluator, labelTree);

        System.out.println(labelTree.toStringTree());
    }
}
