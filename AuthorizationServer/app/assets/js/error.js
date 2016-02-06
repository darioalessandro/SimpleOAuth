/**
 * Created by darioalessandro on 2/6/16.
 */

angular.module("appAuth").factory('error', function () {
    return {
        apply : function(UIMessage,  onClick){
            return {
                m : UIMessage,
                onClick : onClick
            };
        }
    };
});