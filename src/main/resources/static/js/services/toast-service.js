require('../../sass/toast.scss');

(function () {
    'use strict';

    angular
        .module('nodeStandControllers')
        .service('ToastService', ToastService)
        .controller('ToastController', ToastController);

    function ToastService($mdToast) {
        var self = this;

        self.success = function(message) {
            toast(message, 'success');
        };

        self.error = function (message) {
            toast(message, 'error');
        };
            
        function toast(message, type) {
            $mdToast.show({
                templateUrl: 'partials/toast-template.html',
                hideDelay: type === 'error' ? 10000 : 800,
                position: 'bottom',
                locals: {message: message, type: type},
                bindToController: true,
                controller: 'ToastController',
                controllerAs: 'toastCtrl'
            });
        }
    }

    function ToastController($scope) {

    }

})();