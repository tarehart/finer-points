(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeGraph', ['$routeParams', '$uibModal', '$location', 'NodeCache', nodeGraph]);

    function nodeGraph($routeParams, $uibModal, $location, NodeCache) {
        return {
            restrict: "A",
            scope: {
                starterNode: "=starterNode"
            },
            templateUrl: "partials/graph.html",
            link: function (scope) {
                initializeGraph(scope, $routeParams, $uibModal, $location, NodeCache);
            }
        }
    }

    function initializeGraph($scope, $routeParams, $uibModal, $location, NodeCache) {

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
                    toastr.error(err.message);
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
            return true;
        };

        function ensureDetail(node) {
            if (!hasFullDetail(node)) {
                NodeCache.getFullDetail(node.id);
            }
        }

        function hasFullDetail(node) {
            return node.getVersionString();
        }

        $scope.startEditingTitle = function(node) {
            node.editingTitle = true;
        };

        $scope.stopEditingTitle = function(node) {
            node.editingTitle = false;
            saveChanges(node);
        };

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
                    toastr.error(err.message);
                });
            } else {

                NodeCache.saveNodeEdit(node, $scope.rootNode, function(editedNode, data) {
                    if (successCallback) {
                        successCallback();
                    }
                    toastr.success("Saved successfully!");
                }, function (err) {
                    toastr.error(err.message);
                });
            }
        }

        $scope.stopEditingBody = function(node) {
            node.editingBody = false;

            var idsInBody = [];
            var regex = /{{\[([0-9a-z]{1,25})\](.+?)(?=}})}}/g;
            var match = regex.exec(node.body.body);
            while (match != null) {
                idsInBody.push(match[1]);
                match = regex.exec(node.body.body);
            }

            // Remove any children that are no longer supported by the body text.
            for (var i = node.children.length - 1; i >= 0; i--) {
                var child = node.children[i];
                var expectedId = child.body.majorVersion.stableId; // This is a pretty deep reference chain, make sure you populate
                if (idsInBody.indexOf(expectedId) < 0) {
                    // Remove the child
                    node.children.splice(i, 1);
                    $scope.$broadcast("edgeRemoved", node, child);

                    // Keep the removed child around to support a text-based undo of the deletion.
                    node.deletedChildren = node.deletedChildren || {};
                    node.deletedChildren[child.body.majorVersion.stableId] = child;
                }
            }

            // If the user manually restored the text of a link that they previously deleted,
            // restore the link.
            if (node.deletedChildren) {
                $.each(idsInBody, function (index, id) {
                    var nodeForId = node.deletedChildren[id];
                    if (nodeForId && node.children.indexOf(nodeForId) < 0) {
                        node.children.push(nodeForId);
                        $scope.$broadcast("edgeAdded", node, nodeForId);
                    }
                });
            }

            saveChanges(node);
        };

        $scope.linkChild = function(node, linkCallback) {

            function attachChild(child) {
                node.children.push(child); // TODO: insert the child in the right order
                linkCallback(child.body.majorVersion.stableId, child.body.title);

                saveChanges(node);
                ensureDetail(child);
                NodeCache.fetchGraphForId(child.stableId, function() {
                    $scope.$broadcast("nodeAdded", node, child);
                });
            }

            function errorHandler(err) {
                toastr.error(err.message);
            }

            function nodeChosenForLinking(result) {
                if (result.chosenNode) {
                    var child = NodeCache.addOrUpdateNode(result.chosenNode);
                    attachChild(child);
                } else {
                    NodeCache.createAndSaveNode(result.newTitle, result.type, attachChild, errorHandler);
                }
            }

            $uibModal.open({
                templateUrl: "partials/link-child.html",
                controller: "LinkChildController",
                resolve: {
                    linkCallback: function() {return nodeChosenForLinking; },
                    currentNode: function() {return node; }
                }
            });

        };

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