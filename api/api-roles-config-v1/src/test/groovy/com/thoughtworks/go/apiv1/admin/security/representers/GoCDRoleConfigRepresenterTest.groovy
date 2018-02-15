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

package com.thoughtworks.go.apiv1.admin.security.representers

import com.thoughtworks.go.spark.mocks.TestRequestContext
import com.thoughtworks.go.api.util.GsonTransformer
import com.thoughtworks.go.config.CaseInsensitiveString
import com.thoughtworks.go.config.RoleConfig
import com.thoughtworks.go.config.RoleUser
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat

class GoCDRoleConfigRepresenterTest {
  private final LinkedHashMap<String, Object> map = [
    _links    : [
      doc : [href: 'https://api.gocd.org/#roles'],
      self: [href: 'http://test.host/go/api/admin/security/roles/admins'],
      find: [href: 'http://test.host/go/api/admin/security/roles/:role_name']
    ],
    name      : 'admins',
    type      : 'gocd',
    attributes: [
      users: ['bob', 'alice']
    ]
  ]
  private
  final RoleConfig roleConfig = new RoleConfig(new CaseInsensitiveString("admins"), new RoleUser("bob"), new RoleUser("alice"))

  @Test
  void shouldGenerateJSON() {
    Map map = RoleRepresenter.toJSON(roleConfig, new TestRequestContext())
    JsonFluentAssert.assertThatJson(map).isEqualTo(this.map)
  }

  @Test
  void shouldBuildObjectFromJson() {
    def jsonReader = GsonTransformer.instance.jsonReaderFrom(map)
    def roleConfig = RoleRepresenter.fromJSON(jsonReader)
    assertThat(roleConfig).isEqualTo(this.roleConfig)
  }
}
