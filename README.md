# Atoms of Confusion Benchmark Suite (Java/JMH Edition)
This is an experimental evaluation tool for analyzing the impact of Atoms of Confusion on runtime performance and hardware utilization compared to clean code equivalents within the Java Virtual Machine (JVM).

## About the Project
This project consists of a suite of micro-benchmarks designed to test code snippets containing **Atoms of Confusion** (syntactically valid programming patterns that are highly confusing to humans).

The main goal is to answer the question: *Does the structural confusion in code also result in performance penalties for the machine, or is it merely a cognitive burden for the programmer?*

Java utilizes a Just-In-Time (JIT) compiler. This suite leverages the Java Microbenchmark Harness (JMH) and Linux hardware counters to determine if the JIT compiler successfully optimizes these confusing structures or if they still degrade CPU and L1 Cache performance.

⚠️ **Warning:** This serves purely as a laboratory for academic and personal testing and experimentation.

## Prerequisites
To compile the project and extract physical hardware metrics, your environment must meet the following requirements:
* **OpenJDK 25** (or compatible modern JDK)
* **Apache Maven** (3.9+)
* **Python 3** (for data extraction)
* **Linux Performance Counters (**`perf`**):** Required by the JMH `perfnorm` profiler to read hardware events.
  * **Ubuntu/Debian:** `sudo apt install linux-tools-generic linux-tools-common`
  * **WSL2:** You must enable hardware performance counters in Windows by adding `hardwarePerformanceCounters=true` to your `.wslconfig` file and restarting WSL

## Compilation
The benchmark is managed via Maven. Any time you modify the atoms in the source code, you must recompile the project to update the JMH annotation processor map.

Open the terminal in the `atoms-benchmark` directory and run:
```bash
mvn clean package
```
This generates the executable `benchmarks.jar` inside the `target/` directory

## Automated Data Collection Pipeline
The data collection is divided into a two-step automated pipeline. Unlike the C version, CPU and Memory (L1 Cache) metrics are captured simultaneously using the JMH `perfnorm` profiler.
Before using the collector, grant execution permissions:
```bash
chmod +x collector_java.sh
```
### 1. Data Collection (`collector_java.sh`)
This Bash script orchestrates the JMH execution. It pins the JVM to a single CPU core (`taskset -c 0`) to prevent context-switching and runs 30 isolated forks of the benchmark. It captures Execution Time, IPC, Branch Misses and L1 Cache data
```
./collector_java.sh
```
### 2. Data Extraction (`extractor_jmh.py`)
Because virtualized PMUs can sometimes fail to attach in time during rapid JVM forks, this Python script cleans the data. It parses the 30 JSON files, drops any execution where the hardware profiler failed to record instructions, calculate derived metrics (like operations per second), and exports everything to a structured spreadsheet.
```
python3 extractor_jmh.py
```
**Output:** `data.csv` (Formatted with semicolons and commas for importing into localized spreadsheet software)

## Running Manually
If you want to run a quick test without generating data files, you can execute the JAR directly. You can pass a regex filter to run a specific atom:
```
java -jar target/benchmarks.jar "conditionalOperator" -wi 3 -i 5 -f 1
```
### Important Execution Notes
* In the C version, optimizations were disabled to force the compiler to read the confused code. In Java, we cannot disable JIT optimizations easily without ruining the benchmark. Instead, this suite uses JMH's `@State` and `Blackhole.consume()` to force the CPU to compute the values, preventing Dead Code Elimination while allowing realistic JIT behavior.

## Repository Structure
* `pom.xml`: Maven configuration, compiler targets, and JMH dependencies.
* `src/main/java/com/atoms/MyBenchmark.java`: The core Java source file containing the paired implementations (Confused AC vs. Clean NAC) for the studied atoms.
* `collector_java.sh`: Bash orchestrator for isolated JVM fork iteration
* `extractor_jmh.py`: Python script for parsing JMH JSON outputs, filtering hardware errors, and generating the final dataset.
* `.gitignore`: Ensures compiled binaries (`target/`) and heavy raw JSON files remain local and out of the version control history.
