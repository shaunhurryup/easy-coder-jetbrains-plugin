package com.easycoder.intellij.utils;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface EasyCoderIcons {
    Icon Action = IconLoader.getIcon("/icons/actionIcon.svg", EasyCoderIcons.class);
    Icon ActionDark = IconLoader.getIcon("/icons/actionIcon_dark.svg", EasyCoderIcons.class);
    Icon WidgetEnabled = IconLoader.getIcon("/icons/widgetEnabled.svg", EasyCoderIcons.class);
    Icon WidgetEnabledDark = IconLoader.getIcon("/icons/widgetEnabled_dark.svg", EasyCoderIcons.class);
    Icon WidgetDisabled = IconLoader.getIcon("/icons/widgetDisabled.svg", EasyCoderIcons.class);
    Icon WidgetDisabledDark = IconLoader.getIcon("/icons/widgetDisabled_dark.svg", EasyCoderIcons.class);
    Icon WidgetError = IconLoader.getIcon("/icons/widgetError.svg", EasyCoderIcons.class);
    Icon WidgetErrorDark = IconLoader.getIcon("/icons/widgetError_dark.svg", EasyCoderIcons.class);
}
