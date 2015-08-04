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

        $scope.publishableNodes = [];

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
        }

        $scope.toggleSelect = function (node) {
            node.isSelected = !node.isSelected;
            if (node.isSelected) {
                ensureDetail(node);

                // Proactively get the children too because we might need their majorVersion id's during editing
                $.each(node.children, function(index, child) {
                    ensureDetail(child);
                })
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
            NodeCache.fetchNodeDetails(node.id);
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
                    NodeCache.saveDraftNode(function(newNode) {
                        var index = $scope.rootNodes.indexOf(node);
                        if (index >= 0) {
                            $scope.rootNodes[index].id = newNode.id;
                        }
                    });
                } else {
                    NodeCache.saveNodeEdit(node, function(editedNode) {
                        if (node.id != editedNode.id) {
                            $scope.enterEditMode(editedNode);
                            var index = $scope.rootNodes.indexOf(node);
                            if (index >= 0) {
                                $scope.rootNodes[index] = editedNode;
                            }
                        }
                    });
                }
            }

            node.doStopEditingBody = function() {
                node.editingBody = false;

                var idsInBody = [];
                var regex = /{{\[([0-9]+)\](.+?)(?=}})}}/g;
                var match = regex.exec(node.body.body);
                while (match != null) {
                    idsInBody.push(match[1]);
                    match = regex.exec(node.body.body);
                }

                // Remove any children that are no longer supported by the body text.
                for (var i = node.children.length - 1; i >= 0; i--) {
                    var child = node.children[i];
                    var expectedId = child.body.majorVersion.id;
                    if (idsInBody.indexOf(expectedId) < 0) {
                        // Remove the child
                        node.children.splice(i, 1);

                        // Keep the removed child around to support a text-based undo of the deletion.
                        node.deletedChildren = node.deletedChildren || {};
                        node.deletedChildren[child.body.majorVersion.id] = child;
                    }
                }

                // If the user manually restored the text of a link that they previously deleted,
                // restore the link.
                if (node.deletedChildren) {
                    $.each(idsInBody, function (index, id) {
                        var nodeForId = node.deletedChildren[id];
                        if (nodeForId && node.children.indexOf(nodeForId) < 0) {
                            node.children.push(nodeForId);
                        }
                    });
                }

                saveChanges(node);
            }

            node.doLinkChild = function(linkCallback) {

                function attachChild(child) {
                    node.children.push(child);
                    linkCallback(child.body.majorVersion.id, child.body.title);

                    saveChanges(node);
                    ensureDetail(child);
                    NodeCache.fetchGraphForId(child.id);
                }

                function nodeChosenForLinking(result) {
                    if (result.chosenNode) {
                        var child = NodeCache.addOrUpdateNode(result.chosenNode);
                        attachChild(child);
                    } else {
                        NodeCache.createAndSaveNode(result.newTitle, result.type, attachChild);
                    }
                }

                $modal.open({
                    templateUrl: "partials/link-child.html",
                    controller: "LinkChildController",
                    resolve: {
                        linkCallback: function() {return nodeChosenForLinking; },
                        currentNode: function() {return node; }
                    }
                });

            };
        }

        $scope.readyToPublish = function(node) {
            return node.body.draft && allowsPublish(node, {});
        };

        $scope.publishNode = function(node) {
            var publishableSet = {};
            if (allowsPublish(node, publishableSet)) {
                NodeCache.publishNode(node, function() {
                    $.each(publishableSet, function(key) {
                        // TODO: This may not fetch the new build version, since it focuses on the body.
                        // You may want to expand on what fetchNodeDetails does.
                        NodeCache.fetchNodeDetails(key);
                    });
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
                // TODO: uncomment once we can put URLs on sources
                //if (node.body.url) {
                //    publishableSet[node.id] = 1;
                //    return true;
                //}
                //return false;

                // TODO: remove this
                publishableSet[node.id] = 1;
                return true;

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