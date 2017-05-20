package br.usp.sistemasreativos;

import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;

class State{
    private int name;
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
        if(nextStateList.size()==0){
            return false;
        }
        for(i=0;i<nextStateList.size();i++){
            if(!nextStateList.get(i).verifyLabel(label)){
                return false;
            }
        }
        return true;
    }
}

class StateMachine{
    private List<State> stateList;
    public StateMachine(int size){
        int i;
        stateList = new ArrayList<State>();

        for(i=0;i<=size;i++){
            State state = new State(i);
            stateList.add(state);
            stateList.get(i).addLabel(0);
        }
    }
    public boolean addProperty(int stateId,String propertyName){
        System.out.println("stateId = "+stateId+" propertyName = "+ propertyName);
        return stateList.get(stateId).addProperty(propertyName);
    }
    public boolean addNextState(int stateId,int idNextState){
        System.out.println("stateId = "+stateId+" nextState = "+ idNextState);
        return stateList.get(stateId).addNextState(stateList.get(idNextState));
    }
    /******************************************************************
     Implementação das operações na máquina de estado
     */

    public boolean propertyLabel(int idLabel,String property){
        //adiciona label idlabel na propriedade property
        int i;
        for (i=1;i<stateList.size();i++){
            if(stateList.get(i).verifyProperty(property)){
                stateList.get(i).addLabel(idLabel);
            }
        }
        return true;
    }
    public boolean not(int idlabel,int label){
        int i;
        for (i=1;i<stateList.size();i++){
            //!p
            if(!stateList.get(i).verifyLabel(label)){
                stateList.get(i).addLabel(idlabel);
            }
        }
        return true;
    }
    public boolean and(int idLabel,int label1, int label2){
        int i;
        for(i=1;i<stateList.size();i++){
            //p && q
            if(stateList.get(i).verifyLabel(label1)&&stateList.get(i).verifyLabel(label2)){
                stateList.get(i).addLabel(idLabel);
            }
        }
        return true;
    }
    public boolean or(int idLabel,int label1, int label2){
        int i;
        for(i=1;i<stateList.size();i++){
            //p || q
            if(stateList.get(i).verifyLabel(label1) || stateList.get(i).verifyLabel(label2)){
                stateList.get(i).addLabel(idLabel);
            }
        }
        return true;
    }
    public boolean implication(int idLabel,int label1, int label2){
        int i;
        for(i=1;i<stateList.size();i++){
            //q->p = !q||p
            if(!stateList.get(i).verifyLabel(label1) || stateList.get(i).verifyLabel(label2)){
                stateList.get(i).addLabel(idLabel);
            }
        }
        return true;
    }
    public boolean equivalence(int idLabel,int label1, int label2){
        int i;
        for(i=1;i<stateList.size();i++){
            // q=p --> q^p || !q^!p
            if(
                    ( stateList.get(i).verifyLabel(label1) && stateList.get(i).verifyLabel(label2) )
                            ||
                            ((!stateList.get(i).verifyLabel(label1) ) && (!stateList.get(i).verifyLabel(label2)) )
                    )
            {
                stateList.get(i).addLabel(idLabel);
            }
        }
        return true;
    }
    public boolean ex(int idLabel, int label){
        int i;
        for(i=1;i<stateList.size();i++){
            // rotula com idlabel todos que tem um dos próximos estados o rótulo label
            if(stateList.get(i).verifyNextStateLabel(label)){
                stateList.get(i).addLabel(idLabel);
                //System.out.println("label "+idLabel +" adicionado no estado "+i);
            }
        }
        return true;
    }
    private boolean euSecondStap(int idLabel, int label1){
        int i;
        boolean modified=false;
        for(i=1;i<stateList.size();i++){
            if(
                    stateList.get(i).verifyLabel(label1) &&
                            stateList.get(i).verifyNextStateLabel(idLabel) &&
                            (!stateList.get(i).verifyLabel(idLabel))){
                stateList.get(i).addLabel(idLabel);
                modified = true;//se algum rótulo é adicionado a busca recomeça
            }
        }
        return modified;
    }
    public boolean eu(int idLabel, int label1, int label2){
        int i;
        for(i=1;i<stateList.size();i++){
            //rotula todos os estados que possuem label2
            if(stateList.get(i).verifyLabel(label2)){
                stateList.get(i).addLabel(idLabel);
            }
        }
        //rotula todos os estados que possuem label 1 e chegam em um estado que o next tem idlabel
        while(euSecondStap(idLabel,label1));
        return true;
    }
    private boolean afSecondStap(int idLabel){
        int i;
        boolean modified=false;
        for(i=1;i<stateList.size();i++){
            if(
                    stateList.get(i).verifyAllNextStateLabel(idLabel) &&
                            (!stateList.get(i).verifyLabel(idLabel))){
                stateList.get(i).addLabel(idLabel);
                modified = true;//se algum rótulo é adicionado, a busca recomeça
            }
        }
        return modified;
    }
    public boolean af(int idLabel,int label1){
        int i;
        for(i=1;i<stateList.size();i++){
            //rotula todos os estados que possuem label1
            if(stateList.get(i).verifyLabel(label1)){
                stateList.get(i).addLabel(idLabel);
            }
        }
        //rotula todos os estados que possuem todos os next state idlabel
        while(afSecondStap(idLabel));
        return true;
    }
    public List<Integer> getStatesWithLabel(int label){
        int i;
        List<Integer> list = new ArrayList<Integer>();
        for(i=1;i<stateList.size();i++){
            if(stateList.get(i).verifyLabel(label)){
                list.add(i);
            }
        }
        return list;
    }

