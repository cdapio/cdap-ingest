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
        factory(window);
    }
}(function (target, require) {
    'use strict';

    var moduleConstructor = function () {
        var connectionInfo = {
                host: 'localhost',
                port: 10000,
                ssl: false,
                user: '',
                pass: ''
            },
            tokenInfo = {
                value: '',
                type: '',
                expirationDate: 0
            },
            httpConnection = null,
            authUrls = null,
            helpers = null,
            AUTH_HEADER_NAME = 'Authorization',
            AUTH_TYPE = 'Basic',
            TOKEN_EXPIRATION_TIMEOUT = 5000;

        if ('undefined' !== typeof window) {
            helpers = CDAPAuthHelpers.Browser;
            httpConnection = new XMLHttpRequest();
        } else {
            helpers = require('./helper-node');
            httpConnection = require('http-sync');
        }

        var getAuthHeaders = helpers.getAuthHeaders.bind(this, AUTH_HEADER_NAME, AUTH_TYPE, connectionInfo),
            baseUrl = function () {
                return [
                    connectionInfo.ssl ? 'https' : 'http',
                    '://', connectionInfo.host,
                    ':', connectionInfo.port, '/'
                ].join('');
            },
            fetchAuthUrl = helpers.fetchAuthUrl.bind(this, httpConnection, baseUrl),
            getAuthUrl = function () {
                if (!authUrls) {
                    return '';
                }

                return authUrls[Math.floor(Math.random() * authUrls.length)];
            },
            isAuthEnabledImpl = function () {
                if (!authUrls) {
                    authUrls = fetchAuthUrl();
                }

                return !!authUrls;
            },
            fetchToken = helpers.fetchTokenInfo.bind(this, getAuthUrl, httpConnection, getAuthHeaders,
                AUTH_HEADER_NAME),
            getTokenImpl = function () {
                if ((TOKEN_EXPIRATION_TIMEOUT >= (tokenInfo.expirationDate - Date.now()))) {
                    tokenInfo = fetchToken();
                }

                return {
                    token: tokenInfo.value,
                    type: tokenInfo.type
                };
            },
            /**
             * @param {Object} properties {
             *   @param {string} username,
             *   @param {password} password
             * }
             */
            configureImpl = function (properties) {
                if (!properties.username || !properties.password) {
                    throw new Error('"username" and "password" are required');
                }

                if (connectionInfo.user && connectionInfo.pass) {
                    throw new Error('Client is already configured!');
                }

                connectionInfo.user = properties.username;
                connectionInfo.pass = properties.password;
            },
            setConnectionInfoImpl = function (host, port, ssl) {
                connectionInfo.host = host;
                connectionInfo.port = port;
                connectionInfo.ssl = ssl;
            };

        return {
            isAuthEnabled: isAuthEnabledImpl,
            getToken: getTokenImpl,
            configure: configureImpl,
            setConnectionInfo: setConnectionInfoImpl
        };
    };

    if (('undefined' !== typeof module) && module.exports) {
        module.exports = moduleConstructor;
    } else {
        target['CASKAuthManager'] = target['CASKAuthManager'] || moduleConstructor;
    }
}));