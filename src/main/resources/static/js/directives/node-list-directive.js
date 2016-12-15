
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
            template: require("../../partials/node-list.html"),
            controller: "NodeListController",
            controllerAs: "listCtrl"
        }
    }

    function NodeListController($scope) {
        var self = this;
        self.list = $scope.list;
    }

})();