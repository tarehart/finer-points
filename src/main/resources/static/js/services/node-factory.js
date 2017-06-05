(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .factory('Node', NodeFactory);

    function NodeFactory() {

        var Node = function() {
            var self = this;
            self.children = [];
            self.parents = {};
            self.body = { author: {} };

        };

        Node.DRAFT_ID = "draft";
        Node.isLeaf = function(nodeType) {
            return isLeaf(nodeType);
        };

        Node.prototype.getType = function() {
            return this.type;
        };

        Node.prototype.isLeaf = function() {
            return isLeaf(this.getType());
        };

        Node.prototype.getVersionString = function() {

            var self = this;
            var version = "";
            if (self.body.majorVersion) {
                version = node.body.majorVersion.versionNumber + ".";
                if (node.body.minorVersion < 0) {
                    return version + "x";
                }
                version += node.body.minorVersion;
            }

            return version;
        };

        Node.prototype.assimilateData = function(data) {
            var self = this;

            if (!self.isPersisted()) {
                self.id = data.id;
                self.stableId = data.stableId;
            }

            if (data.body) {
                if (data.body.majorVersion) {
                    // This is a good indicator that node's body is fully fleshed out and should be trusted.
                    self.body = data.body;
                    self.type = data.type;
                } else {
                    self.body = self.body || data.body;
                    self.body.body = data.body.body;
                    self.body.title = data.body.title;
                    self.body.qualifier = data.body.qualifier;
                    self.body.public = data.body.public;
                }
            }

            if (data.draft !== undefined && data.draft !== null) {
                self.draft = data.draft;
            }

            self.childOrder = data.childOrder;

            self.sortChildren();
        };

        Node.prototype.addChild = function(child) {
            // TODO: insert the child in the right order

            var self = this;

            for (var i = 0; i < self.children.length; i++) {
                if (self.children[i].id === child.id) {
                    return;
                }
            }

            self.children.push(child);
            child.parents[self.id] = self;
        };

        Node.prototype.removeChild = function(child) {
            var self = this;

            for (var i = 0; i < self.children.length; i++) {
                if (self.children[i].id === child.id) {
                    self.children.splice(i, 1);
                    delete child.parents[self.id];
                    return true;
                }
            }

            return false;
        };

        Node.prototype.sortChildren = function() {
            var self = this;
            if (!self.childOrder || !self.children || !(self.children.length > 1)) {
                return;
            }

            var childOrder = self.childOrder.split(",");

            if (childOrder.length !== self.children.length) {
                console.log("Child array has different length than childOrder.");
            }

            self.children.sort(function(a, b) {
                return childOrder.indexOf(a.stableId) - childOrder.indexOf(b.stableId);
            });
        };

        Node.prototype.isLegalType = function(nodeType) {
            var self = this;

            if (!nodeType) {
                return false;
            }
            if (isLeaf(nodeType) && self.children.length) {
                return false;
            }
            if (nodeType === 'interpretation' &&
                (self.children.length > 1 || self.children.length && self.children[0].children.length)) {
                return false;
            }
            return true;
        };

        Node.prototype.isPersisted = function() {
            return !!this.body.majorVersion;
        };

        function isLeaf(nodeType) {
            return nodeType === 'source' || nodeType === 'subject';
        }

        return Node;
    }

})();