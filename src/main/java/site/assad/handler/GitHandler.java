package site.assad.handler;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.assad.vo.GitConfVO;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * git操作类
 *
 * @author Al-assad
 * @since 2019/2/24
 * created by Intellij-IDEA
 */
public class GitHandler {
    
    private final static transient Logger LOGGER = LoggerFactory.getLogger(GitHandler.class);
    private GitConfVO confVO;
    private BlockingQueue<String> msgQueue;
    
    public GitHandler(@Nonnull GitConfVO confVO, BlockingQueue<String> msgQueue) {
        this.confVO = confVO;
        this.msgQueue = msgQueue;
    }
    
    /**
     * clone仓库并进行预合并
     */
    public void preMerge(@Nonnull final String remoteUrl, @Nonnull final String baseBranch, @Nonnull final String targetBranch) throws InterruptedException, IOException {
        
        msgQueue.put(String.format("【Task Info】 Merge from %s to %s (%s)", baseBranch, targetBranch, remoteUrl));
        File localRepo = GitUtil.createFile(remoteUrl, confVO.getTempFilePath());
        Git git = initRepo(remoteUrl, localRepo);
        if (git == null) {
            msgQueue.put("Repository init fail!\n" + "Merge branch fail");
            return;
        }
        try {
            checkoutAndPull(git, baseBranch);
            checkoutAndPull(git, targetBranch);
            //合并分支
            MergeResult mergeResult = git.merge().
                    include(git.getRepository().resolve(baseBranch)).
                    setCommit(false).
                    setFastForward(MergeCommand.FastForwardMode.NO_FF).
                    setSquash(false).
                    call();
            resetMaster(git);
            printMergeResult(git, mergeResult);
        } catch (GitAPIException e) {
            LOGGER.error(e.getMessage());
            msgQueue.put("Merge branch fail！");
        } finally {
            msgQueue.put("\n");
        }
    }
    
    /**
     * 初始化仓库
     */
    private Git initRepo(@Nonnull String remoteUrl, File localRepo) throws InterruptedException, IOException {
        Git git = null;
        //本地仓库不存在，clone 仓库
        if (!localRepo.exists()) {
            try {
                git = gitClone(remoteUrl, localRepo);
            } catch (GitAPIException e) {
                LOGGER.error(e.getMessage());
                msgQueue.put("Clone repository fail!");
                //清除错误仓库
                FileUtils.deleteDirectory(localRepo);
            }
        }
        //本地仓库存在，更新仓库
        else {
            try {
                git = gitOpen(localRepo);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                msgQueue.put("Open local repository fail...");
                //打开本地仓库失败，尝试clone仓库
                try {
                    git = gitClone(remoteUrl, localRepo);
                } catch (GitAPIException e1) {
                    LOGGER.error(e.getMessage());
                    msgQueue.put("Clone repository fail!");
                }
            }
        }
        return git;
    }
    
    
    /**
     * clone仓库
     */
    private Git gitClone(@Nonnull String remoteUrl, @Nonnull File localPath) throws GitAPIException, InterruptedException {
        msgQueue.put("Cloning from " + remoteUrl);
        Git git = Git
                .cloneRepository()
                .setURI(remoteUrl)
                .setCredentialsProvider(confVO.getCredentialsProvider())
                .setDirectory(localPath)
                .setCloneAllBranches(true)
                .call();
        msgQueue.put("Cloned repository: " + GitUtil.getProjectName(remoteUrl));
        return git;
    }
    
    /**
     * 打开本地仓库
     */
    private Git gitOpen(@Nonnull File localRepo) throws IOException, InterruptedException {
        Git git = Git.open(localRepo);
        msgQueue.put("Use local repository");
        return git;
    }
    
    /**
     * checkout分支
     */
    private void checkoutAndPull(@Nonnull Git git, @Nonnull String branchName) throws GitAPIException, IOException {
        //切换分支
        git.checkout()
                .setCreateBranch(!existLocalBranch(git, branchName))
                .setName(branchName)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .setStartPoint(GitUtil.getRemoteBranchName(branchName))
                .call();
        //重置HEAD
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef("HEAD").call();
        //更新分支
        git.pull().setStrategy(MergeStrategy.THEIRS);
        LOGGER.debug("checkoutAndPull " + git.getRepository().getBranch());
    }
    
    /**
     * 验证本地分支是否存在
     */
    private boolean existLocalBranch(@Nonnull Git git, @Nonnull String branchName) throws GitAPIException {
        List<Ref> branches = git.branchList().call();
        for (Ref ref : branches) {
            String[] refNameParts = ref.getName().split("/");
            String refName = refNameParts[refNameParts.length - 1];
            if (refName.equals(branchName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 恢复到现场到 master
     *
     * @param git
     * @throws GitAPIException
     * @throws IOException
     */
    private void resetMaster(Git git) throws GitAPIException, IOException {
        //恢复合并状态
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef("HEAD").call();
        git.pull().setStrategy(MergeStrategy.THEIRS);
        //恢复到master默认分支
        checkoutAndPull(git, "master");
    }
    
    /**
     * 打印合并结果
     *
     * @param mergeResult
     */
    public void printMergeResult(@Nonnull Git git, MergeResult mergeResult) throws GitAPIException, InterruptedException {
        if (mergeResult == null) {
            msgQueue.put("No merge report");
            return;
        }
        msgQueue.put("【Merge Successful】 " + mergeResult.getMergeStatus().isSuccessful());
        msgQueue.put("【Merge Status】 " + mergeResult.getMergeStatus().toString());
        if (mergeResult.getMergeStatus().isSuccessful()) {
            return;
        }
        
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Set<RevCommit> commitSet = new HashSet<>();
        msgQueue.put("【Conflict Detail】 ");
        //打印冲突详情
        for (Map.Entry<String, int[][]> entry : mergeResult.getConflicts().entrySet()) {
            String file = entry.getKey();
            List<Integer> conflitLines = new ArrayList<>(entry.getValue().length);
            for (int[] arr : entry.getValue()) {
                conflitLines.add(arr[1]);
            }
            
            msgQueue.put("\tConflict file: " + file);
            //获取冲突文件提交信息
            BlameResult result = git.blame().setFilePath(file)
                    .setTextComparator(RawTextComparator.WS_IGNORE_ALL)
                    .call();
            RawText rawText = result.getResultContents();
            for (Integer lineIndex : conflitLines) {
                PersonIdent author = result.getSourceAuthor(lineIndex);
                RevCommit sourceCommit = result.getSourceCommit(lineIndex);
                commitSet.add(sourceCommit);
                msgQueue.put("\t\t"
                        + author.getName()
                        + (sourceCommit != null ? ": " + sourceCommit.getName() + ": " + sourceCommit.getShortMessage() + " 【" + sf.format(author.getWhen()) + "】 " : "")
                        + "->" + rawText.getString(lineIndex).trim());
            }
        }
        //打印简要commit冲突列表
        if (CollectionUtils.isNotEmpty(commitSet)) {
            msgQueue.put("【Conflict Commit List】");
            commitSet.forEach(commit -> {
                try {
                    msgQueue.put(commit.getCommitterIdent().getName() + ": "
                            + commit.getName() + ": "
                            + commit.getShortMessage() + " 【"
                            + sf.format(commit.getCommitterIdent().getWhen()) + "】 ");
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage());
                }
            });
        }
    }
    
}
