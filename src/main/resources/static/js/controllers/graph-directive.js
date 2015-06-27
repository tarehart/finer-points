(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeGraph', ['$routeParams', '$http', 'NodeCache', nodeGraph]);

    function nodeGraph($routeParams, $http, NodeCache) {
        return {
            restrict: "A",
            scope: {
                starterNode: "=starterNode"
            },
            templateUrl: "partials/graph.html",
            link: function (scope) {
                initializeGraph(scope, $routeParams, $http, NodeCache);
            }
        }
    }

    function initializeGraph($scope, $routeParams, $http, NodeCache) {

        if ($routeParams && $routeParams.rootId) {
            NodeCache.fetchGraphForId($routeParams.rootId, function() {
                $scope.rootNodes = [];
                $scope.rootNodes.push(NodeCache.get($routeParams.rootId));
            });
        } else {
            var starterNode = $scope.starterNode;
            starterNode.isSelected = true;
            $scope.rootNodes = [starterNode];
        }

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
            if (!node.body.body) {
                fetchDetail(node);
            }
        }

        function fetchDetail(node) {
            NodeCache.fetchNodeDetails(node);
        }
    }

})();