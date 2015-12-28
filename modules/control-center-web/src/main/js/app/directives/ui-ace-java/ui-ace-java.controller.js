/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const SERVER_CFG = 'ServerConfigurationFactory';
const CLIENT_CFG = 'ClientConfigurationFactory';

export default ['$scope', 'IgniteUiAceOnLoad', function($scope, onLoad) {
    const ctrl = this;

    // Scope methods.
    $scope.onLoad = onLoad;

    // Watchers definition.
    const clusterWatcher = (value) => {
        delete ctrl.data;

        if (!value)
            return;

        const type = $scope.cfg ? CLIENT_CFG : SERVER_CFG;

        // TODO IGNITE-2054: need move $generatorJava to services.
        ctrl.data = $generatorJava.cluster($scope.cluster, 'config', type, $scope.cfg);
    };

    // Setup watchers.
    $scope.$watch('cfg', clusterWatcher, true);
    $scope.$watch('cluster', clusterWatcher);
}];
