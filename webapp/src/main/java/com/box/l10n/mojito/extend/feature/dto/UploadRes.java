package com.box.l10n.mojito.extend.feature.dto;

import com.box.l10n.mojito.entity.Repository;
import com.box.l10n.mojito.entity.RepositoryStatistic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadRes {

    private String repoName;
    private String sourceLocale;

    private Long usedTextUnitCount = 0L;
    private Long usedTextUnitWordCount = 0L;
    private Long unusedTextUnitCount = 0L;
    private Long unusedTextUnitWordCount = 0L;

    private List<String> repositoryLocales;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private DateTime createdDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private DateTime lastModifiedDate;

    public UploadRes(Repository repository) {
        if (ObjectUtils.isNotEmpty(repository)) {
            this.repoName = repository.getName();
            this.sourceLocale = repository.getSourceLocale().getBcp47Tag();

            RepositoryStatistic repositoryStatistic = repository.getRepositoryStatistic();
            this.usedTextUnitCount = repositoryStatistic.getUsedTextUnitCount();
            this.usedTextUnitWordCount = repositoryStatistic.getUsedTextUnitWordCount();
            this.unusedTextUnitCount = repositoryStatistic.getUnusedTextUnitCount();
            this.unusedTextUnitWordCount = repositoryStatistic.getUnusedTextUnitWordCount();

            this.repositoryLocales = repository.getRepositoryLocales().stream()
                    .map(v -> v.getLocale().getBcp47Tag())
                    .collect(Collectors.toList());

            this.createdDate = repository.getCreatedDate();
            this.lastModifiedDate = repository.getLastModifiedDate();
        }
    }

    public String getRepoName() {
        return repoName;
    }

    public String getSourceLocale() {
        return sourceLocale;
    }

    public Long getUsedTextUnitCount() {
        return usedTextUnitCount;
    }

    public Long getUsedTextUnitWordCount() {
        return usedTextUnitWordCount;
    }

    public Long getUnusedTextUnitCount() {
        return unusedTextUnitCount;
    }

    public Long getUnusedTextUnitWordCount() {
        return unusedTextUnitWordCount;
    }

    public List<String> getRepositoryLocales() {
        return repositoryLocales;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public DateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
}
