'use strict';

var nodeStandControllers = angular.module('nodeStandControllers', []);

nodeStandControllers.controller('DetailController', ['$scope', '$routeParams', '$http',
    function ($scope, $routeParams, $http) {
        $http.get('/detail', {params:{"id": $routeParams.id}}).success(function(data) {
            $scope.node = data;
        });
    }
]);


nodeStandControllers.controller('GraphController', ['$scope', '$routeParams', '$http',
    function ($scope, $routeParams, $http) {

        $http.get('/graph', {params:{"rootId": $routeParams.rootId}}).success(function(data) {
            var nodes = {};
            for (var i = 0; i < data.nodes.length; i++) {
                nodes[data.nodes[i].id] = data.nodes[i];
            }

            $scope.nodes = nodes;
            $scope.edges = data.edges;

            Object.keys(nodes).forEach(function(id) {
                var node = nodes[id];
                node.children = [];
                var edges = $scope.edges.filter(function(el) {
                    return el[0] == node.id;
                });

                for (var j = 0; j < edges.length; j++) {
                    node.children.push(nodes[edges[j][1]]);
                }
            });

            $scope.rootNodes = [];
            $scope.rootNodes.push($scope.nodes[$routeParams.rootId]);
        });

        $scope.addChild = function(node) {
            var newNode = {};
            node.children.push(newNode);
            newNode.title = "Empty";
            newNode.children = [];
            return false;
        };

        $scope.hasChild = function(node) {
            return node.children && node.children.length;
        };

        // Selection stuff. I have functions for these even though they could
        // fit in the html because in html there are a bunch of nested scopes
        // and access to $scope there is weird.

        $scope.isSelected = function(node) {
            return node.isSelected;
        }

        $scope.toggleSelect = function(node) {
            node.isSelected = !node.isSelected;
        }

        $scope.toggleChildren = function(node) {
            node.hideChildren = !node.hideChildren;
        }


    }
]);

nodeStandControllers.controller('NodeMenuController', ['$scope', '$http',
    function ($scope, $http) {

        $http.get('/nodeMenu').success(function(data) {
            $scope.nodes = data;
        });
    }
]);