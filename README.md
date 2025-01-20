# Project Assignment POO  - J. POO Morgan - Phase Two

**Name**: Stanislav Alexandru
**Group**: 324CAb
---

## Homework details

This is the new updated version of the banking system of the first phase. It has
many new features, such as `Businnes Account`, `Cashbacks` and some new commands.

## What I added new to my previous version ?

### **Package: `account`**

- **`BusinessAccount` Class**:  
  Which extends the `Account` class and implements the `SpecialAccountFunctions`
interface for adding employees to the list of employees of the business account.
I have a LinkedHashMap for the employees to keep track of the order of insertion.

### **Package: `commands`**:
  I changed almost everything how every command class was, because now I don't
have Utility classes, and every class from the commmands package implements
the `accept()` method from `VisitableCommand` class. In the `CommandExecutorVisitor`
class I have a `visit()` which has a parameter, which represents every task and
executes the `execute()` method from the neccessary class.

- **`BusinessReport.java`**:  
  Here I created the two new helper classes `DetailsCommerciantReport` and 
`DetailsTransactionReport` which are used to store important information about
the details for the specific report.

- **`CashbackStrategy NrOfTransactionsCashback and SpendingThresholdCashback`**:  
  I created two new classes which implement the `CashbackStrategy` interface.
I don't need to explain them, because they are explained on the OCW page of the
assigment.

- **`SplitPayment`**:  
  This class also has a helper class `SplitCommand` which is used to store the
split command information and a map of the accounts implicated in the split.

  This is all I wanted to clarify about the new features of my banking system,
about the commands part.

### **Package: `commerciants`**:

This package contains a new class `Commerciant` which is used to store the
commerciant information.

### **Package: `ebanking`**:

I pretty much changed the aspect of my `Ebanking` class, because I wanted to
adapt it to the visitor pattern of my commands. I also used a `Singleton` pattern.

## Why did I use these design patterns?

- **Singleton**:  
  I used this pattern for the `Ebanking` class, because I wanted to have only
one instance of the class that I would reset every test to not have any problems.

- **Visitor**:
  I used this design pattern for all my commands, because I wanted to have a
cleaner code and to have a better separation of concerns.

- **Strategy**:
  I used this design pattern for the cashbacks, because I wanted to have a
flexible way of adding new cashbacks in the future.

- **Factory**:
  I was already using this design pattern from the previous phase for accounts
to handle their creation and assignation better.

## How I developed the solution?
  This was a hard ongoing process of hard work to implement this new features,
such as interfaces with default methods, lambda expressions and new design patterns.
The most challenging part was to understand the statement of the homework.





