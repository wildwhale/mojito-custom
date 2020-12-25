package com.box.l10n.mojito.extend.feature.service;

import com.box.l10n.mojito.extend.feature.command.CommandDirectories;
import com.box.l10n.mojito.extend.feature.command.CommandException;
import com.box.l10n.mojito.extend.feature.command.CommandHelper;
import com.box.l10n.mojito.extend.feature.filefinder.FileMatch;
import com.box.l10n.mojito.json.ObjectMapper;
import com.box.l10n.mojito.rest.client.AssetClient;
import com.box.l10n.mojito.rest.client.exception.AssetNotFoundException;
import com.box.l10n.mojito.rest.entity.*;
import com.box.l10n.mojito.rest.entity.LocalizedAssetBody;
import com.box.l10n.mojito.rest.entity.Repository;
import com.box.l10n.mojito.rest.entity.RepositoryLocale;
import com.box.l10n.mojito.service.NormalizationUtils;
import com.box.l10n.mojito.service.asset.AssetRepository;
import com.box.l10n.mojito.service.asset.AssetService;
import com.box.l10n.mojito.service.repository.RepositoryLocaleRepository;
import com.box.l10n.mojito.service.tm.TMService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PullService {
    static Logger logger = LoggerFactory.getLogger(PullService.class);

    @Autowired
    AssetClient assetClient;
    @Autowired
    CommandHelper commandHelper;
    @Autowired
    ObjectMapper objectMapper;

    public List<String> execute(String mojitoRepoName, String sourceDirPath, LocalizedAssetBody.Status status) throws CommandException {

        Repository repository = commandHelper.findRepositoryByName(mojitoRepoName);
        CommandDirectories commandDirectories = new CommandDirectories(sourceDirPath, null);
        Map<String, RepositoryLocale> repositoryLocalesWithoutRootLocale = initRepositoryLocalesMapAndRootRepositoryLocale(repository);
        List<String> localizedFiles = new ArrayList<>();
        for (FileMatch sourceFileMatch : commandHelper.getSourceFileMatches(commandDirectories, null, null, null)) {
            List<String> filterOptions = commandHelper.getFilterOptionsOrDefaults(sourceFileMatch.getFileType(), null);
            List<String> files = generateLocalizedFilesWithoutLocaleMapping(repository, sourceFileMatch, repositoryLocalesWithoutRootLocale, filterOptions, status);
            localizedFiles.addAll(files);
        }
        return localizedFiles;
    }

    /**
     * Default generation, uses the locales defined in the repository to
     * generate the localized files.
     *
     * @param repository
     * @param sourceFileMatch
     * @param filterOptions
     * @throws CommandException
     * @return
     */
    List<String> generateLocalizedFilesWithoutLocaleMapping(Repository repository, FileMatch sourceFileMatch, Map<String, RepositoryLocale> repositoryLocalesWithoutRootLocale, List<String> filterOptions, LocalizedAssetBody.Status status) throws CommandException {

        logger.debug("Generate localized files (without locale mapping)");

        List<String> localizedFiles = new ArrayList<>();
        for (RepositoryLocale repositoryLocale : repositoryLocalesWithoutRootLocale.values()) {
            String localizedFile = generateLocalizedFile(repository, sourceFileMatch, filterOptions, null, repositoryLocale, status);
            localizedFiles.add(localizedFile);
        }
        return localizedFiles;
    }

    String generateLocalizedFile(Repository repository, FileMatch sourceFileMatch, List<String> filterOptions, String outputBcp47tag, RepositoryLocale repositoryLocale, LocalizedAssetBody.Status status) throws CommandException {
        if (shouldGenerateLocalizedFile(repositoryLocale)) {
            LocalizedAssetBody localizedAsset = getLocalizedAsset(repository, sourceFileMatch, repositoryLocale, outputBcp47tag, filterOptions, status);
            return writeLocalizedAssetToTargetDirectory(localizedAsset, sourceFileMatch);
        } else {
            logger.info(" - Skipping locale: {} --> not fully translated", repositoryLocale.getLocale().getBcp47Tag());
            return null;
        }
    }

    /**
     * Gets the list of {@link RepositoryLocale}s of a {@link Repository}
     * excluding the root locale (the only locale that has no parent locale).
     *
     * @param repository the repository
     * @return the list of {@link RepositoryLocale}s excluding the root locale.
     */
    private Map<String, RepositoryLocale> initRepositoryLocalesMapAndRootRepositoryLocale(Repository repository) {

        Map<String, RepositoryLocale> repositoryLocalesWithoutRootLocale = new HashMap<>();
        logger.debug("locale num of count : {}", repository.getRepositoryLocales().size());
        for (RepositoryLocale repositoryLocale : repository.getRepositoryLocales()) {
            if (repositoryLocale.getParentLocale() != null) {
                repositoryLocalesWithoutRootLocale.put(repositoryLocale.getLocale().getBcp47Tag(), repositoryLocale);
            } else {
                logger.warn("Not found parentLocale: {}", repositoryLocale.getId());
//                rootRepositoryLocale = repositoryLocale;
            }
        }
        return repositoryLocalesWithoutRootLocale;
    }

    String writeLocalizedAssetToTargetDirectory(LocalizedAssetBody localizedAsset, FileMatch sourceFileMatch) throws CommandException {

        CommandDirectories commandDirectories = new CommandDirectories(null, null);
        Path targetPath = sourceFileMatch.getPath().getParent().resolve(sourceFileMatch.getTargetPath(localizedAsset.getBcp47Tag()));

        commandHelper.writeFileContent(localizedAsset.getContent(), targetPath, sourceFileMatch);

        Path relativeTargetFilePath = commandDirectories.relativizeWithUserDirectory(targetPath);

        logger.info(" --> {}", relativeTargetFilePath.toString());
        return relativeTargetFilePath.toString();
    }

    LocalizedAssetBody getLocalizedAsset(Repository repository, FileMatch sourceFileMatch, RepositoryLocale repositoryLocale, String outputBcp47tag, List<String> filterOptions, LocalizedAssetBody.Status status) throws CommandException {
        logger.info(" - Processing locale: {}", repositoryLocale.getLocale().getBcp47Tag());

        String sourcePath = sourceFileMatch.getSourcePath();
        com.box.l10n.mojito.rest.entity.Asset assetByPathAndRepositoryId;
        try {
            logger.debug("Getting the asset for path: {} and locale: {}", sourcePath, repositoryLocale.getLocale().getBcp47Tag());
            assetByPathAndRepositoryId = assetClient.getAssetByPathAndRepositoryId(sourcePath, repository.getId());
        } catch (AssetNotFoundException e) {
            throw new CommandException("Asset with path [" + sourcePath + "] was not found in repo ", e);
        }

        String assetContent = commandHelper.getFileContentWithXcodePatch(sourceFileMatch);

        LocalizedAssetBody localizedAsset = null;

        localizedAsset = getLocalizedAssetBodySync(sourceFileMatch, repositoryLocale, outputBcp47tag, filterOptions, assetByPathAndRepositoryId, assetContent, localizedAsset, status);

        logger.trace("LocalizedAsset content = {}", localizedAsset.getContent());

        return localizedAsset;
    }

    LocalizedAssetBody getLocalizedAssetBodySync(FileMatch sourceFileMatch, RepositoryLocale repositoryLocale, String outputBcp47tag, List<String> filterOptions, Asset assetByPathAndRepositoryId, String assetContent, LocalizedAssetBody localizedAsset, LocalizedAssetBody.Status status) {
        //TODO remove this is temporary, Async service is implemented but we don't use it yet by default
        int count = 0;
        int maxCount = 5;
        while (localizedAsset == null && count < maxCount) {
            try {
                localizedAsset = assetClient.getLocalizedAssetForContent(
                        assetByPathAndRepositoryId.getId(),
                        repositoryLocale.getLocale().getId(),
                        assetContent,
                        outputBcp47tag,
                        sourceFileMatch.getFileType().getFilterConfigIdOverride(),
                        filterOptions,
                        status,
                        LocalizedAssetBody.InheritanceMode.REMOVE_UNTRANSLATED
                );
            } catch (Exception e) {
                count++;
                logger.info("Attempt {} / {} for locale: {} failed. Retrying...", count, maxCount, repositoryLocale.getLocale().getBcp47Tag());
            }
        }
        return localizedAsset;
    }

    private boolean shouldGenerateLocalizedFile(RepositoryLocale repositoryLocale) {
        boolean localize = true;

//        if (onlyIfFullyTranslated) {
//            if (repositoryLocale.isToBeFullyTranslated()) {
//                localize = localeFullyTranslated.get(repositoryLocale.getLocale().getBcp47Tag());
//            } else {
//                localize = localeFullyTranslated.get(repositoryLocale.getParentLocale().getLocale().getBcp47Tag());
//            }
//        }

        return localize;
    }

}
