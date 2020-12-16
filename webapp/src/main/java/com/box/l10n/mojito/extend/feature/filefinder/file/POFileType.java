package com.box.l10n.mojito.extend.feature.filefinder.file;


import com.box.l10n.mojito.extend.feature.filefinder.FilePattern;
import com.box.l10n.mojito.extend.feature.filefinder.locale.POLocaleType;

/**
 *
 * @author jaurambault
 */
public class POFileType extends FileType {

    public POFileType() {
        this.sourceFileExtension = "pot";
        this.targetFileExtension = "po";
        this.subPath = "(?:LC_MESSAGES/)?";
        this.parentPath = "(?:(?:(?!LC_MESSAGES).)+/)?";
        this.sourceFilePatternTemplate = "{" + FilePattern.PARENT_PATH + "}" + "{" + FilePattern.SUB_PATH + "}" + "{" + FilePattern.BASE_NAME + "}" + FilePattern.DOT + "{" + FilePattern.FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = "{" + FilePattern.PARENT_PATH + "}" + "{" + FilePattern.LOCALE + "}" + FilePattern.PATH_SEPERATOR + "{" + FilePattern.SUB_PATH + "}" + "{" + FilePattern.BASE_NAME + "}" + FilePattern.DOT + "{" + FilePattern.FILE_EXTENSION + "}";
        this.localeType = new POLocaleType();
        this.gitBlameType = GitBlameType.TEXT_UNIT_USAGES;
    }

}
