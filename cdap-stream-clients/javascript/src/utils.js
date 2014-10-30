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
        window['CDAPStreamClient'] = window['CDAPStreamClient'];
        window['CDAPStreamClient']['Utils'] = window['CDAPStreamClient']['Utils'] || {};
        factory(window['CDAPStreamClient']['Utils']);
    }
}(function (target, require) {
    'use strict';

    /**
     * Simple object copying implementation without deep copy.
     *
     * @param {Object} src1
     * @param {Object} src2
     * @returns {Object}
     */
    target.copyObject = function copyObject
        (src1, src2) {
        var result = {};

        var addToResult = function (src) {
            var srcKeys = Object.keys(src),
                prop = '';

            while (srcKeys.length) {
                prop = srcKeys.shift();
                result[prop] = src[prop];
            }
        };

        addToResult(src1);
        addToResult(src2);

        return result;
    };
    target.baseUrl = function baseUrl(hostname, port, ssl) {
        return [(ssl ? 'https' : 'http'), '://', hostname, ':', port].join('');
    };
    target.formRequest = function formRequest() {
        var args = Array.prototype.slice.call(arguments);
        return args.join('/');
    };
}));
