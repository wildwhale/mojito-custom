package com.box.l10n.mojito.custom.feature.filefinder.file;


import com.box.l10n.mojito.custom.feature.filefinder.locale.AnyLocaleTargetNotSourceType;

import static com.box.l10n.mojito.custom.feature.filefinder.FilePattern.*;

/**
 *
 * @author jyi
 */
public class XtbFileType extends FileType {

    public XtbFileType() {
        this.sourceFileExtension = "xtb";
        this.sourceFilePatternTemplate = "{" + PARENT_PATH + "}{" + BASE_NAME + "}" + HYPHEN + "{" + LOCALE + "}" + DOT + "{" + FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = sourceFilePatternTemplate;
        this.localeType = new AnyLocaleTargetNotSourceType();
    }
}
