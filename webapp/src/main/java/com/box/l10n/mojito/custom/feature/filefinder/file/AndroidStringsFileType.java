package com.box.l10n.mojito.custom.feature.filefinder.file;


import com.box.l10n.mojito.custom.feature.filefinder.locale.AndroidLocaleType;

import java.util.regex.Pattern;

import static com.box.l10n.mojito.custom.feature.filefinder.FilePattern.*;


/**
 *
 * @author jaurambault
 */
public class AndroidStringsFileType extends FileType {

    public AndroidStringsFileType() {
        this.sourceFileExtension = "xml";
        this.baseNamePattern = "strings";
        this.subPath = "res/values";
        this.sourceFilePatternTemplate = "{" + PARENT_PATH + "}{" + SUB_PATH + "}" + PATH_SEPERATOR + "{" + BASE_NAME + "}" + DOT + "{" + FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = "{" + PARENT_PATH + "}{" + SUB_PATH + "}" + HYPHEN + "{" + LOCALE + "}" + PATH_SEPERATOR + "{" + BASE_NAME + "}" + DOT + "{" + FILE_EXTENSION + "}";
        this.localeType = new AndroidLocaleType();
        this.textUnitNameToTextUnitNameInSourceSingular = Pattern.compile("(?<s>.*?)(_\\d+)?$");
        this.textUnitNameToTextUnitNameInSourcePlural = Pattern.compile("(?<s>.*?)_(zero|one|two|few|many|other)$");
    }
}
