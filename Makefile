EvalBogglePlayer: EvalBogglePlayer.class BogglePlayer.class Word.class Location.class words.txt
	java EvalBogglePlayer words.txt 

EvalBogglePlayer.class: EvalBogglePlayer.java BogglePlayer.class Word.class Location.class 
	javac EvalBogglePlayer.java

BatchEvalBogglePlayer: BatchEvalBogglePlayer.class BogglePlayer.class Word.class Location.class 
	java BatchEvalBogglePlayer

BatchEvalBogglePlayer.class: BatchEvalBogglePlayer.java BogglePlayer.class Word.class Location.class 
	javac BatchEvalBogglePlayer.java

BogglePlayer.class Word.class Location.class: BogglePlayer.java
	javac BogglePlayer.java 

clean:
	rm -r *.class