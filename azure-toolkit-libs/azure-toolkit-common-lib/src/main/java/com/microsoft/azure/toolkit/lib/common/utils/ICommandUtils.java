/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.common.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.azure.toolkit.lib.common.utils.CommandUtils.getSafeWorkingDirectory;
import static com.microsoft.azure.toolkit.lib.common.utils.CommandUtils.isWSL;
import static com.microsoft.azure.toolkit.lib.common.utils.CommandUtils.isWindows;

public interface ICommandUtils {
    String WINDOWS_STARTER = "cmd.exe";
    String LINUX_MAC_STARTER = "/bin/sh";
    String WINDOWS_SWITCHER = "/c";
    String LINUX_MAC_SWITCHER = "-c";

    default List<String> resolveCommandPath(String command) {
        final List<String> list = new ArrayList<>();
        try {
            final String output = CommandUtils.exec((isWindows() ? "where " : "which ") + command);
            if (StringUtils.isBlank(output)) {
                return Collections.emptyList();
            }

            for (final String outputLine : output.split("[\\r\\n]")) {
                final File file = new File(StringUtils.trim(outputLine));
                if (!file.exists() || !file.isFile()) {
                    continue;
                }

                list.add(file.getAbsolutePath());
            }
        } catch (IOException ignored) {
            // ignore
        }
        return list;
    }

    default String exec(final String commandWithArgs) throws IOException {
        return exec(commandWithArgs, new HashMap<>());
    }

    default String exec(final String commandWithArgs, String cwd) throws IOException {
        return exec(commandWithArgs, new HashMap<>(), cwd);
    }

    default String exec(final String commandWithArgs, Map<String, String> env) throws IOException {
        return exec(commandWithArgs, env, null);
    }

    default String exec(final String commandWithArgs, Map<String, String> env, String cwd) throws IOException {
        return exec(commandWithArgs, env, cwd, false);
    }

    default String exec(final String commandWithArgs, String cwd, boolean mergeErrorStream) throws IOException {
        return exec(commandWithArgs, new HashMap<>(), cwd, mergeErrorStream);
    }

    default String exec(final String commandWithArgs, Map<String, String> env, String cwd, boolean mergeErrorStream) throws IOException {
        final String starter = isWindows() ? WINDOWS_STARTER : LINUX_MAC_STARTER;
        final String switcher = isWindows() ? WINDOWS_SWITCHER : LINUX_MAC_SWITCHER;
        final String workingDirectory = StringUtils.firstNonBlank(cwd, getSafeWorkingDirectory());
        if (StringUtils.isEmpty(workingDirectory)) {
            throw new IllegalStateException("A Safe Working directory could not be found to execute command from.");
        }
        final String commandWithPath = isWindows() || isWSL() ? commandWithArgs : String.format("export PATH=$PATH:/usr/local/bin ; %s", commandWithArgs);
        return executeCommandAndGetOutput(starter, switcher, commandWithPath, new File(workingDirectory), env, mergeErrorStream);
    }

    default String executeCommandAndGetOutput(final String commandWithArgs,
                                              final File directory, Map<String, String> env, boolean mergeErrorStream) throws IOException {
        final CommandLine commandLine = new CommandLine(commandWithArgs);
        return executeCommandAndGetOutput(commandLine, directory, env, mergeErrorStream);
    }

    default String executeCommandAndGetOutput(final String starter, final String switcher, final String commandWithArgs,
                                              final File directory, Map<String, String> env, boolean mergeErrorStream) throws IOException {
        final CommandLine commandLine = new CommandLine(starter);
        commandLine.addArgument(switcher, false);
        commandLine.addArgument(commandWithArgs, false);
        return executeCommandAndGetOutput(commandLine, directory, env, mergeErrorStream);
    }

    String executeCommandAndGetOutput(final CommandLine commandLine, final File directory, Map<String, String> env, boolean mergeErrorStream) throws IOException;
}
