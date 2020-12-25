package com.box.l10n.mojito.extend.feature.service;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GitUtils {

    private static Logger logger = LoggerFactory.getLogger(GitUtils.class);

    public static Git init(File dir) throws Exception {
        logger.info("{} : git repo init", dir.getAbsolutePath());
        return Git.init().setDirectory(dir).call();
    }

    public static Git open(File dir) throws Exception {
        Git git = null;
        try {
            git = Git.open(dir);
            logger.info("{} dir git open", dir.getAbsolutePath());
        } catch (RepositoryNotFoundException e) {
            git = GitUtils.init(dir);
        }
        return git;
    }

    public static void pushCommand(Git gitRepo, String id, String password) throws GitAPIException, IOException {
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(id, password);
        gitRepo.push()
                .setCredentialsProvider(cp)
                .setForce(true)
                .call();
        logger.info("push : {}", gitRepo.getRepository().getBranch());
    }

    public static void pushAllChanged(Git gitRepo, String id, String password, String message) throws Exception {
        gitRepo.add().addFilepattern(".").call();
        gitRepo.commit().setMessage(message).call();
        pushCommand(gitRepo, id, password);
        logger.info("all changed push : {}", gitRepo.getRepository().getBranch());
    }

    public static void pull(Git git, String id, String password) throws Exception {
        git.pull()
            .setCredentialsProvider(new UsernamePasswordCredentialsProvider(id, password))
            .call();

        logger.info("pull : {}", git.getRepository().getDirectory().getAbsolutePath());
    }

    public static Git checkout(String gitUri, String id, String password, String repository) throws Exception {
        Git gitRepo = Git.cloneRepository()
                .setURI(gitUri) // remote 주소
                .setDirectory(getWorkingDir(repository)) // 다운받을 로컬의 위치
                .setNoCheckout(false)//
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(id, password))
                .call();
        logger.info("checkout : {}", gitUri);
        return gitRepo;
    }

    public static Git checkoutCommand(String repositoryName, String branch) throws Exception {
        File repo = getWorkingDir(repositoryName);
        Git gitRepo = open(repo);
        return checkoutCommand(gitRepo, branch);
    }

    public static Git checkoutCommand(Git gitRepo, String branch) throws Exception {
        CheckoutCommand checkout = gitRepo.checkout();
        if (isLocalBranchExist(gitRepo, branch)) {
            checkout.setName(branch);
        } else {
            checkout.setName(branch)
                    .setCreateBranch(true)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK);
        }
        checkout.call();
        logger.info("checkout : {}", branch);
        return gitRepo;
    }

    public static void mergeCommand(Git gitRepo, String fromBranch, String toBranch) throws Exception {
        gitRepo.checkout().setCreateBranch(false).setName(toBranch)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .call();
        ObjectId objectId = gitRepo.getRepository().resolve(fromBranch);
        MergeResult mergeResult = gitRepo.merge().include(objectId).call();

        logger.info("{} git repo merge [ {} ] to [ {} ] success", gitRepo.getRepository().getDirectory().getAbsolutePath(), fromBranch, toBranch);
        if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)){
            logger.error("merge error : {}", mergeResult.getConflicts().toString());
            // inform the user he has to handle the conflicts
            throw new Exception("merge error : " + mergeResult.getConflicts().toString());
        }
    }

    public static File downloadResource(String url, String id, String password, String branch, String repositoryName) throws Exception {
        File repoFile = getWorkingDir(repositoryName);
        // create repository
        Git gitRepo = null;
        if (!repoFile.exists()) {
            logger.info("{}: not exist, resource checkout", repositoryName);
            gitRepo = checkout(url, id, password, repositoryName);
        } else {
            gitRepo = open(repoFile);
        }
        checkoutCommand(gitRepo, branch);
        GitUtils.pull(gitRepo, id, password);
        gitRepo.close();
        return repoFile;
    }

    public static boolean isLocalBranchExist(Git gitRepo, String branch) throws GitAPIException {
        final List<Ref> branchListResult = gitRepo.branchList().call();
        return branchListResult
                .stream()
                .anyMatch(ref -> ref.getName().endsWith(branch));
    }

    public static File getWorkingDir(String dirName) {
        Path workingPath = Paths.get("");
        if (StringUtils.isNoneEmpty(dirName)) {
            workingPath = workingPath.resolve(dirName);
        }
        return new File(workingPath.toAbsolutePath().toString());
    }
}


