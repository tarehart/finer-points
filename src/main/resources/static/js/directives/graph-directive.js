(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeGraph', ['$routeParams', '$modal', '$location', 'NodeCache', nodeGraph]);

    function nodeGraph($routeParams, $modal, $location, NodeCache) {
        return {
            restrict: "A",
            scope: {
                starterNode: "=starterNode"
            },
            templateUrl: "partials/graph.html",
            link: function (scope) {
                initializeGraph(scope, $routeParams, $modal, $location, NodeCache);
            }
        }
    }

    function initializeGraph($scope, $routeParams, $modal, $location, NodeCache) {

        $scope.publishableNodes = [];

        $scope.enterEditMode = function (node) {
            node.inEditMode = true;
        };

        if ($routeParams && $routeParams.rootStableId) {
            NodeCache.fetchGraphForId($routeParams.rootStableId, function() {
                $scope.rootNodes = [];
                $scope.rootNodes.push(NodeCache.getByStableId($routeParams.rootStableId));
                $scope.rootNode = $scope.rootNodes[0];
                ensureDetail($scope.rootNode);
                initSigma($scope.rootNode);
            });
        } else if ($scope.starterNode) {
            $scope.rootNodes = [$scope.starterNode];
            $scope.rootNode = $scope.rootNodes[0];
        } else {
            var starterNode = NodeCache.getOrCreateDraftNode();
            $scope.rootNodes = [starterNode];
            $scope.rootNode = $scope.rootNodes[0];
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

        $scope.toggleSelect = function (node) {
            node.isSelected = !node.isSelected;
            if (node.isSelected) {
                ensureDetail(node);

                // Proactively get the children too because we might need their majorVersion id's during editing
                $.each(node.children, function(index, child) {
                    ensureDetail(child);
                })
            }
        };

        $scope.toggleChildren = function (node) {
            node.hideChildren = !node.hideChildren;
        };


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
            if (!hasFullDetail(node)) {
                fetchDetail(node);
            }
        }

        function hasFullDetail(node) {
            return node.getVersionString();
        }

        function fetchDetail(node) {
            NodeCache.fetchNodeDetails(node.id);
        }


        $scope.startEditingTitle = function(node) {
            node.editingTitle = true;
        };

        $scope.stopEditingTitle = function(node) {
            node.editingTitle = false;
            saveChanges(node);
        };

        function saveChanges(node) {
            if (NodeCache.isDraftNode(node)) {
                NodeCache.saveDraftNode(function(newNode) {

                    // Change the page url and reload the graph. All the UI state should stay the same because
                    // the nodes are in the NodeCache.
                    $location.path("/graph/" + newNode.stableId);
                }, function(err) {
                    toastr.error(err.message);
                });
            } else {
                // At the moment, this list of rootNodes is always size 1.
                // The reason it's currently a list and not just a variable is to support the way that
                // the angular template kicks off its recursion.
                var rootNode = $scope.rootNodes[0];

                NodeCache.saveNodeEdit(node, rootNode, function(editedNode, data) {
                    var editedNode = data.editedNode; // TODO: what is the editedNode param doing here?
                    if (node.id != editedNode.id) { // This indicates that the node had never before been saved.

                        // TODO: this shouldn't be necessary because the NodeCache should be updating the node
                        // in a stable way.
                        $scope.enterEditMode(editedNode);

                        if (data.graph) {
                            $location.path("/graph/" + data.graph.rootStableId);
                        }
                    }
                }, function (err) {
                    toastr.error(err.message);
                });
            }
        }

        $scope.stopEditingBody = function(node) {
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
        };

        $scope.linkChild = function(node, linkCallback) {

            function attachChild(child) {
                node.children.push(child);
                linkCallback(child.body.majorVersion.id, child.body.title);

                saveChanges(node);
                ensureDetail(child);
                NodeCache.fetchGraphForId(child.stableId);
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

            $modal.open({
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

        function initSigma(rootNode) {

            //sigma.renderers.def = sigma.renderers.canvas;
            var s = new sigma('sigma-container');

            var addedNodes = {};

            addSigmaNodesRecursive(rootNode, null);

            function addSigmaNodesRecursive(node, parent) {

                if (!addedNodes[node.id]) {

                    s.graph.addNode({
                        id: "" + node.id,
                        label: node.body.title,
                        x: Math.random(),
                        y: Math.random() * 0.2,
                        size: parent ? 4 : 6,
                        color: getColor(node)
                    });

                    addedNodes[node.id] = 1;

                    for(var i = 0; i < node.children.length; i++) {
                        addSigmaNodesRecursive(node.children[i], node);
                    }
                }

                if (parent) {
                    s.graph.addEdge({
                        id: parent.id + "-" + node.id,
                        source: "" + parent.id,
                        target: "" + node.id,
                        color: "#AAAAAA"
                    });
                }

            }

            function getColor(node) {
                if (node.getType() === 'assertion') {
                    return "#8888DD";
                }
                if (node.getType() === 'interpretation') {
                    return "#88DD88";
                }
                if (node.getType() === 'source') {
                    return "#DD8888";
                }

                return "#000000";
            }

            // Finally, let's ask our sigma instance to refresh:
            s.refresh();
            //sigma.plugins.dragNodes(s, s.renderers[0]);
            s.startForceAtlas2();
        }

    }

})();