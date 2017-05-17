package br.usp.sistemasreativos;

import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;

class State {
    public final int name;
    private List<State> nextStateList;
    private List<String> propertyList;
    private List<Integer> labelList;

    public State(int name){
        this.name = name;
        nextStateList = new ArrayList<State>();
        propertyList = new ArrayList<String>();
        labelList = new ArrayList<Integer>();
    }
    public boolean addProperty(String property){
        return propertyList.add(property);
    }
    public boolean addNextState(State nextState){
        return nextStateList.add(nextState);
    }
    public boolean addLabel(int label){
        return labelList.add(label);
    }
    public boolean verifyProperty(String property){
        return propertyList.contains(property);
    }
    public boolean verifyNextState(State nextState){
        return nextStateList.contains(nextState);
    }
    public boolean verifyLabel(int label){
        return labelList.contains(label);
    }
    public boolean verifyNextStateLabel(int label){
        int i;
        for(i=0;i<nextStateList.size();i++){
            if(nextStateList.get(i).verifyLabel(label)){
                return true;
            }
        }
        return false;
    }
    public boolean verifyAllNextStateLabel(int label){
        int i;
        for(i=0;i<nextStateList.size();i++){
            if(!nextStateList.get(i).verifyLabel(label)){
                return false;
            }
        }
        return true;
    }

}
class StateMachine {
    private List<State> stateList;

    public StateMachine(int size) {
        int i;
        stateList = new ArrayList<State>();

        for (i = 0; i <= size; i++) {
            State state = new State(i);
            stateList.add(state);
        }
    }

    public boolean addProperty(int stateId, String propertyName) {
        System.out.println("stateId = " + stateId + " propertyName = " + propertyName);
        return stateList.get(stateId).addProperty(propertyName);
    }

    public boolean addNextState(int stateId, int idNextState) {
        System.out.println("stateId = " + stateId + " nextState = " + idNextState);
        return stateList.get(stateId).addNextState(stateList.get(idNextState));
    }

    /******************************************************************
     Implementação das operações na máquina de estado
     */

    public boolean propertyLabel(int idLabel, String property) {
        //adiciona label idlabel na propriedade property
        int i;
        for (i = 0; i < stateList.size(); i++) {
            if (stateList.get(i).verifyProperty(property)) {
                stateList.get(i).addLabel(idLabel);
            }
        }
        return true;
    }

    public boolean not(int idlabel, int label) {
        int i;
        for (i = 0; i < stateList.size(); i++) {
            //!p
            if (!stateList.get(i).verifyLabel(label)) {
                stateList.get(i).addLabel(idlabel);
            }
        }
        return true;
    }

    public boolean and(int idLabel, int label1, int label2) {
        int i;
        for (i = 0; i < stateList.size(); i++) {
            //p && q
            if (stateList.get(i).verifyLabel(label1) && stateList.get(i).verifyLabel(label2)) {
                stateList.get(i).addLabel(idLabel);
            }
        }
        return true;
    }

    public boolean or(int idLabel, int label1, int label2) {
        int i;
        for (i = 0; i < stateList.size(); i++) {
            //p || q
            if (stateList.get(i).verifyLabel(label1) || stateList.get(i).verifyLabel(label2)) {
                stateList.get(i).addLabel(idLabel);
            }
        }
        return true;
    }

    public boolean implication(int idLabel, int label1, int label2) {
        int i;
        for (i = 0; i < stateList.size(); i++) {
            //q->p = !q||p
            if (!stateList.get(i).verifyLabel(label1) || stateList.get(i).verifyLabel(label2)) {
                stateList.get(i).addLabel(idLabel);
            }
        }
        return true;
    }

    public boolean equivalence(int idLabel, int label1, int label2) {
        int i;
        for (i = 0; i < stateList.size(); i++) {
            // q=p --> q^p || !q^!p
            if (
                    (stateList.get(i).verifyLabel(label1) && stateList.get(i).verifyLabel(label2))
                            ||
                            ((!stateList.get(i).verifyLabel(label1)) && (!stateList.get(i).verifyLabel(label2)))
                    ) {
                stateList.get(i).addLabel(idLabel);
            }
        }
        return true;
    }

