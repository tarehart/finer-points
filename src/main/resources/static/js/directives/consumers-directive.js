
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

    function NodeConsumersController($scope, $http, ToastService) {

        var self = this;
        self.nodes = [];
        
        fetchConsumers($scope.node.id, $http, function(data) {
            self.nodes.push.apply(self.nodes, data); // Push data to nodes
        },
        function(err) {
            ToastService.error(err.message);
        });
    }

    function fetchConsumers(nodeId, $http, successCallback, errorCallback) {

        $http.get('/consumerNodes', {params: {nodeId: nodeId}}).then(
            function (response) {
                if (successCallback) {
                    successCallback(response.data);
                }
            },
            function(err) {
                if (errorCallback) {
                    errorCallback(err);
                }
            });
    }



})();