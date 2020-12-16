package com.box.l10n.mojito.extend.feature.filefinder;

/**
 * @author jaurambault
 */
public class FileFinderException extends Exception {

    public FileFinderException(String message) {
        super(message);
    }

    public FileFinderException(String message, Throwable ex) {
        super(message, ex);
    }

}
