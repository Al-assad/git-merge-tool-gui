package site.assad.handler;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.UUID;


/**
 * Git 操作工具类型
 *
 * @author Al-assad
 * @since 2019/2/24
 * created by Intellij-IDEA
 */
public class GitUtil {
    
    /**
     * 远程仓库前缀
     */
    private final static String REMOTE_BRANCH_PREFIX = "origin/";
    
    public final static String TEMP_LOCAL_PATH = "./donNotFuckDeleteIt";
    
    /**
     * 从远程链接中构建项目地址
     *
     * @param remoteUrl    远程git地址
     * @param tempFilePath 本地临时文件
     * @return this
     */
    public static String getLocalGitFilePath(@Nonnull String remoteUrl, @Nonnull String tempFilePath) {
        String projectName = getProjectName(remoteUrl);
        if (StringUtils.isEmpty(projectName)) {
            return genRandomFilePath(tempFilePath);
        }
        return String.format("%s/%s", tempFilePath, projectName);
    }
    
    /**
     * 从仓库 url 中获取项目名称
     *
     * @param remoteUrl 远程git地址
     */
    public static String getProjectName(@Nonnull String remoteUrl) {
        if (StringUtils.isEmpty(remoteUrl)) {
            return null;
        }
        String[] splits = remoteUrl.split("/");
        if (ArrayUtils.isEmpty(splits)) {
            return null;
        }
        String projectGitName = splits[splits.length - 1];
        String[] gitNameSplit = projectGitName.trim().split("\\.");
        if (ArrayUtils.isEmpty(gitNameSplit)) {
            return null;
        }
        String projectName = gitNameSplit[0];
        if (StringUtils.isEmpty(projectName)) {
            return null;
        }
        return projectName;
    }
    
    private static String genRandomFilePath(String tempFilePath) {
        return String.format("%s/%s", tempFilePath, UUID.randomUUID());
    }
    
    /**
     * 创建本地仓库目录
     *
     * @param remoteUrl 远程仓库url
     * @return this
     */
    public static File createFile(@Nonnull String remoteUrl, @Nonnull String tempFilePath) throws IOException {
        File localFile = new File(getLocalGitFilePath(remoteUrl, tempFilePath));
        return localFile;
    }
    
    /**
     * 获取指定分支的远程
     *
     * @param branch 分支名称
     * @return this
     */
    public static String getRemoteBranchName(@Nonnull String branch) {
        return REMOTE_BRANCH_PREFIX + branch;
    }
    
}
