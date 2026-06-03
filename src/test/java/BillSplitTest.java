package billsplit; // 需與核心程式碼套件一致

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * 階段 2：單元測試[cite: 1]
 */
public class BillSplitTest {
    private BillSplitCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new BillSplitCalculator();
    }

    /**
     * 目標單元：addExpense (均分)
     * 對應 Assignment 1 情境 1[cite: 1, 2]
     */
    @Test
    void testAddExpenseBasic() {
        calc.addExpense(300, "A", Arrays.asList("A", "B", "C"), null);
        Map<String, Double> res = calc.getBalancesMap();
        assertEquals(200.0, res.get("A"), 0.01);
        assertEquals(-100.0, res.get("B"), 0.01);
    }

    /**
     * 目標單元：addExpense (精確度)
     * 驗證餘數處理邏輯[cite: 1, 2]
     */
    @Test
    void testRounding() {
        calc.addExpense(100, "A", Arrays.asList("A", "B", "C"), null);
        double total = calc.getBalancesMap().values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(0.0, total, 0.01);
    }
}