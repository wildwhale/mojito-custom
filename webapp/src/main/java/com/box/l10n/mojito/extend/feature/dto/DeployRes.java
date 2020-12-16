package com.box.l10n.mojito.extend.feature.dto;

import java.util.List;

public class DeployRes {

    private List<String> localizedFiles;

    public DeployRes(List<String> localizedFiles) {
        this.localizedFiles = localizedFiles;
    }

    public List<String> getLocalizedFiles() {
        return localizedFiles;
    }
}
