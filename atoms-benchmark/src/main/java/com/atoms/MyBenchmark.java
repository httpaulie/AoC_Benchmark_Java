package com.atoms;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class MyBenchmark {

    private final int DATA_SIZE = 8192;
    private final int MASK = DATA_SIZE - 1;
    
    private int[] conditions;
    private int[] valuesA;
    private int[] valuesB;
    
    private int index = 0;
    
    // Contadores para testes de fluxo
    public int conditionalCounter = 0;
    public int inconditionalCounter = 0;

    @Setup(Level.Trial)
    public void setup() {
        conditions = new int[DATA_SIZE];
        valuesA = new int[DATA_SIZE];
        valuesB = new int[DATA_SIZE];
        
        for (int i = 0; i < DATA_SIZE; i++) {
            conditions[i] = i % 2;
            valuesA[i] = (i % 10) + 1;
            valuesB[i] = (i % 5) + 1;
        }
    }

    // Ações simuladas
    private void dependentAction(Blackhole bh) {
        conditionalCounter++;
        bh.consume(conditionalCounter);
    }

    private void independentAction(Blackhole bh) {
        inconditionalCounter++;
        bh.consume(inconditionalCounter);
    }

    // =========================================================
    // 1. INFIX OPERATOR PRECEDENCE
    // =========================================================
    @Benchmark
    public void infixPrecedenceAC(Blackhole bh) {
        int vA = valuesA[index & MASK];
        int vB = valuesB[index & MASK];
        index++;
        bh.consume(vA - 4 / vB);
    }

    @Benchmark
    public void infixPrecedenceNAC(Blackhole bh) {
        int vA = valuesA[index & MASK];
        int vB = valuesB[index & MASK];
        index++;
        bh.consume(vA - (4 / vB));
    }

    // =========================================================
    // 2. POST-INCREMENT/DECREMENT
    // =========================================================
    @Benchmark
    public void postIncrementAC(Blackhole bh) {
        int v2 = valuesA[index & MASK];
        index++;
        int v1 = v2++;
        bh.consume(v1);
        bh.consume(v2);
    }

    @Benchmark
    public void postIncrementNAC(Blackhole bh) {
        int v2 = valuesA[index & MASK];
        index++;
        int v1 = v2;
        v2 += 1;
        bh.consume(v1);
        bh.consume(v2);
    }

    // =========================================================
    // 3. PRE-INCREMENT/DECREMENT
    // =========================================================
    @Benchmark
    public void preIncrementAC(Blackhole bh) {
        int v2 = valuesA[index & MASK];
        index++;
        int v1 = ++v2;
        bh.consume(v1);
        bh.consume(v2);
    }

    @Benchmark
    public void preIncrementNAC(Blackhole bh) {
        int v2 = valuesA[index & MASK];
        index++;
        v2 += 1;
        int v1 = v2;
        bh.consume(v1);
        bh.consume(v2);
    }

    // =========================================================
    // 4. CONDITIONAL OPERATOR
    // =========================================================
    @Benchmark
    public void conditionalOperatorAC(Blackhole bh) {
        int v1 = valuesA[index & MASK];
        index++;
        int v2 = (v1 == 3) ? 2 : 1;
        bh.consume(v2);
    }

    @Benchmark
    public void conditionalOperatorNAC(Blackhole bh) {
        int v1 = valuesA[index & MASK];
        index++;
        int v2;
        if (v1 == 3) {
            v2 = 2;
        } else {
            v2 = 1;
        }
        bh.consume(v2);
    }

    // =========================================================
    // 5. ARITHMETIC AS LOGIC
    // =========================================================
    @Benchmark
    public void arithmeticAsLogicAC(Blackhole bh) {
        int v1 = valuesA[index & MASK];
        int v2 = valuesB[index & MASK];
        index++;
        boolean result = (v1 - 3) * (v2 - 4) != 0;
        bh.consume(result);
    }

    @Benchmark
    public void arithmeticAsLogicNAC(Blackhole bh) {
        int v1 = valuesA[index & MASK];
        int v2 = valuesB[index & MASK];
        index++;
        boolean result = (v1 != 3) && (v2 != 4);
        bh.consume(result);
    }

    // =========================================================
    // 6. LOGIC AS CONTROL FLOW
    // =========================================================
    @Benchmark
    public void logicAsControlFlowAC(Blackhole bh) {
        int v1 = valuesA[index & MASK];
        int v2 = valuesB[index & MASK];
        index++;
        // Short-circuiting para controle de fluxo
        boolean result = (v1 == ++v1) || (++v2 > 0);
        bh.consume(result);
        bh.consume(v1);
        bh.consume(v2);
    }

    @Benchmark
    public void logicAsControlFlowNAC(Blackhole bh) {
        int v1 = valuesA[index & MASK];
        int v2 = valuesB[index & MASK];
        index++;
        
        if (!(v1 + 1 > 0)) {
            v2 += 1;
        }
        v1 += 1;
        
        bh.consume(v1);
        bh.consume(v2);
    }

    // =========================================================
    // 7. REPURPOSED VARIABLES
    // =========================================================
    @Benchmark
    public void repurposedVariablesAC(Blackhole bh) {
        int limit = conditions[index & MASK] + 2; 
        index++;
        int execs = 0;
        
        // Loop intencionalmente confuso (v1 incrementado no loop interno). 
        // Trava de segurança (execs) adicionada para evitar infinite loop no benchmark.
        for (int v1 = 0; v1 < limit; v1++) {
            for (int v2 = 0; v2 < limit && execs < 5; v1++) {
                bh.consume(v2);
                execs++;
            }
        }
    }

    @Benchmark
    public void repurposedVariablesNAC(Blackhole bh) {
        int limit = conditions[index & MASK] + 2;
        index++;
        int execs = 0;
        
        for (int v1 = 0; v1 < limit; v1++) {
            for (int v2 = 0; v2 < limit && execs < 5; v2++) {
                bh.consume(v2);
                execs++;
            }
        }
    }

    // =========================================================
    // 8. CHANGE OF LITERAL ENCODING
    // =========================================================
    @Benchmark
    public void changeLiteralEncodingAC(Blackhole bh) {
        int v1 = 013; // Octal compilado nativamente
        bh.consume(v1);
    }

    @Benchmark
    public void changeLiteralEncodingNAC(Blackhole bh) {
        int v1 = Integer.parseInt("13", 8); // Parse em runtime
        bh.consume(v1);
    }

    // =========================================================
    // 9. OMITTED CURLY BRACES
    // =========================================================
    @Benchmark
    public void omittedCurlyBracesAC(Blackhole bh) {
        boolean cond = conditions[index & MASK] == 1;
        index++;
        
        if (cond) dependentAction(bh); independentAction(bh);
    }

    @Benchmark
    public void omittedCurlyBracesNAC(Blackhole bh) {
        boolean cond = conditions[index & MASK] == 1;
        index++;
        
        if (cond) { 
            dependentAction(bh); 
        } 
        independentAction(bh);
    }

    // =========================================================
    // 10. TYPE CONVERSION
    // =========================================================
    @Benchmark
    public void typeConversionAC(Blackhole bh) {
        int v1 = (int) 1.99f;
        bh.consume(v1);
    }

    @Benchmark
    public void typeConversionNAC(Blackhole bh) {
        int v1 = (int) Math.floor(1.99f);
        bh.consume(v1);
    }
}