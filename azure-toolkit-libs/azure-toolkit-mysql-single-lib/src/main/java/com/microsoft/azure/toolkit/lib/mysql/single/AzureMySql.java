/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.lib.mysql.single;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.resourcemanager.mysql.MySqlManager;
import com.azure.resourcemanager.mysql.models.ServerVersion;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.AzureConfiguration;
import com.microsoft.azure.toolkit.lib.auth.Account;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzServiceSubscription;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class AzureMySql extends AbstractAzService<MySqlServiceSubscription, MySqlManager> {

    public AzureMySql() {
        super("Microsoft.DBforMySQL");
    }

    @Nonnull
    public MySqlServerModule servers(@Nonnull String subscriptionId) {
        final MySqlServiceSubscription rm = get(subscriptionId, null);
        assert rm != null;
        return rm.getServerModule();
    }

    @Nonnull
    public List<MySqlServer> servers() {
        return this.list().stream().flatMap(m -> m.servers().list().stream()).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    protected MySqlManager loadResourceFromAzure(@Nonnull String subscriptionId, String resourceGroup) {
        final Account account = Azure.az(AzureAccount.class).account();
        final AzureConfiguration config = Azure.az().config();
        final String userAgent = config.getUserAgent();
        final HttpLogDetailLevel logLevel = Optional.ofNullable(config.getLogLevel()).map(HttpLogDetailLevel::valueOf).orElse(HttpLogDetailLevel.NONE);
        final AzureProfile azureProfile = new AzureProfile(null, subscriptionId, account.getEnvironment());
        return MySqlManager.configure()
            .withHttpClient(AbstractAzServiceSubscription.getDefaultHttpClient())
            .withLogOptions(new HttpLogOptions().setLogLevel(logLevel))
            .withPolicy(AbstractAzServiceSubscription.getUserAgentPolicy(userAgent))
            .authenticate(account.getTokenCredential(subscriptionId), azureProfile);
    }

    @Nonnull
    @Override
    protected MySqlServiceSubscription newResource(@Nonnull MySqlManager manager) {
        return new MySqlServiceSubscription(manager, this);
    }

    @Nonnull
    public List<String> listSupportedVersions() {
        return ServerVersion.values().stream().map(ExpandableStringEnum::toString).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public String getResourceTypeName() {
        return "Azure Database for MySQL servers";
    }
}
