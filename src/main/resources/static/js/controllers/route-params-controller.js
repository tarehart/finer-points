(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('RouteParamsController', RouteParamsController);

    /* This thing is just here to make route params available when we have a super generic view that needs nothing else. */
    function RouteParamsController($routeParams) {

        var self = this;
        self.routeParams = $routeParams;
    }

})();