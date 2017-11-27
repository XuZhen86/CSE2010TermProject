/*
    Authors (group members): Zhen Xu, Tariq Maashani, Jim Harrell
    Email addresses of group members: zxu2016@my.fit.edu, talmaashani2016@my.fit.edu, jharrell2014@my.fit.edu
    Group name: 2a
    Course: CSE 2010
    Section: 02

    Description of the overall algorithm and key data structures:
        Algorithm: DFS
        Data structure: DictionaryTree
        Other techniques: Bit-Wise Operation, Process-Oriented Programming
*/

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

class Location{// copied Location.java
    public int row, col;
    public Location(int aRow, int aCol){row = aRow;col = aCol;}
    public Location(){row = 0;col = 0;}
    public String toString(){return String.format("[%d,%d]",row,col);}
}

class Word implements Comparable<Word>{// copied and extended from Word.java
    private String word;
    private ArrayList<Location> path;
    public Word(){word = "";path = new ArrayList<Location>(8);  }
    public Word(int initialMaxPathLength){
        word = "";
        path = new ArrayList<Location>(initialMaxPathLength); 
        for (int index = 0; index < initialMaxPathLength; index++)
            path.add(new Location(0, 0));
    }
    public Word(String aWord){word = aWord;path = new ArrayList<Location>(word.length()); }
    public void setWord(String aWord){
        if (word != null)
            word = aWord;
        else
            System.out.println("Warning: Word:setPath()--aWord is null");
    }
    public void setPath(ArrayList<Location> aPath){
        if (aPath != null)
            path = aPath;
        else
            System.out.println("Warning: Word:setPath()--aPath is null");
    }
    public void addLetterRowAndCol(int row, int col){
        path.add(new Location(row, col));
    }
    public void setLetterRowAndCol(int letterIndex, int row, int col){
        if ((letterIndex >= 0) && (letterIndex < path.size()))
            {
            Location loc = path.get(letterIndex);
            if (loc != null) // overwrite the location
                {
                loc.row = row;
                loc.col = col;
                }
            else // create a new location object
                path.set(letterIndex, new Location(row, col));
            }
        else
            System.err.println("Word.setLetterRowAndCol(): letterIndex out of bound: " + letterIndex);
    }
    public String getWord(){return word;}
    public int getPathLength(){return path.size();}
    public int getLetterRow(int letterIndex){
        Location loc = getLetterLocation(letterIndex);
        if (loc != null)
            return loc.row;
        else
            return -1;
    }
    public int getLetterCol(int letterIndex){
        Location loc = getLetterLocation(letterIndex);
        if (loc != null)
            return loc.col;
        else
            return -1;
    }
    public Location getLetterLocation(int letterIndex){
        if ((letterIndex >= 0) && (letterIndex < path.size()))
        {	
            Location loc = path.get(letterIndex);
            if (loc != null)
                return loc;
            else
                {
                System.err.println("Word.getLetterLocation(): no location at letterIndex" + letterIndex);
                return null;
                }
        }
        else
        {
            System.err.println("Word.getLetterLocation(): out of bound at letterIndex" + letterIndex);
            return null;
        }
    }

    // the folowing 3 functions are added
    public boolean equals(Word w){return this.word.equals(w.getWord());}
    public String toString(){return String.format("[%s,%s]",word,path);}// for debugging
    public int compareTo(Word w){
        if(getPathLength()!=w.getPathLength()){
            return w.getPathLength()-getPathLength();
        }else{
            return w.getWord().compareTo(getWord());
        }
    }
}

public class BogglePlayer{// all data structures were intended to be implemented in the "lowest level" to save memory
    public static final int[][] NEXT_STEP={
        {-1,-1},{-1, 0},{-1, 1},
        { 0,-1},        { 0, 1},
        { 1,-1},{ 1, 0},{ 1, 1}
    };

    public static int[][] d;
    public static byte[][] board;
    public static boolean[][] visited;
    public static byte[] traceByte,stringByte;// the traceByte is the original byte[] to store the trace of the dfs, the stringByte is for the special case of QU
    public static int[][] traceXY;
    public static ArrayList<Word> answers;
    public static Word[] myWords;

    public BogglePlayer(String wordFile){
        Scanner scan;// open the scanner
        try{scan=new Scanner(new File(wordFile));}
        catch(FileNotFoundException e){System.out.println(e);return;}

        // build dictionary tree 
        // the three data structures represent the three fields in each virtual node
        ArrayList<Boolean> isAWord=new ArrayList<Boolean>();
        ArrayList<Byte> alphabet=new ArrayList<Byte>();
        ArrayList<ArrayList<Integer>> child=new ArrayList<ArrayList<Integer>>();
        
        // initialize the root node of the tree
        isAWord.add(new Boolean(false));// the root is not a word
        ArrayList<Integer> intArray=new ArrayList<Integer>(26);
        for(int i=0;i<26;i++){
            intArray.add(new Integer(0));
        }
        child.add(intArray);// the root must have childrens
        alphabet.add(new Byte((byte)-1));// the root shouldn't have alphabet,put -1 to hold the place

        // add all VALID words to the tree
        while(scan.hasNext()){
            String str=scan.next().toUpperCase();// case insensitive and use all uppse case

            if(isValidWord(str)){
                int p=0;// the virtual pointer of the tree nodes, start with the root
                for(int i=0;i<str.length();i++){
                    if(child.get(p).get(str.charAt(i)-'A')==0){// if that child has not been added yet
                        newDictionaryNode(p,str.charAt(i),child,isAWord,alphabet);
                    }
                    p=child.get(p).get(str.charAt(i)-'A');// move to the target node

                    if(str.charAt(i)=='Q'){// skip the Qu
                        i++;
                    }
                }
                isAWord.set(p,true);// set the node represent a word
            }
        }
        scan.close();

        // fill in the data
        d=new int[isAWord.size()][];// the optimized data array for the dictionary tree
        for(int i=0;i<isAWord.size();i++){
            int childCount=0;// count valid childrens
            for(int j=0;j<child.get(i).size();j++){
                if(child.get(i).get(j)!=0){
                    childCount++;
                }
            }

            if(childCount!=0){
                d[i]=new int[childCount];// allocate exactly amount of memory
                childCount=0;// reused
                for(int j=0;j<child.get(i).size();j++){
                    if(child.get(i).get(j)!=0){
                        d[i][childCount]=dCompose(isAWord.get(child.get(i).get(j)),(byte)j,child.get(i).get(j));
                        childCount++;
                    }
                }
            }
        }

        board=new byte[4][4];
        visited=new boolean[4][4];
        traceByte=new byte[20];
        stringByte=new byte[20];
        traceXY=new int[20][2];
        answers=new ArrayList<Word>();
        myWords=new Word[20];
    }

