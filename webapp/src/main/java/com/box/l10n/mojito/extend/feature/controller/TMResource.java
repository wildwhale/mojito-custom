package com.box.l10n.mojito.extend.feature.controller;

import com.box.l10n.mojito.entity.Repository;
import com.box.l10n.mojito.extend.feature.dto.DeployParam;
import com.box.l10n.mojito.extend.feature.dto.DeployRes;
import com.box.l10n.mojito.extend.feature.dto.UploadRes;
import com.box.l10n.mojito.extend.feature.dto.UploadParam;
import com.box.l10n.mojito.extend.feature.service.ResourceService;
import com.box.l10n.mojito.rest.client.exception.RepositoryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TMResource {
    static Logger logger = LoggerFactory.getLogger(TMResource.class);
    /*
     *  gitUri : access token 포함된..?
     */
    @Autowired
    ResourceService resourceService;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/extend/api/resource/upload", method = RequestMethod.POST)
    public ResponseEntity<UploadRes> upload(@RequestBody UploadParam uploadParam) throws Exception {
        Repository repository = resourceService.upload(uploadParam);
        UploadRes response = new UploadRes(repository);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /*
     * status : ALL, ACCEPTED_OR_NEEDS_REVIEW, ACCEPTED
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/extend/api/resource/deploy", method = RequestMethod.POST)
    public ResponseEntity<DeployRes> deploy(@RequestBody DeployParam deployParam) throws Exception {
        List<String> localizedFiles = resourceService.deploy(deployParam);
        DeployRes response = new DeployRes(localizedFiles);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/extend/api/resource/delete", method = RequestMethod.POST)
    public void delete(@RequestParam(value = "repositoryName") String repositoryName) throws RepositoryNotFoundException {
        resourceService.deleteRepository(repositoryName);
    }
}
