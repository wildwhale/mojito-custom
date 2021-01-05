package com.box.l10n.mojito.extend.feature.dto;


import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class UploadParam {
    @NotNull
    private String gitUrl;
    @NotNull
    private String gitId;
    @NotNull
    private String gitPassword;
    @NotNull
    private String branch;
    @NotNull
    private String gitRepoName;
    private String mojitoRepoName;
    @NotNull
    private String fileType;
    @NotNull
    private String[] locales;

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getGitId() {
        return gitId;
    }

    public void setGitId(String gitId) {
        this.gitId = gitId;
    }

    public String getGitPassword() {
        return gitPassword;
    }

    public void setGitPassword(String gitPassword) {
        this.gitPassword = gitPassword;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getGitRepoName() {
        return gitRepoName;
    }

    public void setGitRepoName(String gitRepoName) {
        this.gitRepoName = gitRepoName;
    }

    public String getMojitoRepoName() {
        return mojitoRepoName;
    }

    public void setMojitoRepoName(String mojitoRepoName) {
        this.mojitoRepoName = mojitoRepoName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String[] getLocales() {
        return locales;
    }

    public void setLocales(String[] locales) {
        this.locales = locales;
    }
}
