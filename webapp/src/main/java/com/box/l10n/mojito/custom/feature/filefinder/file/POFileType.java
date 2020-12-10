package com.box.l10n.mojito.custom.feature.filefinder.file;


import com.box.l10n.mojito.custom.feature.filefinder.FilePattern;
import com.box.l10n.mojito.custom.feature.filefinder.locale.POLocaleType;

import static com.box.l10n.mojito.custom.feature.filefinder.FilePattern.*;

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
        this.sourceFilePatternTemplate = "{" + PARENT_PATH + "}" + "{" + FilePattern.SUB_PATH + "}" + "{" + BASE_NAME + "}" + DOT + "{" + FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = "{" + PARENT_PATH + "}" + "{" + LOCALE + "}" + PATH_SEPERATOR + "{" + SUB_PATH + "}" + "{" + BASE_NAME + "}" + DOT + "{" + FILE_EXTENSION + "}";
        this.localeType = new POLocaleType();
        this.gitBlameType = GitBlameType.TEXT_UNIT_USAGES;
    }

}
