/**
 * Created by darioalessandro on 9/3/15.
 */

angular.module('bfa', ['ui.router', 'ui.bootstrap'])
    .config(function($locationProvider){
        //$locationProvider.html5Mode(true).hashPrefix('!');
    })
    .config(function($stateProvider, $urlRouterProvider) {

    $urlRouterProvider.otherwise("/resume");

    $stateProvider
        .state('resume', {
            url: "/resume",
            templateUrl: "resume",
            controller : "ResumeController"
        })
        .state('portfolio', {
            url: "/portfolio",
            templateUrl: "portfolio"
        })
        .state('blog', {
            url: "/blog",
            templateUrl: "blog"
        });
});