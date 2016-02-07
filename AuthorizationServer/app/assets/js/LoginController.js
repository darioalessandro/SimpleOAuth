/**
 * Created by darioalessandro on 9/5/15.
 */

function LoginController($scope, $window, error, loginSvc, $state) {

    $scope.errors = {
        login :  null
    };

    $scope.requests = {
        login : null
    };


    $scope.submit = function(username, password) {

        var loginPayload = {
            username : username,
            password : password,
            client_id : $window.client_id,
            scope : $window.scope
        };

        $scope.requests.login = loginSvc.execute(loginPayload,
            function(APISuccess) {
                $scope.errors.login = null;
                window.loginData = APISuccess.data;
                $state.go("loginSuccess");
            },function(APIError) {
                $scope.errors.login = error.apply(APIError.m, function() {
                    return $scope.submit($scope.username, $scope.password);
                });
            }, function(HTTPError) {
                $scope.errors.login = error.apply("Please verify your internet connection", function() {
                    return $scope.submit($scope.username, $scope.password);
                });
        });
    };

    $scope.isLoginWorking = function(){
        return !$scope.isValueInvalid($scope.requests.login) &&
            $scope.requests.login.$$state.status !== 1;
    };

    $scope.isValueInvalid = function(value) {
        return value === undefined || value === null;
    };

}


angular.module("appAuth").controller("LoginController", LoginController);