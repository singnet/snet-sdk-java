package io.singularitynet.sdk.plugin;

public class PluginException extends Exception {

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable exception) {
        super(message, exception);
    }

}
