package com.billsplit; // 💡 核心修正：將 package 修改為對應 pom.xml 的 com.billsplit

import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

public class MainApp {
    public static void main(String[] args) {
        // 這段 Java 程式碼會被 TeaVM 翻譯成 JavaScript，並直接操作網頁的 HTML！
        HTMLDocument document = HTMLDocument.current();
        HTMLElement messageElement = document.createElement("h1");
        messageElement.setInnerText("Hello! Expense Splitter 網頁環境架設成功！");
        document.getBody().appendChild(messageElement);
    }
}