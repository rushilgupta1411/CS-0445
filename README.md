# CS 0445 – Algorithms and ADTs 1 – Assignment #3 [^1]

Due: Friday, July 12th, @11:59pm on Gradescope

Late submission deadline: Sunday, July 14th, @11:59pm with a 10% penalty per late day

You should: 
- submit:
   - `StreamingRadio.java`,
   - the Java files for any exception classes you had to define, and
   - the completed Assignment Information Sheet `InfoSheet.md` to the GradeScope link on Canvas.
- answer the **post-assignment quiz** on GradeScope per its specified deadline.

 [^1]: Assignment adapted from Dr. Garrison’s CS 0445 class. Joe Monaco has adapted the assignment for his Chancellor's Undergraduate Teaching Fellowship in Spring 2019.

## Table of Contents

- [Overview](#overview)
- [Details](#details)
- [Notes](#notes)
- [Testing](#testing)
- [Submission Requirements](#submission-requirements)
- [Rubrics](#rubrics)

## Overview
 
Imagine that you are starting a streaming radio business. Partnerships have been established to provide audio content, which you will stream to users. You plan to use your knowledge of what songs your users like/dislike to make sensible suggestions for new songs that they will like. In this assignment, you will be **only designing** an abstract data type (ADT) for storing your service’s music library and users’ ratings of songs. This ADT will be used by client applications and for administrative uses (e.g., adding new songs to the library). Once you complete this ADT, you would provide it to the programmers of the client applications, and they would be able to develop applications based on the ADT specification. You could also distribute the ADT interface to the programmers in charge of implementing the functionality of the ADT – like what you did with the interfaces in Assignments 1 and 2. Then, these two groups (client programmers and ADT developers) can work in parallel on their respective components; the code of the resulting components should integrate almost seamlessly since it follows the “contract” that your ADT provides.


You'll need to specify all the details that **a client needs to know** to use the ADT. You do not need to consider implementation-level information since you only write the ADT interface. So, you should refrain from specifying how the operations will be carried out or how the data will be organized in memory. The implementation details should be left up to the implementers.

## Details

You will specify an Abstract Data Type using a Java interface. We have given you the names of several methods the ADT would need to support. You must specify them using `Javadoc` comments above each method.


In your specification, you should consider as **many edge** cases as possible and describe what the ADT should do in each. A client who reads your ADT should know all the things that could _go wrong_ and how each will be handled. Error cases should be unambiguous to the client, and the client should always know which specific error was encountered. You must decide on the best way to handle some edge cases. You can make these decisions any way you wish if you are consistent, thorough, and clear in your description.

Note that in `Javadoc`, we document parameters using the `@param` name command, return values with `@return`, and exceptions with `@throws`.


The following files are provided to you:
1. `StreamingRadio.java` – The interface that you will modify.
2. `User.java`, `Song.java`, and `Station.java` – Provided for you to use as return types and parameters in `StreamingRadio`. You do not need to modify these files.
3. `src/doc` is the directory containing the Javadoc. Open `index.html` with your browser to see the documentation.

There are **eight methods** in the `StreamingRadio` ADT. One of them has been specified for you as an example of the kind of specification we are looking for. So, you must complete the specification of the **remaining seven methods**. You will be adding parameters, return types, and exceptions to throw to each. Each has a short description in `StreamingRadio.java`, so read those.

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

Assume that the set of users and radio stations are fixed. This is why we do not need to include
operations to change the user and station collections.

You can use the standard Java exceptions where they make sense. If none of them fits, you can define your exceptions. If you define your exception, you must create a `.java` file for the exception and submit that file as well.
To remind you, you are specifying the interface to the client. The implementors will use this interface, but they will come up with their implementation.
**YOU ARE NOT IMPLEMENTING THE METHODS.**

## TESTING

You’re not writing any code, but you are writing documentation, and your interface should compile with `javac`. You won’t be able to run it, of course. Use this command to produce documentation,
```
javadoc -d doc *.java
```

## Submission Requirements

You must submit your Github repository to GradeScope. We will only grade the following files:

1. `StreamingRadio.java `after completing the `Javadoc` specifications of all the methods
2. The Java files for any exception class that you needed to define
3. The filled Assignment Information Sheet `InfoSheet.md`

_The idea from your submission is that your TA (and/or the autograder if available) can compile and run your programs from the command line WITHOUT ANY additional files or changes, so be sure to test it thoroughly before submitting it. If the TA (and/or the autograder if available) cannot compile or run your submitted code, it will be graded as if the program does not work.
If you can't get the programs working as given, please indicate any changes you made and why on your Assignment Information Sheet.  You will lose some credit for not getting it to work properly, but getting the main programs to work with modifications is better than not getting them to work.  A template for the Assignment Information Sheet can be found in this repository. You do not have to use this template, but your sheet should contain the same information._

_Note: If you use an IDE, such as NetBeans, Eclipse, or IntelliJ, to develop your programs, make sure the programs will compile and run on the command line before submitting – this may require some modifications to your program (e.g., removing package information)._

## Rubrics 

Just so you know, if an autograder is available, its score will be used as guidance for the TA, not as an official final score. Please also note that the autograder rubrics are the definitive rubrics for the assignment. The TA will use the rubrics below to assign partial credit if your code scores <= 40% of the autograder score. If your code is manually graded for partial credit, the maximum you can get for the auto-graded part is 60% of the total autograder score.

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
Assignment Information Sheet | 5 points
