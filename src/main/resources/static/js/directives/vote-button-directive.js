(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('voteButton', ['$http', voteButton]);

    function voteButton($http) {
        return {
            restrict: "A",
            scope: {
                node: "="
            },
            templateUrl: "partials/vote-button.html",
            link: function (scope) {

                initializeVoteButton(scope, scope.node, $http);

                scope.$on("rootData", function(evt, rootNode) {
                   initializeVoteButton(scope, rootNode, $http);
                });
            }
        }
    }

    function initializeVoteButton(scope, rootNode, $http) {

        setupMeters();

        function setupMeters() {
            scope.votes = {great: {}, weak: {}, touche: {}, trash: {}};

            var max = 0;
            $.each(scope.votes, function (key, val) {
                val.num = rootNode.body[key + 'Votes'] || 0;  // e.g. body.greatVotes = 5
                if (val.num > max) {
                    max = val.num;
                }
            });

            $.each(scope.votes, function (key, val) {
                val.pct = max > 0 ? val.num * 100 / max : 0;
            });
        }


        scope.voteClicked = function(voteType) {
            if (voteType === rootNode.body.currentUserVote) {
                revokeVote();
            } else {
                registerVote(voteType);
            }
        };

        function registerVote(voteType) {
            $http.post('/voteBody',
                {
                    voteType: voteType,
                    bodyId: rootNode.body.id
                })
                .success(function (data) {
                    if (rootNode.body.currentUserVote) {
                        rootNode.body[rootNode.body.currentUserVote + 'Votes']--;
                    }
                    rootNode.body[voteType + 'Votes']++;
                    rootNode.body.currentUserVote = voteType;
                    setupMeters();
                })
                .error(function(err) {
                    toastr.error(err.message);
                });
        }

        function revokeVote() {
            $http.post('/unvoteBody',
                {
                    bodyId: rootNode.body.id
                })
                .success(function (data) {
                    rootNode.body[rootNode.body.currentUserVote + 'Votes']--;
                    rootNode.body.currentUserVote = null;
                    setupMeters();
                })
                .error(function(err) {
                    toastr.error(err.message);
                });
        }

    }

})();