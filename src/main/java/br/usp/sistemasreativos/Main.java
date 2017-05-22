package br.usp.sistemasreativos;

import br.usp.sistemasreativos.grammar.CTLLexer;
import br.usp.sistemasreativos.grammar.CTLParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        // NormalizedCTLEmitter.testAppMain(args);
        // EvaluatorEmitter.testAppMain(args);
        // StateMachine.testAppMain(args);
        // testDirectExpression(args);

        defaultMain(args);

    }

    public static void defaultMain(String[] args) throws IOException {
        // NormalizedCTLEmitter.testAppMain(args);
        // EvaluatorEmitter.testAppMain(args);
        // StateMachine.testAppMain(args);

        // Construir máquina
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        StateMachine stateMachine = StateMachine.buildKISSFormat(bufferedReader);

        if (stateMachine == null)
            System.exit(0);

        // Ler expressão CTL
        String CTLExpression = bufferedReader.readLine();
        bufferedReader.close();

        System.err.println("\nConversão da expressão\nOriginal: >" + CTLExpression + "<");

        // Converter a expressão para o formato com número mínimo de operadores
        ParseTreeWalker walker = new ParseTreeWalker();

        CTLParser parser = new CTLParser(
                new CommonTokenStream(new CTLLexer(new ANTLRInputStream(CTLExpression)))
        );
        parser.setBuildParseTree(true);
        ParseTree expressionTree = parser.expr();
        NormalizedCTLEmitter converter = new NormalizedCTLEmitter();
        walker.walk(converter, expressionTree);

        System.err.println("Convertida: >" + converter.getCTL(expressionTree) + "<\n");

        // Computar a expressão convertida
        parser = new CTLParser(new CommonTokenStream(
                new CTLLexer(new ANTLRInputStream(converter.getCTL(expressionTree))))
        );
        parser.setBuildParseTree(true);
        ParseTree convertedTree = parser.expr();
        EvaluatorEmitter evaluator = new EvaluatorEmitter(stateMachine);
        walker.walk(evaluator, convertedTree);

        // Imprimir estados onde a expressão é válida
        List<Integer> answer = stateMachine.getStatesWithLabel(evaluator.getLabel(convertedTree));
        if (answer.isEmpty()) {
            System.out.println("A expressão é falsa para todos os estados");
        } else {
            System.out.println("A expressão é válida nos seguintes estados:");

            Iterator<Integer> it = answer.iterator();
            System.out.print(it.next().toString());
            while (it.hasNext())
                System.out.print(" " + it.next().toString());
        }
    }

    public static void testDirectExpression(String[] args) throws IOException {
        // NormalizedCTLEmitter.testAppMain(args);
        // EvaluatorEmitter.testAppMain(args);
        // StateMachine.testAppMain(args);

        // Construir máquina
        BufferedReader bufferedReader = new BufferedReader(new FileReader("testes/valida"));
        StateMachine stateMachine = StateMachine.buildKISSFormat(bufferedReader);

        if (stateMachine == null)
            System.exit(0);

        // Ler expressão CTL
        String CTLExpression = bufferedReader.readLine();
        bufferedReader.close();

        ParseTreeWalker walker = new ParseTreeWalker();

        CTLParser parser;
        // Computar a expressão convertida
        parser = new CTLParser(new CommonTokenStream(
                new CTLLexer(new ANTLRInputStream(CTLExpression)))
        );
        parser.setBuildParseTree(true);
        ParseTree convertedTree = parser.expr();
        EvaluatorEmitter evaluator = new EvaluatorEmitter(stateMachine);
        walker.walk(evaluator, convertedTree);

        // Imprimir estados onde a expressão é válida
        List<Integer> answer = stateMachine.getStatesWithLabel(evaluator.getLabel(convertedTree));
        if (answer.isEmpty()) {
            System.out.println("A expressão é falsa para todos os estados");
        } else {
            System.out.println("A expressão é válida nos seguintes estados:");

            Iterator<Integer> it = answer.iterator();
            System.out.print(it.next().toString());
            while (it.hasNext())
                System.out.print(" " + it.next().toString());
        }
    }
}
