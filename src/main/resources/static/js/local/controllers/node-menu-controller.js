(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('NodeMenuController', ['$scope', '$http', NodeMenuController]);

    function NodeMenuController($scope, $http) {
        $http.get('/nodeMenu').success(function (data) {
            $scope.nodes = data;
        });
    }

})();