EvalBogglePlayer: EvalBogglePlayer.class BogglePlayer.class words.txt
	java EvalBogglePlayer words.txt 

EvalBogglePlayer.class: EvalBogglePlayer.java BogglePlayer.class
	javac EvalBogglePlayer.java

BogglePlayer.class: BogglePlayer.java
	javac BogglePlayer.java 

clean:
	rm -r *.class