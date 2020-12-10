package com.box.l10n.mojito.custom.feature.service;

import com.box.l10n.mojito.custom.feature.command.CommandException;
import com.box.l10n.mojito.custom.feature.command.CommandHelper;
import com.box.l10n.mojito.rest.entity.LocalizedAssetBody;
import com.box.l10n.mojito.rest.entity.Repository;
import com.box.l10n.mojito.rest.entity.SourceAsset;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class ResourceService {

    static Logger logger = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    MojitoRepository mojitoRepository;
    @Autowired
    ResourceExtractor resourceExtractor;

    public static File getWorkingDir(String dirName) {
        Path workingPath = Paths.get("");
        if (StringUtils.isNoneEmpty(dirName)) {
            workingPath = workingPath.resolve(dirName);
        }
        logger.info("Git repo dir : {}", workingPath.toAbsolutePath().toString());
        return new File(workingPath.toAbsolutePath().toString());
    }

    public void upload(String gitUri, String id, String password, String repositoryName, String resourceName, String fileType, String[] locales) throws Exception {
        // git checkout or pull in local working dir
        File repo = getWorkingDir(repositoryName);
        if (repo.exists()) {
            logger.info("exist");
            Git gitRepo = GitUtils.open(repo);
            GitUtils.pull(gitRepo);
        } else {
            logger.info("not exist");
            GitUtils.checkOut(gitUri, id, password, repositoryName);
        }
        // create repository
        // exist check 로 변경
        try {
            mojitoRepository.create(resourceName, locales);
        } catch (Exception e) {
        }
        // extract
        String resourcePath = repo.getPath() + "/" + resourceName;
        Stream<SourceAsset> assetStream = resourceExtractor.extract(resourcePath, resourceName, fileType);
        // return load info
        Repository repository = resourceExtractor.getRepository(resourceName);
        mojitoRepository.push(repository, assetStream, null);
    }

    public void deploy(String repositoryName, String targetBranchName, LocalizedAssetBody.Status status) throws Exception {
        // pullCommand process 진행
        mojitoRepository.pull(repositoryName, status);
        // modify VERSION file
        String version = "0.0.1";

        // merge
        // push
        Git gitDir = GitUtils.openRepository(repositoryName);
        GitUtils.add(gitDir, ".");
        GitUtils.commit(gitDir, String.format("Version up : %s", version));
        GitUtils.push(gitDir);
        gitDir.getRepository().close();
    }

}
