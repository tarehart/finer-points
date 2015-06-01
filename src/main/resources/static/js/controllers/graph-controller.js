(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('GraphController', ['$scope', '$routeParams', '$http', GraphController]);

    function GraphController($scope, $routeParams, $http) {

        $http.get('/graph', {params: {"rootId": $routeParams.rootId}}).success(function (data) {
            var nodes = {};
            for (var i = 0; i < data.nodes.length; i++) {
                nodes[data.nodes[i].id] = data.nodes[i];
            }

            $scope.nodes = nodes;
            $scope.edges = data.edges;

            Object.keys(nodes).forEach(function (id) {
                var node = nodes[id];
                node.children = [];
                var edges = $scope.edges.filter(function (el) {
                    return el[0] == node.id;
                });

                for (var j = 0; j < edges.length; j++) {
                    node.children.push(nodes[edges[j][1]]);
                }
            });

            $scope.rootNodes = [];
            $scope.rootNodes.push($scope.nodes[$routeParams.rootId]);
        });

        $scope.addChild = function (node) {
            var newNode = {};
            node.children.push(newNode);
            newNode.title = "Empty";
            newNode.children = [];
            return false;
        };

        $scope.hasChild = function (node) {
            return node.children && node.children.length;
        };

        // Selection stuff. I have functions for these even though they could
        // fit in the html because in html there are a bunch of nested scopes
        // and access to $scope there is weird.

        $scope.isSelected = function (node) {
            return node.isSelected;
        }

        $scope.toggleSelect = function (node) {
            node.isSelected = !node.isSelected;
            if (node.isSelected) {
                ensureDetail(node);
            }
        }

        $scope.toggleChildren = function (node) {
            node.hideChildren = !node.hideChildren;
        }


        $scope.hasComment = function (node) {
            return node.comments && node.comments.length;
        };

        $scope.toggleComments = function (node) {
            node.hideComments = !node.hideComments;
        }

        function ensureDetail(node) {
            if (!node.body) {
                fetchDetail(node);
            }
        }

        function fetchDetail(node) {
            $http.get('/detail', {params: {"id": node.id}}).success(function (data) {

                var nodes = {};
                for (var i = 0; i < data.nodes.length; i++) {
                    nodes[data.nodes[i].id] = data.nodes[i];
                    if (data.nodes[i].id == node.id) {
                        // We already have this node cached; just fill in additional data
                        $scope.nodes[node.id].body = data.nodes[i].body;
                        nodes[node.id] = $scope.nodes[node.id];
                    } else {
                        nodes[data.nodes[i].id] = data.nodes[i];
                    }
                }


                Object.keys(nodes).forEach(function (id) {
                    var node = nodes[id];
                    node.comments = [];
                    var edges = data.edges.filter(function (el) {
                        return el[1] == node.id; // Use [1] here because comments point to their parents, so we want to match the tip of the arrow
                    });

                    for (var j = 0; j < edges.length; j++) {
                        node.comments.push(nodes[edges[j][0]]);
                    }
                });

            });
        }
    }

})();