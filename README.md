# COMP2021-1D

|    Name                        | UP Number | Grade | Percentage |
| ------------------------------ | :-------: | :---: |   :---:    | 
| Davide António Ferreira Castro | 201806512 | 18    |    26%     | 
| Diogo Guimarães do Rosário     | 201806582 | 18    |    22%     |
| Henrique Melo Ribeiro          | 201806529 | 18    |    26%     |
| Tiago Duarte da Silva          | 201806516 | 18    |    26%     |

GLOBAL Grade of the project: 18

## SUMMARY: (Describe what your tool does and its main features.)

Our tool allows the compilation of Java-- files into a jasmin file compatible with other Java bytecode.  
  
To run our project, we first need to create a jar file of it.  
After that, we run one of the following the commands:  
- `java Main [-r=<num>] [-o] <input_file.jmm>`
- `java -jar COMP2021-1D.jar [-r=<num>] [-o] <input_file.jmm>`

We start by making a syntactic analysis of the provided file to check if it respects our grammar.  
During this phase, any error made in a while expression, when declaring variables, assigning variables and calling methods are reported with an error message.  
  
The second phase is a semantic analysis. This checks if what is written in the file makes sense and is consistent.  
Here we report mismatches of variable assignments, returns values and many other things. We also try to "clean" up the code a bit by, for example, deleting variables that are not being used anywhere.
We also do some optimizations in this phase such as constant propagation and folding, making the code more efficient.  
  
The third phase is generation of `OLLIR` code.  
This is the code that makes the transition to jasmin code easier. Here we also do some optimizations, for example, using while templates that require one less `goto` instruction.

The last phase is the jasmin code generation phase.  
TODO
#### Main features

- Method overloading
- Good error recovery
- Descriptive error reports
- Constant propagation & folding
- Removal of unused variables (after all other optimizations)
- Initialization of uninitialized variables

## DEALING WITH SYNTACTIC ERRORS:

(Describe how the syntactic error recovery of your tool works. Does it exit after the first error?)

Whenever the compiler detects a syntactic error, it attempts to recover, in order to find further errors in the code.  
This error recovery was implemented in the statements (imports, var declarations, assignements, method calls, etc) and while loops.  
These errors are added to a list called `reports` and later shown to the user.
  
These errors are detected inside a `try` statement and dealt with inside the `catch`statement.  
To deal with these errors we created a `error_skipto` function that receives as a parameter a token value, and skips all tokens until it reaches the one received as parameter.

## SEMANTIC ANALYSIS:

The following semantic rules implemented by our tool.

**Errors:**
- Undefined/Undeclared variables, methods and identifiers
- Bad method arguments
- Array size or index not `int`
- Call of `length`  on non array
- Duplicate method or field declarations
- Type mismatches
- Incompatible operations
- Invalid Types (takes imports into consideration)
- Method calls on literal types (`int`, `boolean`)
- Instantiation of literal types using `new`
- Invalid use of `this` inside `static Main`
- Conflicting method signatures
- Return type mismatch

**Warnings:**
- Uninitialized variables
- Unused variables


## CODE GENERATION:

(describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)


## TASK DISTRIBUTION: 

|     Task     | Members responsible    |
| :----------: | :--------------------- |
| Parser       | Henrique, Diogo        |
| Semantic     | Tiago                  |
| Ollir        | Henrique, Davide       |
| Jasmin       | Davide                 |
| Optimization | Tiago, Henrique        |
| Tests        | Tiago, Henrique, Diogo |


## PROS:

- Method calls respect the rules of Java (inside ifs, whiles or even other method calls)
- Method overloading with method resolution
    - Finds the closest match to the paramenters and uses it if there are not conflicting signatures
    - Useful when combined with external method calls whose return type is unknown
- Good error recovery
- Descriptive error reports
- Constant propagation & folding
- Removal of unused variables (after all other optimizations)
- Initialization of uninitialized variables
- Use of optimized jasmin instructions


## CONS:

- No register allocation
- Some errors bypass our error recovery
    - `a = 2; = 3;` would stop the syntactic analysis
