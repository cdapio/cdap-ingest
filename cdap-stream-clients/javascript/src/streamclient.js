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
        window['CDAPStreamClient']['StreamClient'] = window['CDAPStreamClient']['StreamClient'] || {};
        factory(window['CDAPStreamClient']['StreamClient']);
    }
}(function (target, require) {
    'use strict';

    var Utils = ('undefined' !== typeof window) ? CDAPStreamClient.Utils : require('./utils'),
        ServiceConnector = ('undefined' !== typeof window) ? CDAPStreamClient.ServiceConnector :
            require('./serviceconnector'),
        StreamWriter = ('undefined' !== typeof window) ? CDAPStreamClient.StreamWriter :
            require('./streamwriter');

    var GATEWAY_VERSION = '/v2',
        REQUEST_PLACEHOLDERS = {
            StreamID: '<streamid>'
        },
        REQUESTS = {
            BaseStreams: GATEWAY_VERSION + '/streams'
        };
    REQUESTS.Stream = Utils.formRequest(REQUESTS.BaseStreams, REQUEST_PLACEHOLDERS.StreamID);
    REQUESTS.ConsumerID = Utils.formRequest(REQUESTS.Stream, 'consumer-id');
    REQUESTS.Dequeue = Utils.formRequest(REQUESTS.Stream, 'dequeue');
    REQUESTS.Config = Utils.formRequest(REQUESTS.Stream, 'config');
    REQUESTS.Info = Utils.formRequest(REQUESTS.Stream, 'info');
    REQUESTS.Truncate = Utils.formRequest(REQUESTS.Stream, 'truncate');

    var serviceConnector,
        authManager;

    /**
     * Forms URI
     *
     * @param {Object} params {
     *   @param {string} request,
     *   @param {string} [placeholder = 'StreamID'],
     *   @param {string} [data = '']
     * }
     *
     * @returns {string}
     */
    var prepareUri = function prepareUri(params) {
            params.placeholder = params.placeholder || 'StreamID';
            params.data = params.data || '';

            return REQUESTS[params.request].replace(REQUEST_PLACEHOLDERS[params.placeholder], params.data);
        },
        checkErrorResponse = function checkErrorResponse(response) {
            var status = ('status' in response) ? response.status : response.statusCode,
                responseText = ('responseText' in response) ? response.responseText : response.body.toString();

            if (200 !== status) {
                throw {
                    status: status,
                    message: responseText,
                    toString: function () {
                        return [
                            'Error code: ', this.status, ' ',
                            'Error message: ', this.message
                        ].join('');
                    }
                };
            }

            return response;
        };

    /**
     * @constructor
     *
     * @param {Object} config {
     *   @param {string} host,
     *   @param {number} port,
     *   @param {boolean} [ssl=false],
     *   @param {@link CDAPAuthManager} [authManager=null]
     * }
     */
    var StreamClient = function StreamClient(config) {
        if (!config.host || !config.port) {
            throw new Error('"host" and "port" fields are required');
        }
        config.ssl = (null != config.ssl) ? config.ssl : false;

        authManager = config.authManager || null;
        serviceConnector = new ServiceConnector(config);

        /**
         * Creates a stream with the given name.
         *
         * @param {string} stream          -- stream name to create
         */
        var createImpl = function createImpl(stream) {
                var uri = prepareUri({
                    request: 'Stream',
                    data: stream
                });

                return serviceConnector.request({
                    method: 'PUT',
                    path: uri,
                    async: true
                });
            },
            /**
             * Set the Time-To-Live (TTL) property of the given stream.
             *
             * @param {string} stream       - stream name to create or to retrieve
             * @param {number} ttl          - Time-To-Live in seconds
             */
            setTTLImpl = function setTTLImpl(stream, ttl) {
                var uri = prepareUri({
                        request: 'Config',
                        data: stream
                    }),
                    objectToSend = {
                        ttl: ttl
                    };


                return serviceConnector.request({
                    method: 'PUT',
                    path: uri,
                    data: JSON.stringify(objectToSend),
                    async: true
                });
            },
            /**
             * Retrieves the Time-To-Live (TTL) property of the given stream.
             *
             * @param {string} stream           - stream name to retrieve ttl for
             *
             * @returns {number}                - Time-To-Live property in seconds
             */
            getTTLImpl = function getTTLImpl(stream) {
                var uri = prepareUri({
                    request: 'Info',
                    data: stream
                });

                return serviceConnector.request({
                    method: 'GET',
                    path: uri,
                    async: true
                });
            },
            /**
             * Truncates all existing events in the give stream.
             *
             * @param {string} stream               - stream name to truncate
             */
            truncateImpl = function truncateImpl(stream) {
                var uri = prepareUri({
                    request: 'Truncate',
                    data: stream
                });

                return serviceConnector.request({
                    method: 'POST',
                    path: uri,
                    async: false
                });
            },
            /**
             * Creates a {@link CDAPStreamClient.StreamWriter} instance for writing events to the given stream.
             *
             * @params {string} stream              - stream name to get StreamWrite instance for
             *
             * @returns {CDAPStreamClient.StreamWriter}
             */
            createWriterImpl = function createWriterImpl(stream) {
                /**
                 * A bit ugly, but effective method to check if stream exists.
                 * The main idea is there is could not be presented info for
                 * invalid stream.
                 */
                getTTLImpl.apply(this, [stream]);

                var uri = prepareUri({
                    request: 'Stream',
                    data: stream
                });

                return new StreamWriter(serviceConnector, uri);
            };

        return {
            create: createImpl,
            setTTL: setTTLImpl,
            getTTL: getTTLImpl,
            truncate: truncateImpl,
            createWriter: createWriterImpl
        };
    };

    if (('undefined' !== typeof module) && module.exports) {
        module.exports = StreamClient;
    } else {
        window['CDAPStreamClient']['StreamClient'] = StreamClient;
    }
}));
