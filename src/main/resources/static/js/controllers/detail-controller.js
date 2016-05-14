(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('DetailController', ['$scope', '$routeParams', '$http', DetailController]);

    function DetailController($scope, $routeParams, $http) {
        $http.get('/detail', {params: {"id": $routeParams.id}}).success(function (data) {
            $scope.node = data;
        });
    }

})();