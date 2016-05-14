(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('LoginController', ['$scope', '$http', LoginController]);

    function LoginController($scope, $http) {

        $scope.csrfToken = $("meta[name='_csrf']").attr("content");

    }

})();