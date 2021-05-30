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

TODO: Describe a bit more

#### Main features

- Method overloading
- Good error recovery
- Descriptive error reports
- Constant propagation & folding
- Removal of unused variables (after all other optimizations)
- Initialization of uninitialized variables

## DEALING WITH SYNTACTIC ERRORS:

(Describe how the syntactic error recovery of your tool works. Does it exit after the first error?)

TODO: How does it actually work

Whenever the compiler detects a syntactic error, it attempts to recover, in order to find further errors in the code.  
This error recovery was implemented in the statements (imports, var declarations, assignements, method calls, etc) and while loops.

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
