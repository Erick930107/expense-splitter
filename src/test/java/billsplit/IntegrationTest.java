// package billsplit;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

@SuppressWarnings("java:S5960") // 強制忽略 SonarLint 對於測試檔案中 Assertion 的生產環境誤判警告
public class IntegrationTest {
    private BillSplitCalculator calc;

    private static final String USER_A = "A";
    private static final String USER_B = "B";
    private static final String USER_C = "C";

    @BeforeEach
    void init() {
        calc = new BillSplitCalculator();
    }

    /**
     * 目標單元：addExpense() 與 getBalancesMap()
     * 整合步驟 1：多筆消費累積與成員排除
     * 納入類別：BillSplitCalculator
     * 對應情境：情境 3 (特定成員排除分攤)
     * Given：系統中存在 A、B、C 三位參與者。
     * When：
     * 1. A 墊 300 元，由 ABC 三人均分。
     * 2. A 墊 800 元，僅由 AB 兩人均分 (C 排除)。
     * Then：驗證 A 累積餘額應為 600，B 為 -500，C 為 -100。
     */
    @Test
    @DisplayName("整合步驟 1：多筆記帳與排除邏輯連動")
    void testStep1MultipleExpensesAndExclusion() {
        calc.addExpense(300, USER_A, Arrays.asList(USER_A, USER_B, USER_C), null);
        calc.addExpense(800, USER_A, Arrays.asList(USER_A, USER_B), null);
        
        Map<String, Double> res = calc.getBalancesMap();
        assertEquals(600.0, res.get(USER_A), 0.01);  
        assertEquals(-500.0, res.get(USER_B), 0.01); 
        assertEquals(-100.0, res.get(USER_C), 0.01); 
    }

    /**
     * 目標單元：addExpense()、optimizeDebts() 與 getBalancesMap()
     * 整合步驟 2：債務最佳化 (消除中間人)
     * 納入類別：BillSplitCalculator
     * 對應情境：情境 5 (債務最佳化)
     * Given：A 欠 B 100 元，B 欠 C 100 元。
     * When：執行 optimizeDebts()。
     * Then：B 的中轉債務應被完全抵銷（餘額歸零），系統僅剩 A 與 C 的轉帳關係。
     */
    @Test
    @DisplayName("整合步驟 2：執行最佳化演算法消除中間人")
    void testStep2DebtOptimization() {
        calc.addExpense(100, USER_B, Arrays.asList(USER_A), null);
        calc.addExpense(100, USER_C, Arrays.asList(USER_B), null);
        
        calc.optimizeDebts(); 
        
        Map<String, Double> res = calc.getBalancesMap();
        assertEquals(-100.0, res.get(USER_A), 0.01);
        assertEquals(0.0, res.get(USER_B), 0.01); 
        assertEquals(100.0, res.get(USER_C), 0.01);
    }

    /**
     * 目標單元：addExpense() 與 getBalancesMap()
     * 整合步驟 3：混合邊界情境之狀態累積
     * 納入類別：BillSplitCalculator
     * 補充情境：情境 8 (代墊者不分攤)、情境 9 (單人分攤)
     * Given：系統初始化。
     * When：
     * 1. A 幫 BC 代墊 200 元，A 本人不參與分攤。
     * 2. A 代墊 100 元，且分攤名單僅有 A。
     * Then：驗證 A 的總代墊餘額正確累積為 200 元 (200 + 0)。
     */
    @Test
    @DisplayName("整合步驟 3：混合邊界情境之狀態累積")
    void testStep3BoundaryScenarios() {
        calc.addExpense(200, USER_A, Arrays.asList(USER_B, USER_C), null);
        calc.addExpense(100, USER_A, Arrays.asList(USER_A), null);
        assertEquals(200.0, calc.getBalancesMap().get(USER_A), 0.01); 
    }

    /**
     * 目標單元：addExpense()、optimizeDebts() 與 getBalancesMap()
     * 整合步驟 4：雙向負債完全抵銷
     * 納入類別：BillSplitCalculator
     * 補充情境：情境 7 (債務完全抵銷清零)
     * Given：A 與 B 互欠 100 元。
     * When：執行債務最佳化演算法。
     * Then：所有成員之帳務餘額應精確回歸 0.0，且系統成功判定無須轉帳。
     */
    @Test
    @DisplayName("整合步驟 4：雙向負債完全抵銷之極端整合")
    void testStep4NetZeroOptimization() {
        calc.addExpense(100, USER_B, Arrays.asList(USER_A), null);
        calc.addExpense(100, USER_A, Arrays.asList(USER_B), null);
        calc.optimizeDebts(); 
        
        Map<String, Double> res = calc.getBalancesMap();
        for (double bal : res.values()) {
            assertEquals(0.0, bal, 0.01);
        }
    }
}