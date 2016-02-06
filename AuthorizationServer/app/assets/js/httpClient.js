

function httpClient($http, $log) {
    var _httpWithConfig = function (config, APISuccess_callback, APIError_callback, HTTPError_callback) {
        try {
            if (config.data === undefined || config.data === null) {
                throw ("Can't create an httpClient request with empty data, you might use an empty object '{}'.");
            }

            [APISuccess_callback, APIError_callback, HTTPError_callback].forEach(function (callback, i) {
                if (callback === undefined || callback === null) {
                    throw ("Can't create an httpClient request with empty callbacks.");
                }
            });
        } catch (e) {
            $log.error(e);
            console.error(e);
        }
        return $http(config).success(function (r) {
            if (r.error === undefined) {
                APISuccess_callback(r);
            } else {
                APIError_callback(r.error);
            }
        }).error(function (e) {
            if (e === null || e === undefined) {
                e = {status: 500};
            }
            HTTPError_callback(e);
        });
    };

    return {
        putForm : function(form, url, APISuccess, APIError, HTTPError){
           return _httpWithConfig({
           method: 'PUT',
           url: url,
           data: form,
           headers: {'Content-Type': 'application/x-www-form-urlencoded'}
           },APISuccess, APIError, HTTPError);
        },
        upload : function(file, keyValueForm, url, APISuccess_callback, APIProgress_callback, APIError_callback, HTTPError_Callback) {
            return {};
        },
        get: function (url, data, APISuccess_callback, APIError_callback, HTTPError_callback) {
            return _httpWithConfig({
                method: "GET",
                url: url,
                data: data
            }, APISuccess_callback, APIError_callback, HTTPError_callback);
        },
        post: function (url, data, APISuccess_callback, APIError_callback, HTTPError_callback) {
            return _httpWithConfig({
                method: "POST",
                url: url,
                data: data
            }, APISuccess_callback, APIError_callback, HTTPError_callback);
        },
        put: function (url, data, APISuccess_callback, APIError_callback, HTTPError_callback) {
            return _httpWithConfig({
                method: "PUT",
                url: url,
                data: data
            }, APISuccess_callback, APIError_callback, HTTPError_callback);
        }
    };
}

angular.module("appAuth").factory('httpClient', httpClient);