    public boolean ex(int idLabel, int label) {
        int i;
        for (i = 0; i < stateList.size(); i++) {
            // rotula com idlabel todos que tem um dos próximos estados o rótulo label
            if (stateList.get(i).verifyNextStateLabel(label)) {
                stateList.get(i).addLabel(idLabel);
            }
        }
        return true;
    }

    private boolean euSecondStap(int idLabel, int label1) {
        int i;
        boolean modified = false;
        for (i = 0; i < stateList.size(); i++) {
            if (
                    stateList.get(i).verifyLabel(label1) &&
                            stateList.get(i).verifyNextStateLabel(idLabel) &&
                            (!stateList.get(i).verifyLabel(idLabel))) {
                stateList.get(i).addLabel(idLabel);
                modified = true;//se algum rótulo é adicionado a busca recomeça
            }
        }
        return modified;
    }

    public boolean eu(int idLabel, int label1, int label2) {
        int i;
        for (i = 0; i < stateList.size(); i++) {
            //rotula todos os estados que possuem label2
            if (stateList.get(i).verifyLabel(label2)) {
                stateList.get(i).addLabel(idLabel);
            }
        }
        //rotula todos os estados que possuem label 1 e chegam em um estado que o next tem idlabel
        while (euSecondStap(idLabel, label1)) ;
        return true;
    }

    private boolean afSecondStap(int idLabel) {
        int i;
        boolean modified = false;
        for (i = 0; i < stateList.size(); i++) {
            if (
                    stateList.get(i).verifyAllNextStateLabel(idLabel) &&
                            (!stateList.get(i).verifyLabel(idLabel))) {
                stateList.get(i).addLabel(idLabel);
                modified = true;//se algum rótulo é adicionado, a busca recomeça
            }
        }
        return modified;
    }

    public boolean af(int idLabel, int label1) {
        int i;
        for (i = 0; i < stateList.size(); i++) {
            //rotula todos os estados que possuem label1
            if (stateList.get(i).verifyLabel(label1)) {
                stateList.get(i).addLabel(idLabel);
            }
        }
        //rotula todos os estados que possuem todos os next state idlabel
        while (afSecondStap(idLabel)) ;
        return true;
    }

    public List<State> getStatesWithLabel(int label) {
        List<State> states = new ArrayList<State>();
        for(int i = 0; i < stateList.size(); i++) {
            if(stateList.get(i).verifyLabel(label))
                states.add(stateList.get(i));
        }
        return states;
    }

    public static StateMachine buildKISSFormat(BufferedReader bufferedReader) throws IOException {
        int totLinhas = Integer.parseInt(bufferedReader.readLine());
        int idLinha;
        StateMachine stateMachine = new StateMachine(totLinhas);

        for (idLinha = 1; idLinha <= totLinhas; idLinha++) {
            //tratamento das linhas de cada estado
            String linha;
            linha = bufferedReader.readLine();
            String[] words = linha.split(" ");
            int vectorPointer;
            //words: vetor de palavras de uma linha
            //vectorPointer: ponteiro do vetor words

            int propVectorPointer = Integer.parseInt(words[1]) + 2;
            int maxVectorPointer = Integer.parseInt(words[propVectorPointer]) + propVectorPointer + 1;
            for (vectorPointer = 2; vectorPointer < propVectorPointer; vectorPointer++) {
                //adiciona propriedades ao estado idLinha
                stateMachine.addProperty(idLinha, words[vectorPointer]);
            }
            for (vectorPointer = propVectorPointer + 1; vectorPointer < maxVectorPointer; vectorPointer++) {
                //adiciona proximos estados ao estado idLinha
                stateMachine.addNextState(idLinha, Integer.parseInt(words[vectorPointer]));
            }
        }

        return stateMachine;
    }

    public static void testAppMain(String[] args) throws IOException {
        StateMachine stateMachine;
        String fileName;
        BufferedReader bufferedReader;

        System.out.println("Digite o arquivo da maquina de estado");
        Scanner sc = new Scanner(System.in);
        fileName = sc.nextLine();
        sc.close();

        bufferedReader = new BufferedReader(new FileReader(fileName));
        stateMachine = buildKISSFormat(bufferedReader);
    }
}