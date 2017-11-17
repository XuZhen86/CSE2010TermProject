import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

class Location{
    public int row, col;  // row and column index on the board
    public Location(int aRow, int aCol){row = aRow;col = aCol;}
    public Location(){row = 0;col = 0;}
    public String toString(){return String.format("[%d,%d]",row,col);}
}

class Word{
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
    public boolean equals(Word w){return this.word.equals(w.getWord());}
    public String toString(){return String.format("[%s,%s]",word,path);}
    public int compareTo(Word w){
        if(getPathLength()!=w.getPathLength()){return w.getPathLength()-getPathLength();}
        else{return w.getWord().compareTo(getWord());}
    }
}

class Seeker extends Thread{
    public static final int[][] NEXT_STEP={
        {-1,-1},{-1, 0},{-1, 1},
        { 0,-1},        { 0, 1},
        { 1,-1},{ 1, 0},{ 1, 1}
    };

    public int[][] d;
    public byte[][] board;
    public int startX,startY;

    public boolean[][] visited;
    public byte[] traceByte,stringByte;
    public int[][] traceXY;
    public ArrayList<Word> answers;

    public Seeker(int[][] d,int startX,int startY){
        // System.out.printf("[Seeker(%d,%d)]\n",startX,startY);
        this.d=d;
        this.startX=startX;
        this.startY=startY;

        visited=new boolean[4][4];
        traceByte=new byte[20];
        stringByte=new byte[20];
        traceXY=new int[20][2];
        answers=new ArrayList<Word>();
    }

    public void run(){
        traceXY[0][0]=startX;
        traceXY[0][1]=startY;
        dfs(0,startX,startY,0);
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

                if(dGetBoolean(d[p][index])){
                    int j,k;
                    for(j=0,k=0;j<=depth;j++){
                        stringByte[k++]=traceByte[j];
                        if(traceByte[j]==(byte)'Q'){
                            stringByte[k++]=(byte)'U';
                        }
                    }

                    Word aWord=new Word();
                    aWord.setWord(new String(stringByte,0,k));// FIX QU
                    for(j=1;j<=depth+1;j++){
                        aWord.addLetterRowAndCol(traceXY[j][0],traceXY[j][1]);
                    }
                    answers.add(aWord);
                }

                dfs(dGetInt(d[p][index]),newX,newY,depth+1);
            }
        }

        visited[x][y]=false;
    }

    public int findIndex(int p,int x,int y){
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

    public boolean dGetBoolean(int data){
        return data<0;
    }
    public byte dGetByte(int data){
        return (byte)(data>>23);
    }
    public int dGetInt(int data){
        return data&0x7fffff;
    }
}

class WordComparator implements Comparator<Word>{
    @Override    
    public int compare(Word a,Word b){
        if(a.getPathLength()!=b.getPathLength()){
            return b.getPathLength()-a.getPathLength();
        }else{
            return b.getWord().compareTo(a.getWord());
        }
    }
}
 
// all data structures were intended to be implemented in the "lowest level" to save memory
public class BogglePlayer{
    // this is the data, stores everything
    // should not be modified after initialized
    public int[][] d;
    public Seeker[][] seekers;
    public ArrayList<Word> answers;

    public BogglePlayer(String wordFile){
        // open the scanner
        Scanner scan;
        try{
            scan=new Scanner(new File(wordFile));
        }
        catch(FileNotFoundException e){
            System.out.println(e);
            return;
        }

        // build dictionary tree 
        ArrayList<Boolean> isAWord=new ArrayList<Boolean>();
        ArrayList<ArrayList<Integer>> child=new ArrayList<ArrayList<Integer>>();
        ArrayList<Byte> alphabet=new ArrayList<Byte>();

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
                    p=child.get(p).get(str.charAt(i)-'A');

                    if(str.charAt(i)=='Q'){// skip the Qu
                        i++;
                    }
                }
                isAWord.set(p,true);
            }
        }
        scan.close();

        // fill in the data
        d=new int[isAWord.size()][];
        for(int i=0;i<isAWord.size();i++){
            int childCount=0;
            for(int j=0;j<child.get(i).size();j++){
                if(child.get(i).get(j)!=0){
                    childCount++;
                }
            }

            if(childCount!=0){
                d[i]=new int[childCount];
                childCount=0;
                for(int j=0;j<child.get(i).size();j++){
                    if(child.get(i).get(j)!=0){
                        d[i][childCount]=dCompose(isAWord.get(child.get(i).get(j)),(byte)j,child.get(i).get(j));
                        childCount++;
                    }
                }
            }
        }

        // prepare seekers!
        seekers=new Seeker[4][4];
        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                seekers[i][j]=new Seeker(d,i,j);
            }
        }

        answers=new ArrayList<Word>();
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

    public int dCompose(boolean isAWord,byte alphabet,int child){
        return ((isAWord?1:0)<<31)|(((int)alphabet)<<23)|(child);
    }

    public Word[] getWords(char[][] board){
        byte[][] byteBoard=new byte[4][4];
        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                byteBoard[i][j]=(byte)board[i][j];
            }
            // System.err.printf("[%s]\n",Arrays.toString(board[i]));
        }

        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                seekers[i][j].board=byteBoard;
                seekers[i][j].start();
            }
            for(int j=0;j<4;j++){
                try{seekers[i][j].join();}
                catch(InterruptedException e){System.out.println(e);}
                answers.addAll(seekers[i][j].answers);
            }
        }

        Word[] myWords=new Word[20];
        Collections.sort(answers,new WordComparator());
        // System.out.printf("[answers=%s]\n",answers);

        myWords[0]=answers.get(0);
        int i,j;
        for(i=1,j=0;i<answers.size()&&j<myWords.length-1;i++){
            if(!answers.get(i).equals(myWords[j])){
                myWords[++j]=answers.get(i);
            }
        }

        // System.err.printf("[myWords=%s]\n",Arrays.toString(myWords));
        return myWords;
    }
}
