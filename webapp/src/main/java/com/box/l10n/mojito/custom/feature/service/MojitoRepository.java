package com.box.l10n.mojito.custom.feature.service;

import com.box.l10n.mojito.custom.feature.command.CommandException;
import com.box.l10n.mojito.rest.client.LocaleClient;
import com.box.l10n.mojito.rest.client.RepositoryClient;
import com.box.l10n.mojito.rest.client.exception.ResourceNotCreatedException;
import com.box.l10n.mojito.rest.entity.*;
import com.box.l10n.mojito.rest.entity.Locale;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component
public class MojitoRepository {

    static Logger logger = LoggerFactory.getLogger(MojitoRepository.class);
    private static final Pattern BCP47_TAG_BRACKET_PATTERN = Pattern.compile("\\((?<bcp47Tag>.*?)\\)");

    @Autowired
    LocaleClient localeClient;
    @Autowired
    RepositoryClient repositoryClient;

    @Autowired
    PushService pushService;

    @Autowired
    PullService pullService;

    public void push(Repository repository, Stream<SourceAsset> sourceAssetStream, String branchName) throws CommandException {
        pushService.execute(repository, sourceAssetStream, branchName, PushService.PushType.NORMAL);
    }

    public void pull(String repositoryName, LocalizedAssetBody.Status status) throws CommandException {
        pullService.execute(repositoryName, status);
    }

    public void create(String repositoryName, String[] locales) throws CommandException {
        try {
            List<String> encodedBcp47Tags = Stream.of(locales).collect(toList());
            Set<RepositoryLocale> repositoryLocales = extractRepositoryLocalesFromInput(encodedBcp47Tags, true);
//            Set<IntegrityChecker> integrityCheckers = extractIntegrityCheckersFromInput(integrityCheckParam, true);

//            Locale sourceLocale = null;
//            if (sourceLocaleBcp47Tags != null) {
//                sourceLocale = localeClient.getLocaleByBcp47Tag(sourceLocaleBcp47Tags);
//                sourceLocale = localeClient.getLocaleByBcp47Tag("en");
//            }

            repositoryClient.createRepository(repositoryName, null, null, repositoryLocales, null, false);
        } catch (ResourceNotCreatedException ex) {
            throw new CommandException(ex.getMessage(), ex);
        }
    }

    private Set<RepositoryLocale> extractRepositoryLocalesFromInput(List<String> encodedBcp47Tags, boolean doPrint) throws CommandException {
        Set<RepositoryLocale> repositoryLocales = new LinkedHashSet<>();

        if (encodedBcp47Tags != null) {
            List<Locale> locales = localeClient.getLocales();
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
