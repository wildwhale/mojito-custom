package com.box.l10n.mojito.extend.feature.service;

import com.box.l10n.mojito.entity.Asset;
import com.box.l10n.mojito.extend.feature.command.CommandException;
import com.box.l10n.mojito.extend.feature.command.CommandHelper;
import com.box.l10n.mojito.rest.asset.AssetWS;
import com.box.l10n.mojito.rest.client.AssetClient;
import com.box.l10n.mojito.rest.client.RepositoryClient;
import com.box.l10n.mojito.rest.client.exception.PollableTaskException;
import com.box.l10n.mojito.rest.entity.Branch;
import com.box.l10n.mojito.rest.entity.PollableTask;
import com.box.l10n.mojito.rest.entity.Repository;
import com.box.l10n.mojito.rest.entity.SourceAsset;
import com.box.l10n.mojito.rest.repository.RepositoryWithIdNotFoundException;
import com.box.l10n.mojito.service.NormalizationUtils;
import com.box.l10n.mojito.service.asset.AssetService;
import com.box.l10n.mojito.service.branch.BranchRepository;
import com.box.l10n.mojito.service.pollableTask.PollableFuture;
import com.box.l10n.mojito.service.repository.RepositoryRepository;
import com.google.common.collect.Sets;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.box.l10n.mojito.rest.repository.BranchSpecification.*;
import static com.box.l10n.mojito.specification.Specifications.ifParamNotNull;
import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * @author jaurambault
 */
@Component
public class PushService {

    /**
     * logger
     */
    static Logger logger = LoggerFactory.getLogger(PushService.class);

    @Autowired
    AssetService assetService;
    @Autowired
    RepositoryRepository repositoryRepository;
    @Autowired
    BranchRepository branchRepository;
    @Autowired
    CommandHelper commandHelper;

    @Autowired
    ModelMapper modelMapper;

