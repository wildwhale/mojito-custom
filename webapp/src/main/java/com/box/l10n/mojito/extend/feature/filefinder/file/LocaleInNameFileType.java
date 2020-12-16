package com.box.l10n.mojito.extend.feature.filefinder.file;


import com.box.l10n.mojito.extend.feature.filefinder.locale.AnyLocaleType;
import com.box.l10n.mojito.extend.feature.filefinder.FilePattern;

/**
 *
 * @author jaurambault
 */
public abstract class LocaleInNameFileType extends FileType {

    public LocaleInNameFileType() {
        this.sourceFilePatternTemplate = "{" + FilePattern.PARENT_PATH + "}{" + FilePattern.BASE_NAME + "}" + FilePattern.DOT + "{" + FilePattern.FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = "{" + FilePattern.PARENT_PATH + "}{" + FilePattern.BASE_NAME + "}" + FilePattern.UNDERSCORE + "{" + FilePattern.LOCALE + "}" + FilePattern.DOT + "{" + FilePattern.FILE_EXTENSION + "}";
        this.localeType = new AnyLocaleType();
    }

}
