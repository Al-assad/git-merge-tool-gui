package site.assad.vo;

import org.apache.commons.lang3.StringUtils;

/**
 * 合并任务数据类
 *
 * @author Al-assad
 * @since 2019/3/15
 * created by Intellij-IDEA
 */
public class MergeTaskVO {
    /**
     * 远程仓库url
     */
    private String remoteUrl;
    /**
     * 基础分支
     */
    private String baseBranch;
    /**
     * 目标分支
     */
    private String targetBranch;
    
    public static MergeTaskVO of (String remoteUrl, String baseBranch, String targetBranch){
        MergeTaskVO taskVO = new MergeTaskVO();
        taskVO.setBaseBranch(baseBranch);
        taskVO.setRemoteUrl(remoteUrl);
        taskVO.setTargetBranch(targetBranch);
        return taskVO;
    }
    
    public String getRemoteUrl() {
        return remoteUrl;
    }
    
    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }
    
    public String getBaseBranch() {
        return baseBranch;
    }
    
    public void setBaseBranch(String baseBranch) {
        this.baseBranch = baseBranch;
    }
    
    public String getTargetBranch() {
        return targetBranch;
    }
    
    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }
    
    public boolean isEmpty(){
        return StringUtils.isEmpty(remoteUrl) || StringUtils.isEmpty(baseBranch) || StringUtils.isEmpty(targetBranch);
    }
    
    @Override
    public String toString() {
        return "MergeTaskVO{" +
                "remoteUrl='" + remoteUrl + '\'' +
                ", baseBranch='" + baseBranch + '\'' +
                ", targetBranch='" + targetBranch + '\'' +
                '}';
    }
}
