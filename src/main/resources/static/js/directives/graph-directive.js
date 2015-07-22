(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeGraph', ['$routeParams', '$modal', 'NodeCache', nodeGraph]);

    function nodeGraph($routeParams, $modal, NodeCache) {
        return {
            restrict: "A",
            scope: {
                starterNode: "=starterNode"
            },
            templateUrl: "partials/graph.html",
            link: function (scope) {
                initializeGraph(scope, $routeParams, $modal, NodeCache);
            }
        }
    }

    function initializeGraph($scope, $routeParams, $modal, NodeCache) {

        $scope.enterEditMode = function (node) {
            prepareNodeForEditing(node);
            node.inEditMode = true;
        }

        if ($routeParams && $routeParams.rootId) {
            NodeCache.fetchGraphForId($routeParams.rootId, function() {
                $scope.rootNodes = [];
                $scope.rootNodes.push(NodeCache.get($routeParams.rootId));
            });
        } else if ($scope.starterNode) {
            var starterNode = $scope.starterNode;
            $scope.rootNodes = [starterNode];
        } else {
            var starterNode = NodeCache.getOrCreateDraftNode();
            $scope.rootNodes = [starterNode];
            $scope.enterEditMode(starterNode);
            starterNode.isSelected = true;
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
        };

        $scope.authorizedForEdit = function (node) {
            return true;
        };



        function ensureDetail(node) {
            if (!node.body.body) {
                fetchDetail(node);
            }
        }

        function fetchDetail(node) {
            NodeCache.fetchNodeDetails(node);
        }

        function prepareNodeForEditing(node) {

            node.startEditingTitle = function() {
                node.editingTitle = true;
            };

            node.stopEditingTitle = function() {
                node.editingTitle = false;
                saveChanges(node);
            };

            function saveChanges(node) {
                if (NodeCache.isDraftNode(node)) {
                    NodeCache.saveDraftNode(null, function(newNode) {
                        var index = $scope.rootNodes.indexOf(node);
                        if (index) {
                            $scope.rootNodes[index].id = newNode.id;
                        }
                    });
                } else {
                    NodeCache.saveNodeEdit(node, function(editedNode) {
                        if (node.id != editedNode.id) {
                            $scope.enterEditMode(editedNode);
                            var index = $scope.rootNodes.indexOf(node);
                            if (index) {
                                $scope.rootNodes[index] = editedNode;
                            }
                        }
                    });
                }
            }

            node.doStopEditingBody = function() {
                node.editingBody = false;
                saveChanges(node);
            }

            node.doLinkChild = function(linkCallback) {

                function nodeChosenForLinking(child) {
                    child = NodeCache.addOrUpdateNode(child);
                    node.children.push(child);
                    saveChanges(node);

                    NodeCache.fetchGraphForId(child.id);

                    linkCallback(child.body.majorVersion.id, child.body.title);
                }

                $modal.open({
                    templateUrl: "partials/link-child.html",
                    controller: "LinkChildController",
                    resolve: {
                        linkCallback: function() {return nodeChosenForLinking; }
                    }
                });

            };
        }
    }

})();