package com.box.l10n.mojito.extend.feature.filefinder.file;


import com.box.l10n.mojito.extend.feature.filefinder.locale.AnyLocaleTargetNotSourceType;
import com.box.l10n.mojito.extend.feature.filefinder.FilePattern;

/**
 *
 * @author jyi
 */
public class XtbFileType extends FileType {

    public XtbFileType() {
        this.sourceFileExtension = "xtb";
        this.sourceFilePatternTemplate = "{" + FilePattern.PARENT_PATH + "}{" + FilePattern.BASE_NAME + "}" + FilePattern.HYPHEN + "{" + FilePattern.LOCALE + "}" + FilePattern.DOT + "{" + FilePattern.FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = sourceFilePatternTemplate;
        this.localeType = new AnyLocaleTargetNotSourceType();
    }
}
