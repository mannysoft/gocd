/*
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.plugin.access.notification;

import com.thoughtworks.go.plugin.access.common.settings.PluginSettingsJsonMessageHandler;
import com.thoughtworks.go.plugin.access.common.settings.PluginSettingsJsonMessageHandler2_0;
import com.thoughtworks.go.plugin.access.notification.v3.JsonMessageHandler3_0;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import org.hamcrest.core.Is;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.go.plugin.access.common.settings.PluginSettingsConstants.REQUEST_NOTIFY_PLUGIN_SETTINGS_CHANGE;
import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static com.thoughtworks.go.plugin.domain.common.PluginConstants.NOTIFICATION_EXTENSION;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class NotificationExtensionTestForV3 extends NotificationExtensionTestBase {
    @Mock
    private PluginSettingsJsonMessageHandler2_0 pluginSettingsJSONMessageHandlerv2;

    @Mock
    private JsonMessageHandler3_0 jsonMessageHandlerv3;

    @Override
    protected String apiVersion() {
        return "3.0";
    }

    @Override
    protected PluginSettingsJsonMessageHandler pluginSettingsJSONMessageHandler() {
        return pluginSettingsJSONMessageHandlerv2;
    }

    @Override
    protected JsonMessageHandler jsonMessageHandler() {
        return jsonMessageHandlerv3;
    }

    @Test
    public void shouldNotifyPluginSettingsChange() throws Exception {
        String supportedVersion = "3.0";
        Map<String, String> settings = Collections.singletonMap("foo", "bar");
        ArgumentCaptor<GoPluginApiRequest> requestArgumentCaptor = ArgumentCaptor.forClass(GoPluginApiRequest.class);

        when(pluginManager.resolveExtensionVersion("pluginId", Arrays.asList("1.0", "2.0","3.0"))).thenReturn(supportedVersion);
        when(pluginManager.isPluginOfType(NOTIFICATION_EXTENSION, "pluginId")).thenReturn(true);
        when(pluginManager.submitTo(eq("pluginId"), requestArgumentCaptor.capture())).thenReturn(new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, ""));

        NotificationExtension extension = new NotificationExtension(pluginManager);
        extension.notifyPluginSettingsChange("pluginId", settings);

        assertRequest(requestArgumentCaptor.getValue(), NOTIFICATION_EXTENSION,
                supportedVersion, REQUEST_NOTIFY_PLUGIN_SETTINGS_CHANGE, "{\"foo\":\"bar\"}");
    }

    @Test
    public void shouldSerializePluginSettingsToJSON() throws Exception {
        String pluginId = "plugin_id";
        HashMap<String, String> pluginSettings = new HashMap<>();
        pluginSettings.put("key1", "val1");
        pluginSettings.put("key2", "val2");

        NotificationExtension notificationExtension = new NotificationExtension(pluginManager);

        when(pluginManager.resolveExtensionVersion(pluginId, notificationExtension.goSupportedVersions())).thenReturn(apiVersion());
        String pluginSettingsJSON = notificationExtension.pluginSettingsJSON(pluginId, pluginSettings);

        assertThat(pluginSettingsJSON, is("{\"key1\":\"val1\",\"key2\":\"val2\"}"));
    }

    @Test
    public void shouldSerializeServerInfoToJSON() throws Exception {
        String pluginId = "plugin_id";

        NotificationExtension notificationExtension = new NotificationExtension(pluginManager);

        when(pluginManager.resolveExtensionVersion(pluginId, notificationExtension.goSupportedVersions())).thenReturn("1.0");

        String serverInfoJSON = notificationExtension.serverInfoJSON(pluginId, "x12ad", "http://build.com", "https://build.com");

        assertThat(serverInfoJSON, Is.is("{\"server_id\":\"x12ad\",\"site_url\":\"http://build.com\",\"secure_site_url\":\"https://build.com\"}"));
    }

    private void assertRequest(GoPluginApiRequest goPluginApiRequest, String extensionName, String version, String requestName, String requestBody) throws JSONException {
        Assert.assertThat(goPluginApiRequest.extension(), Is.is(extensionName));
        Assert.assertThat(goPluginApiRequest.extensionVersion(), Is.is(version));
        Assert.assertThat(goPluginApiRequest.requestName(), Is.is(requestName));
        JSONAssert.assertEquals(requestBody, goPluginApiRequest.requestBody(), true);
    }
}