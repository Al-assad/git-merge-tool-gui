package site.assad.vo;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import site.assad.handler.GitUtil;

import java.util.List;

/**
 * 任务配置数据类
 *
 * @author Al-assad
 * @since 2019/3/15
 * created by Intellij-IDEA
 */
public class TaskConfVO {
    /**
     * 登录名称
     */
    private String userName;
    /**
     * 登录密码
     */
    private String password;
    
    private List<MergeTaskVO> mergeTaskVOS;
    
    
    public TaskConfVO() {
    }
    
    public TaskConfVO(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public List<MergeTaskVO> getMergeTaskVOS() {
        return mergeTaskVOS;
    }
    
    public void setMergeTaskVOS(List<MergeTaskVO> mergeTaskVOS) {
        this.mergeTaskVOS = mergeTaskVOS;
    }
    
    public GitConfVO newGitConfVO(){
        return GitConfVO.of(getUserName(), getPassword(), GitUtil.TEMP_LOCAL_PATH);
    }
    
    public boolean isEmpty(){
        return StringUtils.isEmpty(userName) || StringUtils.isEmpty(password) || CollectionUtils.isEmpty(mergeTaskVOS);
    }
}
