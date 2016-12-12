
require('../services/toast-service');
require('./node-list-directive');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeConsumers', nodeConsumers)
        .controller('NodeConsumersController', NodeConsumersController);
        

    function nodeConsumers() {
        return {
            restrict: "A",
            scope: {
                node: "="
            },
            template: '<div node-list list="consumersCtrl.nodes"></div>',
            controller: "NodeConsumersController",
            controllerAs: "consumersCtrl"
        }
    }

    function NodeConsumersController($scope, NodeCache, ToastService) {

        var self = this;
        self.nodes = [];

        fetchConsumers($scope.node, NodeCache, function(data) {
            self.nodes.push.apply(self.nodes, data); // Push data to nodes
        },
        function(err) {
            ToastService.error(err.message);
        });
    }

    function fetchConsumers(node, NodeCache, successCallback, errorCallback) {

        NodeCache.fetchConsumers(node.id, successCallback, errorCallback);
    }



})();