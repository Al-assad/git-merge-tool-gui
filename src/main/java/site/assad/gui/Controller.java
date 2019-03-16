package site.assad.gui;

import com.alibaba.fastjson.JSON;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.Blend;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import site.assad.handler.GitHandler;
import site.assad.vo.ConsoleVO;
import site.assad.vo.GitConfVO;
import site.assad.vo.MergeTaskVO;
import site.assad.vo.TaskConfVO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class Controller {
    
    @FXML
    private Button addTaskBtn;
    
    @FXML
    private VBox taskPane;
    
    @FXML
    private TextField userNameTfd;
    
    @FXML
    private TextField passwordTfd;
    
    @FXML
    private TextArea consoleTa;
    
    @FXML
    private Button startBtn;
    
    @FXML
    private Button cleanBtn;
    
    @FXML
    private Button modeBtn;
    
    /**
     * 用户配置文件路径
     */
    private final String CONF_FILE_PATH = "./conf.json";
    /**
     * 任务计数
     */
    private final int TASK_START = 1;
    private int taskCount = 1;
    /**
     * 控制台dark模式开关
     */
    private boolean darkMode = false;
    /**
     * 运行消息缓存队列
     */
    private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>();
    /**
     * ui监听线程池
     */
    private ExecutorService uiListenExecutor = Executors.newCachedThreadPool();
    /**
     * 任务运行状态
     */
    private volatile AtomicBoolean isTaskRun = new AtomicBoolean(false);
    
    private static final String helpMsg = "> 仓库首次运行时，由于clone操作，执行时间会较久\n" +
            "> bug 请提交到 github issue: <https://github.com/Al-assad/git-merge-tool-gui/> \n" +
            "> 最后，合并不规范，同事泪两行，请关爱同事的心智健康\n";
    
    /**
     * 窗体初始化
     */
    @FXML
    private void initialize() {
        ConsoleVO.of(consoleTa).println(helpMsg);
        try {
            readConf();
        } catch (IOException e) {
            System.exit(0);
        }
        //运行信息重绘线程
        uiListenExecutor.execute(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                    ConsoleVO.of(consoleTa).println(msgQueue.take());
                    //控制台打印的精髓
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        //运行时的按钮行为
        uiListenExecutor.execute(() -> {
            while (true) {
                if (isTaskRun.get()) {
                    startBtn.setDisable(true);
                    cleanBtn.setDisable(true);
                    modeBtn.setDisable(true);
                } else {
                    startBtn.setDisable(false);
                    cleanBtn.setDisable(false);
                    modeBtn.setDisable(false);
                }
            }
        });
        uiListenExecutor.shutdown();
    }
    
    /**
     * 程序退出
     */
    @FXML
    public void exitApplication(ActionEvent event) {
        uiListenExecutor.shutdownNow();
        Platform.exit();
    }
    
    /**
     * 添加创建面板事件
     */
    public void addTaskBox() {
        taskPane.getChildren().add(createTaskVBox(null, null, null));
    }
    
    /**
     * 开始进行合并任务
     */
    public void startTask() throws IOException, InterruptedException {
        ConsoleVO.of(consoleTa).clean();
        ConsoleVO.of(consoleTa).printlnV("Pre-Merge Task Running ... \n");
        TaskConfVO taskConfVO = getTaskConfTaskPane();
        GitConfVO confVO = taskConfVO.newGitConfVO();
        List<MergeTaskVO> mergeTaskVOS = taskConfVO.getMergeTaskVOS();
        //任务校验
        mergeTaskVOS = mergeTaskVOS.stream().filter(taskVO -> !taskVO.isEmpty()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(mergeTaskVOS)) {
            ConsoleVO.of(consoleTa).printlnV("Not Contain valid Task\n");
            return;
        }
        
        //合并任务线程池
        ExecutorService gitTaskExecutor = Executors.newSingleThreadExecutor();
        for (MergeTaskVO taskVO : mergeTaskVOS) {
            gitTaskExecutor.execute(() -> {
                GitHandler gitHandler = new GitHandler(confVO, msgQueue);
                isTaskRun.set(true);
                try {
                    gitHandler.preMerge(taskVO.getRemoteUrl(), taskVO.getBaseBranch(), taskVO.getTargetBranch());
                } catch (IOException | InterruptedException e) {
                    gitTaskExecutor.shutdownNow();
                    ConsoleVO.of(consoleTa).printlnV("Pre-Merge Task Fail!\n");
                    isTaskRun.set(false);
                }
            });
        }
        gitTaskExecutor.execute(() -> {
            isTaskRun.set(false);
        });
        gitTaskExecutor.shutdown();
    }
    
    
    /**
     * 创建任务面板
     */
    private HBox createTaskVBox(String remoteUrl, String baseBranch, String targetBranch) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setPrefHeight(55.0);
        hBox.setPrefWidth(900.0);
        hBox.setSpacing(5.0);
        
        Label labTask = new Label("任务" + ++taskCount);
        labTask.setEffect(new Blend());
        Label labUrl = new Label("仓库地址");
        Label labBaseBranch = new Label("基础分支");
        Label labTargetBranch = new Label("目标分支");
        
        TextField fieldUrl = new TextField();
        fieldUrl.setPrefWidth(310);
        if (StringUtils.isNotEmpty(remoteUrl)) {
            fieldUrl.setText(remoteUrl);
        }
        TextField fieldBase = new TextField();
        fieldBase.setPrefWidth(180);
        if (StringUtils.isNotEmpty(baseBranch)) {
            fieldBase.setText(baseBranch);
        }
        TextField fieldTarget = new TextField();
        fieldTarget.setPrefWidth(120);
        if (StringUtils.isNotEmpty(targetBranch)) {
            fieldTarget.setText(targetBranch);
        }
        
        Button removeBtn = new Button("删除");
        setBtnRemoveTask(removeBtn);
        hBox.getChildren().add(labTask);
        hBox.getChildren().add(labUrl);
        hBox.getChildren().add(fieldUrl);
        hBox.getChildren().add(labBaseBranch);
        hBox.getChildren().add(fieldBase);
        hBox.getChildren().add(labTargetBranch);
        hBox.getChildren().add(fieldTarget);
        hBox.getChildren().add(removeBtn);
        
        return hBox;
    }
    
    private void setBtnRemoveTask(Button btn) {
        btn.setOnAction((ActionEvent e) -> {
            Node box = btn.getParent();
            VBox taskBox = (VBox) box.getParent();
            taskBox.getChildren().remove(box);
            taskCount--;
            int curIndex = TASK_START;
            for (Node node : taskBox.getChildren()) {
                HBox hBox = (HBox) node;
                Label labelTask = (Label) hBox.getChildren().get(0);
                labelTask.setText("任务" + curIndex++);
            }
        });
    }
    
    /**
     * 清除控制台信息
     */
    public void cleanConsole() {
        ConsoleVO.of(consoleTa).clean();
    }
    
    /**
     * 切换控制台 dark/light mode
     */
    public void changeConsoleMode() {
        if (!darkMode) {
            consoleTa.setStyle("-fx-control-inner-background: #222222;" +
                    " -fx-text-fill: #16e008;");
            darkMode = true;
            modeBtn.setText("light mode");
        } else {
            consoleTa.setStyle("-fx-control-inner-background: white;" +
                    " -fx-text-fill: black;");
            darkMode = false;
            modeBtn.setText("dark mode");
        }
    }
    
    /**
     * 从页面节点中获取任务数据
     */
    private TaskConfVO getTaskConfTaskPane() {
        String userName = userNameTfd.getText();
        String password = passwordTfd.getText();
        TaskConfVO taskConfVO = new TaskConfVO(userName, password);
        List<MergeTaskVO> mergeTaskVOS = new ArrayList<>(5);
        for (Node box : taskPane.getChildren()) {
            HBox hBox = (HBox) box;
            TextField urlTF = (TextField) hBox.getChildren().get(2);
            TextField baseTF = (TextField) hBox.getChildren().get(4);
            TextField targetTF = (TextField) hBox.getChildren().get(6);
            MergeTaskVO taskVO = MergeTaskVO.of(urlTF.getText(), baseTF.getText(), targetTF.getText());
            mergeTaskVOS.add(taskVO);
        }
        taskConfVO.setMergeTaskVOS(mergeTaskVOS);
        return taskConfVO;
    }
    
    /**
     * 写入用户设置
     */
    public void saveConf() throws IOException {
        TaskConfVO taskConfVO = getTaskConfTaskPane();
        if (taskConfVO == null || taskConfVO.isEmpty()) {
            ConsoleVO.of(consoleTa).printlnV("Empty Configuration, Nothing to Update\n");
            return;
        }
        String jsonStr = JSON.toJSONString(taskConfVO, true);
        FileUtils.write(new File(CONF_FILE_PATH), jsonStr, "utf8");
        ConsoleVO.of(consoleTa).printlnV("Save Configuration Successful!\n");
    }
    
    /**
     * 从文件读取用户设置
     */
    private void readConf() throws IOException {
        File confFile = new File(CONF_FILE_PATH);
        if (!confFile.exists()) {
            return;
        }
        String jsonStr = FileUtils.readFileToString(confFile, "utf8");
        if (StringUtils.isEmpty(jsonStr)) {
            return;
        }
        TaskConfVO taskConfVO = JSON.parseObject(jsonStr, TaskConfVO.class);
        String userName = taskConfVO.getUserName();
        String password = taskConfVO.getPassword();
        List<MergeTaskVO> mergeTaskVOS = taskConfVO.getMergeTaskVOS();
        //重新绘制数据
        userNameTfd.setText(userName);
        passwordTfd.setText(password);
        if (CollectionUtils.isEmpty(mergeTaskVOS)) {
            return;
        }
        //删除所有任务面板节点
        taskPane.getChildren().remove(0, taskPane.getChildren().size());
        taskCount = 0;
        //重新绘制任务面板
        for (MergeTaskVO taskVO : mergeTaskVOS) {
            HBox box = createTaskVBox(taskVO.getRemoteUrl(), taskVO.getBaseBranch(), taskVO.getTargetBranch());
            taskPane.getChildren().add(box);
        }
    }
    
}
