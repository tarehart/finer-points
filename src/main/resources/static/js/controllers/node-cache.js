(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .factory('NodeCache', ['$http', NodeCache]);

    function NodeCache($http) {

        var cache = {};

        cache.nodes = {};
        cache.edges = [];

        var DRAFT_ID = "draft";

        // This is supposed to be idempotent.
        function decorateWithRequiredProperties(node) {
            node.children = node.children || [];
            node.body = node.body || {};
            node.body.author = node.body.author || {};

            node.getType = function() {
                if (node.type) {
                    return node.type;
                }
                return null;
            };

            node.getVersionString = function() {

                var version = "";
                if (node.body.majorVersion) {
                    version = node.body.majorVersion.versionNumber + ".";
                    if (node.body.minorVersion < 0) {
                        return version + "x";
                    }
                    version += node.body.minorVersion;
                }

                return version;
            };

            return node;
        }

        cache.get = function(id) {
            return cache.nodes[id];
        };

        cache.getByStableId = function(stableId) {
            var foundNode = null;

            $.each(cache.nodes, function(id, node) {
                if (node && node.stableId === stableId) {
                    foundNode = node;
                    return false; // break out of the loop
                }
            });

            return foundNode;
        };

        cache.getOrCreateNode = function(id) {
            if (cache.get(id)) {
                return cache.get(id);
            }

            var node = decorateWithRequiredProperties({id: id});

            cache.nodes[id] = node;
            return node;
        };

        cache.getOrCreateDraftNode = function() {

            if (cache.nodes[DRAFT_ID]) {
                return cache.nodes[DRAFT_ID];
            }

            // TODO: actually create something server-side
            var node = decorateWithRequiredProperties({
                id: DRAFT_ID,
                editingBody: true,
                editingTitle: true,
                type: "assertion",
                body: {draft: true, qualifier: "Original version"}
            });

            cache.nodes[node.id] = node;
            return node;
        };

        // This refers to whether this node is completely unsaved and does not exist in the database,
        // NOT whether it has been published (which is what node.isDraft determines).
        cache.isBlankSlateNode = function(node) {
            return node == cache.get(DRAFT_ID);
        };

        cache.saveBlankSlateNode = function(alias, successCallback, errorCallback) {
            var node = cache.get(DRAFT_ID);
            node.body.title = node.body.title || 'Untitled';
            saveNewNode(node, alias, function(data) {
                // Next time the draft node is requested, a fresh blank one should be built.
                cache.nodes[DRAFT_ID] = null;
                if (successCallback) {
                    successCallback(data); // This callback probably ought to change the URL to incorporate the new id.
                }
            }, errorCallback);
        };

        cache.createAndSaveNode = function(title, qualifier, type, alias, successCallback, errorCallback) {
            var node = {
                body: {title: title, qualifier:qualifier},
                children: [],
                getType: function() {return type;}
            };

            saveNewNode(node, alias, successCallback, errorCallback);
        };

        function saveNewNode(node, alias, successCallback, errorCallback) {
            if (node.getType() == "assertion") {
                saveNewAssertion(node, alias, successCallback, errorCallback);
            } else if (node.getType() == "interpretation") {
                saveNewInterpretation(node, alias, successCallback, errorCallback);
            } else if (node.getType() == "source") {
                saveNewSource(node, alias, successCallback, errorCallback);
            }
        }

        function saveNewAssertion(node, alias, successCallback, errorCallback) {

            var links = node.children.map(function(child) {
                return child.id;
            });

            $http.post('/createAssertion',
                {
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    body: node.body.body,
                    links: links,
                    authorStableId: alias.stableId
                })
                .success(function (data) {
                    mergeIntoNode(node, data);
                    insertNode(node);

                    if (successCallback) {
                        successCallback(node); // This callback probably ought to change the URL to incorporate the new id.
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        function saveNewInterpretation(node, alias, successCallback, errorCallback) {

            var sourceId = null;
            if (node.children && node.children.length) {
                sourceId = node.children[0].id;
            }

            $http.post('/createInterpretation',
                {
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    body: node.body.body,
                    sourceId: sourceId,
                    authorStableId: alias.stableId
                })
                .success(function (data) {
                    mergeIntoNode(node, data);
                    insertNode(node);

                    if (successCallback) {
                        successCallback(node); // This callback probably ought to change the URL to incorporate the new id.
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        function saveNewSource(node, alias, successCallback, errorCallback) {

            $http.post('/createSource',
                {
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    url: node.body.url,
                    authorStableId: alias.stableId
                })
                .success(function (data) {
                    mergeIntoNode(node, data);
                    insertNode(node);

                    if (successCallback) {
                        successCallback(node); // This callback probably ought to change the URL to incorporate the new id.
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        cache.saveNodeEdit = function(node, successCallback, errorCallback) {
            if (node.getType() == "assertion") {
                saveAssertionEdit(node, successCallback, errorCallback);
            } else if (node.getType() == "interpretation") {
                saveInterpretationEdit(node, successCallback, errorCallback);
            } else if (node.getType() == "source") {
                saveSourceEdit(node, successCallback, errorCallback);
            } else {
                console.log("Can't edit node because its type is unknown!");
            }
        };

        function handleDraftCreation(draftResponse) {
            var draftNode = cache.addOrUpdateNode(draftResponse.editedNode);

            if (draftResponse.graph) {
                inductQuickGraph(draftResponse.graph);
            }

            return draftNode;
        }

        cache.makeDraft = function(node, alias, successCallback, errorCallback) {
            $http.post('/makeDraft',
                {
                    nodeId: node.id,
                    authorStableId: alias.stableId
                })
                .success(function (data) {
                    var draftNode = handleDraftCreation(data);

                    if (successCallback) {
                        successCallback(draftNode, data); // This callback probably ought to change the URL to incorporate the new id.
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        };

        function handleNodeEdit(editedNode) {
            return cache.addOrUpdateNode(editedNode);
        }

        function saveAssertionEdit(node, successCallback, errorCallback) {

            var links = node.children.map(function(child) {
                return child.id;
            });

            // Iff the node is not a draft, this operation will produce a new draft node with a new id.
            $http.post('/editAssertion',
                {
                    nodeId: node.id,
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    body: node.body.body,
                    links: links
                })
                .success(function (data) {
                    var editedNode = handleNodeEdit(data);

                    if (successCallback) {
                        successCallback(editedNode);
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        function saveInterpretationEdit(node, successCallback, errorCallback) {

            var sourceId = null;
            if (node.children && node.children.length) {
                sourceId = node.children[0].id;
            }

            // Iff the node is not a draft, this operation will produce a new draft node with a new id.
            $http.post('/editInterpretation',
                {
                    nodeId: node.id,
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    body: node.body.body,
                    sourceId: sourceId
                })
                .success(function (data) {
                    var editedNode = handleNodeEdit(data);

                    if (successCallback) {
                        successCallback(editedNode);
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        function saveSourceEdit(node, successCallback, errorCallback) {

            var links = node.children.map(function(child) {
                return child.id;
            });

            // Iff the node is not a draft, this operation will produce a new draft node with a new id.
            $http.post('/editSource',
                {
                    nodeId: node.id,
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    url: node.body.url
                })
                .success(function (data) {
                    var editedNode = handleNodeEdit(data);

                    if (successCallback) {
                        successCallback(editedNode);
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        cache.publishNode = function(node, successCallback, errorCallback) {
            $http.post('/publishNode',
                {
                    nodeId: node.id
                })
                .success(function (data) {
                    inductQuickGraph(data);

                    if (data.rootId != node.id) {
                        // The publish has resulted in the draft node being replaced, and probably destroyed.
                        // In any case, we should forget about the draft node. Remove it from the cache.

                        forgetNode(node.id);
                    }

                    if (successCallback) {
                        successCallback(data); // This callback probably ought to change the URL to incorporate the new id.
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        };

        function mergeIntoNode(cachedNode, newData) {
            if (newData.body) {
                if (newData.body.majorVersion) {
                    // This is a good indicator that node's body is fully fleshed out and should be trusted.
                    cachedNode.body = newData.body;
                    cachedNode.type = newData.type;
                } else {
                    cachedNode.body = cachedNode.body || newData.body;
                    cachedNode.body.body = newData.body.body;
                    cachedNode.body.title = newData.body.title;
                    cachedNode.body.qualifier = newData.body.qualifier;
                    cachedNode.body.public = newData.body.public;
                }
            }

            if (!cachedNode.id || cachedNode.id === DRAFT_ID) {
                cachedNode.id = newData.id;
                cachedNode.stableId = newData.stableId;
            }

            if (newData.draft != undefined && newData.draft != null) {
                cachedNode.draft = newData.draft;
            }

            cachedNode.childOrder = newData.childOrder;

            sortChildren(cachedNode);
        }

        function insertNode(node) {
            cache.nodes[node.id] = decorateWithRequiredProperties(node);
        }

        cache.addOrUpdateNode = function(node) {
            var cachedNode = cache.get(node.id);
            if (cachedNode) {
                // Node is already in the cache, so perform updates
                mergeIntoNode(cachedNode, node);

            } else {
                // node must be created and added to the cache
                insertNode(node);
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

        function sortChildren(node) {
            if (!node.childOrder || !node.children || !node.children.length > 1) {
                return;
            }

            var childOrder = node.childOrder.split(",");

            if (childOrder.length !== node.children.length) {
                console.log("Child array has different length than childOrder.");
            }

            node.children.sort(function(a, b) {
                return childOrder.indexOf(a.stableId) - childOrder.indexOf(b.stableId);
            });
        }

        cache.addChildToNode = function(node, child) {
            // TODO: insert the child in the right order

            for (var i = 0; i < node.children.length; i++) {
                if (node.children[i].id === child.id) {
                    return;
                }
            }

            node.children.push(child);
        };

        cache.discardDraft = function(node, successCallback) {
            $http.post('/discardDraft', node.stableId).success(function () {

                forgetNode(node.id);

                if (successCallback) {
                    successCallback();
                }
            });
        };

        function forgetNode(nodeId) {
            var nodeToForget = cache.get(nodeId);
            if (!nodeToForget) {
                return;
            }

            $.each(cache, function(id, node) {
                var idx;
                if (node.children) {
                    idx = node.children.indexOf(nodeToForget);
                    if (idx > -1) {
                        // Remove nodeToForget from the list of children.
                        node.children.splice(idx, 1);
                    }
                }

                if (node.consumers) {
                    idx = node.consumers.indexOf(nodeToForget);
                    if (idx > -1) {
                        // Remove nodeToForget from the list of children.
                        node.consumers.splice(idx, 1);
                    }
                }
            });

            delete cache[nodeId];
        }

        function populateChildren(nodeMap, edgeList) {
            Object.keys(nodeMap).forEach(function (id) {
                var node = cache.get(id);
                node.children = [];
                var edges = edgeList.filter(function (el) {
                    return el.start == id;
                });

                for (var j = 0; j < edges.length; j++) {
                    var child = cache.get(edges[j].end);
                    if (child === undefined) {
                        console.error("Could not locate child in cache with id of " + edges[j].end);
                    } else {
                        node.children.push(child);
                    }
                }

                sortChildren(node);

                if (isInfinite(node)) {
                    // That's illegal! Remove those children.
                    node.children = [];
                    console.log("Took away the children of " + node.stableId + " because they create a cycle!");
                }
            });
        }

        function isInfinite(node) {

            var stackSet = new Set();

            function isInfiniteHelper(node) {
                if (stackSet.has(node)) {
                    return true;
                }
                stackSet.add(node);

                for (var i = 0; i < node.children.length; i++) {
                    if (isInfiniteHelper(node.children[i])) {
                        return true;
                    }
                }

                stackSet.delete(node);
                return false;
            }

            return isInfiniteHelper(node);
        }

        cache.fetchGraphForId = function(stableId, successCallback, errorCallback, force) {

            var rootNode = cache.getByStableId(stableId);

            if (rootNode && rootNode.hasFullGraph && !force) {
                if (successCallback) {
                    successCallback(rootNode);
                }
            } else {
                $http.get('/graph', {params: {"rootStableId": stableId}}).success(function (data) {

                    inductQuickGraph(data);
                    rootNode = cache.getByStableId(stableId);
                    rootNode.hasFullGraph = true;
                    if (successCallback) {
                        successCallback(rootNode);
                    }
                }).error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
            }
        };

        function inductQuickGraph(quickGraphResponse) {
            var addedNodes = cache.addNodesUnlinked(quickGraphResponse.nodes);
            populateChildren(addedNodes, quickGraphResponse.edges);
        }

        cache.getFullDetail = function(nodeStableId) {

            $http.get('/fullDetail', {params: {stableId: nodeStableId}}).success(function (node) {

                // returns the current ArgumentNode in full detail
                cache.addOrUpdateNode(node);
            });
        };

        cache.getLinkChoices = function(majorVersionId, successCallback, errorCallback) {

            $http.get('/nodesInMajorVersion', {params: {"majorVersionId": majorVersionId}}).success(function (nodeList) {
                var nodes = [];
                $.each(nodeList, function(index, node) {
                    nodes.push(cache.addOrUpdateNode(node));
                });
                successCallback(nodes);

            }).error(function(err) {
                if (errorCallback) {
                    errorCallback(err);
                }
            });
        };

        cache.fetchConsumers = function(nodeId, successCallback, errorCallback) {

            $http.get('/consumerNodes', {params: {nodeId: nodeId}}).then(
                function (response) {
                    var nodeMap = cache.addNodesUnlinked(response.data);
                    var node = cache.get(nodeId);
                    node.consumers = [];
                    Object.keys(nodeMap).forEach(function (id) {
                        node.consumers.push(nodeMap[id]);
                    });

                    if (successCallback) {
                        successCallback(response.data);
                    }
                },
                function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        };

        return cache;

    }

})();