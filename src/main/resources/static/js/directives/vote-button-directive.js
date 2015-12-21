(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('voteButton', [voteButton]);

    function voteButton() {
        return {
            restrict: "A",
            scope: {
                node: "="
            },
            templateUrl: "partials/vote-button.html",
            link: function (scope) {

                initializeVoteButton(scope, scope.node);

                scope.$on("rootData", function(evt, rootNode) {
                   initializeVoteButton(scope, rootNode);
                });
            }
        }
    }

    function initializeVoteButton(scope, rootNode) {

        // TODO: remove. This is test data, in the future the node will already have it.
        rootNode.votes = {great: 45, weak: 5, touche: 20, trash: 2};

        scope.votes = {great: {}, weak: {}, touche: {}, trash: {}};

        var max = 0;
        $.each(scope.votes, function(key, val) {
            val.num = rootNode.votes[key];
            if (val.num > max) {
                max = val.num;
            }
        });

        $.each(scope.votes, function(key, val) {
            val.pct = max > 0 ?  val.num * 100 / max : 0;
        });

    }

})();