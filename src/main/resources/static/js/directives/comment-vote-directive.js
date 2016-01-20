(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('commentVoteButton', ['$http', 'UserService', voteButton]);

    function voteButton($http, UserService) {
        return {
            restrict: "A",
            scope: {
                comment: "="
            },
            templateUrl: "partials/comment-vote-button.html",
            link: function (scope) {

                initializeVoteButton(scope, scope.comment, $http, UserService);
            }
        }
    }

    function initializeVoteButton(scope, comment, $http, UserService) {


        var user = UserService.getLoggedInUser();
        if (user == null) {
            UserService.subscribeSuccessfulLogin(scope, function() {
                user = UserService.getLoggedInUser();
                getUserVote();
            });
        } else {
            getUserVote();
        }

        function getUserVote() {
            if (user && user.bodyVotes) {
                scope.userVote = user.commentVotes[comment.id];
                return scope.userVote;
            }
            return null;
        }

        function setUserVote(vote) {
            if (user) {
                if (!user.commentVotes) {
                    user.commentVotes = {};
                }
                scope.userVote = user.commentVotes[comment.id] = vote;
            }
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

        function registerVote(voteDelta, currentVote) {
            $http.post('/voteComment',
                {
                    commentId: comment.id,
                    isUpvote: voteDelta > 1
                })
                .success(function (data) {
                    if (currentVote) {
                        // User must be reversing their vote, so there's a two point swing.
                        comment.score += voteDelta * 2;
                    } else {
                        comment.score += voteDelta;
                    }
                    setUserVote(voteDelta);
                })
                .error(function(err) {
                    toastr.error(err.message);
                });
        }

        function revokeVote(currentVote) {
            $http.post('/unvoteComment',
                {
                    commentId: comment.id
                })
                .success(function (data) {
                    comment.score -= currentVote;
                    setUserVote(null);
                })
                .error(function(err) {
                    toastr.error(err.message);
                });
        }

    }

})();