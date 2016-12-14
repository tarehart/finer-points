
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
            templateUrl: 'partials/consumers.html',
            controller: "NodeConsumersController",
            controllerAs: "consumersCtrl"
        }
    }

    function NodeConsumersController($scope, $location, NodeCache, ToastService, UserService) {

        var self = this;
        self.nodes = [];

        fetchConsumers($scope.node, NodeCache, function(data) {
            self.nodes.push.apply(self.nodes, data); // Push data to nodes
        },
        function(err) {
            ToastService.error(err.message);
        });

        self.newConsumer = function() {
            NodeCache.createNodeWithSupport($scope.node, UserService.getActiveAlias(), function(newNode) {
                $location.path("/graph/" + newNode.stableId);
            });
        };

        self.canCreateConsumer = function() {
            return !!UserService.getUser();
        }

    }

    function fetchConsumers(node, NodeCache, successCallback, errorCallback) {

        NodeCache.fetchConsumers(node.id, successCallback, errorCallback);
    }



})();