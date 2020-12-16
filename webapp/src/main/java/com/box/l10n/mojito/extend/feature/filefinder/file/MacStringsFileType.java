package com.box.l10n.mojito.extend.feature.filefinder.file;


import com.box.l10n.mojito.extend.feature.filefinder.locale.AnyLocaleTargetNotSourceType;
import com.box.l10n.mojito.extend.feature.filefinder.FilePattern;

/**
 *
 * @author jaurambault
 */
public class MacStringsFileType extends FileType {

    public MacStringsFileType() {
        this.sourceFileExtension = "strings";
        this.baseNamePattern = ".*?Localizable|InfoPlist";
        this.subPath = "lproj";
        this.sourceFilePatternTemplate = "{" + FilePattern.PARENT_PATH + "}{" + FilePattern.LOCALE + "}" + FilePattern.DOT + "{" + FilePattern.SUB_PATH + "}" + FilePattern.PATH_SEPERATOR + "{" + FilePattern.BASE_NAME + "}" + FilePattern.DOT + "{" + FilePattern.FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = sourceFilePatternTemplate;
        this.localeType = new AnyLocaleTargetNotSourceType();
        this.gitBlameType = GitBlameType.TEXT_UNIT_USAGES;
    }

}
