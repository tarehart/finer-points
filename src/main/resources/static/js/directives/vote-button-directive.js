require('../../sass/vote-button.scss');
require('../services/toast-service');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('voteButton', voteButton)
        .controller('VoteButtonController', VoteButtonController);

    function voteButton() {
        return {
            restrict: "A",
            scope: {
                node: "="
            },
            templateUrl: "partials/vote-button.html",
            controller: 'VoteButtonController'
        }
    }

    function VoteButtonController($scope, $rootScope, $http, UserService, ToastService) {

        var rootNode = $scope.node;
        var majorVersion = rootNode.body.majorVersion;
        if (rootNode == null) {
            $scope.$on("rootData", function(evt, node) {
                rootNode = node;
                setupMeters();
            });
        } else {
            setupMeters();
        }

        var user = UserService.getUser();
        if (user == null) {
            UserService.subscribeSuccessfulLogin($scope, function() {
                user = UserService.getUser();
                getUserVote();
            });
        } else {
            getUserVote();
        }

        setupMeters();

        function getUserVote() {
            if (user && user.bodyVotes) {
                $scope.userVote = user.bodyVotes[majorVersion.id];
                return $scope.userVote;
            }
            return null;
        }

        function setUserVote(voteType) {
            if (user) {
                if (!user.bodyVotes) {
                    user.bodyVotes = {};
                }
                $scope.userVote = user.bodyVotes[majorVersion.id] = voteType ? voteType.toUpperCase() : null;
            }
        }

        function setupMeters() {
            $scope.votes = {GREAT: {}, WEAK: {}, TOUCHE: {}, TRASH: {}};

            var max = 0;
            var sum = 0;
            $.each($scope.votes, function (key, val) {
                val.num = majorVersion[getBodyVotesKey(key)] || 0;  // e.g. majorVersion.greatVotes = 5
                if (val.num > max) {
                    max = val.num;
                }
                sum += val.num;
            });

            $.each($scope.votes, function (key, val) {
                val.pctMax = max > 0 ? val.num * 100 / max : 25;
                val.pct = sum > 0 ? val.num * 100 / sum : 25;
            });
        }

        function getBodyVotesKey(voteType) {
            return voteType.toLowerCase() + 'Votes';
        }

        $scope.voteClicked = function(voteType) {
            if (!user) {
                ToastService.error("Must be logged in to vote!");
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
                    majorVersionId: majorVersion.id
                })
                .success(function (data) {
                    if (currentVote) {
                        majorVersion[getBodyVotesKey(currentVote)]--;
                    }
                    majorVersion[getBodyVotesKey(voteType)]++;
                    setUserVote(voteType);
                    setupMeters();
                    $rootScope.$broadcast("voteChanged", rootNode);
                })
                .error(function(err) {
                    ToastService.error(err.message);
                });
        }

        function revokeVote(currentVote) {
            $http.post('/unvoteBody',
                {
                    majorVersionId: majorVersion.id
                })
                .success(function (data) {
                    majorVersion[getBodyVotesKey(currentVote)]--;
                    setUserVote(null);
                    setupMeters();
                })
                .error(function(err) {
                    ToastService.error(err.message);
                });
        }

    }

})();