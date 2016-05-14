require('../directives/particles-directive');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('GatewayController', ['$scope', '$http', GatewayController]);

    function GatewayController($scope, $http) {
        $http.get('/rootNodes').success(function (data) {
            $scope.nodes = data;
        });
    }

})();