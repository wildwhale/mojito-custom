package com.box.l10n.mojito.extend.feature.service;

import com.box.l10n.mojito.entity.Locale;
import com.box.l10n.mojito.entity.RepositoryLocale;
import com.box.l10n.mojito.extend.feature.command.CommandException;
import com.box.l10n.mojito.rest.client.LocaleClient;
import com.box.l10n.mojito.rest.client.RepositoryClient;
import com.box.l10n.mojito.rest.client.exception.AssetNotFoundException;
import com.box.l10n.mojito.rest.entity.LocalizedAssetBody;
import com.box.l10n.mojito.rest.entity.Repository;
import com.box.l10n.mojito.rest.entity.SourceAsset;
import com.box.l10n.mojito.rest.repository.RepositoryWithIdNotFoundException;
import com.box.l10n.mojito.service.locale.LocaleRepository;
import com.box.l10n.mojito.service.repository.RepositoryLocaleCreationException;
import com.box.l10n.mojito.service.repository.RepositoryNameAlreadyUsedException;
import com.box.l10n.mojito.service.repository.RepositoryService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.box.l10n.mojito.rest.locale.LocaleSpecification.bcp47TagEquals;
import static com.box.l10n.mojito.specification.Specifications.ifParamNotNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class MojitoRepository {

    static Logger logger = LoggerFactory.getLogger(MojitoRepository.class);
    private static final Pattern BCP47_TAG_BRACKET_PATTERN = Pattern.compile("\\((?<bcp47Tag>.*?)\\)");

//    @Autowired
//    LocaleClient localeClient;
//    @Autowired
//    RepositoryClient repositoryClient;

    @Autowired
    LocaleRepository localeRepository;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    PushService pushService;
    @Autowired
    PullService pullService;

    public com.box.l10n.mojito.entity.Repository push(Repository repository, Stream<SourceAsset> sourceAssetStream, String branchName) throws CommandException, RepositoryWithIdNotFoundException {
        return pushService.execute(repository, sourceAssetStream, branchName, PushService.PushType.NORMAL);
    }

    public List<String> pull(String gitRepoName, String mojitoRepoName, LocalizedAssetBody.Status status) throws CommandException, AssetNotFoundException {
        String sourceDirPath = null;
        // mojitoRepoName 설정 하지 않을 경우는 깃 디렉토리 이름이 레포 이름
        if (StringUtils.isEmpty(gitRepoName)) {
            sourceDirPath = GitUtils.getWorkingDir(mojitoRepoName).toString();
        } else {
            sourceDirPath = GitUtils.getWorkingDir(String.format("%s/%s", gitRepoName, mojitoRepoName)).toString();
        }
        return pullService.execute(mojitoRepoName, sourceDirPath, status);
    }

    public void create(String repositoryName, String[] locales) throws CommandException {
        try {
            List<String> encodedBcp47Tags = Stream.of(locales).collect(toList());
            Set<RepositoryLocale> repositoryLocales = extractRepositoryLocalesFromInput(encodedBcp47Tags, true);
            repositoryService.createRepository(repositoryName, null, null, false, Collections.EMPTY_SET, repositoryLocales);
//            repositoryClient.createRepository()
        } catch (RepositoryNameAlreadyUsedException e) {
            logger.error(e.getMessage());
        } catch (RepositoryLocaleCreationException e) {
            logger.error(e.getMessage());
        }
    }

    private Set<RepositoryLocale> extractRepositoryLocalesFromInput(List<String> encodedBcp47Tags, boolean doPrint) throws CommandException {
        Set<RepositoryLocale> repositoryLocales = new LinkedHashSet<>();

        if (encodedBcp47Tags != null) {
            List<Locale> locales = localeRepository.findAll(
                    where(ifParamNotNull(bcp47TagEquals(null)))
            );
            Map<String, Locale> localeMapByBcp47Tag = getLocaleMapByBcp47Tag(locales);

            for (String encodedBcp47Tag : encodedBcp47Tags) {
                RepositoryLocale repositoryLocale = getRepositoryLocaleFromEncodedBcp47Tag(localeMapByBcp47Tag, encodedBcp47Tag, doPrint);
                repositoryLocales.add(repositoryLocale);
            }
        }
        return repositoryLocales;
    }

    private Map<String, Locale> getLocaleMapByBcp47Tag(List<Locale> locales) {
        Map<String, Locale> bcp47LocaleMap = new LinkedHashMap<>(locales.size());

        for (Locale locale : locales) {
            bcp47LocaleMap.put(locale.getBcp47Tag(), locale);
        }

        return bcp47LocaleMap;
    }

    private RepositoryLocale getRepositoryLocaleFromEncodedBcp47Tag(Map<String, Locale> localeMapByBcp47Tag, String encodedBcp47Tag, boolean doPrint) throws CommandException {

        RepositoryLocale repositoryLocale = new RepositoryLocale();

        List<String> bcp47Tags = Lists.newArrayList(encodedBcp47Tag.split("->"));

        for (String bcp47Tag : bcp47Tags) {
            Matcher matcher = BCP47_TAG_BRACKET_PATTERN.matcher(bcp47Tag);
            if (matcher.find()) {
                repositoryLocale.setToBeFullyTranslated(false);
                bcp47Tag = matcher.group("bcp47Tag");
            }

            Locale locale = localeMapByBcp47Tag.get(bcp47Tag);

            if (locale == null) {
                throw new CommandException("Locale [" + bcp47Tag + "] does not exist in the system");
            }

            if (repositoryLocale.getLocale() == null) {
                repositoryLocale.setLocale(locale);
            } else {
                addLocaleAsTheLastParent(repositoryLocale, locale);
            }
        }

        return repositoryLocale;
    }

    private void addLocaleAsTheLastParent(RepositoryLocale repositoryLocale, Locale parentLocale) {
        while (repositoryLocale.getParentLocale() != null) {
            repositoryLocale = repositoryLocale.getParentLocale();
        }

        RepositoryLocale parentRepoLocale = new RepositoryLocale();
        parentRepoLocale.setLocale(parentLocale);

        repositoryLocale.setParentLocale(parentRepoLocale);
    }
}
