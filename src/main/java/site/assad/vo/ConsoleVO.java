package site.assad.vo;


import javafx.scene.control.TextArea;

/**
 * 消息面板组件
 * @author Al-assad
 * @since 2019/3/15
 * created by Intellij-IDEA
 */
public class ConsoleVO {
    
    private TextArea textArea;
    
    public static ConsoleVO of(TextArea textArea) {
        ConsoleVO consoleVO = new ConsoleVO(textArea);
        return consoleVO;
    }
    
    private ConsoleVO(TextArea textArea) {
        this.textArea = textArea;
    }
    
    public TextArea getTextArea() {
        return textArea;
    }
    
    /**
     * 打印一行信息
     */
    public void println(String msg) {
        textArea.appendText(msg + "\n");
    }
    
    /**
     * 打印一行指令信息
     */
    public void printlnV(String msg) {
        textArea.appendText("> " + msg + "\n");
    }
    
    /**
     * 清除信息
     */
    public void clean(){
        textArea.clear();
    }
}
