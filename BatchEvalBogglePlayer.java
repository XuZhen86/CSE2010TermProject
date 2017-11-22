import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;


public class BatchEvalBogglePlayer {
    private static ArrayList<String> dictionary = new ArrayList<>();
    private static final ArrayList<String> boggleDices = new ArrayList<>(Arrays.asList(
            "AAEEGN", "ABBJOO", "ACHOPS", "AFFKPS", "AOOTTW", "CIMOTU", "DEILRX", "DELRVY",
            "DISTTY", "EEGHNW", "EEINSU", "EHRTVW", "EIOSST", "ELRTTY", "HIMNUQ", "HLNNRZ"));

    public static void main(String[] args) throws IOException {

        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: EvalBogglePlayer wordFile [seed]");
            System.exit(-1);
        }

        // for getting cpu time
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (!bean.isCurrentThreadCpuTimeSupported()) {
            System.err.println("cpu time not supported, use wall-clock time:");
            System.err.println("Use System.nanoTime() instead of bean.getCurrentThreadCpuTime()");
            System.exit(-1);
        }

        //Read the dictionary
        Scanner file = new Scanner(new File(args[0]));
        while (file.hasNext()) {
            dictionary.add(file.nextLine().toUpperCase());
        }

        //Preprocessing in BogglePlayer
        //System.out.println("Preprocessing in BogglePlayer...");
        long startPreProcTime = bean.getCurrentThreadCpuTime();
        BogglePlayer player = new BogglePlayer(args[0]);
        long endPreProcTime = bean.getCurrentThreadCpuTime();

        //Stop if pre-processing runs for more than 10 minutes.
        double processingTimeInSec = (endPreProcTime - startPreProcTime) / 1E9;
        if (processingTimeInSec > 600) {
            System.err.println("Preprocessing time \"" + processingTimeInSec + " sec\" is too long...");
            System.exit(-1);
        }

        // report time and memory spent on preprocessing
        DecimalFormat df = new DecimalFormat("0.####E0");
        //System.out.println("Pre-processing in seconds (not part of score): " + df.format(processingTimeInSec));
System.out.print(processingTimeInSec+",");
        Runtime runtime = Runtime.getRuntime();
        //runtime.gc();
        //System.out.println("Used memory after pre-processing in bytes (not part of score): " + (runtime.totalMemory() - runtime.freeMemory()));
System.out.print((runtime.totalMemory() - runtime.freeMemory())+",");

        //Default seed if second argument is not passed
        long seed = 123456789;
        if (args.length == 2) {
            seed = Long.parseLong(args[1]);
        }
        //System.out.println("Playing Boggle...");


        Random rnd = new Random(seed);

        //Generate random board, and pass it to the BogglePlayer to play Boggle
        char[][] board = new char[4][4];
        int length = boggleDices.size();

        //Create random board
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int diceIndex = rnd.nextInt(length);
                String dice = boggleDices.get(diceIndex);
                board[i][j] = dice.charAt(rnd.nextInt(6));
                boggleDices.set(diceIndex, boggleDices.get(length - 1));
                boggleDices.set(length - 1, dice);
                length--;
            }
        }

        //Calculate the time taken to find the words on the board
        long startTime = bean.getCurrentThreadCpuTime();
        //Play the game of Boggle and find the words
        Word[] words = player.getWords(board);
        long endTime = bean.getCurrentThreadCpuTime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        double totalElapsedTime = endTime - startTime;

        //Convert elapsed time into seconds, and calculate the Average time
        totalElapsedTime = totalElapsedTime / 1.0E9;
	if (totalElapsedTime > 180)  // longer than 3 minutes
	    {
		System.err.println("player.getWords() exceeded 3 minutes");
		System.exit(-1);
	    }


        //Calculate points for the words found
        int totalPoints = calculatePoints(words, board);
        //System.out.printf("Points: %d\n", totalPoints);
System.out.print(totalPoints+",");

        //To format the Average time upto 4 decimal places.
        //DecimalFormat df = new DecimalFormat("0.####E0"); // moved to near initialization
        //System.out.println("Time in seconds: " + df.format(totalElapsedTime));
System.out.print(totalElapsedTime+",");

        // Get the Java runtime
        // Runtime runtime = Runtime.getRuntime();  // moved to near initialization
        // Run the garbage collector
        // runtime.gc();
        // Calculate the used memory
        
        //System.out.println("Used memory in bytes: " + memory);
System.out.print(memory+",");
        //OverAll Performance
        //System.out.printf("Overall Performance: %.4f\n", (totalPoints * totalPoints) / Math.sqrt(totalElapsedTime * memory));
System.out.println((totalPoints * totalPoints) / Math.sqrt(totalElapsedTime * memory));

        BogglePlayer player2 = player;  // keep player used to avoid garbage collection of player
    }

    private static int calculatePoints(Word[] words, char[][] board) {
        int points = 0;
        //Penalty if more than 20 words were returned
        if (words.length > 20) {
            for (int i = 20; i < words.length; i++) {
                points -= (words[i].getPathLength() - 2) * (words[i].getPathLength() - 2);
            }
        }

        //Calculate points for the first 20 words
        for (int index = 0; index < 20; index++) {
            Word w = words[index];
            if (w != null) {
                //Check if the word is unique
                boolean duplicate = false;
                for (int i = 0; i < index; i++) {
                    if (w.getWord().equals(words[i].getWord())) {
                        duplicate = true;
                    }
                }

                //If the word is duplicate then give penalty, else check if word is valid
                if (duplicate) {
                    points -= (w.getPathLength() - 2) * (w.getPathLength() - 2);
                } else {
                    points += checkForWordValidity(w, board);
                }
            }
        }
        return points;
    }

    private static int checkForWordValidity(Word word, char[][] board) {
        int length = word.getWord().length();

        //Check if word has atleast 3 letters
        if (length < 3)
            return -1;

        //Check if the letters of the word are adjacent on the board
        for (int i = 1; i < word.getPathLength(); i++) {
            if (squareDistance(word.getLetterLocation(i - 1), word.getLetterLocation(i)) > 2) {
                return -((length - 2) * (length - 2));
            }
        }


        //Check each letter on the board is used at most once and if letter are are the board or not
        boolean[][] used = new boolean[4][4];
        for (int i = 0, letterIndex = 0; i < word.getPathLength(); i++, letterIndex++) {

            int row = word.getLetterRow(i);
            int col = word.getLetterCol(i);

            if (used[row][col] || board[row][col] != word.getWord().charAt(letterIndex))
                return -((length - 2) * (length - 2));
            else
                used[row][col] = true;
            if(word.getWord().charAt(letterIndex) == 'Q')
                letterIndex++;
        }

        //Check if word exists in the dictionary
        if (!dictionary.contains(word.getWord().toUpperCase())) {
            return -((length - 2) * (length - 2));
        }


        return (length - 2) * (length - 2);
    }

    private static int squareDistance(Location l1, Location l2) {
        return (l1.row - l2.row) * (l1.row - l2.row) + (l1.col - l2.col) * (l1.col - l2.col);
    }
}
