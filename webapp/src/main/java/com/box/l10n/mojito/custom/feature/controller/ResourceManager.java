package com.box.l10n.mojito.custom.feature.controller;

import com.box.l10n.mojito.custom.feature.service.ResourceService;
import com.box.l10n.mojito.rest.entity.LocalizedAssetBody;
import com.box.l10n.mojito.rest.textunit.TextUnitWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ResourceManager {
    static Logger logger = LoggerFactory.getLogger(ResourceManager.class);
    /*
     *  gitUri : access token 포함된..?
     */
    @Autowired
    ResourceService resourceService;

    public void loadResource(String gitUri, String repository, String[] locales) {

    }

    @RequestMapping(value = "api/resource/upload", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public String upload(
            @RequestParam(value = "gitUrl") String gitUrl
            , @RequestParam(value = "userId") String userId
            , @RequestParam(value = "userPW") String userPW
            , @RequestParam(value = "repositoryName") String repositoryName
            , @RequestParam(value = "resourceName") String resourceName
            , @RequestParam(value = "fileType") String fileType
            , @RequestParam(value = "locales") String[] locales) throws Exception
    {
        resourceService.upload(gitUrl, userId, userPW, repositoryName, resourceName, fileType, locales);
        System.out.println("OK");
        return "OK";
    }

    /*
     * status : ALL, ACCEPTED_OR_NEEDS_REVIEW, ACCEPTED
     */
    @RequestMapping(value = "/api/resource/deploy", method = RequestMethod.POST)
    public void deploy(
            @RequestParam(value = "repositoryName") String repositoryName
            , @RequestParam(value = "targetBranchName") String targetBranchName
            , @RequestParam(value = "status") LocalizedAssetBody.Status status
    ) throws Exception {
        resourceService.deploy(repositoryName, targetBranchName, status);
    }
}
