require('./vivagraph-directive');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('graphSketch', graphSketch);

    function graphSketch() {
        return {
            restrict: "A",
            templateUrl: "partials/graph-sketch.html",
            controller: sketchController,
            controllerAs: 'sketchCtrl'
        }
    }

    function sketchController($scope, $location, Node, ToastService, NodeCache, UserService) {
        var self = this;
        self.activeTool = 'selectionTool';
        self.highlightedNode = null;
        self.nodes = {};

        var SEEKING_PARENT = 1;
        var SEEKING_CHILD = 2;

        var sketchNum = 0;
        
        var edgeState = null;
        var edgeParent = null;

        self.addNode = function() {
            var node = new Node();
            node.id = "sketch-" + sketchNum++;
            node.type = 'assertion';
            node.body.qualifier = 'Original Version';

            var isFirstNode = $.isEmptyObject(self.nodes);
            self.nodes[node.id] = node;
            $scope.$broadcast("nodeAdded", null, node);
            if (isFirstNode) {
                highlightNode(node);
            }
        };

        self.hasNodes = function () {
            return Object.keys(self.nodes).length;
        };

        // Recursively saves nodes with no un-persisted children, one at a time, until all are saved.
        function saveNodesBottomUp(nodes) {

            var numNodes = Object.keys(nodes).length;
            var numSaved = 0;

            saveBottomUpHelper();

            function saveBottomUpHelper() {

                nodeLoop:
                for (var id in nodes) {
                    if (nodes.hasOwnProperty(id)) {
                        var node = nodes[id];
                        if (node.isPersisted()) {
                            continue;
                        }
                        for (var i = 0; i < node.children.length; i++) {
                            if (!node.children[i].isPersisted()) {
                                continue nodeLoop;
                            }
                        }
                        node.body.title = node.body.title || 'Untitled';
                        NodeCache.saveSketchNode(node, UserService.getActiveAlias(),
                            function (data) {
                                numSaved++;
                                if (numSaved < numNodes) {
                                    saveBottomUpHelper(); // Continue on to save more nodes.
                                } else {
                                    $location.path("/graph/" + data.stableId);
                                }
                            }, function (err) {
                                ToastService.error(err.message);
                            }
                        );
                        break;
                    }
                }
            }
        }

        self.convertToReal = function() {

            if (NodeCache.hasCycle(self.nodes)) {
                ToastService.error("You have a cycle that you need to get rid of! Look for arrows going in a circle.")
            }

            saveNodesBottomUp(self.nodes);

        };

        self.activeToolChanged = function() {
            if (self.activeTool === 'edgeTool') {
                self.enterEdgeMode();
            } else {
                self.exitEdgeMode();
            }
        };

        self.enterEdgeMode = function() {
            edgeState = SEEKING_PARENT;
            edgeParent = null;
        };
        
        self.exitEdgeMode = function () {
            edgeState = null;
            edgeParent = null;
        };
        
        self.isSeekingParent = function() {
            return edgeState === SEEKING_PARENT;
        };

        self.isSeekingChild = function() {
            return edgeState === SEEKING_CHILD;
        };

        self.typeChanged = function() {
            propagateTypeChange(self.highlightedNode, self.highlightedNode.getType(), null, true);
        };

        self.setBody = function (node, text) {
            node.body.body = text;
        };

        function propagateTypeChange(node, nodeType, previousNode, force) {
            if (node.getType() === nodeType && !force) {
                return;
            }

            if (node.getType() && node.getType() !== nodeType) {
                ToastService.info("Changed a card type to keep everything proper!");
            }

            node.type = nodeType;

            $scope.$broadcast('nodeChanged', node);

            if (node.isLeaf()) {
                $.each(node.parents, function(id, parent) {
                    if (!parent.isPersisted()) {
                        propagateTypeChange(parent, 'interpretation', node, true);
                    }
                });
            } else if (node.getType() === 'interpretation') {
                if (node.children.length) {
                    if (node.children.length > 1) {
                        // Detach some nodes, rather than tolerate an illegal graph.
                        var childToKeep = node.children[0];
                        for (var i = node.children.length - 1; i >= 0; i--) {
                            if (node.children[i] === previousNode) {
                                childToKeep = previousNode;
                                continue;
                            }
                            if (node.children[i] !== childToKeep) {
                                var childRemoved = node.children[i];
                                node.removeChild(childRemoved);
                                $scope.$broadcast("edgeRemoved", node, childRemoved);
                            }
                        }
                        ToastService.info("Detached some nodes to keep everything proper!");
                    }

                    $.each(node.children, function(idx, child) {
                        if (!child.isPersisted() && !child.isLeaf()) {
                            propagateTypeChange(child, 'source', node);
                        }
                    });
                }


                $.each(node.parents, function(id, parent) {
                    if (!parent.isPersisted()) {
                        propagateTypeChange(parent, 'assertion', node);
                    }
                });
            } else if (node.getType() === 'assertion') {
                $.each(node.children, function(idx, child) {
                    if (!child.isPersisted() && child.isLeaf()) {
                        propagateTypeChange(child, 'interpretation', node);
                    }
                });
                $.each(node.parents, function(id, parent) {
                    if (!parent.isPersisted()) {
                        propagateTypeChange(parent, 'assertion', node);
                    }
                });
            }
        }

        function findLegalType(node) {
            if (node.isLegalType(node.getType())) {
                return node.getType();
            }

            return node.isLegalType('source') ? 'source' :
                node.isLegalType('interpretation') ? 'interpretation' : 'assertion';
        }

        $scope.$on("nodeTapped", function(event, node) {
            if (self.activeTool === 'edgeTool') {
                runEdgeTool(node);
            } else if (self.activeTool === 'detachTool') {
                $.each(node.parents, function(id, parent) {
                    if (!parent.isPersisted()) {
                        parent.removeChild(node);
                        $scope.$broadcast("edgeRemoved", parent, node);
                    }
                });
            } else {
                highlightNode(node);
            }
        });

        function highlightNode(node) {
            self.highlightedNode = node;
            $scope.$broadcast("nodeHighlighted", node);
        }

        function runEdgeTool(node) {
            if (edgeState === SEEKING_PARENT) {
                edgeParent = node;
                self.highlightedNode = null;
                $scope.$broadcast("nodeHighlighted", node, 'edge-parent');
                edgeState = SEEKING_CHILD;
            } else if (edgeState === SEEKING_CHILD) {
                if (!edgeParent) {
                    // Should never happen.
                    self.exitEdgeMode();
                }
                if (node.id === edgeParent.id) {
                    // Can't make a self-referencing edge. Do nothing.
                    return;                    
                }

                if (edgeParent.isPersisted()) {
                    ToastService.error("Can't use the sketch tool to add support to this node.");
                    return;
                }

                if (node.removeChild(edgeParent)) { // If there's an existing edge in the opposite direction, remove it.
                    $scope.$broadcast("edgeRemoved", node, edgeParent);
                }

                edgeParent.addChild(node);

                if (!edgeParent.getType() && !node.getType()) {
                    node.type = 'assertion';
                }



                if (edgeParent.isLegalType(edgeParent.getType()) && !node.isPersisted() &&
                    !(hasBodyText(node) && !hasBodyText(edgeParent))) {

                    console.log("propagating down because parent looks stronger.");
                    propagateTypeChange(edgeParent, edgeParent.getType(), null, true);
                } else {
                    console.log("propagating up because child looks stronger.");
                    propagateTypeChange(node, findLegalType(node), null, true);
                }

                $scope.$broadcast("edgeAdded", edgeParent, node);
                $scope.$broadcast("highlightRemoved", node);
                ToastService.success("Arrow Created!");
                edgeState = SEEKING_PARENT;
            }
        }

        function hasBodyText(n) {
            return !!(n.body.body || n.body.url);
        }

    }

})();