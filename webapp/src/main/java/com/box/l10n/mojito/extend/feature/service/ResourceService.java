package com.box.l10n.mojito.extend.feature.service;

import com.box.l10n.mojito.extend.feature.command.CommandException;
import com.box.l10n.mojito.extend.feature.dto.DeployParam;
import com.box.l10n.mojito.extend.feature.dto.UploadParam;
import com.box.l10n.mojito.rest.client.RepositoryClient;
import com.box.l10n.mojito.rest.client.exception.RepositoryNotFoundException;
import com.box.l10n.mojito.rest.entity.Repository;
import com.box.l10n.mojito.rest.entity.SourceAsset;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ResourceService {

    static Logger logger = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    MojitoRepository mojitoRepository;
    @Autowired
    ResourceExtractor resourceExtractor;
    @Autowired
    RepositoryClient repositoryClient;

    public com.box.l10n.mojito.entity.Repository upload(UploadParam param) throws Exception {
        // git checkout or pull in local working dir
        File repo = GitUtils.downloadResource(param.getGitUrl(), param.getGitId(), param.getGitPassword(), param.getBranch(), param.getGitRepoName());
        // 모히또 레포 없으면 생성
        createMojitoRepository(param.getMojitoRepoName(), param.getLocales());
        // extract
        Stream<SourceAsset> assetStream = extractResource(repo, param.getMojitoRepoName(), param.getFileType());
        // push repository
        return pushMojitoResource(param.getMojitoRepoName(), assetStream);
    }

    @Async
    public void asyncUpload(UploadParam param) throws Exception {
        upload(param);
    }

    public List<String> deploy(DeployParam param) throws Exception {
        // working git branch로 변경
        Git gitRepo = GitUtils.checkoutCommand(param.getGitRepoName(), param.getFromBranch());
        // pullCommand process 진행 (mojito db 내용을 file로 생성 )
        List<String> localizedFiles = mojitoRepository.pull(param.getGitRepoName(), param.getMojitoRepoName(), param.getStatus());
        // modify VERSION file ( file open & 수정 )
        modifyVersion(gitRepo, param.getVersion());
        // commit & push
        GitUtils.pushAllChanged(gitRepo, param.getGitId(), param.getGitPassword(), param.getCommitMessage());
        // checkout target branch
        GitUtils.checkoutCommand(param.getGitRepoName(), param.getToBranch());
        // merge ( working -> target )
        GitUtils.mergeCommand(gitRepo, param.getFromBranch(), param.getToBranch());
        // push
        GitUtils.pushCommand(gitRepo, param.getGitId(), param.getGitPassword());

        gitRepo.close();
        return localizedFiles;
    }

    @Async
    public void asyncDeploy(DeployParam param) throws Exception {
        deploy(param);
    }

    private void createMojitoRepository(String mojitoRepoName, String[] locales) throws CommandException {
        mojitoRepository.create(mojitoRepoName, locales);
    }

    private Stream<SourceAsset> extractResource(File repo, String mojitoRepoName, String fileType) throws CommandException {
        String resourcePath = repo.getPath() + "/" + mojitoRepoName;
        return resourceExtractor.extract(resourcePath, mojitoRepoName, fileType);
    }

    private com.box.l10n.mojito.entity.Repository pushMojitoResource(String mojitoRepoName, Stream<SourceAsset> assetStream) throws CommandException {
        Repository repository = resourceExtractor.getRepository(mojitoRepoName);
        return mojitoRepository.push(repository, assetStream, null);
    }

    private void modifyVersion(Git gitRepo, String version) {
        Path versionFilePath = gitRepo.getRepository().getWorkTree().toPath().resolve("VERSION");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(versionFilePath.toFile()))) {
            writer.write(version.trim());
        } catch (IOException e) {
            logger.error("{} : file not exist", versionFilePath.toString());
        }
    }

    public void deleteRepository(String repositoryName) throws RepositoryNotFoundException {
        repositoryClient.deleteRepositoryByName(repositoryName);
        logger.info("{} delete complete", repositoryName);
    }
}
