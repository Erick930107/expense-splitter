package billsplit; // 確保與您的資料夾名稱一致

import java.util.*;

/**
 * Assignment 2 - 核心控制器
 * 實作分帳邏輯與債務最佳化演算法[cite: 1, 2]
 */
public class BillSplitCalculator {
    private Map<String, Double> balances = new LinkedHashMap<>();

    public void addExpense(double totalAmount, String payer, List<String> participants, Map<String, Integer> weights) {
        if (totalAmount <= 0 || participants == null || participants.isEmpty()) {
            System.out.println("[錯誤]：消費金額與分攤人數必須大於零。");
            return;
        }

        balances.putIfAbsent(payer, 0.0);
        for (String p : participants) {
            balances.putIfAbsent(p, 0.0);
        }

        double totalWeight = 0;
        for (String p : participants) {
            totalWeight += (weights != null && weights.containsKey(p)) ? weights.get(p) : 1;
        }

        double allocatedSum = 0;
        Map<String, Double> individualDebts = new HashMap<>();

        for (int i = 0; i < participants.size(); i++) {
            String p = participants.get(i);
            double weight = (weights != null && weights.containsKey(p)) ? weights.get(p) : 1;
            
            double share;
            if (i == participants.size() - 1) {
                share = Math.round((totalAmount - allocatedSum) * 100.0) / 100.0;
            } else {
                share = Math.round((totalAmount * (weight / totalWeight)) * 100.0) / 100.0;
                allocatedSum += share;
            }
            individualDebts.put(p, share);
        }

        balances.put(payer, balances.get(payer) + totalAmount);
        for (String p : participants) {
            balances.put(p, balances.get(p) - individualDebts.get(p));
        }
    }

    public void getBalances() {
        System.out.println("--- 目前餘額狀態 ---");
        balances.forEach((name, balance) -> 
            System.out.printf("%s: %.2f 元\n", name, balance));
    }

    /**
     * 提供給測試類別讀取狀態的方法
     */
    public Map<String, Double> getBalancesMap() {
        return new HashMap<>(this.balances);
    }

    public void optimizeDebts() {
        System.out.println("--- 最終轉帳建議 ---");
        List<Map.Entry<String, Double>> creditors = new ArrayList<>();
        List<Map.Entry<String, Double>> debtors = new ArrayList<>();

        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            double val = Math.round(entry.getValue() * 100.0) / 100.0;
            if (val > 0) creditors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), val));
            else if (val < 0) debtors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), -val));
        }

        int cIdx = 0, dIdx = 0;
        boolean hasTransfers = false;
        while (cIdx < creditors.size() && dIdx < debtors.size()) {
            double settleAmount = Math.min(creditors.get(cIdx).getValue(), debtors.get(dIdx).getValue());
            if (settleAmount > 0.01) {
                System.out.printf("%s 應轉帳 %.2f 元給 %s\n", 
                    debtors.get(dIdx).getKey(), settleAmount, creditors.get(cIdx).getKey());
                hasTransfers = true;
            }
            creditors.get(cIdx).setValue(creditors.get(cIdx).getValue() - settleAmount);
            debtors.get(dIdx).setValue(debtors.get(dIdx).getValue() - settleAmount);
            if (creditors.get(cIdx).getValue() < 0.01) cIdx++;
            if (debtors.get(dIdx).getValue() < 0.01) dIdx++;
        }
        if (!hasTransfers) System.out.println("所有帳務已清零，無需轉帳。");
    }

    public void reset() {
        balances.clear();
    }
}