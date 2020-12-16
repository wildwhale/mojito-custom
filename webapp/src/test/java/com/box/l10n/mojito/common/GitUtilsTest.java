package com.box.l10n.mojito.common;

import com.box.l10n.mojito.extend.feature.service.GitUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GitUtilsTest {

    @Test
    public void checkoutTest() throws Exception {
        String repositoryName = "mojito-test";
        String url = "https://github.com/wildwhale/mojito-test.git";
        String id = "wild.whale.jin@gmail.com";
        String password = "1@google@1";
        String branch = "main";
//        File repoFile = getWorkingDir(repositoryName);
        File repoFile = new File("/Users/whale/Downloads/mojito/mojito-test");


        System.out.println(repoFile.toString());
        Git gitRepo = null;
        if (!repoFile.exists()) {
            gitRepo = GitUtils.checkout(url, id, password, repositoryName);
        } else {
            gitRepo = GitUtils.open(repoFile);
        }
        List<Ref> refs = gitRepo.branchList().call();
        for (Ref ref : refs) {
            System.out.println(ref.getName());
        }
        final List<Ref> branchListResult = gitRepo.branchList().call();
        final boolean localBranchExists = branchListResult.stream()
                .anyMatch(ref -> ref.getName().endsWith(branch));
        if (!localBranchExists) {
            gitRepo.checkout().setName(branch)
                    .setCreateBranch(true)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                    .call();
        } else {
            gitRepo.checkout().setName(branch).call();
        }
    }

    public static File getWorkingDir(String dirName) {
        Path workingPath = Paths.get("");
        if (StringUtils.isNoneEmpty(dirName)) {
            workingPath = workingPath.resolve(dirName);
        }
        return new File(workingPath.toAbsolutePath().toString());
    }

    @Test
    public void VersionRead() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/Users/whale/Downloads/mojito/mojito-test/VERSION"))) {
            System.out.println(reader.readLine());
        } catch (IOException e) {
        }
    }

}