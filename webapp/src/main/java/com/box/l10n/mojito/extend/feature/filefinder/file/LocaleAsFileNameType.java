package com.box.l10n.mojito.extend.feature.filefinder.file;


import com.box.l10n.mojito.extend.feature.filefinder.locale.AnyLocaleTargetNotSourceType;
import com.box.l10n.mojito.extend.feature.filefinder.FilePattern;

/**
 *
 * @author jaurambault
 */
public abstract class LocaleAsFileNameType extends FileType {

    public LocaleAsFileNameType() {
        this.baseNamePattern = "";
        this.sourceFilePatternTemplate = "{" + FilePattern.PARENT_PATH + "}{" + FilePattern.LOCALE + "}" + FilePattern.DOT + "{" + FilePattern.FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = sourceFilePatternTemplate;
        this.localeType = new AnyLocaleTargetNotSourceType();
    }

}
