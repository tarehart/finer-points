
(function () {
    'use strict';

    angular
        .module('nodeStandControllers')
        .service('TokenStorage', TokenStorage);

    function TokenStorage() {
        var self = this;
        var AUTH_TOKEN_KEY = "finerPointsAuthToken";


        self.store = function(token) {
            window.localStorage.setItem(AUTH_TOKEN_KEY, token);
        };

        self.retrieve = function() {
            return window.localStorage.getItem(AUTH_TOKEN_KEY);
        };

        self.clear = function() {
            window.localStorage.removeItem(AUTH_TOKEN_KEY);
        };
    }

})();