(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('CreateController', ['$scope', '$http', CreateController]);

    function CreateController($scope, $http) {

        $scope.title;
        $scope.body;

        $scope.submit = function () {
            $http.post('/create', {title: $scope.title, body: $scope.body, parentId: null})
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
            $scope.text = text;

        }

        $scope.setHtml = function (html) {
            $scope.html = html;
        }
    }

})();