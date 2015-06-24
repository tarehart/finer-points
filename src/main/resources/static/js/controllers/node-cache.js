(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .factory('NodeCache', ['$http', NodeCache]);

    function NodeCache($http) {

        var cache = {};

        cache.nodes = {};
        cache.edges = [];

        function decorateWithRequiredProperties(node) {
            node.children = node.children || [];
            return node;
        }

        cache.get = function(id) {
            return cache.nodes[id];
        };

        cache.getOrCreateNode = function(id) {
            if (cache.get(id)) {
                return cache.get(id);
            }

            var node = decorateWithRequiredProperties({id: id});

            cache.nodes[id] = node;
            return node;
        };

        cache.createDraftNode = function() {
            // TODO: actually create something server-side
            var node = decorateWithRequiredProperties({
                id: "draft",
                editingBody: true,
                editingTitle: true
            });

            cache.nodes[node.id] = node;
            prepareNodeForEditing(node);
            return node;
        }

        function prepareNodeForEditing(node) {
            node.setBody = function(text) {
                node.body = text;
            }
            node.stopEditingBody = function() {
                node.editingBody = false;
            }
            node.linkChild = function(linkCallback) {
                // TODO: get an actual nodeid to pass

                linkCallback(6);
            }
            node.isEditable = true;
        }

        cache.addOrUpdateNode = function(node) {
            var cachedNode = cache.get(node.id);
            if (cachedNode) {
                // Node is already in the cache, so perform updates
                cachedNode.title = node.title;
                cachedNode.body = node.body;
            } else {
                // node must be created and added to the cache
                cache.nodes[node.id] = decorateWithRequiredProperties(node);
            }

            return cache.get(node.id);
        };

        // nodes is a list of objects that have ids. Returns a map of the added nodes for further processing.
        cache.addNodesUnlinked = function(nodes) {
            var addedNodes = {};
            for (var i = 0; i < nodes.length; i++) {
                var node = cache.addOrUpdateNode(nodes[i]);
                addedNodes[node.id] = node;
            }
            return addedNodes;
        };

        function populateChildren(nodeMap, edgeList) {
            Object.keys(nodeMap).forEach(function (id) {
                var node = cache.get(id);
                node.children = [];
                var edges = edgeList.filter(function (el) {
                    return el[0] == id;
                });

                for (var j = 0; j < edges.length; j++) {
                    node.children.push(cache.get(edges[j][1]));
                }
            });
        }

        cache.fetchGraphForId = function(id, successCallback, errorCallback) {

            if (!cache.get(id)) {
                $http.get('/graph', {params: {"rootId": id}}).success(function (data) {

                    var addedNodes = cache.addNodesUnlinked(data.nodes);
                    populateChildren(addedNodes, data.edges);

                    successCallback();
                }).error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
            }
        }

        cache.fetchNodeDetails = function(node) {
            $http.get('/detail', {params: {"id": node.bodyId}}).success(function (data) {

                // Detail returns the current ArgumentNode in full detail,
                // the full comment tree for the current node,
                // and all the edges to link them together.

                // we're dealing with comments here!
                // TODO: make sure we're aggregating comments across everything within the major version and
                // indicating which minor version they are attributed to.
                var nodes = {};
                for (var i = 0; i < data.nodes.length; i++) {
                    var returnedId = data.nodes[i].id;
                    if (returnedId == node.bodyId) {
                        // We already have this node cached; just fill in additional data
                        cache.get(node.id).body = data.nodes[i].body;

                        // This line is tricky. The node.id does NOT match returnedId. This will ultimately have the affect
                        // of attributing comments to the ArgumentNode when really they belong to the ArgumentBod(ies).
                        nodes[returnedId] = cache.get(node.id);
                    } else {
                        nodes[returnedId] = data.nodes[i];
                    }
                }

                Object.keys(nodes).forEach(function (id) {
                    var returnedNode = nodes[id];
                    returnedNode.comments = [];
                    var edges = data.edges.filter(function (el) {
                        return el[1] == id; // Use [1] here because comments point to their parents, so we want to match the tip of the arrow
                    });

                    for (var j = 0; j < edges.length; j++) {
                        returnedNode.comments.push(nodes[edges[j][0]]);
                    }
                });

            });
        }

        return cache;

    }

})();