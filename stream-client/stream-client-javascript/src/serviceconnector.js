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
     *   @param {string} [host='localhost']   - hostname of a server we are going to connect to.
     *   @param {number} [port=10000]         - port number of a service at the server we are going to connect to.
     *   @param {boolean} [ssl=false]         - should be connection secured or not (true / false)
     * }
     */
    target = target || function ServiceConnector(config) {
        var defaultHeaders = {
                Authorization: ''
            },
            connectionInfo = {
                host: config.host ? config.host : 'localhost',
                port: config.port ? config.port : 10000,
                ssl: (null != config.ssl) ? config.ssl : false
            },
            platformSpecificRequest = ('undefined' !== typeof window) ? CDAPStreamClient.request
                : require('./request-node');

        /**
         * @param {string} method
         * @param {string} uri
         * @param {any} [body = null]
         * @param {Object} [headers = null]
         *
         * @returns {@link CDAPStreamClient.Promise}
         */
        var requestImpl = function requestImpl(method, uri, body, headers) {
                var config = Utils.copyObject(connectionInfo, {
                    method: method,
                    path: uri,
                    headers: Utils.copyObject(defaultHeaders, headers ? headers : {})
                });

                return platformSpecificRequest(config);
            },

            /**
             * This method is supported on NodeJS _only_.
             *
             * @param {string} uri,
             * @param {string} file,
             * @param {Object} headers
             *
             * @returns {@link CDAPStreamClient.Promise}
             */
            sendImpl = function sendImpl(uri, file, headers) {
                var config = Utils.copyObject(connectionInfo, {
                    method: 'POST',
                    path: uri,
                    headers: Utils.copyObject(defaultHeaders, headers ? headers : {}),
                    filename: file
                });
            },
            objectToReturn = {
                request: requestImpl
            };

        if('undefined' === typeof window) {
            objectToReturn.send = sendImpl;
        }

        return objectToReturn;
    };
}));
