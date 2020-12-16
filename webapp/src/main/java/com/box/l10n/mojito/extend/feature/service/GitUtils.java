package com.box.l10n.mojito.extend.feature.service;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

public class GitUtils {
    private static Logger logger = LoggerFactory.getLogger(GitUtils.class);

//    private static String userId = "";
//    private static String userPass = "";
//    private static String userName = "";
//    private static String userEmail = "";
//    private static String hash = "origin/master";
//    private static String url = "https://github.com/xxx.git";
//    private static CredentialsProvider cp = new UsernamePasswordCredentialsProvider(userId, userPass);

    public static Git init(File dir) throws Exception {
        return Git.init().setDirectory(dir).call();
    }
    public static void remoteAdd(Git git, String url) throws Exception {
        // add remote repo:
        RemoteAddCommand remoteAddCommand = git.remoteAdd();
        remoteAddCommand.setName("origin");
        remoteAddCommand.setUri(new URIish(url));
        // you can add more settings here if needed
        remoteAddCommand.call();
    }

    public static void push(Git git, String userId, String userPass) throws Exception {
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(userId, userPass);
        // push to remote:
        git.push()
            .setCredentialsProvider(cp)
            .setForce(true)
            .call();
    }

    public static void add(Git git, String filePattern) throws Exception {
        git.add().addFilepattern(filePattern).call();
    }

    public static void rm(Git git, String filePattern) throws Exception {
        git.rm().addFilepattern(filePattern).call();
    }

    public static void commit(Git git, String msg) throws Exception {
        // Now, we do the commit with a message
        git.commit();
        // .setAuthor(userName, userEmail)
        // .setMessage(msg)
        // .call();
    }


    public static void pull(Git git) throws Exception {
        git.pull().call();
    }

    public static void lsRemote(Git git, String userId, String userPass) throws Exception {

        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(userId, userPass);
        Collection<Ref> remoteRefs = git.lsRemote()
                .setCredentialsProvider(cp)
                .setRemote("origin")
                .setTags(false)
                .setHeads(true)
                .call();
        for (Ref ref : remoteRefs) {
            System.out.println(ref.getName() + " -> " + ref.getObjectId().name());
        }
    }

    public static void checkOutSpecificDir(File dir, String url, String userId, String userPass, String hash) throws Exception {
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(userId, userPass);
        Git gitRepo = Git.cloneRepository()
                .setURI(url) // remote 주소
                .setDirectory(dir) // 다운받을 로컬의 위치
                .setNoCheckout(true)//
                .setCredentialsProvider(cp) // 인증 정보
                .call();

        gitRepo.checkout()
                .setStartPoint(hash) // origin/branch_name //
                .addPath("not thing") // 다운받을 대상 경로
                .call();
        gitRepo.getRepository().close();
    }

    public static Git checkout(String gitUri, String id, String password, String repository) throws Exception {
        Git gitRepo = Git.cloneRepository()
                .setURI(gitUri) // remote 주소
                .setDirectory(getWorkingDir(repository)) // 다운받을 로컬의 위치
                .setNoCheckout(false)//
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(id, password))
                .call();
        return gitRepo;
    }

    public static Git openRepository(String repositoryName) throws Exception {
        Git git = null;
        File dir = getWorkingDir(repositoryName);
        try {
            git = Git.open(dir);
        } catch (RepositoryNotFoundException e) {
            // TODO: Exception
//            git = GitUtils.init(dir);
        }
        return git;
    }

    public static File getWorkingDir(String dirName) {
        Path workingPath = Paths.get("");
        if (StringUtils.isNoneEmpty(dirName)) {
            workingPath = workingPath.resolve(dirName);
        }
        return new File(workingPath.toAbsolutePath().toString());
    }

    public static Git open(File dir) throws Exception {
        Git git = null;
        try {
            git = Git.open(dir);
        } catch (RepositoryNotFoundException e) {
            git = GitUtils.init(dir);
        }
        return git;
    }

    //
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
        logger.info("git checkout : {}", branch);
        GitUtils.pull(gitRepo);
        logger.info("git pull : {}", branch);
        gitRepo.close();
        return repoFile;
    }


    public static boolean isLocalBranchExist(Git gitRepo, String branch) throws GitAPIException {
        final List<Ref> branchListResult = gitRepo.branchList().call();
        return branchListResult.stream()
                .anyMatch(ref -> ref.getName().endsWith(branch));
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

    public static void pushAllChanged(Git gitRepo, String id, String password, String message) throws Exception {
        gitRepo.add().addFilepattern(".").call();
        gitRepo.commit().setMessage(message).call();
        push(gitRepo, id, password);
        logger.info("all changed push : {}", gitRepo.getRepository().getBranch());
    }

    public static void mergeCommand(Git gitRepo, String fromBranch, String toBranch) throws IOException, GitAPIException {
        gitRepo.checkout().setCreateBranch(false).setName(toBranch)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .call();
        ObjectId objectId = gitRepo.getRepository().resolve(fromBranch);
        MergeResult mergeResult = gitRepo.merge().include(objectId).call();

        if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)){
            // TODO: throw Exception
            logger.error("merge error : {}", mergeResult.getConflicts().toString());
            // inform the user he has to handle the conflicts
        }
    }

    public static void pushCommand(Git gitRepo, String id, String password) throws GitAPIException, IOException {
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(id, password);
        gitRepo.push().setCredentialsProvider(cp).setForce(true).call();
        logger.info("all changed push : {}", gitRepo.getRepository().getBranch());
    }

}


