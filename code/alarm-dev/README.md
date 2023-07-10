# ALARM: Active LeArning Rowhammer Mitigations

## How to build and run
The code is using `make` and Maven (https://maven.apache.org/). In order to run ALARM, you'll need to 
build the `jar` file by running 
```
make
``` 
Then

- Find the generated `jar` file in `target`
- Run the code by 
  ```
  java -jar <path to the jar>/ALARM-1.0.jar
  ```

## Reproducing the  plots in the paper

In order to generate all the plots in the paper simply run
```
java -jar <path to the jar>/ALARM-1.0.jar --plotter 
```
The resulting plots will be in a directory named `plots`.

## All  options
We list all available options for our code:

```
-h, --help              Help

-l, --learning          Learning Algorithm (options: ttt, lstar)

-o, --oracle            Equivalence Oracle (options: rw, wp, rwp)

-m, --memory-size       Number of memory rows (options: a non-negative number)

-t, --trr-counters      Number of TRR counters (options: a non-negative number)

-b, --bits              Maximum number of bits to get flipped in a memory row (options: a non-negative number)

-n, --number            Number of accesses to a row in each step (options: a non-negative number)

-s, --trr-threshold     Minimum number of accesses required to a row in an interval to trigger TRR (options: a non-negative number)

-r, --rh-threshold      Minimum number of accesses required to adjacent rows in an interval to trigger a possible bit flip in a row (options: a non-negative number)

-i, --interval          Minimum number of read/write required to issue a refresh to all the memory rows (options: a non-negative number)

-v, --visualise         Visualise and show the output automata

-x, --steps             Maximum number of steps for the Random Walk Eq Oracle (options: a positive number)

-p, --plotter           Run the plotter and plot the figures

-a, --plotter-minimal   Run the plotter in a minimal way without averaging and plot the figures

-e, --ecc-off           Turn off ECC in the memory

-f, --factor            Select a specific parameter to run the plotter against it (options: a positive number in [0,7])

```

