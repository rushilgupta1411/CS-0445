# CS 0445 – Algorithms and Data Structures 1 – Assignment #4

Due: Friday December 9th @ 11:59pm on Gradescope

Late submission deadline: Sunday December 11th @11:59pm with 10% penalty per late day

You should submit `StreamingRadio.java`, the java files for any exception classes that you had to define, and the completed Assignment Information Sheet `InfoSheet.md` to the GradeScope link on Canvas. You must also answer the post-assignment quiz on GradeScope per its specified deadline.

(_Assignment adapted from Dr. Garrison’s CS 0445 class. The assignment has been adapted by Joe Monaco as part of his Chancellor's Undergraduate Teaching Fellowship in Spring 2019._)

## Table of Contents

- [Overview](#overview)
- [Background](#background)
- [TASK 1 - Load a puzzle from a 2-d array of letters](#TASK-1---Load-a-puzzle-from-a-2-d-array-of-letters)
- [TASK 2 - Retrieve a deep copy of the underlying 2-d array of letters](#TASK-2---Retrieve-a-deep-copy-of-the-underlying-2-d-array-of-letters)
- [TASK 3 - Find a word in the puzzle](#TASK-3---Find-a-word-in-the-puzzle)
- [Test Program](#test-program)
- [Submission Requirements](#submission-requirements)
- [Rubrics](#rubrics)

## Overview
 
Imagine that you are starting a streaming radio business. Partnerships have been established to provide you with audio content, and you will stream that content to users. You plan to use your knowledge of what songs your users like/dislike to make effective suggestions for new songs that they will like.
In this assignment, you will be **only designing** an abstract data type (ADT) for storing the service’s music library and users’ ratings of songs. This ADT will be used by client applications as well as for administrative uses (like adding new songs to the library). Once you complete this ADT, you could provide it to the programmers of the client applications, and they will be able to develop applications based on this specification. You could also distribute the ADT interface to the programmers in charge of implementing the functionality of the data structure – like what you did with the interfaces in Assignment 1, 2, and 3. Then these two groups (client programmers and data structure developers) can work in parallel on their respective components; the code of the resulting components will integrate since it follows the “contract” that your ADT provides.


You will specify the full set of details that **a client needs to know** in order to use the data structure. You do not need to consider implementation-level details, since you are only writing the ADT interface. So, you should not specify the details of how the operations will be carried out or how the data will be organized in memory. The implementation details should be left up to the implementers.

## Details

You must write an abstract data type as a Java interface. We have given you the names of several abstract methods that the data structure would need to support. You must specify them in the `Javadoc` comments above each method.


You should consider all possible **corner cases** in your specification and describe what the class should do in all cases. A client that reads your ADT should know all the things that could go wrong, and how each will be handled.
Error cases should be unambiguous to the client, and the client should always know which specific error was encountered. For some corner cases, you will need to make a design decision regarding the best way to handle them. You have the freedom to make these decisions any way you wish as long as you are consistent, thorough, and clear in your description.

Note that in `Javadoc` we document parameters by using the `@param` name command, return values with `@return`, and exceptions with `@throws`.


The following files are provided to you:
1. `StreamingRadio.java` – The interface that you will modify.
2. `User.java`, `Song.java`, and `Station.java` – Provided for you to use as return types and parameters in `StreamingRadio`. You do not need to modify these files.
3. `src/doc` – Directory containing the `Javadoc`. Open `index.html` with your browser to see the documentation.

There are **8 methods** in the `StreamingRadio` ADT. One of them has been specified for you, as an example of the kind of specification we are looking for. So, you must complete the specification of the **remaining seven methods**. You will be adding parameters, return types, and exceptions to throw to each. Each one has a short description in `StreamingRadio.java`, so read those.

1. `addSong`
2. `removeSong`
3. `addToStation`
4. `removeFromStation`
5. `rateSong`
6. `clearRating `(This one has been completed for you. You can change it for your needs, if you want.)
7. `predictRating`
8. `suggestSong`

**You are not required to write implementations of these methods.**

## NOTES

Assume that the set of users and radio stations are fixed. That way, you do not need to include
operations to change the collections.

You can use the standard java exceptions where they make sense. If none of them fits, you are free
to define your own exceptions. If you define your own exception, you must create a `.java` file for the exception and submit that file as well.
Remember, you are specifying the interface to the client. The implementors will use this interface, but they will come up with their own implementation.
**YOU ARE NOT IMPLEMENTING THE METHODS.**

## TESTING

Well, you’re not writing any code, but you are writing documentation, and your interface should compile with `javac`. You won’t be able to run it, of course. Use this command to produce documentation,
```
javadoc -d doc *.java
```

## Submission Requirements

You must submit your Github repository to GradeScope. We will only grade the following files:

1. `StreamingRadio.java `after completing the `Javadoc` specifications of all the methods
2. The java files for any exception class that you needed to define
3. The filled Assignment Information Sheet `InfoSheet.md`

_The idea from your submission is that your TA (and/or the autograder if available) can compile and run your programs from the command line WITHOUT ANY additional files or changes, so be sure to test it thoroughly before submitting it. If the TA (and/or the autograder if available) cannot compile or run your submitted code it will be graded as if the program does not work.
If you cannot get the programs working as given, clearly indicate any changes you made and clearly indicate why on your Assignment Information Sheet.  You will lose some credit for not getting it to work properly, but getting the main programs to work with modifications is better than not getting them to work at all.  A template for the Assignment Information Sheet can be found in this repository. You do not have to use this template, but your sheet should contain the same information._

_Note: If you use an IDE, such as NetBeans, Eclipse, or IntelliJ, to develop your programs, make sure the programs will compile and run on the command-line before submitting – this may require some modifications to your program (e.g., removing package information)._

## Rubrics 

Please note that if an autograder is available, its score will be used as a guidance for the TA, not as an official final score. Please also note that the autograder rubrics are the definitive rubrics for the assignment. The rubrics below will be used by the TA to assign partial credit in case your code scored less than 40% of the autograder score. If your code is manually graded for partial credit, the maximum you can get for the autograded part is 60%.

Item|Points
----|------
Code compiles (correct form)|	5
`Javadoc`generates documentation without error | 5 points
Specification of addSong | 8 points
Specification of removeSong | 8 points
Specification of addToStation | 12 points
Specification of removeFromStation |  12 points
Specification of rateSong | 12 points
predictRating | 14 points
Specification of suggestSong | 12 points
Post-assignment quiz on GradeScope | 7 points
Assigment Information Sheet | 5 points
