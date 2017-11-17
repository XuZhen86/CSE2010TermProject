EvalBogglePlayer: EvalBogglePlayer.class BogglePlayer.class words.txt
	java EvalBogglePlayer words.txt 

BogglePlayer: BogglePlayer.class TestWords.txt
	java BogglePlayer TestWords.txt

EvalBogglePlayer.class: EvalBogglePlayer.java BogglePlayer.class
	javac EvalBogglePlayer.java

BogglePlayer.class: BogglePlayer.java
	javac BogglePlayer.java 

clean:
	rm -r *.class