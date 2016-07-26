require("./token-storage");

// http://blog.jdriven.com/2014/10/stateless-spring-security-part-2-stateless-authentication/
(function () {
    'use strict';

    angular
        .module('nodeStandControllers')
        .factory('TokenAuthInterceptor', TokenAuthInterceptor)
        .config(function($httpProvider) {
            $httpProvider.interceptors.push('TokenAuthInterceptor');
        });

    function TokenAuthInterceptor($q, TokenStorage) {
        return {
            request: function(config) {
                var authToken = TokenStorage.retrieve();
                if (authToken) {
                    config.headers['X-AUTH-TOKEN'] = authToken;
                }
                return config;
            },
            responseError: function(error) {
                return $q.reject(error);
            }
        };
    }

})();