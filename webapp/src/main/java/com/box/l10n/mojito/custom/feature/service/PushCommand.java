//package com.box.l10n.mojito.custom.feature.service;
//
//import com.box.l10n.mojito.custom.feature.command.CommandDirectories;
//import com.box.l10n.mojito.custom.feature.command.CommandException;
//import com.box.l10n.mojito.custom.feature.command.CommandHelper;
//import com.box.l10n.mojito.custom.feature.filefinder.FileMatch;
//import com.box.l10n.mojito.custom.feature.filefinder.file.FileType;
//import com.box.l10n.mojito.rest.client.RepositoryClient;
//import com.box.l10n.mojito.rest.entity.Repository;
//import com.box.l10n.mojito.rest.entity.SourceAsset;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Stream;
//
///**
// * @author jaurambault
// */
//@Component
////@Scope("prototype")
////@Parameters(commandNames = {"push", "p"}, commandDescription = "Push assets to be localized to TMS")
//public class PushCommand {
//    /**
//     * logger
//     */
//    static Logger logger = LoggerFactory.getLogger(PushCommand.class);
//
////    @Parameter(names = {Param.BRANCH_LONG, Param.BRANCH_SHORT}, arity = 1, required = false, description = Param.BRANCH_DESCRIPTION)
//    String branchName;
//
////    @Parameter(names = {"--branch-createdby", "-bc"}, arity = 1, required = false, description = "username of text unit author")
//    String branchCreatedBy;
//
//    @Autowired
//    RepositoryClient repositoryClient;
//    @Autowired
//    CommandHelper commandHelper;
//    @Autowired
//    PushService pushService;
//
//    public void execute(String repositoryName) throws CommandException {
//
//        CommandDirectories commandDirectories = new CommandDirectories(null);
//
//        Repository repository = commandHelper.findRepositoryByName(repositoryName);
//
//        ArrayList<FileMatch> sourceFileMatches = commandHelper.getSourceFileMatches(commandDirectories, null, null, null);
//
//        Stream<SourceAsset> sourceAssetStream = sourceFileMatches.stream().map(sourceFileMatch -> {
//            String sourcePath = sourceFileMatch.getSourcePath();
//
//            String assetContent = commandHelper.getFileContentWithXcodePatch(sourceFileMatch);
//
//            SourceAsset sourceAsset = new SourceAsset();
//            sourceAsset.setBranch(null);
//            sourceAsset.setBranchCreatedByUsername(branchCreatedBy);
//            sourceAsset.setPath(sourcePath);
//            sourceAsset.setContent(assetContent);
//            sourceAsset.setExtractedContent(false);
//            sourceAsset.setRepositoryId(repository.getId());
//            sourceAsset.setFilterConfigIdOverride(sourceFileMatch.getFileType().getFilterConfigIdOverride());
//            sourceAsset.setFilterOptions(commandHelper.getFilterOptionsOrDefaults(sourceFileMatch.getFileType(), null));
//
//            return sourceAsset;
//        });
//
//        pushService.push(repository, sourceAssetStream, null, PushService.PushType.NORMAL);
//    }
//}
