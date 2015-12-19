(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('voteButton', [nodeGraph]);

    function nodeGraph() {
        return {
            restrict: "A",
            scope: {
                node: "="
            },
            templateUrl: "partials/vote-button.html",
            link: function (scope) {
                initializeVoteButton(scope);
            }
        }
    }

    function initializeVoteButton(scope) {

    }

})();