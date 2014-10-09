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
    Mime = require('mime'),
    Utils = require('./utils'),
    Promise = require('./promise'),
    connection = params.ssl ? require('https') : require('http');

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
 *   @param {string} [filename = null]    - Name of a file to be sent within a request.
 *                                          NOTE: This param is supported for NodeJS _only_.
 * }
 *
 * @returns {@link CDAPStreamClient.Promise}
 */

module.exports = function request(params) {
    if (!params.method || !params.path || !params.host || !params.port) {
        throw Error('"host", "port", "method", "path" properties are required');
    }

    var fileStat = null;

    params.ssl = (null != params.ssl) ? params.ssl : false;
    params.headers = params.headers || {};
    params.data = params.data || {};

    if (params.filename) {
        fileStat = Fs.statSync(params.filename);

        params.headers['Content-Type'] = Mime.lookup(params.filename);
        params.headers['Content-Length'] = fileStat.length;
    }

    var promise = new Promise(),
        request = connection.request(params, function (response) {
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

    if(params.filename) {
        request.pipe(Fs.createReadStream(params.filename));
    }

    request.write(params.data);
    request.end();

    return promise;
};
