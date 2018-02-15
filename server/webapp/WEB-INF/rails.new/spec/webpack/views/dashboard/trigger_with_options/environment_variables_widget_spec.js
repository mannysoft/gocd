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

describe("Dashboard Environment Variables Trigger Widget", () => {
  const m             = require("mithril");
  const $             = require("jquery");
  const simulateEvent = require('simulate-event');

  const EnvironmentVariablesWidget = require("views/dashboard/trigger_with_options/environment_variables_widget");
  const EnvironmentVariables       = require('models/dashboard/environment_variables');

  let $root, root;
  beforeEach(() => {
    [$root, root] = window.createDomElementForTest();
  });

  afterEach(() => {
    window.destroyDomElementForTest();
  });


  describe("Plain Text Variables", () => {
    const json = [
      {
        "name":   "version",
        "secure": false,
        "value":  "1.0.0"
      },
      {
        "name":   "foobar",
        "secure": false,
        "value":  "asdf"
      }
    ];

    const variables = EnvironmentVariables.fromJSON(json);

    beforeEach(() => {
      mount('Plain', variables);
    });

    afterEach(unmount);

    it("should render plain text variables", () => {
      expect($root.find('.environment-variables .name')).toHaveLength(2);
      expect($root.find('.environment-variables input')).toHaveLength(2);

      expect($root.find('.environment-variables .name').get(0)).toContainText(json[0].name);
      expect($root.find('.environment-variables .name').get(1)).toContainText(json[1].name);

      expect($root.find('.environment-variables input').get(0)).toHaveValue(json[0].value);
      expect($root.find('.environment-variables input').get(1)).toHaveValue(json[1].value);
    });

    it("it should display variable overriden message", () => {
      const valueInputField = $root.find('.environment-variables input').get(0);

      expect(valueInputField).toHaveValue(json[0].value);
      expect($root.find('.overridden-message')).not.toBeInDOM();

      const newValue = "ldap";
      $(valueInputField).val(newValue);
      simulateEvent.simulate(valueInputField, 'input');
      m.redraw();

      expect(variables[0].value()).toBe(newValue);
      expect(valueInputField).toHaveValue(newValue);
      expect($root.find('.overridden-message')).toContainText(`Overriden. Default: ${json[0].value}`);
    });
  });

  describe("Secure Text Variables", () => {
    const json = [
      {
        "name":   "password",
        "secure": true,
      },
      {
        "name":   "ip_address",
        "secure": true,
      }
    ];

    const variables = EnvironmentVariables.fromJSON(json);

    beforeEach(() => {
      mount('Secure', variables);
    });

    afterEach(unmount);

    it("should render Secure text variables", () => {
      expect($root.find('.environment-variables .name')).toHaveLength(2);
      expect($root.find('.environment-variables input')).toHaveLength(2);

      expect($root.find('.environment-variables .name').get(0)).toContainText(json[0].name);
      expect($root.find('.environment-variables .name').get(1)).toContainText(json[1].name);

      expect($root.find('.environment-variables input').get(0)).toHaveValue('*****');
      expect($root.find('.environment-variables input').get(1)).toHaveValue('*****');
    });

    it("it should display variable overriden message", () => {
      const valueInputField = $root.find('.environment-variables input').get(0);
      expect(valueInputField).toBeDisabled();

      expect(valueInputField).toHaveValue('*****');
      expect($root.find('.reset')).not.toBeInDOM();

      simulateEvent.simulate($root.find('.override').get(0), 'click');
      m.redraw();
      expect(valueInputField).not.toBeDisabled();
      expect($root.find('.reset')).toBeInDOM();

      const newValue = "ldap";
      $(valueInputField).val(newValue);
      simulateEvent.simulate(valueInputField, 'input');
      m.redraw();

      expect(variables[0].value()).toBe(newValue);
      expect(valueInputField).toHaveValue(newValue);

      simulateEvent.simulate($root.find('.reset').get(0), 'click');
      m.redraw();
      expect(valueInputField).toBeDisabled();
      expect($root.find('.reset')).not.toBeInDOM();
    });
  });

  function mount(widgetName, variables) {
    m.mount(root, {
      view() {
        return m(EnvironmentVariablesWidget[widgetName], {
          variables
        });
      }
    });
    m.redraw(true);
  }

  function unmount() {
    m.mount(root, null);
    m.redraw();
  }
});
