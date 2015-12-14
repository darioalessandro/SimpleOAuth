/**
 * Created by darioalessandro on 9/5/15.
 */

function ResumeController($scope) {
    $scope.status = {
        isFirstOpen: true,
        isFirstDisabled: false
    };
}

angular.module("bfa").controller("ResumeController", ResumeController);