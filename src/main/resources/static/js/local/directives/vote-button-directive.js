require('../../../sass/vote-button.scss');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('voteButton', ['$http', 'UserService', voteButton]);

    function voteButton($http, UserService) {
        return {
            restrict: "A",
            scope: {
                node: "="
            },
            templateUrl: "partials/vote-button.html",
            link: function (scope) {

                initializeVoteButton(scope, scope.node, $http, UserService);

                scope.$on("rootData", function(evt, rootNode) {
                   initializeVoteButton(scope, rootNode, $http, UserService);
                });
            }
        }
    }

    function initializeVoteButton(scope, rootNode, $http, UserService) {


        var user = UserService.getUser();
        if (user == null) {
            UserService.subscribeSuccessfulLogin(scope, function() {
                user = UserService.getUser();
                getUserVote();
            });
        } else {
            getUserVote();
        }

        setupMeters();

        function getUserVote() {
            if (user && user.bodyVotes) {
                scope.userVote = user.bodyVotes[rootNode.body.id];
                return scope.userVote;
            }
            return null;
        }

        function setUserVote(voteType) {
            if (user) {
                if (!user.bodyVotes) {
                    user.bodyVotes = {};
                }
                scope.userVote = user.bodyVotes[rootNode.body.id] = voteType ? voteType.toUpperCase() : null;
            }
        }

        function setupMeters() {
            scope.votes = {GREAT: {}, WEAK: {}, TOUCHE: {}, TRASH: {}};

            var max = 0;
            $.each(scope.votes, function (key, val) {
                val.num = rootNode.body[getBodyVotesKey(key)] || 0;  // e.g. body.greatVotes = 5
                if (val.num > max) {
                    max = val.num;
                }
            });

            $.each(scope.votes, function (key, val) {
                val.pct = max > 0 ? val.num * 100 / max : 0;
            });
        }

        function getBodyVotesKey(voteType) {
            return voteType.toLowerCase() + 'Votes';
        }

        scope.voteClicked = function(voteType) {
            if (!user) {
                toastr.error("Must be logged in to vote!");
                return;
            }

            var currentVote = getUserVote();
            if (voteType === currentVote) {
                revokeVote(currentVote);
            } else {
                registerVote(voteType, currentVote);
            }
        };

        function registerVote(voteType, currentVote) {
            $http.post('/voteBody',
                {
                    voteType: voteType,
                    bodyId: rootNode.body.id
                })
                .success(function (data) {
                    if (currentVote) {
                        rootNode.body[getBodyVotesKey(currentVote)]--;
                    }
                    rootNode.body[getBodyVotesKey(voteType)]++;
                    setUserVote(voteType);
                    setupMeters();
                })
                .error(function(err) {
                    toastr.error(err.message);
                });
        }

        function revokeVote(currentVote) {
            $http.post('/unvoteBody',
                {
                    bodyId: rootNode.body.id
                })
                .success(function (data) {
                    rootNode.body[getBodyVotesKey(currentVote)]--;
                    setUserVote(null);
                    setupMeters();
                })
                .error(function(err) {
                    toastr.error(err.message);
                });
        }

    }

})();