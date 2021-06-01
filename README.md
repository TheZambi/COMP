# COMP2021-1D

|    Name                        | UP Number | Grade | Percentage |
| ------------------------------ | :-------: | :---: |   :---:    | 
| Davide António Ferreira Castro | 201806512 | 18    |    26%     | 
| Diogo Guimarães do Rosário     | 201806582 | 18    |    22%     |
| Henrique Melo Ribeiro          | 201806529 | 18    |    26%     |
| Tiago Duarte da Silva          | 201806516 | 18    |    26%     |

GLOBAL Grade of the project: 18

## SUMMARY:

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
In this phase, after parsing our `OLLIR` code with the `OLLIR` tool, we use its output to generate the jasmin code. 
We do this by selecting JVM instructions in jasmin format for each `OLLIR` method and their instructions so it can be translated into Java bytecode classes using the jasmin tool.

#### Main features

- Method overloading
- Good error recovery
- Descriptive error reports
- Constant propagation & folding
- Removal of unused variables (after all other optimizations)
- Initialization of uninitialized variables

## DEALING WITH SYNTACTIC ERRORS:

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

### Ollir



### Jasmin

Taking the previously generated `OLLIR` code, it is parsed using the provided `OLLIR` tool and we take its output (ClassUnit object - ollirClass), using it to generate the JVM code in jasmin format.
We make use of the variable table containing all registers and the list of instructions of each method to select the corresponding JVM instructions.

Lower cost instructions are selected in multiple cases, like *iinc* instructions on variable increments (i = i + 1) instead of *iadd* and *isub*, use of *if\<cond\>* on comparisons with 0
and the different constant loading instructions ('iconst_m1', 'iconst_n', 'bipush n', 'sipush n', 'ldc') depending on the constant value.

We also use the variable tables given by the `OLLIR` tool to calculate the local variables limit for each method and, throughout the generation of each method, a required stack size is calculated for each instruction and the maximum size in a method is selected as the stack limit.

As an extra optimization to stack size and operations, some operations are replaced by their results in the generation. When generating LTH operations with both operands being literals the result is pushed into the stack, for example, a = 1 < 2 will result in iconst_1; istore_1 (assuming 1 is the register of 'a').
This is also applied to ANDB operations (cases when at least one of the operands is a False literal or both are True literals) and NOTB operations.

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

- Method calls can be called anywhere
  - Due to the lack of knowledge of external method's signatures, we only support external calls made inside internal (own class) calls
  - If the return type is known by the user, attributing the method call to a variable will make the call successful
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
