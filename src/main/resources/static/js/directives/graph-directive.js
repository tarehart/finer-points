require('../../sass/graph.scss');
require('../controllers/node-cache');
require('./vivagraph-directive');
require('./comments-directive');
require('./node-editor-directive');
require('./vote-button-directive');
require('./markdown-directive');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeGraph', nodeGraph)
        .controller('GraphController', GraphController);

    function nodeGraph() {
        return {
            restrict: "A",
            scope: {
                starterNode: "=starterNode"
            },
            templateUrl: "partials/graph.html",
            controller: 'GraphController'
        }
    }

    function GraphController($scope, $routeParams, $location, NodeCache, ToastService) {

        $scope.publishableNodes = [];

        function setHighlighted(node) {
            $scope.highlightedNode = node;
        }

        $scope.$on("nodeHighlighted", function(e, node) {
            setHighlighted(node);
        });

        $scope.enterEditMode = function (node) {
            if (node.body.public) {
                NodeCache.makeDraft(node, function(draftNode, data) {
                    $location.path("/graph/" + data.graph.rootStableId);

                }, function (err) {
                    ToastService.error(err.message);
                });

            } else {
                node.inEditMode = true;

                // Proactively get the children too because we might need their majorVersion id's during editing
                $.each(node.children, function(index, child) {
                    ensureDetail(child);
                });
            }
        };

        if ($routeParams && $routeParams.rootStableId) {
            NodeCache.fetchGraphForId($routeParams.rootStableId, function() {
                $scope.rootNodes = [];
                $scope.rootNodes.push(NodeCache.getByStableId($routeParams.rootStableId));
                $scope.rootNode = $scope.rootNodes[0];
                ensureDetail($scope.rootNode);
                $scope.$broadcast("rootData", $scope.rootNode);
            });
        } else if ($scope.starterNode) {
            $scope.rootNodes = [$scope.starterNode];
            $scope.rootNode = $scope.rootNodes[0];
            $scope.$broadcast("rootData", $scope.rootNode);
        } else {
            var starterNode = NodeCache.getOrCreateDraftNode();
            $scope.rootNodes = [starterNode];
            $scope.rootNode = $scope.rootNodes[0];
            $scope.$broadcast("rootData", $scope.rootNode);
            $scope.draftNodes = [starterNode];
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
        };

        $scope.isPersisted = function (node) {
            return node && node.id !== "draft";
        };

        $scope.toggleSelect = function (node) {
            node.isSelected = !node.isSelected;
            if (node.isSelected) {
                ensureDetail(node);
            }

            $scope.$broadcast("nodeHighlighted", node);

            return true;
        };

        $scope.toggleChildren = function (node) {
            node.hideChildren = !node.hideChildren;
        };

        $scope.navigateToNode = function (node) {
            // Change the page url and reload the graph. All the UI state should stay the same because
            // the nodes are in the NodeCache.
            $location.path("/graph/" + node.stableId);
        };

        $scope.authorizedForEdit = function (node) {
            // Currently, I only want to allow child editing if the children are already private.
            // This is because I don't intend to mess with draft propagation anymore.
            // If you want to edit a public child, the user needs to navigate to it first.
            return node === $scope.rootNode || !node.body.public;
        };

        function ensureDetail(node) {
            if (!hasFullDetail(node)) {
                NodeCache.getFullDetail(node.id);
            }
        }

        function hasFullDetail(node) {
            return node.getVersionString();
        }

        $scope.saveNode = function(node) {
            saveChanges(node, function() {
                node.inEditMode = false;
            });
        };

        function saveChanges(node, successCallback) {
            if (NodeCache.isDraftNode(node)) {
                NodeCache.saveDraftNode(function(newNode) {

                    // Change the page url and reload the graph. All the UI state should stay the same because
                    // the nodes are in the NodeCache.
                    $location.path("/graph/" + newNode.stableId);
                }, function(err) {
                    ToastService.error(err.message);
                });
            } else {

                NodeCache.saveNodeEdit(node, $scope.rootNode, function(editedNode, data) {
                    if (successCallback) {
                        successCallback();
                    }
                    ToastService.success("Saved successfully!");
                }, function (err) {
                    ToastService.error(err.message);
                });
            }
        }

        $scope.setBody = function(node, text) {
            node.body.body = text;
        };

        $scope.readyToPublish = function(node) {
            return (!node.body.public) && allowsPublish(node, {});
        };

        $scope.publishNode = function(node) {
            var publishableSet = {};
            if (allowsPublish(node, publishableSet)) {
                NodeCache.publishNode(node, function(resultingNode) {

                    var rootNode = $scope.rootNodes[0];

                    if (node === rootNode) {
                        NodeCache.get(resultingNode.id).hasFullGraph = false; // Make sure the full graph is fetched again upon reload.
                        $location.path("/graph/" + resultingNode.stableId); // Change url back to public version
                    } else {
                        // TODO: it seems like this is not getting rid of the publish buttons on children as expected.
                        NodeCache.fetchGraphForId(rootNode.stableId, null, null, true); // Refresh the graph
                    }
                });
            } else {
                // TODO: display an error
            }
        };

        function allowsPublish(node, publishableSet) {

            if (publishableSet[node.id] || node.body.draft === false) {
                return true;
            }

            if (!node.body.title) {
                return false;
            }

            if (node.type == "source") {
                if (node.body.url) {
                    publishableSet[node.id] = 1;
                    return true;
                }
                return false;
            }

            if (node.type == "interpretation") {
                if (node.body.body && node.children.length == 1 && allowsPublish(node.children[0], publishableSet)) {
                    publishableSet[node.id] = 1;
                    return true;
                }
                return false;
            }

            if (node.type == "assertion") {
                if (node.body.body && node.children.length) {
                    for (var i = 0; i < node.children.length; i++) {
                        if (!allowsPublish(node.children[i], publishableSet)) {
                            return false;
                        }
                    }
                    publishableSet[node.id] = 1;
                    return true;
                } else {
                    return false;
                }
            }
        }

    }

})();