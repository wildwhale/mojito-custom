package com.box.l10n.mojito.extend.feature.command;

import com.box.l10n.mojito.extend.feature.filefinder.FileFinder;
import com.box.l10n.mojito.extend.feature.filefinder.FileFinderException;
import com.box.l10n.mojito.extend.feature.filefinder.FileMatch;
import com.box.l10n.mojito.extend.feature.filefinder.file.FileType;
import com.box.l10n.mojito.extend.feature.filefinder.file.XcodeXliffFileType;
import com.box.l10n.mojito.rest.client.PollableTaskClient;
import com.box.l10n.mojito.rest.client.RepositoryClient;
import com.box.l10n.mojito.rest.client.WaitForPollableTaskListener;
import com.box.l10n.mojito.rest.client.exception.PollableTaskException;
import com.box.l10n.mojito.rest.client.exception.PollableTaskExecutionException;
import com.box.l10n.mojito.rest.client.exception.PollableTaskTimeoutException;
import com.box.l10n.mojito.rest.client.exception.RestClientException;
import com.box.l10n.mojito.rest.entity.Locale;
import com.box.l10n.mojito.rest.entity.PollableTask;
import com.box.l10n.mojito.rest.entity.Repository;
import com.box.l10n.mojito.rest.entity.RepositoryLocale;
import com.box.l10n.mojito.service.pollableTask.PollableTaskBlobStorage;
import com.box.l10n.mojito.service.pollableTask.PollableTaskService;
import com.box.l10n.mojito.service.repository.RepositoryService;
import com.box.l10n.mojito.service.tm.TMXliffRepository;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashBiMap;
import com.google.common.io.Files;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

import static com.box.l10n.mojito.rest.client.PollableTaskClient.NO_TIMEOUT;

/**
 * @author wyau
 */
@Component
public class CommandHelper {

    /**
     * logger
     */
    static Logger logger = LoggerFactory.getLogger(CommandHelper.class);

    /**
     * Supported BOM
     */
    private final ByteOrderMark[] boms = {ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE};

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    PollableTaskService pollableTaskService;
    @Autowired
    PollableTaskBlobStorage pollableTaskBlobStorage;

    @Autowired
    ModelMapper modelMapper;

    /**
     * @param repositoryName Name of repository
     * @return
     */
    public Repository findRepositoryByName(String repositoryName) throws CommandException {

        Preconditions.checkNotNull(repositoryName, "Repository name can't be null");
        List<com.box.l10n.mojito.entity.Repository> repositories = repositoryService.findRepositoriesIsNotDeletedOrderByName(repositoryName);
        if (ObjectUtils.isEmpty(repositories)) {
            throw new CommandException("Repository [" + repositoryName + "] is not found");
        }
        return convertRepositoryObject(repositories.get(0));
    }

    private Repository convertRepositoryObject(com.box.l10n.mojito.entity.Repository from) {
        return modelMapper.map(from, Repository.class);
    }

    /**
     * package com.box.l10n.mojito.custom.feature.filefinder;
     * Get list of {@link FileMatch} from
     * source directory
     *
     * @param commandDirectories
     * @param fileType
     * @param sourceLocale
     * @param sourcePathFilterRegex
     * @return
     * @throws CommandException
     */
    public ArrayList<FileMatch> getSourceFileMatches(
            CommandDirectories commandDirectories,
            FileType fileType,
            String sourceLocale,
            String sourcePathFilterRegex) throws CommandException {
        logger.debug("Search for source asset to be localized");
        FileFinder fileFinder = getFileFinder(commandDirectories, fileType, sourceLocale, sourcePathFilterRegex);
        return fileFinder.getSources();
    }

    /**
     * Get list of {@link FileMatch} from
     * target directory
     *
     * @param commandDirectories
     * @param fileType
     * @param sourcePathFilterRegex
     * @return
     * @throws CommandException
     */
    public ArrayList<FileMatch> getTargetFileMatches(CommandDirectories commandDirectories, FileType fileType, String sourceLocale, String sourcePathFilterRegex) throws CommandException {
        logger.debug("Search for target assets that are already localized");
        FileFinder fileFinder = getFileFinder(commandDirectories, fileType, sourceLocale, sourcePathFilterRegex);
        return fileFinder.getTargets();
    }

    /**
     * Get {@link FileFinder} from source directory
     *
     * @param commandDirectories
     * @param fileType
     * @param sourceLocale
     * @param sourcePathFilterRegex
     * @return
     * @throws CommandException
     */
    protected FileFinder getFileFinder(CommandDirectories commandDirectories, FileType fileType, String sourceLocale, String sourcePathFilterRegex) throws CommandException {
        FileFinder fileFinder = new FileFinder();
        fileFinder.setSourceDirectory(commandDirectories.getSourceDirectoryPath());
        fileFinder.setTargetDirectory(commandDirectories.getTargetDirectoryPath());
        fileFinder.setSourcePathFilterRegex(sourcePathFilterRegex);

        if (fileType != null) {
            fileFinder.setFileTypes(fileType);
        }

        if (!Strings.isNullOrEmpty(sourceLocale)) {
            for (FileType fileTypeForUpdate : fileFinder.getFileTypes()) {
                fileTypeForUpdate.getLocaleType().setSourceLocale(sourceLocale);
            }
        }

        try {
            fileFinder.find();
        } catch (FileFinderException e) {
            throw new CommandException(e.getMessage(), e);
        }

        return fileFinder;
    }

