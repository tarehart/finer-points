(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('CreateController', ['$scope', '$http', CreateController]);

    function CreateController($scope, $http) {

        $scope.starterNode = {
            body: "",
            title: ""
        };

        $scope.submit = function () {
            $http.post('/create', {title: $scope.starterNode.title, body: $scope.starterNode.body, parentId: null})
                .success(function (data) {
                    alert("Success! " + data);
                })
                .error(function() {
                    // TODO: remember the request the user attempted, sign in via ajax,
                    // and let them try again.
                    window.location = '/signin';
                });
        }

        $scope.setText = function (text) {
            $scope.starterNode.body = text;
        }
    }

})();