package com.box.l10n.mojito.custom.feature.filefinder.file;

import com.box.l10n.mojito.custom.feature.filefinder.locale.LocaleType;
import com.box.l10n.mojito.custom.feature.filefinder.locale.PropertiesJavaLocaleType;
import com.box.l10n.mojito.okapi.FilterConfigIdOverride;

/**
 * @author jaurambault
 */
public class PropertiesJavaFileType extends LocaleInNameFileType {

    public PropertiesJavaFileType() {
        this.sourceFileExtension = "properties";
        this.filterConfigIdOverride = FilterConfigIdOverride.PROPERTIES_JAVA;
    }

    @Override
    public LocaleType getLocaleType() {
        return new PropertiesJavaLocaleType();
    }
}