    /**
     * Get content from {@link Path} using UTF8
     *
     * @param path
     * @return
     * @throws CommandException
     */
    public String getFileContent(Path path) {
        try {
            File file = path.toFile();
            BOMInputStream inputStream = new BOMInputStream(FileUtils.openInputStream(file), false, boms);
            String fileContent;
            if (inputStream.hasBOM()) {
                fileContent = IOUtils.toString(inputStream, inputStream.getBOMCharsetName());
            } else {
                fileContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
            return fileContent;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot get file content for path: " + path.toString(), e);
        }
    }

    /**
     * Writes the content into a file using same format as source file
     *
     * @param content         content to be written
     * @param path            path to the file
     * @param sourceFileMatch
     * @throws CommandException
     */
    public void writeFileContent(String content, Path path, FileMatch sourceFileMatch) throws CommandException {
        try {
            File outputFile = path.toFile();
            BOMInputStream inputStream = new BOMInputStream(FileUtils.openInputStream(sourceFileMatch.getPath().toFile()), false, boms);
            if (inputStream.hasBOM()) {
                FileUtils.writeByteArrayToFile(outputFile, inputStream.getBOM().getBytes());
                FileUtils.writeByteArrayToFile(outputFile, content.getBytes(inputStream.getBOMCharsetName()), true);
            } else {
                FileUtils.writeStringToFile(outputFile, content, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new CommandException("Cannot write file content in path: " + path.toString(), e);
        }
    }

    /**
     * Writes the content into a file in UTF8
     *
     * @param content content to be written
     * @param path    path to the file
     * @throws CommandException
     */
    public void writeFileContent(String content, Path path) throws CommandException {
        try {
            Files.write(content, path.toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommandException("Cannot write file content in path: " + path.toString(), e);
        }
    }

    /**
     * Waits for {@link PollableTask} to be all finished (see {@link PollableTask#isAllFinished()
     * }). Infinite timeout.
     *
     * @param pollableId the {@link PollableTask#id}
     * @throws CommandException
     */
    public void waitForPollableTask(Long pollableId) throws CommandException {

        logger.debug("Running, task id: {} ", pollableId);

        try {
//            pollableTaskClient.waitForPollableTask(pollableId, NO_TIMEOUT, new CommandWaitForPollableTaskListener());
            waitForPollableTask(pollableId, NO_TIMEOUT, new CommandWaitForPollableTaskListener());
        } catch (PollableTaskException e) {
            throw new CommandException(e.getMessage(), e.getCause());
        }
    }

    private void waitForPollableTask(Long pollableId, long timeout, WaitForPollableTaskListener waitForPollableTaskListener) {
        long timeoutTime = System.currentTimeMillis() + timeout;
        long waitTime = 0;

        PollableTask pollableTask = null;

        while (pollableTask == null || !pollableTask.isAllFinished()) {

            logger.debug("Waiting for PollableTask: {} to finish", pollableId);

            pollableTask = getPollableTask(pollableId);

            List<PollableTask> pollableTaskWithErrors = getAllPollableTasksWithError(pollableTask);

            if (!pollableTaskWithErrors.isEmpty()) {

                for (PollableTask pollableTaskWithError : pollableTaskWithErrors) {
                    logger.debug("Error happened in PollableTask {}: {}", pollableTaskWithError.getId(), pollableTaskWithError.getErrorMessage().getMessage());
                }

                // Last task is the root task if it has an error or any of the sub task
                // TODO(P1) we might want to show all errors
                PollableTask lastTaskInError = pollableTaskWithErrors.get(pollableTaskWithErrors.size() - 1);

                throw new PollableTaskExecutionException(lastTaskInError.getErrorMessage().getMessage());
            }

            if (waitForPollableTaskListener != null) {
                waitForPollableTaskListener.afterPoll(pollableTask);
            }

            if (!pollableTask.isAllFinished()) {

                if (timeout != NO_TIMEOUT && System.currentTimeMillis() > timeoutTime) {
                    logger.debug("Timed out waiting for PollableTask: {} to finish. \n{}", pollableId, ReflectionToStringBuilder.toString(pollableTask));
                    throw new PollableTaskTimeoutException("Timed out waiting for PollableTask: " + pollableId);
                }

                try {
                    Thread.sleep(waitTime);
                    waitTime = getNextWaitTime(waitTime);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            } else {
                logger.debug("PollableTask: {} finished", pollableId);
            }
        }
    }

    private long getNextWaitTime(long lastWaitTime) {
        int maxTime = 500;
        long nextWaitTime = lastWaitTime + 25;
        nextWaitTime = Math.max(maxTime, nextWaitTime);
        return nextWaitTime;
    }

    private List<PollableTask> getAllPollableTasksWithError(PollableTask pollableTask) {
        List<PollableTask> result = new ArrayList<>();
        recursivelyGetAllPollableTaskWithError(pollableTask, result);
        return result;
    }

    private void recursivelyGetAllPollableTaskWithError(PollableTask pollableTask, List<PollableTask> pollableTasksWithError) {
        for (PollableTask subTask : pollableTask.getSubTasks()) {
            recursivelyGetAllPollableTaskWithError(subTask, pollableTasksWithError);
        }

        if (pollableTask.getErrorMessage() != null) {
            pollableTasksWithError.add(pollableTask);
        }
    }

    private PollableTask getPollableTask(Long pollableId) {
        com.box.l10n.mojito.entity.PollableTask pollableTask = pollableTaskService.getPollableTask(pollableId);
        return modelMapper.map(pollableTask, PollableTask.class);
    }

    /**
     * Gets the locale mapping given the locale mapping param
     *
     * @param localeMapppingParam locale mapping param coming from the CLI
     * @return A map containing the locale mapping
     */
    public Map<String, String> getLocaleMapping(String localeMapppingParam) {

        Map<String, String> localeMappings = null;
        if (localeMapppingParam != null) {
            localeMappings = Splitter.on(",").withKeyValueSeparator(":").split(localeMapppingParam);
        }

        return localeMappings;
    }

    /**
     * Gets the inverse locale mapping given the locale mapping param
     *
     * @param localeMapppingParam locale mapping param coming from the CLI
     * @return A map containing the inverse locale mapping
     */
    public Map<String, String> getInverseLocaleMapping(String localeMapppingParam) {

        Map<String, String> inverseLocaleMapping = null;

        if (localeMapppingParam != null) {
            inverseLocaleMapping = HashBiMap.create(getLocaleMapping(localeMapppingParam)).inverse();

        }

        return inverseLocaleMapping;
    }

    /**
     * Gets the repository locales sorted so that parent are before child
     * locales.
     *
     * @param repository
     * @return
     */
    public Map<String, Locale> getSortedRepositoryLocales(Repository repository) {

        LinkedHashMap<String, Locale> locales = new LinkedHashMap<>();

        ArrayDeque<RepositoryLocale> toProcess = new ArrayDeque<>(repository.getRepositoryLocales());
        Locale rootLocale = null;

        for (RepositoryLocale rl : toProcess) {
            if (rl.getParentLocale() == null) {
                rootLocale = rl.getLocale();
                toProcess.remove(rl);
                break;
            }
        }

        Set<Long> localeIds = new HashSet<>();

        while (!toProcess.isEmpty()) {
            RepositoryLocale rl = toProcess.removeFirst();
            Long parentLocaleId = rl.getParentLocale().getLocale().getId();
            if (parentLocaleId.equals(rootLocale.getId()) || localeIds.contains(parentLocaleId)) {
                localeIds.add(rl.getLocale().getId());
                locales.put(rl.getLocale().getBcp47Tag(), rl.getLocale());
            } else {
                toProcess.addLast(rl);
            }
        }

        return locales;
    }

    /**
     * Returns the filter options provided or defaults to the file type filter options.
     * (that can be null too)
     *
     * @param fileType
     * @param filterOptions
     * @return the filter options provided or the default options for the file type (can be null)
     */
    public List<String> getFilterOptionsOrDefaults(FileType fileType, List<String> filterOptions) {
        return filterOptions == null ? fileType.getDefaultFilterOptions() : filterOptions;
    }

    /**
     * Returns the date of last week if the condition is true else {@code null}
     *
     * @param condition
     * @return
     */
    DateTime getLastWeekDateIfTrue(boolean condition) {
        DateTime dateTime = null;
        if (condition) {
            dateTime = DateTime.now(DateTimeZone.UTC).minusWeeks(1);
        }
        return dateTime;
    }

    /**
     * Get content from {@link Path} using UTF8 and if it is an XLIFF from XCode patch the content
     * (Adds attribute xml:space="preserve" in the trans-unit element in the xliff)
     *
     * @param sourceFileMatch
     * @return
     * @throws CommandException
     */
    public String getFileContentWithXcodePatch(FileMatch sourceFileMatch) {
        String assetContent = getFileContent(sourceFileMatch.getPath());

        // TODO(P1) This is to inject xml:space="preserve" in the trans-unit element
        // in the xcode-generated xliff until xcode fixes the bug of not adding this attribute
        // See Xcode bug http://www.openradar.me/23410569
        if (XcodeXliffFileType.class == sourceFileMatch.getFileType().getClass()) {
            assetContent = assetContent.replaceAll("<trans-unit id=\"(.*?)\">", "<trans-unit id=\"$1\" xml:space=\"preserve\">");
        }

        return assetContent;
    }

}
