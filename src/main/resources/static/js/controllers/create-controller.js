(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('CreateController', ['$scope', '$routeParams', '$http', CreateController]);

    function CreateController($scope, $routeParams, $http) {

        $scope.title;
        $scope.body;

        $scope.submit = function () {
            $http.post('/create', {title: $scope.title, body: $scope.body, parentId: null})
                .success(function (data) {
                    alert("Success!");
                });
        }
    }

})();