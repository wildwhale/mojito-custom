package com.box.l10n.mojito.extend.feature.service;

import com.box.l10n.mojito.extend.feature.command.CommandException;
import com.box.l10n.mojito.extend.feature.command.CommandHelper;
import com.box.l10n.mojito.rest.client.AssetClient;
import com.box.l10n.mojito.rest.client.RepositoryClient;
import com.box.l10n.mojito.rest.client.exception.PollableTaskException;
import com.box.l10n.mojito.rest.entity.Branch;
import com.box.l10n.mojito.rest.entity.PollableTask;
import com.box.l10n.mojito.rest.entity.Repository;
import com.box.l10n.mojito.rest.entity.SourceAsset;
import com.box.l10n.mojito.service.repository.RepositoryRepository;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
    AssetClient assetClient;
    @Autowired
    RepositoryClient repositoryClient;
    @Autowired
    RepositoryRepository repositoryRepository;
    @Autowired
    CommandHelper commandHelper;

    public com.box.l10n.mojito.entity.Repository execute(Repository repository, Stream<SourceAsset> sourceAssetStream, String branchName, PushType pushType) throws CommandException {

        List<PollableTask> pollableTasks = new ArrayList<>();
        Set<Long> usedAssetIds = new HashSet<>();
        sourceAssetStream.forEach(sourceAsset -> {
            logger.info(" - Uploading: {}", sourceAsset.getPath());

            SourceAsset assetAfterSend = assetClient.sendSourceAsset(sourceAsset);
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

        Branch branch = repositoryClient.getBranch(repository.getId(), branchName);

        if (branch == null) {
            logger.debug("No branch in the repository, no asset must have been pushed yet, no need to delete");
        } else {
            logger.debug("process deleted assets here");
            Set<Long> assetIds = Sets.newHashSet(assetClient.getAssetIds(repository.getId(), false, false, branch.getId()));

            assetIds.removeAll(usedAssetIds);
            if (!assetIds.isEmpty()) {
                logger.info("Delete assets from repository, ids: {}", assetIds.toString());
                PollableTask pollableTask = assetClient.deleteAssetsInBranch(assetIds, branch.getId());
                logger.info(" --> task id: {}", pollableTask.getId());
                commandHelper.waitForPollableTask(pollableTask.getId());
            }
        }
        return repositoryRepository.findOne(repository.getId());
    }

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
