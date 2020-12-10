package com.box.l10n.mojito.custom.feature.filefinder.file;


import static com.box.l10n.mojito.custom.feature.filefinder.FilePattern.*;

/**
 *
 * @author jaurambault
 */
public class ResxFileType extends LocaleInNameFileType {

    public ResxFileType() {
        this.sourceFileExtension = "resx";
        this.targetFilePatternTemplate = "{" + PARENT_PATH + "}{" + BASE_NAME + "}" + DOT + "{" + LOCALE + "}" + DOT + "{" + FILE_EXTENSION + "}";
    }

}