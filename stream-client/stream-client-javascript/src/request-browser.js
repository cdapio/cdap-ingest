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
 *   @param {string} host                 - Hostname of the CDAP Gateway.
 *   @param {number} port                 - Port number CDAP Gateway listen to.
 *   @param {string} method               - HTTP method to be used(GET, POST, etc.).
 *   @param {string} path                 - URL to send request to.
 *   @param {boolean} [ssl = false]       - True if connection has to be secured.
 *   @param {object} [headers = {}]       - HTTP headers to send within a request.
 *                                          { Authorization: 'Basic alsdh8asdna03inrd' }
 *   @param {object} [data = {}]          - Data to send within a request.
 * }
 *
 * @returns {@link CDAPStreamClient.Promise}
 */

window.CDAPStreamClient.request = window.CDAPStreamClient.request ||
    function request() {
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
            requestUrl = Utils.baseUrl(params.host, params.port, params.ssl) + params.path,
            responseHandler = function(response) {
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

        connection.onreadystatechange = responseHandler;

        connection.open(params.method, requestUrl, true);
        connection.send(params.data);

        return promiseInst;
    };
