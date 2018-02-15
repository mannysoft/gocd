/*
 * Copyright 2018 ThoughtWorks, Inc.
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

package com.thoughtworks.go.spark;


import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.thoughtworks.go.spark.spring.SparkSpringController;
import org.apache.http.HttpStatus;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static spark.Spark.*;

public class RoutesHelper {
    private static final String TIMER_START = RuntimeHeaderEmitter.class.getName();

    private List<SparkSpringController> controllers;
    private List<SparkController> sparkControllers;

    public RoutesHelper(SparkSpringController... controllers) {
        this(controllers, null);
    }

    public RoutesHelper(SparkController... sparkControllers) {
        this(null, sparkControllers);
    }

    private RoutesHelper(SparkSpringController[] controllers, SparkController[] apiControllers) {
        this.controllers = controllers == null ? Collections.emptyList() : Arrays.asList(controllers);
        this.sparkControllers = apiControllers == null ? Collections.emptyList() : Arrays.asList(apiControllers);
    }

    public void init() {
        before("/*", (request, response) -> request.attribute(TIMER_START, new RuntimeHeaderEmitter(request, response)));
        before("/*", (request, response) -> response.header("Cache-Control", "max-age=0, private, must-revalidate"));

        controllers.forEach(SparkSpringController::setupRoutes);
        sparkControllers.forEach(SparkController::setupRoutes);

        exception(JsonParseException.class, this::invalidJsonPayload);

        afterAfter("/*", (request, response) -> request.<RuntimeHeaderEmitter>attribute(TIMER_START).render());
    }

    private void invalidJsonPayload(JsonParseException ex, Request req, Response res) {
        res.body(new Gson().toJson(Collections.singletonMap("error", "Payload data is not valid JSON: " + ex.getMessage())));
        res.status(HttpStatus.SC_BAD_REQUEST);
    }

    private class RuntimeHeaderEmitter {
        private final Request request;
        private final Response response;
        private final long timerStart;

        public RuntimeHeaderEmitter(Request request, Response response) {
            this.timerStart = System.currentTimeMillis();
            this.request = request;
            this.response = response;
        }

        public void render() {
            if (!response.raw().isCommitted()) {
                response.header("X-Runtime", String.valueOf(System.currentTimeMillis() - timerStart));
            }
        }
    }
}
