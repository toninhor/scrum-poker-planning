var loginControllers = angular.module('loginControllers', []);

loginControllers.controller('loginCtrl', ['$scope', '$location', '$sessionStorage', '$log', 'DATA', 'Services',
    function ($scope, $location, $sessionStorage, $log, DATA, Services) {
        function init() {
            $log.info("username:" + $sessionStorage.username);
            $log.info("sessionId:" + $sessionStorage.sessionId);

            if ($sessionStorage.username && $sessionStorage.sessionId) {
                $location.path('/home/' + $sessionStorage.sessionId);
            }
        }

        $scope.connect = function () {
            Services.send("connect", function (payload, headers, res) {
                if (payload.statusCode == "OK") {
                    $log.info("ws send!!");
                    $log.info(payload);
                    $sessionStorage.username = $scope.username;
                    $sessionStorage.sessionId = $scope.sessionId;
                    $sessionStorage.isAdmin = payload.body.isAdmin;
                    $sessionStorage.color = payload.body.color;
                    $location.path('/home/' + $sessionStorage.sessionId);
                    $scope.$apply();
                } else {
                    $log.info("problem in connection");
                }
            }, {
                username: $scope.username,
                sessionId: $scope.sessionId
            });
        };

        $scope.generateIdSession = function () {
            var hash = "";
            var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

            for (var i = 0; i < 10; i++) {
                hash += possible.charAt(Math.floor(Math.random() * possible.length));
            }

            $scope.sessionId = hash;
        };

        init();
    }]);

loginControllers.resolve = {
    ws: ['Services', '$q', function (Services, $q) {
        var delay = $q.defer();
        Services.connect();
        delay.resolve();
        return delay.promise;
    }]
};

