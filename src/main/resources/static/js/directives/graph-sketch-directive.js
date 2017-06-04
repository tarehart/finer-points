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

    function sketchController($scope, Node) {
        var self = this;
        self.edgeToolActive = false;

        var SEEKING_PARENT = 1;
        var SEEKING_CHILD = 2;

        var sketchNum = 0;
        
        var edgeState = null;
        var edgeParent = null;

        self.addNode = function() {
            var node = new Node();
            node.id = "sketch-" + sketchNum++;
            $scope.$broadcast("nodeAdded", null, node);
        };

        self.edgeToolToggled = function() {
            if (self.edgeToolActive) {
                self.enterEdgeMode();
            } else {
                self.exitEdgeMode();
            }
        };

        self.enterEdgeMode = function() {
            self.edgeToolActive = true;
            edgeState = SEEKING_PARENT;
            edgeParent = null;
        };
        
        self.exitEdgeMode = function () {
            self.edgeToolActive = false;
            edgeState = null;
            edgeParent = null;
        };
        
        self.isSeekingParent = function() {
            return edgeState === SEEKING_PARENT;
        };

        self.isSeekingChild = function() {
            return edgeState === SEEKING_CHILD;
        };

        $scope.$on("nodeTapped", function(event, node) {
            if (edgeState === SEEKING_PARENT) {
                edgeParent = node;
                edgeState = SEEKING_CHILD;
                return;
            }
            
            if (edgeState === SEEKING_CHILD) {
                if (!edgeParent) {
                    // Should never happen.
                    self.exitEdgeMode();
                }
                if (node.id === edgeParent.id) {
                    // Can't make a self-referencing edge. Do nothing.
                    return;                    
                }
                
                $scope.$broadcast("edgeAdded", edgeParent, node);
                edgeState = SEEKING_PARENT;
            }
        });

    }

})();