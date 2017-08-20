(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('SettingsController', SettingsController);

    function SettingsController($auth, SatellizerConfig, UserService) {

        var self = this;

        self.enrollEmail = function() {

            SatellizerConfig.providers['google'].scope = ['email'];

            $auth.authenticate('google')
                .then(function(response) {
                    UserService.refreshUser();
                })
                .catch(function(response) {
                    ToastService.error(response);
                });
        }
    }

})();