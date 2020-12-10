package com.box.l10n.mojito.custom.feature.filefinder.file;


import com.box.l10n.mojito.custom.feature.filefinder.locale.AnyLocaleType;

import static com.box.l10n.mojito.custom.feature.filefinder.FilePattern.*;

/**
 *
 * @author jaurambault
 */
public abstract class LocaleInNameFileType extends FileType {

    public LocaleInNameFileType() {
        this.sourceFilePatternTemplate = "{" + PARENT_PATH + "}{" + BASE_NAME + "}" + DOT + "{" + FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = "{" + PARENT_PATH + "}{" + BASE_NAME + "}" + UNDERSCORE + "{" + LOCALE + "}" + DOT + "{" + FILE_EXTENSION + "}";
        this.localeType = new AnyLocaleType();
    }

}
