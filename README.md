# CS 0445 – Assignment #3 - Java API Design: Library System [^1]

[^1]: Assignment adapted from Dr. Garrison’s CS 0445 class. Joe Monaco has adapted the assignment for his Chancellor's Undergraduate Teaching Fellowship in Spring 2019.

You should:

* Submit:
  * `LibrarySystem.java`
  * Any custom exception classes you define (e.g., `OverdueUserException.java`)
* Answer the **post-assignment quiz** on GradeScope per its specified deadline

**Note:** This assignment was developed with the help of OpenAI ChatGPT to brainstorm and generate parts of the scaffolding and documentation files to speedup prototyping.

## Table of Contents

* [Overview](#overview)
* [Details](#details)
* [Null Safety](#null-safety)
* [Exceptions vs Optional](#exceptions-vs-optional)
* [Common Constraints](#common-constraints)
* [Notes](#notes)
* [Testing](#testing)
* [Submission Requirements](#submission-requirements)
* [Rubrics](#rubrics)

## Overview

In this assignment, you will **design a Java interface** for a library management backend system. The interface will define a contract for managing users, books, and libraries: including borrowing, returning, and reserving books; computing fines; and more.

Your job is to define this contract by writing **complete and detailed ****************************************************`Javadoc`**************************************************** specifications** for the methods in the interface. This design should guide both the developers of the system and any clients who use it (e.g., UI programmers, admin tools).

You are **not** implementing the system — only writing the API.

## Details

You will modify the `LibrarySystem.java` file. It contains a list of method names related to key library operations.

* One method (`borrowBook`) has been completed for you as an example. It includes:

  * Parameters (`User`, `Book`, `Library`, and a `dueDate`)
  * A meaningful return value
  * Clear and exhaustive Javadoc that covers:

    * Normal behavior
    * Error conditions
    * Exceptions thrown

* Your task is to complete **the remaining methods** with:

  * Realistic parameters
  * Return types (if needed)
  * Proper exceptions (Java standard or custom)
  * Rich, clear `Javadoc` comments documenting:

    * Each parameter (`@param`)
    * The return value (`@return`)
    * All thrown exceptions (`@throws`)
    * All corner and error cases clients should know

## Null Safety

Your methods must handle and clearly **document what happens if a ****************************************************`null`**************************************************** parameter is passed**. For example:

```java
@throws NullPointerException if the `book` or `user` is null
```

This tells the client:

* That nulls are not allowed
* That they need to check before calling your method

Use `NullPointerException` when:

* The client violates the contract by passing `null`
* The method *cannot proceed meaningfully* with a `null` input

Avoid silently ignoring or tolerating `null` values unless absolutely required.

## Exceptions vs Optional

When writing the API, you may face cases where a method might **fail** or **produce no result**. Here’s how to decide what to return:

### Use `Exception` when:

* The method is **misused** (e.g., user already has the book)
* The failure is **unexpected** or represents a rule violation

Example:

```java
@throws IllegalStateException if the book is already borrowed
```

### Use `Optional<T>` when:

* A method might *legitimately* have **no result**
* The failure is **expected and not exceptional**

Example:

```java
Optional<Book> suggestBook(User user);
```

This is appropriate if the system **couldn’t find a good suggestion** but everything was used correctly.

> ✨ For this assignment, you may use `Optional<T>` or exceptions, but **be consistent and justify your choice** in the Javadoc.

## Common Constraints

When designing your methods, be sure to consider constraints like:

* ✅ **User eligibility**:

  * Has overdue books?
  * Reached borrowing limit?
* ✅ **Book availability**:

  * Already borrowed or reserved?
  * Belongs to a different library?
* ✅ **Date checks**:

  * Is the due date in the future?
  * Is the book past due?
* ✅ **Reservation logic**:

  * Is the user already in the queue?
  * Is the book available now?
* ✅ **Object identity**:

  * Does the user/book/library exist in the system?
  * Is the user registered at the library?

You must document these in your Javadoc using `@throws` clauses and detailed method descriptions.

## Notes

* You should **not** implement the methods — only write the interface.
* You may use standard Java exceptions (`IllegalArgumentException`, `IllegalStateException`, `NullPointerException`).
* If none fit, you may create a custom exception (e.g., `OverdueUserException.java`). Be sure to submit the class file with the assignment.
* Assume that user and library collections are fixed and pre-populated.

## Testing

You are writing documentation, not code. However, your `LibrarySystem.java` file must **compile without errors**, and you should be able to generate clean documentation using:

```bash
javadoc -d doc *.java
```

Check that:

* All methods are documented
* Javadoc runs without warnings
* The interface compiles using `javac`

## Submission Requirements

Submit your Github repository to GradeScope with the following files:

1. `LibrarySystem.java` — complete with full `Javadoc`
2. Any custom exception `.java` files, if needed

Your interface must:

* Compile cleanly
* Be fully documented with Javadoc
* Use standard Java types or those provided (`User`, `Book`, `Library`)
* Have no missing return types, parameter types, or annotations

💡 *Make sure your code compiles without packages, and works from the command line.*

## Rubrics

| Item                                          | Points  |
| --------------------------------------------- | ------- |
| Code compiles (correct form)                  | 5       |
| Javadoc generates documentation without error | 5       |
| Specification of returnBook                   | 10       |
| Specification of registerUser                 | 9      |
| Specification of reserveBook                  | 12       |
| Specification of cancelReservation            | 10      |
| Specification of reportLostBook               | 10      |
| Specification of computeFine                  | 10     |
| Specification of transferBookBetweenLibraries | 12      |
| Specification of suggestBook                  | 10       |
| Post-assignment quiz on GradeScope            | 7       |
| **Total**                                     | **100** |
