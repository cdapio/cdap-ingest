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
        window['CDAPStreamClient'] = window['CDAPStreamClient'] || { ServiceConnector: null };
        factory(window['CDAPStreamClient']['ServiceConnector']);
    }
}(function (target, require) {
    'use strict';

    var Utils = ('undefined' !== typeof window) ? CDAPStreamClient.Utils : require('./utils');

    /**
     * @constructor
     *
     * @param {Object} config {
     *   @param {string} [host='localhost']     - hostname of a server we are going to connect to.
     *   @param {number} [port=10000]           - port number of a service at the server we are going to connect to.
     *   @param {boolean} [ssl=false]           - should be connection secured or not (true / false)
     *   @param {@link CDAPAuthManager} [authManager=null]
     * }
     */
    var ServiceConnector = function ServiceConnector(config) {
        config = config || {};

        var defaultHeaders = {
                Authorization: ''
            },
            connectionInfo = {
                host: config.host ? config.host : 'localhost',
                port: config.port ? config.port : 10000,
                ssl: (null != config.ssl) ? config.ssl : false
            },
            authenticationManager = config.authManager || null,
            platformSpecific = ('undefined' !== typeof window) ? CDAPStreamClient
                : require('./request-node');

        /**
         * @param {Object} config {
         *   @param {string} method
         *   @param {string} path
         *   @param {*} [body = null]
         *   @param {Object} [headers = null]
         *   @param {boolean} [async = true]
         * }
         *
         * @returns {@link CDAPStreamClient.Promise}
         */
        var requestImpl = function requestImpl(config) {
                if (authenticationManager) {
                    if (authenticationManager.isAuthEnabled()) {
                        var token = authenticationManager.getToken();
                        defaultHeaders.Authorization = [token.type, token.token].join(' ');
                    }
                }

                var config = Utils.copyObject(connectionInfo, {
                    method: config.method,
                    path: config.path,
                    headers: Utils.copyObject(defaultHeaders, config.headers ? config.headers : {}),
                    async: (null != config.async) ? config.async : true
                });

                return platformSpecific.request(config);
            },

            /**
             * @param {string} uri,
             * @param {FormData|string|[string]} file,            - Browser: FormData.
             *                                                      NodeJS: string|[string]
             * @param {Object} headers
             *
             * @returns {@link CDAPStreamClient.Promise}
             */
            sendImpl = function sendImpl(uri, file, headers) {
                if (authenticationManager) {
                    if (authenticationManager.isAuthEnabled()) {
                        var token = authenticationManager.getToken();
                        defaultHeaders.Authorization = [token.type, token.token].join(' ');
                    }
                }

                var config = Utils.copyObject(connectionInfo, {
                    method: 'POST',
                    path: uri,
                    headers: Utils.copyObject(defaultHeaders, headers ? headers : {}),
                    filename: file
                });

                return platformSpecific.send(config);
            },
            objectToReturn = {
                request: requestImpl
            };

        var isFormDataSupported = false;

        try {
            isFormDataSupported = 'FormData' in window;
        } catch(e) {
            // Nothig to do
        }

        if ('undefined' === typeof window || isFormDataSupported) {
            objectToReturn.send = sendImpl;
        }

        return objectToReturn;
    };

    if (('undefined' !== typeof module) && module.exports) {
        module.exports = ServiceConnector;
    } else {
        target = target || ServiceConnector;
    }
}));
