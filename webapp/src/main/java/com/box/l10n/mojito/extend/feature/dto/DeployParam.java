package com.box.l10n.mojito.extend.feature.dto;

import com.box.l10n.mojito.rest.entity.LocalizedAssetBody;

import javax.validation.constraints.NotNull;

public class DeployParam {

    @NotNull
    private String gitRepoName;
    @NotNull
    private String gitId;
    @NotNull
    private String gitPassword;
    private String fromBranch = "develop";
    private String toBranch = "master";
    private String mojitoRepoName;
    @NotNull
    private String fileType;
    @NotNull
    private String version;
    private String commitMessage = "fix: version up";
    private LocalizedAssetBody.Status status = LocalizedAssetBody.Status.ACCEPTED;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getGitRepoName() {
        return gitRepoName;
    }

    public void setGitRepoName(String gitRepoName) {
        this.gitRepoName = gitRepoName;
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

    public String getFromBranch() {
        return fromBranch;
    }

    public void setFromBranch(String fromBranch) {
        this.fromBranch = fromBranch;
    }

    public String getToBranch() {
        return toBranch;
    }

    public void setToBranch(String toBranch) {
        this.toBranch = toBranch;
    }

    public String getMojitoRepoName() {
        return mojitoRepoName;
    }

    public void setMojitoRepoName(String mojitoRepoName) {
        this.mojitoRepoName = mojitoRepoName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalizedAssetBody.Status getStatus() {
        return status;
    }

    public void setStatus(LocalizedAssetBody.Status status) {
        this.status = status;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }
}
