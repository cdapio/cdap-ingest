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
        window['CDAPStreamClient'] = window['CDAPStreamClient'] || { Promise: null };
        factory(window['CDAPStreamClient']['Promise']);
    }
}(function (target, require) {
    'use strict';

    /**
     * @constructor
     * @returns {Object}
     */
    var PromiseConstructor = function PromiseConstructor() {
        var success_handlers_stack = [],
            error_handlers_stack = [],
            notification_handlers_stack = [],

            resolve_value = null,
            reject_reason = null,
            notify_value_stack = [],

            fired = false;

        var fireResolve = function () {
                if (!fired && resolve_value && success_handlers_stack.length) {
                    while (success_handlers_stack.length) {
                        success_handlers_stack.shift()(resolve_value);
                    }

                    error_handlers_stack = [];
                    notification_handlers_stack = [];
                    fired = true;
                }
            },
            fireReject = function () {
                if (!fired && reject_reason && error_handlers_stack.length) {
                    while (error_handlers_stack.length) {
                        error_handlers_stack.shift()(reject_reason);
                    }

                    success_handlers_stack = [];
                    notification_handlers_stack = [];
                    fired = true;
                }
            },
            fireNotify = function () {
                if (!fired && notify_value_stack.length && notification_handlers_stack.length) {
                    var message = null;

                    while (notify_value_stack.length > 0) {
                        message = notify_value_stack.shift();

                        while (notification_handlers_stack.length) {
                            notification_handlers_stack.shift()(message);
                        }
                    }
                }
            },

            /**
             * Sets up event handlers.
             *
             * @param {function} [success=null] - fired in case of successful promise resolving.
             * @param {function} [error=null]   - fired in case of promise resolving with error
             * @param {function} [notify=null]  - used to notify about promise working progress.
             */
            thenImpl = function (success, error, notify) {
                if (null != success) {
                    if ('function' === typeof success) {
                        success_handlers_stack.push(success);
                    } else {
                        throw new TypeError('"success" parameter have to be a function.');
                    }
                }

                if (null != error) {
                    if ('function' === typeof error) {
                        error_handlers_stack.push(error);
                    } else {
                        throw new TypeError('"error" parameter have to be a function.');
                    }
                }

                if (null != notify) {
                    if ('function' === typeof notify) {
                        notification_handlers_stack.push(notify);
                    } else {
                        throw new TypeError('"notify" parameter have to be a function.');
                    }
                }

                fireResolve();
                fireReject();
                fireNotify();

                return this;
            },

            /**
             * Syntax sugar for 'then(null, error_handler)'
             *
             * @param {function} handler
             */
            catchImpl = function (handler) {
                return thenImpl.apply(this, [null, handler]);
            },

            /**
             * Resolves the derived promise with the value.
             *
             * @param {*} value
             */
            resolveImpl = function (value) {
                resolve_value = resolve_value || value;

                fireResolve();
            },

            /**
             * Rejects the derived promise with the reason.
             *
             * @param {*} reason
             */
            rejectImpl = function (reason) {
                reject_reason = !reject_reason ? reason : reject_reason;
                fireReject();
            },

            /**
             * Notifies the derived promise with the value.
             *
             * @param {*} value
             */
            notifyImpl = function (value) {
                notify_value_stack.push(value);
                fireNotify();
            };

        return {
            'then': thenImpl,
            'catch': catchImpl,
            'resolve': resolveImpl,
            'reject': rejectImpl,
            'notify': notifyImpl
        };
    };

    if (('undefined' !== typeof module) && module.exports) {
        module.exports = PromiseConstructor;
    } else {
        target = target || PromiseConstructor;
    }
}));
