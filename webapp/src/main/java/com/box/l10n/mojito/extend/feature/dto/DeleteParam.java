package com.box.l10n.mojito.extend.feature.dto;


import javax.validation.constraints.NotNull;

public class DeleteParam {

    @NotNull
    private String repositoryName;

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
}