    public static StateMachine buildKISSFormat(BufferedReader bufferedReader) throws IOException {
        //Parser da entrada (máquina de estado)
        int totLinhas = Integer.parseInt(bufferedReader.readLine());
        int idLinha;
        StateMachine stateMachine = new StateMachine(totLinhas);
        for(idLinha = 1; idLinha <= totLinhas; idLinha++){

            //tratamento das linhas de cada estado
            String linha;
            linha = bufferedReader.readLine();
            String[] words = linha.split(" ");
            int vectorPointer;
            //words: vetor de palavras de uma linha
            //vectorPointer: ponteiro do vetor words

            int propVectorPointer = Integer.parseInt(words[1]) + 2;
            int maxVectorPointer = Integer.parseInt(words[propVectorPointer]) + propVectorPointer+1;
            for(vectorPointer=2 ; vectorPointer<propVectorPointer ; vectorPointer++){
                //adiciona propriedades ao estado idLinha
                stateMachine.addProperty(idLinha,words[vectorPointer]);
            }
            for(vectorPointer=propVectorPointer+1; vectorPointer<maxVectorPointer ; vectorPointer++){
                //adiciona proximos estados ao estado idLinha
                stateMachine.addNextState(idLinha,Integer.parseInt(words[vectorPointer]));
            }
        }

        return stateMachine;
    }

    public static void testMainApp(String[] args){
        StateMachine stateMachine;
        String fileName;
        BufferedReader bufferedReader;
        System.out.println("Digite o arquivo da maquina de estado");
        Scanner sc = new Scanner (System.in);
        fileName = sc.nextLine();
        sc.close();
        try{
            FileReader fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);

            stateMachine = StateMachine.buildKISSFormat(bufferedReader);

			/*testes
			//adiciona rotulos nos estados com propriedades
			int idLabel=1;
			stateMachine.propertyLabel(idLabel++,"p");
			stateMachine.propertyLabel(idLabel++,"q");
			stateMachine.propertyLabel(idLabel++,"s");
			stateMachine.propertyLabel(idLabel++,"r");
			//testa operação
			stateMachine.eu(idLabel++,0,1);
			stateMachine.and(idLabel++,1,2);
			stateMachine.or(idLabel++,1,2);
			stateMachine.not(idLabel++,1);
			List<Integer> labelList = new ArrayList<Integer>();
			labelList = stateMachine.getStateLabel(1);
			for (int stateLabel:labelList){
				System.out.println("stados p "+stateLabel);
			}
			labelList = stateMachine.getStateLabel(2);
			for (int stateLabel:labelList){
				System.out.println("stados q "+stateLabel);
			}
			labelList = stateMachine.getStateLabel(3);
			for (int stateLabel:labelList){
				System.out.println("stados s "+stateLabel);
			}
			labelList = stateMachine.getStateLabel(4);
			for (int stateLabel:labelList){
				System.out.println("stados r "+stateLabel);
			}
			labelList = stateMachine.getStateLabel(5);
			for (int stateLabel:labelList){
				System.out.println("stados eu(p,q) "+stateLabel);
			}
			labelList = stateMachine.getStateLabel(6);
			for (int stateLabel:labelList){
				System.out.println("stados and(p,q) "+stateLabel);
			}
			labelList = stateMachine.getStateLabel(7);
			for (int stateLabel:labelList){
				System.out.println("stados or(p,q) "+stateLabel);
			}
			labelList = stateMachine.getStateLabel(8);
			for (int stateLabel:labelList){
				System.out.println("stados not(p) "+stateLabel);
			}
			//*/

        }
        catch(NumberFormatException ex){
            System.out.println(ex);
        }
        catch(IOException ex){
            System.out.println(ex);
        }
    }

}
