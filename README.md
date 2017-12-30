# CSE2010_TermProject

Presented to you with pride by Xu Zhen, Tariq Maashani and Jim Harrell.

## Description
The goal of this term project is to implement a boggle player that plays the [Boggle](https://en.wikipedia.org/wiki/Boggle) game. Students should submit a ```BogglePlayer``` class that returns the longest 20 words it found according to the given dictionary and the board. The score was calculated based on the length of the words, the elapsed time and the memory consumed.

## Requirement
1. The ```BogglePlayer``` class should implement at least the constructor ```BogglePlayer(String wordFile)``` and the method ```Word[] getWords(char[][] board)```.
    1. The ```BogglePlayer(String wordFile)``` should read available words from the ```wordFile``` and do any preparation before getting the ```board ```. The construction time was not counted toward the score but should take less than 15 minutes.
    2. The ```Word[] getWords(char[][] board)``` was called with the randomly generated ```board```. This method should return at most 20 valid words with the path of each word within 3 minutes. The time spent and the memory used in this method was counted toward the score.
    3. As the way the score was calculated, the program should be as fast as possible and should use less memory while was able to find the longest words.
2. An ```EvalBogglePlayer``` class and the ```words.txt``` provided by the course instructor will be used to evaluate the ```BogglePlayer``` and give out the score.

## Analysis
1. Imagine playing a Boggle game with an enchanted dictionary in your hand. You can flip to any page, know the page range of any prefix and cross out a word or a range of words within a blink of an eye. When you start from the first box with, say, letter ```X```, you goto the page where all the words start with ```X``` while knowing the page range for ```X```-words is between the end page for ```Y``` and the start page for ```Z```. Then you goto the second box that is next to the first box, and found it has letter ```U``` on it. You goto the page within the range and found there is a word ```XU``` and it is possibly a prefix of more words! After you checked the third box with letter ```Z```, you found there is no ```XUZ``` in the dictionary and  there is no other words that uses ```XU``` as the prefix. This bad news made you feel depressed and you decided never go search the forth box because there is simply no hope of finding any longer words following that path. The tick-tock sound of the clock reminds you that the time is passing away as you wonder around. You pulled yourself together and chose another second-box. And the journey continues...
2. From this ~~lovely~~ story, we can conclude the basis of finding words:
    * Explore from this box
    * for other boxes adjacent to it
    * check validity of the word including the other box
    * if the branch is valid
    * make that box as base box and explore from that box
    * else the branch is not valid
    * do nothing and check for another box adjacent to this box
3. The basic algorithm should be the combination of [depth-first search](https://en.wikipedia.org/wiki/Depth-first_search) and [trie](https://en.wikipedia.org/wiki/Trie) (or "dictionary tree" as we prefer in this documentation). Naturally, the DFS algorithm generates a logical DFS tree. If we apply the DFS on the given board, the resulting tree should present all the possible letter sequences that could be found on the board, regardless validity. In the mean time, the dictionary tree should be used to store the dictionary because its constant time complexity for looking up a word, which is less than O(16) in this case. Binary search can also be used in this case with time complexity of around O(17), but was not preferred when combining with the DFS. As the DFS tree contains all letter sequences and the dictionary tree contains all valid words, the common branches of both tree will be the answer set.
4. While to find the common branch of two tree sounds difficult, we can implement the algorithm in such a way that while triversing one tree, we check the pending branch with another tree to decide if we actually proceed onto that branch. When checking, the pending branch should also present in another tree to be valid. This method is very effective that the expected time complexity is the common part of two tree and is less or equal to the size of the smaller tree. In practice, both tree are actually quite random, so the actual time complexity is hard to tell but proven to be very small.
5. In conclusion, the program will do DFS starting with all 16 boxes, trim branches with the dictionary tree and record the answer when encounter a true word.

## Memory Optimization
Java has more advanced and complete support in object-oriented programming than C++ in exchange of memory use and execution time. But the situation is, we are trying to save memory in a programming language that is not so memory-efficient. To tackle down this problem, we need to have a deeper understanding of how Java handles objects.

### How Java Handles Objects
* Let us first see examples of some standard Java objects. Objects have the 12 bytes padding in the front and arrays have 16 bytes padding in the front.
* ```java.lang.Integer``` ![java.lang.Integer](https://www.ibm.com/developerworks/library/j-codetoheap/figure2.gif)
* ```java.lang.String``` ![java.lang.String](https://www.ibm.com/developerworks/library/j-codetoheap/figure4.gif)
* Credit: ```https://www.ibm.com/developerworks/library/j-codetoheap/index.html```
* In the above examples, the space was wasted because of the padding for objects and for arrays. In contrast, all primitive types do not have such padding. So we have decided to avoid using objects during processing as much as possible to save memory.

### What We did
1. We need the ```Node``` object for storing the dictionary tree and the implementation is simple and straightforward.
```java
class Node{
    char c;
    boolean b;
    Node[] child;
}

```
2. The object reference is quite dangerous, because if we accidently overwrite any of the reference to a node, the whole subtree would be wiped and there is no way of getting it back even when we try to debyg the tree. So we reorganized all nodes into an array and changed the object referencing to integers.
```java
class Node{
    char c;
    boolean b;
    int[] child;
}
Node[] nodes;
```
3. Due to the space-wasting problem, the actual situation does not prefer the use of objects. We realized the individual fields of each node are primitive types and came up with the idea of separating each field and put them into three arrays. In this way, there is no real node but a concept of nodes when the code was actually implemented.
```java
char c[];
boolean b[];
int[][] child;
```
4. For the initial submission, we managed to store the whole tree using a 2-dimentional integer array. This 2-dimentional integer array has exactlly the same dimention as the ```int[][] child``` from the previous implementaion but packed with all the information needed. The key to achieve this is to divide a single integer into three parts. The first part uses 1 bit that stores the ```boolean``` value. The second part uses 8 bits that stores the ```byte```, which was reduced from ```char``` as we only have 26 letters. The rest 23 bits will be used to store the integer index as it was. There is possible overflow in the ```int``` part as the available space was reduced but according to our testing, the standard dictionary uses less than 10% of available space and we even have at least two more bits to spare! See [this commit](https://github.fit.edu/zxu2016/CSE2010_TermProject/commit/f05a127c30e9a11a6890848a57169a932de380e6) for detailed implementation.
5. For the final submission, we reduced the 2-dinentional array to a 1-dimentional array. The new array is a flatten-out version of the old array and have exactly the same layout inside each integer. This new array further reduced the memory use by elimilating the headers for the secondary arrays. See [this commit](https://github.fit.edu/zxu2016/CSE2010_TermProject/commit/b9d33ce15cc4c75fae8080da116b629cc90f307f) for detailed implementation.

## Performance
> With blazing fast processing speed and minimized memory consumption, this program has the tendency to bring sunshine to embedded devices! The object-less design of the core algorithm makes it easy to implement in C or assembly language for further optimization.

| Submission | Points | Time(s) | Memory(MB) | Performance |
| --- | --- | --- | --- | --- |
| Initial | 247.2 | 0.00573235 | 42.6 | 127.231 |
| InitialHidden | 220.9 | 0.00542515 | 42.6 | 115.405 |
| Final | 247.2 | 0.00554522 | 32 | 163.471 |
| FinalHidden | 220.9 | 0.00530034 | 32 | 134.833 |

## Files and What Do They Do
```
CSE2010_TermProject
├── BatchEvalBogglePlayer.java  // This file was slightly changed to output differently for batch testing.
├── BatchRun.sh                 // This is the shell script that tests the program for however times you want!
├── BogglePlayer.java           // This is where the spotlight shines on.
├── EvalBogglePlayer.java       // The official program for evaluating BogglePlayer
├── Experiment                  // A small experiment written in C++ to test the word filter
│   ├── WordFilter              // The compiled file from WordFilter.cpp. Was not intended to be here.
│   ├── WordFilter.cpp          // The C++ implementation of the word filter
│   ├── filtered words.txt      // There are words that were not qualified!
│   └── words copy.txt          // The exact same copy of ../words.txt
├── Makefile                    // You know what it is, don't you?
├── Presentation.pptx           // Just a brief presentation that took 30 minutes long.
├── README.md                   // The file you are looking at.
├── Unused                      // These two classes were copied and pasted into BogglePlayer.java, so we have only 1 file to submit!
│   ├── Location.java           // This is a bundle of two integers.
│   └── Word.java               // This is the Word class that stores 
├── termProject.pdf             // The original question.
└── words.txt                   // A      ton of words!
```

## Lisence
According to the Academic Honesty Policy of Florida Institute of Technology, copying the source code, in part or in whole, from this project and use for your own purpose without properly citing this project is considered plagarism and is punishable by the university policy. Please refer to the [Student Handbook](https://policy.fit.edu/student-handbook), [Academic Honesty](https://policy.fit.edu/Student-Handbook/Standards-and-Policies/2490) section for details. You have been warned, proceed at your own risk.

## Warranty
The source code from this project comes with absolutely no warranty. The authors of this project take no responsiblity for any possible damage caused by any form of application of any source code from this project. You have been warned, proceed at your own risk.

## Finally
If you enjoyed and/or learned something from this project, please leave a star! We are looking forward to bring more quality projects to GitHub.fit.edu!
```
┏━╸┏━┓┏━╸┏━┓┏━┓╺┓ ┏━┓   ╺┳╸┏━╸┏━┓┏┳┓┏━┓┏━┓┏━┓ ┏┓┏━╸┏━╸╺┳╸
┃  ┗━┓┣╸ ┏━┛┃┃┃ ┃ ┃┃┃    ┃ ┣╸ ┣┳┛┃┃┃┣━┛┣┳┛┃ ┃  ┃┣╸ ┃   ┃
┗━╸┗━┛┗━╸┗━╸┗━┛╺┻╸┗━┛╺━╸ ╹ ┗━╸╹┗╸╹ ╹╹  ╹┗╸┗━┛┗━┛┗━╸┗━╸ ╹

┏┓ ╻ ╻    ╻ ╻╻ ╻   ╺━┓╻ ╻┏━╸┏┓╻
┣┻┓┗┳┛╹   ┏╋┛┃ ┃   ┏━┛┣━┫┣╸ ┃┗┫
┗━┛ ╹ ╹   ╹ ╹┗━┛   ┗━╸╹ ╹┗━╸╹ ╹
         ╺┳╸┏━┓┏━┓╻┏━┓   ┏┳┓┏━┓┏━┓┏━┓╻ ╻┏━┓┏┓╻╻
          ┃ ┣━┫┣┳┛┃┃┓┃   ┃┃┃┣━┫┣━┫┗━┓┣━┫┣━┫┃┗┫┃
          ╹ ╹ ╹╹┗╸╹┗┻┛   ╹ ╹╹ ╹╹ ╹┗━┛╹ ╹╹ ╹╹ ╹╹
          ┏┓╻┏┳┓   ╻ ╻┏━┓┏━┓┏━┓┏━╸╻  ╻  
           ┃┃┃┃┃   ┣━┫┣━┫┣┳┛┣┳┛┣╸ ┃  ┃  
         ┗━┛╹╹ ╹   ╹ ╹╹ ╹╹┗╸╹┗╸┗━╸┗━╸┗━╸

╺┳╸╻ ╻┏━┓┏┓╻╻┏    ╻ ╻┏━┓╻ ╻╻
 ┃ ┣━┫┣━┫┃┗┫┣┻┓   ┗┳┛┃ ┃┃ ┃╹
 ╹ ╹ ╹╹ ╹╹ ╹╹ ╹    ╹ ┗━┛┗━┛╹
```