    public void newDictionaryNode(int p,char c,ArrayList<ArrayList<Integer>> child,ArrayList<Boolean> isAWord,ArrayList<Byte> alphabet){
        child.get(p).set(c-'A',child.size());// point to new node

        ArrayList<Integer> intArray=new ArrayList<Integer>(26);
        for(int i=0;i<26;i++){
            intArray.add(new Integer(0));
        }
        child.add(intArray);// new node

        isAWord.add(false);
        alphabet.add((byte)c);
    }

    public boolean isValidWord(String str){
        if(str.length()<3||16<str.length()){// the length must be between 3,16
            return false;
        }else{
            for(int i=0;i<str.length()-1;i++){// check for single Q
                if(str.charAt(i)=='Q'&&str.charAt(i+1)!='U'){// invalid if Q is not folowed by U
                    return false;
                }
            }
            return str.charAt(str.length()-1)!='Q';// check if the last char is Q
        }
    }

    // bit 31: isAWord, 30-23: alphabet, 22-0: childPosition. 23 bits are more than enough for the standatd dictionary
    public int dCompose(boolean isAWord,byte alphabet,int child){return ((isAWord?1:0)<<31)|(((int)alphabet)<<23)|(child);}
    // this function is called to disable a word in dictionary as we only need the word once
    public void dDisable(int p,int index){d[p][index]&=0x7fffffff;}// set the bit 31 to 0

    // the folowing three functions are used to extract fields from entry of d[][]. all using bitwise operation
    public boolean dGetBoolean(int data){return data<0;}// it's actually testing if the bit 31 is 1
    public byte dGetByte(int data){return (byte)(data>>23);}// it shift all bit to the left for 23 slots, and the (byte) cuts off the bit 8
    public int dGetInt(int data){return data&0x7fffff;}// it uses bit-and to clear the higher 9 bits

    public Word[] getWords(char[][] board){
        // translate char[][] to byte[][]
        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                this.board[i][j]=(byte)board[i][j];
            }
            // System.out.printf("[%s]\n",Arrays.toString(board[i]));
        }

        // for each box on board, do dfs()
        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                traceXY[0][0]=i;
                traceXY[0][1]=j;
                dfs(0,i,j,0);
            }
        }

        // System.out.printf("[answers=%s]\n",answers);
        Collections.sort(answers);// all answers are sorted based on length.
        
        for(int i=0;i<myWords.length;i++){
            myWords[i]=answers.get(i);
        }

        // System.out.printf("[myWords=%s]\n",Arrays.toString(myWords));
        return myWords;
    }
    
    public void dfs(int p,int x,int y,int depth){
        // System.out.printf("[dfs(%d,%d,%d,%d)]\n",p,x,y,depth);
        visited[x][y]=true;
        for(int i=0;i<NEXT_STEP.length;i++){
            int newX=x+NEXT_STEP[i][0],newY=y+NEXT_STEP[i][1];
            int index=findIndex(p,newX,newY);
            // System.out.printf("[dfs(%d,%d,%d,%d) newX=%d newY=%d index=%d]\n",p,x,y,depth,newX,newY,index);

            if(index!=-1){
                traceByte[depth]=(byte)(dGetByte(d[p][index])+'A');
                traceXY[depth+1][0]=newX;
                traceXY[depth+1][1]=newY;
                
                dfs(dGetInt(d[p][index]),newX,newY,depth+1);

                if(dGetBoolean(d[p][index])){
                    dDisable(p,index);

                    int j,k;
                    for(j=0,k=0;j<=depth;j++){
                        stringByte[k++]=traceByte[j];
                        if(traceByte[j]==(byte)'Q'){
                            stringByte[k++]=(byte)'U';
                        }
                    }

                    Word aWord=new Word();
                    aWord.setWord(new String(stringByte,0,k));
                    for(j=1;j<=depth+1;j++){
                        aWord.addLetterRowAndCol(traceXY[j][0],traceXY[j][1]);
                    }
                    answers.add(aWord);
                }
            }
        }
        visited[x][y]=false;
    }

    public int findIndex(int p,int x,int y){
        // System.out.printf("[p=%d,x=%d,y=%d,d[p].length=%d]\n",p,x,y,d[p].length);
        if(d[p]!=null&&0<=x&&x<4&&0<=y&&y<4&&!visited[x][y]){
            for(int i=0;i<d[p].length;i++){
                // System.out.printf("[findIndex(%d,%d,%d) board[%d][%d]=%s dGetByte(d[%d][%d])=%s]\n",p,x,y,x,y,board[x][y],p,i,dGetByte(d[p][i]));
                if(board[x][y]==dGetByte(d[p][i])+'A'){
                    return i;
                }
            }
        }
        return -1;
    }
}
