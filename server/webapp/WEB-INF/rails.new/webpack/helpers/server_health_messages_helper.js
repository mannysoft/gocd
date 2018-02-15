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

const $                               = require('jquery');
const m                               = require('mithril');
const ServerHealthMessages            = require('models/server_health_messages/server_health_messages');
const ServerHealthMessagesCountWidget = require('views/server_health_messages/server_health_messages_count_widget');
const AjaxPoller                      = require('helpers/ajax_poller');
const Stream                          = require('mithril/stream');

$(() => {
  const serverHealthMessages = Stream(new ServerHealthMessages([]));

  function createRepeater() {
    return new AjaxPoller((xhrCB) => {
      return ServerHealthMessages.all(xhrCB)
        .then((messages) => {
          serverHealthMessages(messages);
        });
    });
  }

  createRepeater().start();

  m.mount($('.server-health-summary').get(0), {
    view() {
      return m(ServerHealthMessagesCountWidget, {serverHealthMessages});
    }
  });
});
