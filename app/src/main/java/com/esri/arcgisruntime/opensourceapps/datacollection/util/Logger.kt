/*
 * Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.opensourceapps.datacollection.util

import android.util.Log

/**
 * Convenience class to send a log message.
 */
class Logger {

    companion object {
        private const val TAG = "Data Collection"

        /**
         * Send a verbose log message.
         *
         * @param msg The message you would like logged.
         */
        fun v(errorMessage: String) {
            Log.v(TAG, errorMessage)
        }

        /**
         * Send a debug log message.
         *
         * @param msg The message you would like logged.
         */
        fun d(errorMessage: String) {
            Log.d(TAG, errorMessage)
        }

        /**
         * Send an info log message.
         *
         * @param msg The message you would like logged.
         */
        fun i(errorMessage: String) {
            Log.i(TAG, errorMessage)
        }

        /**
         * Send a warning message.
         *
         * @param msg The message you would like logged.
         */
        fun w(errorMessage: String) {
            Log.w(TAG, errorMessage)
        }

        /**
         * Send an error message.
         *
         * @param msg The message you would like logged.
         */
        fun e(errorMessage: String) {
            Log.e(TAG, errorMessage)
        }

    }
}
