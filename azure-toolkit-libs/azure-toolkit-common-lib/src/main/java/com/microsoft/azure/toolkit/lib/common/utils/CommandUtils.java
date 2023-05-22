/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.common.utils;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CommandUtils {
    private static final String DEFAULT_WINDOWS_SYSTEM_ROOT = System.getenv("SystemRoot");
    private static final String DEFAULT_MAC_LINUX_PATH = "/bin/";

    @Getter
    private static ICommandUtils instance = new DefaultCommandUtils();

    public static void register(@Nonnull final ICommandUtils utils) {
        instance = utils;
    }

    public static class DefaultCommandUtils implements ICommandUtils {

        @Override
        public String executeCommandAndGetOutput(final CommandLine commandLine, final File directory, Map<String, String> env, boolean mergeErrorStream)
            throws IOException {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ByteArrayOutputStream err = mergeErrorStream ? out : new ByteArrayOutputStream();
            final PumpStreamHandler streamHandler = new PumpStreamHandler(out, err);
            final DefaultExecutor executor = new DefaultExecutor();
            executor.setWorkingDirectory(directory);
            executor.setStreamHandler(streamHandler);
            executor.setExitValues(new int[]{0});
            try {
                Map<String, String> newEnv = new HashMap<>(System.getenv());
                if (env != null) {
                    newEnv.putAll(env);
                }
                executor.execute(commandLine, newEnv);
                final String result = StringUtils.trimToEmpty(out.toString());
                if (!mergeErrorStream && err.size() > 0 && StringUtils.isEmpty(result)) {
                    throw new IOException(StringUtils.trim(err.toString()));
                }
                return result;
            } finally {
                out.close();
                err.close();
            }
        }
    }

    public static List<String> resolveCommandPath(String command) {
        return getInstance().resolveCommandPath(command);
    }

    public static String exec(final String commandWithArgs) throws IOException {
        return getInstance().exec(commandWithArgs);
    }

    public static String exec(final String commandWithArgs, String cwd) throws IOException {
        return getInstance().exec(commandWithArgs, cwd);
    }

    public static String exec(final String commandWithArgs, Map<String, String> env) throws IOException {
        return getInstance().exec(commandWithArgs, env);
    }

    public static String exec(final String commandWithArgs, Map<String, String> env, String cwd) throws IOException {
        return getInstance().exec(commandWithArgs, env, cwd);
    }

    public static String exec(final String commandWithArgs, String cwd, boolean mergeErrorStream) throws IOException {
        return getInstance().exec(commandWithArgs, cwd, mergeErrorStream);
    }

    public static String exec(final String commandWithArgs, Map<String, String> env, String cwd, boolean mergeErrorStream) throws IOException {
        return getInstance().exec(commandWithArgs, env, cwd, mergeErrorStream);
    }

    public static boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    public static boolean isWSL() {
        return SystemUtils.IS_OS_LINUX && System.getenv("WSL_DISTRO_NAME") != null;
    }

    public static String getSafeWorkingDirectory() {
        if (isWindows()) {
            if (StringUtils.isEmpty(DEFAULT_WINDOWS_SYSTEM_ROOT)) {
                return null;
            }
            return DEFAULT_WINDOWS_SYSTEM_ROOT + "\\system32";
        } else {
            return DEFAULT_MAC_LINUX_PATH;
        }
    }
}
