package com.box.l10n.mojito.extend.feature.service;

import com.box.l10n.mojito.extend.feature.command.CommandDirectories;
import com.box.l10n.mojito.extend.feature.command.CommandException;
import com.box.l10n.mojito.extend.feature.command.CommandHelper;
import com.box.l10n.mojito.extend.feature.filefinder.FileMatch;
import com.box.l10n.mojito.extend.feature.filefinder.file.FileTypes;
import com.box.l10n.mojito.rest.entity.Repository;
import com.box.l10n.mojito.rest.entity.SourceAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Stream;

@Component
public class ResourceExtractor {

    @Autowired
    CommandHelper commandHelper;

    public Repository getRepository(String repositoryName) throws CommandException {
        return commandHelper.findRepositoryByName(repositoryName);
    }

    public Stream<SourceAsset> extract(String sourceDirectoryPath, String repositoryName, String fileType) throws CommandException {
        CommandDirectories commandDirectories = new CommandDirectories(sourceDirectoryPath);
        Repository repository = getRepository(repositoryName);
        ArrayList<FileMatch> sourceFileMatches = commandHelper.getSourceFileMatches(commandDirectories, FileTypes.valueOf(fileType.toUpperCase()).toFileType(), null, null);

        return sourceFileMatches.stream().map(sourceFileMatch -> {
            String sourcePath = sourceFileMatch.getSourcePath();

            String assetContent = commandHelper.getFileContentWithXcodePatch(sourceFileMatch);

            SourceAsset sourceAsset = new SourceAsset();
            sourceAsset.setPath(sourcePath);
            sourceAsset.setContent(assetContent);
            sourceAsset.setExtractedContent(false);
            sourceAsset.setRepositoryId(repository.getId());
            sourceAsset.setFilterConfigIdOverride(sourceFileMatch.getFileType().getFilterConfigIdOverride());
            sourceAsset.setFilterOptions(commandHelper.getFilterOptionsOrDefaults(sourceFileMatch.getFileType(), null));

            return sourceAsset;
        });
    }
}
