package com.box.l10n.mojito.extend.feature.filefinder.file;


import com.box.l10n.mojito.extend.feature.filefinder.locale.AnyLocaleTargetNotSourceType;
import com.box.l10n.mojito.extend.feature.filefinder.FilePattern;

/**
 *
 * @author jyi
 */
public class ReswFileType extends FileType {

    public ReswFileType() {
        this.sourceFileExtension = "resw";
        this.sourceFilePatternTemplate = "{" + FilePattern.PARENT_PATH + "}{" + FilePattern.LOCALE + "}" + FilePattern.PATH_SEPERATOR + "{" + FilePattern.BASE_NAME + "}" + FilePattern.DOT + "{" + FilePattern.FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = sourceFilePatternTemplate;
        this.localeType = new AnyLocaleTargetNotSourceType();
    }
    
}
