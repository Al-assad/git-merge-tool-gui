package site.assad.vo;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import javax.annotation.Nonnull;

/**
 * git 基础配置数据类
 *
 * @author Al-assad
 * @since 2019/2/24
 * created by Intellij-IDEA
 */
public class GitConfVO {

    /** 登录用户名称 */
    private String userName;
    /** 登录密码 */
    private String password;
    /** 临时存储地址 */
    private String tempFilePath;

    /**
     * vo 构建器
     */
    public static GitConfVO of(@Nonnull String userName, @Nonnull String password, @Nonnull String tempFilePath) {
        GitConfVO confVO = new GitConfVO();
        confVO.setUserName(userName);
        confVO.setPassword(password);
        confVO.setTempFilePath(tempFilePath);
        return confVO;
    }

    /**
     * 获取 JGit 账户验证提供器
     */
    public UsernamePasswordCredentialsProvider getCredentialsProvider(){
        return new UsernamePasswordCredentialsProvider(getUserName(), getPassword());
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

    public String getTempFilePath() {
        return tempFilePath;
    }

    public void setTempFilePath(String tempFilePath) {
        this.tempFilePath = tempFilePath;
    }


}
