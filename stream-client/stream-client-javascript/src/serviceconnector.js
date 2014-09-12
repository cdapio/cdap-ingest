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
     * @param {string} authKey              - server authorization key
     * @param {string} [host='localhost']   - hostname of a server we are going to connect to.
     * @param {number} [port=10000]         - port number of a service at the server we are going to connect to.
     * @param {boolean} [ssl=false]         - should be connection secured or not (true / false)
     */
    target['ServiceConnector'] = target['ServiceConnector'] || function (url, authKey, host, port, ssl) {
        var server = {
                path: url,
                hostname: host ? host : 'localhost',
                port: port ? port : 10000,
                ssl: ssl ? ssl : false
            },
            authorizationKey = authKey;

        /**
         * @param {object} requestParams
         * Possible fields for requestParams:
         *
         * @param {object} data         - data to send to a server
         *
         * @returns {CDAPTracker.Promise}
         */
        var request = window ? requestBrowser : requestNode,

            /**
             * @param {object) requestParams
             * See 'request' for details
             *
             * @returns {CDAPTracker.Promise}
             */
            requestBrowser = function (requestParams) {
                var httpCon = XMLHttpRequest(),
                    promise = target['Promise'](),
                    request_url = '' + (server.ssl ? 'https' : 'http') + '://'
                        + server.hostname + ':' + server.port + '/' + requestParams.path;

                httpCon.onreadystatechange = function (response) {
                    var readyStates = [
                        'Request not initialized',
                        'Server connection established',
                        'Request received',
                        'Processing request',
                        'Request finished and response is ready'
                    ];

                    promise.notify(readyStates[response.readyState]);

                    if (response.readyState === 4) {
                        if (response.status === 200) {
                            promise.resolve(httpCon.responseText);
                        } else {
                            promise.reject(response.status);
                        }
                    }
                };

                httpCon.open('POST', request_url, true);
                httpCon.setRequestHeader('Authorization', 'Bearer ' + authorizationKey);
                httpCon.send(requestParams.data);

                return promise;
            },

            /**
             * @param {object) requestParams
             * See 'request' for details
             *
             * @returns {CDAPTracker.Promise}
             */
            requestNode = function (requestParams) {
                requestParams.method = 'POST',
                requestParams.headers = {
                    'Authorization': 'Bearer ' + authorizationKey
                };

                var httpCon = server.ssl ? https : http,
                    promiseClass = require('promise.js'),
                    promise = new promiseClass(),
                    request = httpCon.request(Object.create(server, requestParams), function (response) {
                            response.setEncoding('utf-8');

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