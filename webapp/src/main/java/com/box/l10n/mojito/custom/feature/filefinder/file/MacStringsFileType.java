package com.box.l10n.mojito.custom.feature.filefinder.file;


import com.box.l10n.mojito.custom.feature.filefinder.locale.AnyLocaleTargetNotSourceType;

import static com.box.l10n.mojito.custom.feature.filefinder.FilePattern.*;

/**
 *
 * @author jaurambault
 */
public class MacStringsFileType extends FileType {

    public MacStringsFileType() {
        this.sourceFileExtension = "strings";
        this.baseNamePattern = ".*?Localizable|InfoPlist";
        this.subPath = "lproj";
        this.sourceFilePatternTemplate = "{" + PARENT_PATH + "}{" + LOCALE + "}" + DOT + "{" + SUB_PATH + "}" + PATH_SEPERATOR + "{" + BASE_NAME + "}" + DOT + "{" + FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = sourceFilePatternTemplate;
        this.localeType = new AnyLocaleTargetNotSourceType();
        this.gitBlameType = GitBlameType.TEXT_UNIT_USAGES;
    }

}
