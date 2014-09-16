/**
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


(function (factory) {
    'use strict';

    // Support three module loading scenarios
    if (typeof require === 'function' && typeof exports === 'object' && typeof module === 'object') {
        // [1] CommonJS/Node.js
        var target = module['exports'] || exports; // module.exports is for Node.js
        factory(target, require);
    } else if (typeof define === 'function' && define['amd']) {
        // [2] AMD anonymous module
        define(['exports', 'Promise'], factory);
    } else {
        // [3] No module loader (plain <script> tag) - put directly in global namespace
        window['CDAPTracker'] = window['CDAPTracker'] || {};
        factory(window['CDAPTracker']);
    }
}(function (target, require) {
    'use strict';

    /**
     * @constructor
     *
     * @param {string} url                  - url to request from a server
     * @param {string} [host='localhost']   - hostname of a server we are going to connect to.
     * @param {number} [port=10000]         - port number of a service at the server we are going to connect to.
     * @param {boolean} [ssl=false]         - should be connection secured or not (true / false)
     */
    target['ServiceConnector'] = target['ServiceConnector'] || function (url, host, port, ssl) {
        if(null == url || 'string' !== typeof url) {
            throw TypeError('"url" parameter have to be of type "string"');
        }

        var server = {
            path: url,
            hostname: host ? host : 'localhost',
            port: port ? port : 10000,
            ssl: ssl ? ssl : false
        };

        /**
         * @param {object} requestParams
         * Possible fields for requestParams:
         *
         * @param {object} data         - data to send to a server
         *
         * @returns {CDAPTracker.Promise}
         */
        var requestBrowser = function (requestParams) {
            var httpCon = new XMLHttpRequest(),
            promise = target['Promise'](),
            request_url = '' + (server.ssl ? 'https' : 'http') + '://'
                + server.hostname + ':' + server.port + server.path;

            httpCon.onreadystatechange = function (response) {
                var readyStates = [
                    'Request not initialized',
                    'Server connection established',
                    'Request received',
                    'Processing request',
                    'Request finished and response is ready'
                ];

                promise.notify(readyStates[httpCon.readyState]);

                if (httpCon.readyState === 4) {
                    if (httpCon.status === 200) {
                        promise.resolve(httpCon.responseText);
                    } else {
                        promise.reject(httpCon.status);
                    }
                }
            };

            httpCon.open('POST', request_url, true);
            httpCon.send(requestParams.data);

            return promise;
        },

        requestNode = function (requestParams) {
            requestParams.method = 'POST';

            var httpCon = server.ssl ? https : http,
            promiseClass = require('promise.js'),
            promise = new promiseClass(),
            request = httpCon.request(Object.create(server, requestParams), function (response) {
                response.setEncoding('utf-8');

                promise.notify('HTTP status: ' + response.statusCode);

                if (200 !== response.statusCode) {
                    promise.reject(response.statusCode);
                } else {
                    response.on('data', function (content) {
                        promise.resolve(content);
                    });
                }
            }
                                     );

            request.write(requestParams.data);
            request.end();

            return promise;
        },

        request = window ? requestBrowser : requestNode,

        trackImpl = function (data) {
            if (!(data instanceof Object)) {
                throw TypeError('"data" parameter has to be of type "Object"');
            }

            return request({
                data: JSON.stringify(data)
            });
        };

        return {
            track: trackImpl
        };
    };
}));
