
require('../services/toast-service');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeList', nodeList)
        .controller('NodeListController', NodeListController);
        

    function nodeList() {
        return {
            restrict: "A",
            scope: {
                list: "="
            },
            templateUrl: "partials/node-list.html",
            controller: "NodeListController",
            controllerAs: "listCtrl"
        }
    }

    function NodeListController($scope) {
        var self = this;
        self.list = $scope.list;
    }

    function fetchDrafts($http, successCallback, errorCallback) {

        $http.get('/draftNodes')
            .success(function (data) {
                if (successCallback) {
                    successCallback(data);
                }
            })
            .error(function(err) {
                if (errorCallback) {
                    errorCallback(err);
                }
            });
    }

})();