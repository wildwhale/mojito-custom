package com.box.l10n.mojito.extend.feature.filefinder.file;


import com.box.l10n.mojito.extend.feature.filefinder.locale.ChromeExtJsonLocaleType;
import com.box.l10n.mojito.extend.feature.filefinder.FilePattern;

import java.util.Arrays;
import java.util.regex.Pattern;


/**
 * @author jeanaurambault
 */
public class ChromeExtensionJSONFileType extends FileType {

    public ChromeExtensionJSONFileType() {
        this.sourceFileExtension = "json";
        this.baseNamePattern = "messages";
        this.subPath = "_locales";
        this.sourceFilePatternTemplate = "{" + FilePattern.PARENT_PATH + "}{" + FilePattern.SUB_PATH + "}" + FilePattern.PATH_SEPERATOR + "{" + FilePattern.LOCALE + "}" + FilePattern.PATH_SEPERATOR + "{" + FilePattern.BASE_NAME + "}" + FilePattern.DOT + "{" + FilePattern.FILE_EXTENSION + "}";
        this.targetFilePatternTemplate = sourceFilePatternTemplate;
        this.localeType = new ChromeExtJsonLocaleType();
        this.textUnitNameToTextUnitNameInSourceSingular = Pattern.compile("(?<s>.*)/message");
        this.textUnitNameToTextUnitNameInSourcePlural = Pattern.compile("(?<s>.*)"); // plural not support just accept anything
        this.defaultFilterOptions = Arrays.asList("noteKeyPattern=description", "extractAllPairs=false", "exceptions=message");
    }
}
