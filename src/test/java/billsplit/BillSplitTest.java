// package billsplit;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

@SuppressWarnings("java:S5960") // 強制忽略 SonarLint 對於測試檔案中 Assertion 的生產環境誤判警告
public class BillSplitTest {
    private BillSplitCalculator calc;
    
    private static final String USER_A = "A";
    private static final String USER_B = "B";
    private static final String USER_C = "C";

    @BeforeEach
    void setUp() {
        calc = new BillSplitCalculator();
    }

    // --- 有效等價類測試 (Valid Equivalence Class) ---

    /**
     * 目標單元：addExpense()
     * 情境 1：基礎均分 (正常整除)
     * Given：系統中存在 A、B、C 三位參與者。
     * When：新增一筆 300 元消費由 A 代墊，並設定均分。
     * Then：A 的餘額應為 200 (300-100)，B 與 C 各為 -100。
     */
    @Test
    @DisplayName("有效等價類：情境 1 - 基礎均分計算")
    void testScenario1BasicSplit() {
        calc.addExpense(300, USER_A, Arrays.asList(USER_A, USER_B, USER_C), null);
        Map<String, Double> res = calc.getBalancesMap();
        assertEquals(200.0, res.get(USER_A), 0.01);
        assertEquals(-100.0, res.get(USER_B), 0.01);
        assertEquals(-100.0, res.get(USER_C), 0.01);
    }

    /**
     * 目標單元：addExpense()
     * 情境 2：餘數處理 (100/3 除不盡)
     * Given：系統中存在 A、B、C 三位參與者。
     * When：新增一筆 100 元消費由 A 代墊，並設定均分。
     * Then：系統應自動修正精度，確保所有人餘額總和精確為 0.0。
     */
    @Test
    @DisplayName("有效等價類：情境 2 - 精確度與餘數修正")
    void testScenario2RoundingLogic() {
        calc.addExpense(100, USER_A, Arrays.asList(USER_A, USER_B, USER_C), null);
        Map<String, Double> res = calc.getBalancesMap();
        double total = res.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(0.0, total, 0.01);
    }

    /**
     * 目標單元：addExpense()
     * 情境 4：依權重分攤 (攜伴參加)
     * Given：系統中存在 A、B 兩位參與者。
     * When：新增一筆 1500 元消費由 A 代墊，設定權重 A=1, B=2。
     * Then：A 應負擔 500 元，B 應負擔 1000 元，A 餘額最終為 1000。
     */
    @Test
    @DisplayName("有效等價類：情境 4 - 權重分配邏輯")
    void testScenario4WeightSplit() {
        Map<String, Integer> weights = new HashMap<>();
        weights.put(USER_A, 1);
        weights.put(USER_B, 2);
        calc.addExpense(1500, USER_A, Arrays.asList(USER_A, USER_B), weights);
        Map<String, Double> res = calc.getBalancesMap();
        assertEquals(1000.0, res.get(USER_A), 0.01); 
        assertEquals(-1000.0, res.get(USER_B), 0.01);
    }

    // --- 邊界與異常測試 (Boundary & Exception) ---

    /**
     * 目標單元：addExpense()
     * 情境 11：權重為 0 的處理
     * Given：系統中存在 A、B 兩位參與者。
     * When：新增一筆 100 元消費由 A 代墊，設定 B 權重為 0。
     * Then：B 不應分攤費用，A 負擔全部 100 元，故 A 餘額為 0。
     */
    @Test
    @DisplayName("邊界測試：情境 11 - 權重為 0 的分攤")
    void testScenario11ZeroWeight() {
        Map<String, Integer> weights = new HashMap<>();
        weights.put(USER_A, 1);
        weights.put(USER_B, 0);
        calc.addExpense(100, USER_A, Arrays.asList(USER_A, USER_B), weights);
        
        Map<String, Double> res = calc.getBalancesMap();
        assertEquals(0.0, res.get(USER_A), 0.01); 
        assertEquals(0.0, res.get(USER_B), 0.01);
    }

