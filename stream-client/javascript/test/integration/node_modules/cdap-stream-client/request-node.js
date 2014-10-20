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

var Fs = require('fs'),
    Utils = require('./utils'),
    Promise = require('./promise');

var requestAsync = function requestAsync(params) {
        var connection = params.ssl ? require('https') : require('http');

        if (!params.method || !params.path || !params.host || !params.port) {
            throw new Error('"host", "port", "method", "path" properties are required');
        }

        params.ssl = (null != params.ssl) ? params.ssl : false;
        params.headers = params.headers || {};
        params.data = JSON.stringify(params.data || {});
        params.rejectUnauthorized = false;

        var promise = new Promise(),
            request = connection.request(params, function responseHandler(response) {
                response.setEncoding('utf-8');

                promise.notify('Request status: ' + response.statusCode);

                if (200 === response.statusCode) {
                    response.on('data', function (chunk) {
                        promise.resolve(chunk);
                    });
                    response.on('end', function () {
                        promise.resolve(response.statusCode);
                    });
                } else {
                    promise.reject(response.statusCode);
                }
            });

        request.write(params.data);
        request.end();

        return promise;
    },
    requestSync = function requestSync(params) {
        if (!params.method || !params.path || !params.host || !params.port) {
            throw new Error('"host", "port", "method", "path" properties are required');
        }

        var httpSync = require('http-sync');

        params.ssl = (null != params.ssl) ? params.ssl : false;
        params.headers = params.headers || {};
        params.data = JSON.stringify(params.data || {});

        var request = httpSync.request(params),
            response;

        request.write(params.data);
        response = request.end();

        return {
            status: response.statusCode,
            responseText: response.body.toString()
        };
    };

module.exports = {
    /**
     * Performs http request to CDAP Gateway
     *
     * @param {Object} params {
     *   @param {string} host                       - Hostname of the CDAP Gateway.
     *   @param {number} port                       - Port number CDAP Gateway listen to.
     *   @param {string} method                     - HTTP method to be used(GET, POST, etc.).
     *   @param {string} path                       - URL to send request to.
     *   @param {boolean} [ssl = false]             - True if connection has to be secured.
     *   @param {boolean} [async = true]            - Request is asynchronous.
     *   @param {object} [headers = {}]             - HTTP headers to send within a request.
     *                                                  { Authorization: 'Basic alsdh8asdna03inrd' }
     *   @param {object} [data = {}]                - Data to send within a request.
     * }
     *
     * @returns {@link CDAPStreamClient.Promise}
     */
    request: function request(params) {
        params.async = (null != params.async) ? params.async : true;

        if (params.async) {
            return requestAsync(params);
        } else {
            return requestSync(params);
        }
    },
    /**
     * Performs post request to CDAP Gateway to send files.
     *
     * @param {Object} params {
     *   @param {string} host                       - Hostname of the CDAP Gateway.
     *   @param {number} port                       - Port number CDAP Gateway listen to.
     *   @param {string} path                       - URL to send request to.
     *   @param {boolean} [ssl = false]             - True if connection has to be secured.
     *   @param {object} [headers = {}]             - HTTP headers to send within a request.
     *                                                  { Authorization: 'Basic alsdh8asdna03inrd' }
     *   @param {string|[string]} [files = null]    - Name of a file or files` names list to be sent within a request.
     * }
     *
     * @returns {@link CDAPStreamClient.Promise}
     */
    send: function send(params) {
        var connection = params.ssl ? require('https') : require('http');

        if (!params.path || !params.host || !params.port) {
            throw new Error('"host", "port", "path" properties are required');
        }

        if ('string' !== typeof params.files || !(params.files instanceof Array) || !(params.files instanceof FormData)) {
            throw new Error('"files" property has to be one of the types: String, Array, FormData');
        }

        params.method = 'POST';
        params.ssl = (null != params.ssl) ? params.ssl : false;
        params.headers = params.headers || {};

        var promise = new Promise(),
            filesToSend = [],
            request = connection.request(params, function responseHandler(response) {
                response.setEncoding('utf-8');

                promise.notify('Request status: ' + response.statusCode);

                if (200 === response.statusCode) {
                    response.on('data', function (content) {
                        promise.resolve(content);
                    });
                } else {
                    promise.reject(response.statusCode);
                }
            });

        filesToSend = filesToSend.concat(params.files);

        while (filesToSend.length) {
            Fs.createReadStream(filesToSend.shift()).pipe(request);
        }
        request.end();

        return promise;
    }
};