    public com.box.l10n.mojito.entity.Repository execute(Repository repository, Stream<SourceAsset> sourceAssetStream, String branchName, PushType pushType) throws CommandException, RepositoryWithIdNotFoundException {

        List<PollableTask> pollableTasks = new ArrayList<>();
        Set<Long> usedAssetIds = new HashSet<>();
        sourceAssetStream.forEach(sourceAsset -> {
            logger.info(" - Uploading: {}", sourceAsset.getPath());

//            SourceAsset assetAfterSend = assetClient.sendSourceAsset(sourceAsset);
            SourceAsset assetAfterSend = null;
            try {
                com.box.l10n.mojito.rest.asset.SourceAsset assetAfterSendRs = importSourceAsset(convertAsset(sourceAsset, com.box.l10n.mojito.rest.asset.SourceAsset.class));
                assetAfterSend  = convertAsset(assetAfterSendRs, com.box.l10n.mojito.rest.entity.SourceAsset.class);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            pollableTasks.add(assetAfterSend.getPollableTask());

            logger.info(" --> asset id: {}, task: {}", assetAfterSend.getAddedAssetId(), assetAfterSend.getPollableTask().getId());
            usedAssetIds.add(assetAfterSend.getAddedAssetId());
        });

        if (PushType.SEND_ASSET_NO_WAIT_NO_DELETE.equals(pushType)) {
            logger.info("Warning you are using push type: SEND_ASSET_NO_WAIT_NO_DELETE. The" +
                    "command won't wait for the asset processing to finish (ie. if any error " +
                    "happens it will silently fail) and it will skip the asset delete.");
            return null;
        }

        try {
            logger.debug("Wait for all \"push\" tasks to be finished");
            for (PollableTask pollableTask : pollableTasks) {
                commandHelper.waitForPollableTask(pollableTask.getId());
            }
        } catch (PollableTaskException e) {
            throw new CommandException(e.getMessage(), e.getCause());
        }

//        Branch branch = repositoryClient.getBranch(repository.getId(), branchName);
        Branch branch = getBranch(repository.getId(), branchName);

        if (branch == null) {
            logger.debug("No branch in the repository, no asset must have been pushed yet, no need to delete");
        } else {
            logger.debug("process deleted assets here");
//            Set<Long> assetIds = Sets.newHashSet(assetClient.getAssetIds(repository.getId(), false, false, branch.getId()));
            Set<Long> assetIds = Sets.newHashSet(assetService.findAllAssetIds(repository.getId(), null, false, false, branch.getId()));

            assetIds.removeAll(usedAssetIds);
            if (!assetIds.isEmpty()) {
                logger.info("Delete assets from repository, ids: {}", assetIds.toString());
//                PollableTask pollableTask = assetClient.deleteAssetsInBranch(assetIds, branch.getId());
                PollableFuture pollableFuture = assetService.asyncDeleteAssetsOfBranch(assetIds, branch.getId());
                logger.info(" --> task id: {}", pollableFuture.getPollableTask().getId());
                commandHelper.waitForPollableTask(pollableFuture.getPollableTask().getId());
            }
        }
        return repositoryRepository.findOne(repository.getId());
    }

    private <T> T convertAsset(Object from, Class<T> targetClass) {
        return modelMapper.map(from ,targetClass);
    }

    // Controller method 가져옴..
    public com.box.l10n.mojito.rest.asset.SourceAsset importSourceAsset(@RequestBody com.box.l10n.mojito.rest.asset.SourceAsset sourceAsset) throws Throwable {
        logger.debug("Importing source asset");

        // ********************************************
        // TODO(P1) check permission to update the repo
        // ********************************************
        String normalizedContent = NormalizationUtils.normalize(sourceAsset.getContent());
        PollableFuture<Asset> assetFuture = assetService.addOrUpdateAssetAndProcessIfNeeded(
                sourceAsset.getRepositoryId(),
                sourceAsset.getPath(),
                normalizedContent,
                sourceAsset.isExtractedContent(),
                sourceAsset.getBranch(),
                sourceAsset.getBranchCreatedByUsername(),
                sourceAsset.getFilterConfigIdOverride(),
                sourceAsset.getFilterOptions()
        );

        try {
            sourceAsset.setAddedAssetId(assetFuture.get().getId());
        } catch (ExecutionException ee) {
            logger.error(ee.getMessage());
        }

        sourceAsset.setPollableTask(assetFuture.getPollableTask());

        return sourceAsset;
    }

    private Branch getBranch(Long repositoryId, String branchName) throws RepositoryWithIdNotFoundException {
        ResponseEntity<com.box.l10n.mojito.entity.Repository> result;
        com.box.l10n.mojito.entity.Repository repository = repositoryRepository.findOne(repositoryId);

        if (repository == null) {
            throw new RepositoryWithIdNotFoundException(repositoryId);
        }

        List<com.box.l10n.mojito.entity.Branch> branches = branchRepository.findAll(where(
                ifParamNotNull(nameEquals(branchName))).and(
                ifParamNotNull(repositoryEquals(repository))).and(
                ifParamNotNull(deletedEquals(null))).and(
                ifParamNotNull(branchStatisticTranslated(null))).and(
                ifParamNotNull((createdBefore(null))))
        );

        List<Branch> branchList = convertBranchList(branches);
        return branchList.stream().filter((b) -> Objects.equals(b.getName(), branchName))
                .findFirst().orElse(null);
    }

    private List<Branch> convertBranchList(List<com.box.l10n.mojito.entity.Branch> branches) {
        return branches.stream().map(item -> modelMapper.map(item, Branch.class)).collect(Collectors.toList());
    }


//    public com.box.l10n.mojito.entity.Repository execute(Repository repository, Stream<SourceAsset> sourceAssetStream, String branchName, PushType pushType) throws CommandException {
//
//        List<PollableTask> pollableTasks = new ArrayList<>();
//        Set<Long> usedAssetIds = new HashSet<>();
//        sourceAssetStream.forEach(sourceAsset -> {
//            logger.info(" - Uploading: {}", sourceAsset.getPath());
//
//            SourceAsset assetAfterSend = assetClient.sendSourceAsset(sourceAsset);
//            pollableTasks.add(assetAfterSend.getPollableTask());
//
//            logger.info(" --> asset id: {}, task: {}", assetAfterSend.getAddedAssetId(), assetAfterSend.getPollableTask().getId());
//            usedAssetIds.add(assetAfterSend.getAddedAssetId());
//        });
//
//        if (PushType.SEND_ASSET_NO_WAIT_NO_DELETE.equals(pushType)) {
//            logger.info("Warning you are using push type: SEND_ASSET_NO_WAIT_NO_DELETE. The" +
//                    "command won't wait for the asset processing to finish (ie. if any error " +
//                    "happens it will silently fail) and it will skip the asset delete.");
//            return null;
//        }
//
//        try {
//            logger.debug("Wait for all \"push\" tasks to be finished");
//            for (PollableTask pollableTask : pollableTasks) {
//                commandHelper.waitForPollableTask(pollableTask.getId());
//            }
//        } catch (PollableTaskException e) {
//            throw new CommandException(e.getMessage(), e.getCause());
//        }
//
//        Branch branch = repositoryClient.getBranch(repository.getId(), branchName);
//
//        if (branch == null) {
//            logger.debug("No branch in the repository, no asset must have been pushed yet, no need to delete");
//        } else {
//            logger.debug("process deleted assets here");
//            Set<Long> assetIds = Sets.newHashSet(assetClient.getAssetIds(repository.getId(), false, false, branch.getId()));
//
//            assetIds.removeAll(usedAssetIds);
//            if (!assetIds.isEmpty()) {
//                logger.info("Delete assets from repository, ids: {}", assetIds.toString());
//                PollableTask pollableTask = assetClient.deleteAssetsInBranch(assetIds, branch.getId());
//                logger.info(" --> task id: {}", pollableTask.getId());
//                commandHelper.waitForPollableTask(pollableTask.getId());
//            }
//        }
//        return repositoryRepository.findOne(repository.getId());
//    }

    enum PushType {
        /**
         * Normal processing: send asset, wait for them to be process and remove unused assets.
         */
        NORMAL,
        /**
         * Just send the assets to the server. Don't wait for them to be processed. Don't delete the assets.
         * <p>
         * This is can be used to speed up the asset submission. The compromise is that there is no
         * visibility on the success or failure during processing. It also won't run the logic to remove assets
         * that are not used anymore.
         * <p>
         * Usage example is to speed up CI jobs but it is a stop gap until Mojito backend performance are improved and/or
         * more evolved async system is implemented.
         * <p>
         * Don't use unless you know what you're doing.
         */
        SEND_ASSET_NO_WAIT_NO_DELETE
    }
}