    /**
     * 目標單元：addExpense()
     * 情境 12：權重配置缺失處理
     * Given：系統中存在 A、B 兩位參與者。
     * When：傳入的權重表缺少 B 的資料。
     * Then：系統應自動將 B 權重設為預設值 1。
     */
    @Test
    @DisplayName("邊界測試：情境 12 - 權重 Map 缺漏成員時使用預設值")
    void testBoundaryMissingWeightInMap() {
        Map<String, Integer> partialWeights = new HashMap<>();
        partialWeights.put(USER_A, 2);
        calc.addExpense(300, USER_A, Arrays.asList(USER_A, USER_B), partialWeights);
        Map<String, Double> res = calc.getBalancesMap();
        assertEquals(100.0, res.get(USER_A), 0.01); 
    }

    /**
     * 目標單元：addExpense()
     * 情境 11（極端變體）：權重全為 0 的極端邊界攔截
     * Given：系統中存在 A、B 兩位參與者。
     * When：新增一筆消費時，將 A 與 B 的權重全部設定為 0 (總權重為 0)。
     * Then：系統應觸發防禦性攔截直接 return，且 A 與 B 的帳務應完好如初維持在 0.0。
     */
    @Test
    @DisplayName("邊界測試：全體成員權重均為 0 之總權重錯誤攔截")
    void testBoundaryTotalWeightIsZero() {
        Map<String, Integer> zeroWeights = new HashMap<>();
        zeroWeights.put(USER_A, 0);
        zeroWeights.put(USER_B, 0);
        
        calc.addExpense(100, USER_A, Arrays.asList(USER_A, USER_B), zeroWeights);
        
        Map<String, Double> res = calc.getBalancesMap();
        assertEquals(0.0, res.get(USER_A), 0.01);
        assertEquals(0.0, res.get(USER_B), 0.01);
    }

    /**
     * 目標單元：addExpense()
     * 情境 8：非法輸入處理 (負數金額攔截)
     * Given：系統初始化。
     * When：嘗試新增一筆 -500 元的消費。
     * Then：系統應執行防禦性判斷攔截輸入，不更新 any 餘額。
     */
    @Test
    @DisplayName("無效等價類：情境 8 - 負數金額攔截")
    void testScenario6InvalidAmount() {
        calc.addExpense(-500, USER_A, Arrays.asList(USER_A, USER_B), null);
        assertTrue(calc.getBalancesMap().isEmpty());
    }

    /**
     * 目標單元：addExpense()
     * 情境 9：異常參數處理 (金額為 0)
     * Given：系統初始化。
     * When：新增金額為 0 的消費。
     * Then：系統應視為無效操作，保持帳務 Map 為空。
     */
    @Test
    @DisplayName("無效等價類：情境 9 - 金額為 0 攔截")
    void testBoundaryZeroAmount() {
        calc.addExpense(0, USER_A, Arrays.asList(USER_A, USER_B), null);
        assertTrue(calc.getBalancesMap().isEmpty());
    }

    /**
     * 目標單元：addExpense()
     * 情境 9（變體）：異常參數處理 (參與者名單為 null 或空)
     * Given：系統初始化。
     * When：傳入的名單為 null 或 empty list。
     * Then：系統應終止計算，防止 NullPointerException 並保持狀態不變。
     */
    @Test
    @DisplayName("無效等價類：情境 9 - 參與者名單異常處理")
    void testBoundaryInvalidParticipants() {
        calc.addExpense(100, USER_A, null, null); 
        calc.addExpense(100, USER_A, new ArrayList<>(), null); 
        assertTrue(calc.getBalancesMap().isEmpty());
    }

    // --- 覆蓋率與輸出分支補強 ---

    /**
     * 目標單元：getBalances() 與 reset()
     * 情境 10：狀態重置與輸出功能
     * Given：系統中已存在帳務紀錄。
     * When：呼叫 getBalances() 與 reset()。
     * Then：確保 Console 輸出邏輯與 Map 清空邏輯被完全覆蓋執行。
     */
    @Test
    @DisplayName("工具方法：情境 10 - 測試重置與輸出功能")
    void testCoverageUtilityMethods() {
        calc.addExpense(100, USER_A, Arrays.asList(USER_A), null);
        calc.getBalances(); 
        calc.reset(); 
        assertTrue(calc.getBalancesMap().isEmpty());
    }
}