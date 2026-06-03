package billsplit;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * 階段 3：整合測試[cite: 1]
 */
public class IntegrationTest {
    private BillSplitCalculator calc;

    @BeforeEach
    void init() {
        calc = new BillSplitCalculator();
    }

    /**
     * 整合步驟 1：多筆消費後的數據正確性[cite: 1]
     */
    @Test
    void step1_Integration() {
        calc.addExpense(300, "A", Arrays.asList("A", "B", "C"), null);
        calc.addExpense(150, "B", Arrays.asList("A", "B", "C"), null);
        Map<String, Double> res = calc.getBalancesMap();
        assertEquals(150.0, res.get("A"), 0.01);
    }

    /**
     * 整合步驟 2：演算法整合 (消除中間人)
     * 對應 Assignment 1 情境 5[cite: 1, 2]
     */
    @Test
    void step2_Algorithm() {
        calc.addExpense(100, "B", Arrays.asList("A"), null);
        calc.addExpense(100, "C", Arrays.asList("B"), null);
        calc.optimizeDebts();
        assertEquals(0.0, calc.getBalancesMap().get("B"), 0.01);
        assertEquals(100.0, calc.getBalancesMap().get("C"), 0.01);
    }
}