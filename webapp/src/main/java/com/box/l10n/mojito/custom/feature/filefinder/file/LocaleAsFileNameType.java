package com.box.l10n.mojito.custom.feature.filefinder.file;


import com.box.l10n.mojito.custom.feature.filefinder.locale.AnyLocaleTargetNotSourceType;

import static com.box.l10n.mojito.custom.feature.filefinder.FilePattern.*;

/**
 *
 * @author jaurambault
 */
public abstract class LocaleAsFileNameType extends FileType {

    public LocaleAsFileNameType() {
        this.baseNamePattern = "";
        this.sourceFilePatternTemplate = "{" + PARENT_PATH + "}{" + LOCALE + "}" + DOT + "{" + FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = sourceFilePatternTemplate;
        this.localeType = new AnyLocaleTargetNotSourceType();
    }

}
