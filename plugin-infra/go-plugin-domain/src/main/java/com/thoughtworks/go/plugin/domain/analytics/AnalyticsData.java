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

package com.thoughtworks.go.plugin.domain.analytics;

import org.apache.commons.lang.StringUtils;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsData {
    private String data;
    private String viewPath;
    private String assetRoot;

    public AnalyticsData(String data, String viewPath) {
        this.data = data;
        this.viewPath = viewPath;
    }

    public String getData() {
        return data;
    }

    public String getViewPath() {
        return viewPath;
    }

    public String getFullViewPath() {
        if (StringUtils.isBlank(assetRoot)) {
            return viewPath;
        }
        return URI.create(assetRoot + "/" + viewPath).normalize().toString();
    }

    public Map<String, String> toMap() {
        HashMap<String, String> m = new HashMap<>();
        m.put("data", data);
        m.put("view_path", getFullViewPath());

        return m;
    }

    public void setAssetRoot(String assetRoot) {
        this.assetRoot = assetRoot;
    }
}
