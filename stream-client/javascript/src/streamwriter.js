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
        window['CDAPStreamClient'] = window['CDAPStreamClient'] || {};
        window['CDAPStreamClient']['StreamWriter'] = window['CDAPStreamClient']['StreamWriter'] || {};
        factory(window['CDAPStreamClient']['StreamWriter']);
    }
}(function (target, require) {
    'use strict';

    /**
     * @constructor
     *
     * @param {CDAPStreamClient.ServiceConnector} serviceConnector       - reference to connection pool to communicate
     *                                                                     with gateway server.
     * @param {string} uri          - REST URL part to perform request.
     */
    var StreamWriter = function StreamWriter(serviceConnector, uri) {
        if (!serviceConnector || !uri) {
            throw new Error('params are required');
        }

        var connector = serviceConnector,
            serviceUri = uri;

        /**
         * Ingest a stream event with a string as body.
         *
         * @param {Object} data {
         *   @param {string} message,
         *   @param {Object} [headers = {}]
         * }
         */
        var writeImpl = function writeImpl(message, headers) {
            if (!message) {
                throw new Error('"message" is required');
            }

            headers = headers || {};

            return connector.request({
                method: 'POST',
                path: serviceUri,
                data: JSON.stringify(message),
                headers: headers
            });
        };

        return {
            write: writeImpl
        };
    };

    if (('undefined' !== typeof module) && module.exports) {
        module.exports = StreamWriter;
    } else {
        window['CDAPStreamClient']['StreamWriter'] = StreamWriter;
    }
}));
