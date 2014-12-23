'use strict';

var nodeStandControllers = angular.module('nodeStandControllers', []);

nodeStandControllers.controller('SampleController', ['$scope', function ($scope) {
    $scope.fruits = [
        {'name': 'Apple',
            'snippet': 'Red and juicy'},
        {'name': 'Orange',
            'snippet': 'Full of citric acid'},
        {'name': 'Grape',
            'snippet': 'Bite-sized'}
    ];

}]);


nodeStandControllers.controller('GraphController', ['$scope', '$routeParams', '$http',
    function ($scope, $routeParams, $http) {

    $http.get('/graph', {params:{"rootId": $routeParams.rootId}}).success(function(data) {
        $scope.nodes = data.nodes;
        $scope.edges = data.edges;
    });
}]);

nodeStandControllers.controller('NodeMenuController', ['$scope', '$http',
    function ($scope, $http) {

        $http.get('/nodeMenu').success(function(data) {
            $scope.nodes = data;
        });
    }]);