/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.legacy.function;

import com.microsoft.azure.toolkit.lib.legacy.function.template.BindingTemplate;
import com.microsoft.azure.toolkit.lib.legacy.function.template.FunctionSettingTemplate;
import com.microsoft.azure.toolkit.lib.legacy.function.template.FunctionTemplate;
import com.microsoft.azure.toolkit.lib.legacy.function.template.TemplateMetadata;
import com.microsoft.azure.toolkit.lib.legacy.function.template.TemplateResources;
import com.microsoft.azure.toolkit.lib.legacy.function.utils.FunctionUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Constants {
    public static final String APP_SETTING_WEBSITE_RUN_FROM_PACKAGE = "WEBSITE_RUN_FROM_PACKAGE";
    public static final String APP_SETTING_MACHINEKEY_DECRYPTION_KEY = "MACHINEKEY_DecryptionKey";
    public static final String APP_SETTING_WEBSITES_ENABLE_APP_SERVICE_STORAGE = "WEBSITES_ENABLE_APP_SERVICE_STORAGE";
    public static final String APP_SETTING_DISABLE_WEBSITES_APP_SERVICE_STORAGE = "false";
    public static final String APP_SETTING_FUNCTION_APP_EDIT_MODE = "FUNCTION_APP_EDIT_MODE";
    public static final String APP_SETTING_FUNCTION_APP_EDIT_MODE_READONLY = "readOnly";
    public static final String ZIP_EXT = ".zip";
    public static final String LOCAL_SETTINGS_FILE = "local.settings.json";
    public static final String INTERNAL_STORAGE_KEY = "AzureWebJobsStorage";

    public static void main(String[] args) {
        List<FunctionTemplate> functionTemplates = FunctionUtils.loadAllFunctionTemplates();
        final int labelWidth = getLabelWidth();
        List<String> collect = FunctionUtils.loadAllFunctionTemplates().stream()
                .map(Constants::getTemplateLabels)
                .flatMap(List::stream)
                .map(TemplateResources::getResource).collect(Collectors.toList());
        System.out.println(labelWidth);
    }

    private static int getLabelWidth() {
        List<String> collect = FunctionUtils.loadAllFunctionTemplates().stream()
                .map(Constants::getTemplateLabels)
                .flatMap(List::stream)
                .map(TemplateResources::getResource)
                .sorted(Comparator.comparing(label -> label.length()))
                .collect(Collectors.toList());
        return collect.get(0).length();
    }

    private static List<String> getTemplateLabels(final FunctionTemplate functionTemplate) {
        final BindingTemplate binding = functionTemplate.getBinding();
        final List<String> prompts = Optional.ofNullable(functionTemplate.getMetadata())
                .map(TemplateMetadata::getUserPrompt)
                .orElse(Collections.emptyList());
        return Objects.isNull(binding) ? prompts : prompts.stream()
                .map(prompt -> functionTemplate.getBinding().getSettingTemplateByName(prompt))
                .map(FunctionSettingTemplate::getLabel).collect(Collectors.toList());
    }
}
