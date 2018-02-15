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

describe("Dashboard Material Search State Model", () => {
  const SparkRoutes = require('helpers/spark_routes');

  const TriggerWithOptionsInfo = require('models/dashboard/trigger_with_options_info');

  const MaterialSearchVM = require("views/dashboard/models/material_search_vm");

  describe('initialize', () => {
    let vm, materials;
    const pipelineName = 'up42';
    beforeEach(() => {
      materials = TriggerWithOptionsInfo.fromJSON(json).materials;
      vm        = new MaterialSearchVM(pipelineName, materials);
    });

    it('should initialize search in progress state for each material', () => {
      expect(vm[materials[0].name].searchInProgress()).toBe(false);
      expect(vm[materials[1].name].searchInProgress()).toBe(false);
    });

    it('should initialize search text state for each material', () => {
      expect(vm[materials[0].name].searchText()).toBe('');
      expect(vm[materials[1].name].searchText()).toBe('');
    });

    it('should initialize material search results for each material', () => {
      expect(vm[materials[0].name].materialSearchResults()).toEqual([]);
      expect(vm[materials[1].name].materialSearchResults()).toEqual([]);
    });

    it("should select a revision for the material", () => {
      const materialVM = vm[materials[0].name];
      const revision   = 'foo';

      expect(materialVM.searchText()).toBe('');
      expect(materials[0].selection()).toBe(undefined);

      materialVM.selectRevision(revision);

      expect(materialVM.searchText()).toBe(revision);
      expect(materials[0].selection()).toBe(revision);
    });

    it("should update search state and perform search when search is updated", () => {
      const materialVM           = vm[materials[0].name];
      materialVM.debouncedSearch = jasmine.createSpy('debounced-search');
      const newSearchText        = 'foo';

      expect(materialVM.searchText()).toBe('');
      expect(materialVM.debouncedSearch).not.toHaveBeenCalled();

      materialVM.updateSearchText(newSearchText);

      expect(materialVM.searchText()).toBe(newSearchText);
      expect(materialVM.debouncedSearch).toHaveBeenCalled();
    });

    it("should perform search", () => {
      const searchText = 'foobar';

      const newMaterialResults = [
        {
          "type":        "Git",
          "name":        "material3",
          "fingerprint": "3dcc10e7943",
          "revision":    {}
        }
      ];

      jasmine.Ajax.withMock(() => {
        jasmine.Ajax.stubRequest(SparkRoutes.pipelineMaterialSearchPath(pipelineName, materials[0].fingerprint, searchText), undefined, 'GET').andReturn({
          responseText:    JSON.stringify(newMaterialResults),
          responseHeaders: {'Content-Type': 'application/vnd.go.cd.v1+json'},
          status:          200
        });

        const materialVM = vm[materials[0].name];
        materialVM.searchText(searchText);
        materialVM.performSearch();

        expect(materialVM.materialSearchResults()).toEqual(newMaterialResults);

        expect(jasmine.Ajax.requests.count()).toBe(1);
        const request = jasmine.Ajax.requests.mostRecent();
        expect(request.method).toBe('GET');
        expect(request.url).toBe(`/go/api/internal/material_search?fingerprint=${materials[0].fingerprint}&pipeline_name=${pipelineName}&search_text=${searchText}`);
        expect(request.requestHeaders['Accept']).toContain('application/vnd.go.cd.v1+json');
      });
    });
  });

  const json = {
    'variables': [],
    'materials': [
      {
        "type":        "Git",
        "name":        "material1",
        "fingerprint": "3dcc10e7943de637211a4742342fe456ffbe832577bb377173007499434fd819",
        "revision":    {
          "date":              "2018-02-08T04:32:11Z",
          "user":              "Ganesh S Patil <ganeshpl@thoughtworks.com>",
          "comment":           "Refactor Pipeline Widget (#4311)\n\n* Extract out PipelineHeaderWidget and PipelineOperationsWidget into seperate msx files",
          "last_run_revision": "a2d23c5505ac571d9512bdf08d6287e47dcb52d5"
        }
      },

      {
        "type":        "Git",
        "name":        "material2",
        "fingerprint": "3dcc10e7943de637211a4742342fe456ffbe832577bb377173007499434fd819",
        "revision":    {
          "date":              "2018-02-08T04:32:11Z",
          "user":              "Ganesh S Patil <ganeshpl@thoughtworks.com>",
          "comment":           "Refactor Pipeline Widget (#4311)\n\n* Extract out PipelineHeaderWidget and PipelineOperationsWidget into seperate msx files",
          "last_run_revision": "a2d23c5505ac571d9512bdf08d6287e47dcb52d5"
        }
      }
    ]
  };
})
;
