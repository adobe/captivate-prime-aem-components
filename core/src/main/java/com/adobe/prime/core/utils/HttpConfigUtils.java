/*
 * Copyright 2025 Adobe. All rights reserved. This file is licensed to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adobe.prime.core.utils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public final class HttpConfigUtils {

    private HttpConfigUtils() {
    }

    public static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 10000;
    public static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 10000;

    /**
     * Creates a default RequestConfig with standard timeouts.
     * 
     * @return RequestConfig with default timeouts
     */
    public static RequestConfig createDefaultRequestConfig() {
        return RequestConfig.custom()
            .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
            .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
            .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT)
            .build();
    }

    /**
     * Creates a CloseableHttpClient with default timeouts.
     * 
     * @return CloseableHttpClient with default configuration
     */
    public static CloseableHttpClient createHttpClient() {
        return HttpClients.custom()
            .setDefaultRequestConfig(createDefaultRequestConfig())
            .build();
    }
} 