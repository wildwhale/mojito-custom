package com.box.l10n.mojito.extend.feature.filefinder.file;


import com.box.l10n.mojito.extend.feature.filefinder.locale.AndroidLocaleType;
import com.box.l10n.mojito.extend.feature.filefinder.FilePattern;

import java.util.regex.Pattern;


/**
 *
 * @author jaurambault
 */
public class AndroidStringsFileType extends FileType {

    public AndroidStringsFileType() {
        this.sourceFileExtension = "xml";
        this.baseNamePattern = "strings";
        this.subPath = "res/values";
        this.sourceFilePatternTemplate = "{" + FilePattern.PARENT_PATH + "}{" + FilePattern.SUB_PATH + "}" + FilePattern.PATH_SEPERATOR + "{" + FilePattern.BASE_NAME + "}" + FilePattern.DOT + "{" + FilePattern.FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = "{" + FilePattern.PARENT_PATH + "}{" + FilePattern.SUB_PATH + "}" + FilePattern.HYPHEN + "{" + FilePattern.LOCALE + "}" + FilePattern.PATH_SEPERATOR + "{" + FilePattern.BASE_NAME + "}" + FilePattern.DOT + "{" + FilePattern.FILE_EXTENSION + "}";
        this.localeType = new AndroidLocaleType();
        this.textUnitNameToTextUnitNameInSourceSingular = Pattern.compile("(?<s>.*?)(_\\d+)?$");
        this.textUnitNameToTextUnitNameInSourcePlural = Pattern.compile("(?<s>.*?)_(zero|one|two|few|many|other)$");
    }
}
