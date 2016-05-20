require('../directives/particles-directive');
require('../directives/node-list-directive');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('GatewayController', ['$scope', '$http', GatewayController]);

    function GatewayController($scope, $http) {

        $scope.nodes = [];

        $http.get('/rootNodes').success(function (data) {
            //$scope.nodes = data;
            $scope.nodes.push.apply($scope.nodes, data); // Push data to nodes
        });
    }

})();