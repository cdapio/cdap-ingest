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

window.CDAPStreamClient = window.CDAPStreamClient || {};

/**
 * Performs http request to CDAP Gateway
 *
 * @param {Object} params {
 *   @param {string} host                       - Hostname of the CDAP Gateway.
 *   @param {number} port                       - Port number CDAP Gateway listen to.
 *   @param {string} method                     - HTTP method to be used(GET, POST, etc.).
 *   @param {string} path                       - URL to send request to.
 *   @param {boolean} [ssl = false]             - True if connection has to be secured.
 *   @param {object} [headers = {}]             - HTTP headers to send within a request.
 *                                                  { Authorization: 'Basic alsdh8asdna03inrd' }
 *   @param {object} [data = {}]                - Data to send within a request.
 * }
 *
 * @returns {@link CDAPStreamClient.Promise}
 */

window.CDAPStreamClient = window.CDAPStreamClient || {
    request: function request() {
        if (!params.method || !params.path || !params.host || !params.port) {
            throw Error('"host", "port", "method", "path" properties are required');
        }

        params.ssl = (null != params.ssl) ? params.ssl : false;
        params.headers = params.headers || {};
        params.data = params.data || {};

        var connection = new XMLHttpRequest(),
            Promise = CDAPStreamClient.Promise,
            Utils = CDAPStreamClient.Utils,
            promiseInst = new Promise(),
            requestUrl = Utils.baseUrl(params.host, params.port, params.ssl) + params.path;

        connection.onreadystatechange = function responseHandler(response) {
            var readyStates = [
                'Request not initialized',
                'Server connection established',
                'Request received',
                'Processing request',
                'Request finished and response is ready'
            ];

            promiseInst.notify(readyStates[connection.readyState]);

            if (XMLHttpRequest.DONE === connection.readyState) {
                if (200 === connection.status) {
                    promiseInst.resolve(connection.responseText);
                } else {
                    promiseInst.reject(connection.status);
                }
            }
        };

        connection.open(params.method, requestUrl, true);
        connection.send(params.data);

        return promiseInst;
    }
};

if (window.FormData) {
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
     *   @param {FormData} [files = null]           - List of files to be sent within a request.
     * }
     *
     * @returns {@link CDAPStreamClient.Promise}
     */
    window.CDAPStreamClient.send = function send(params) {
        if (!params.path || !params.host || !params.port) {
            throw new Error('"host", "port", "path" properties are required');
        }

        if ('string' !== typeof params.files || !(params.files instanceof Array) || !(params.files instanceof FormData)) {
            throw new Error('"files" property has to be one of the types: String, Array, FormData');
        }

        params.method = 'POST';
        params.ssl = (null != params.ssl) ? params.ssl : false;
        params.headers = params.headers || {};

        var Utils = CDAPStreamClient.Utils,
            Promise = CDAPStreamClient.Promise,
            connection = new XMLHttpRequest(),
            promiseInst = new Promise(),
            requestUrl = Utils.baseUrl(params.host, params.port, params.ssl) + params.path;

        connection.onload = function responseHandler() {
            if (200 === connection.status) {
                promiseInst.resolve(connection.status);
            } else {
                promiseInst.reject(connection.status);
            }
        };

        connection.open(params.method, requestUrl, true);
        connection.send(formData);

        return promiseInst;
    };
}