/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-mobile/blob/master/LICENSE.md
 */
package org.mifos.mobile.core.network.utils

class BaseURL {
    val url: String
        get() = PROTOCOL_HTTPS + API_ENDPOINT + API_PATH

    val defaultBaseUrl: String
        get() = PROTOCOL_HTTPS + API_ENDPOINT

    fun getUrl(endpoint: String): String {
        return endpoint + API_PATH
    }

    val customUrl: String
        get() = PROTOCOL_HTTPS + CUSTOM_API_ENDPOINT + CUSTOM_API_PATH

    companion object {
//        const val API_ENDPOINT = "tt.mifos.community"
//        const val API_ENDPOINT = "10.0.2.2:80"

        const val API_ENDPOINT = "cbs-server.afidingcapital.com"
        const val API_PATH = "/fineract-provider/api/v1/self/"
        const val PROTOCOL_HTTPS = "https://"
//        const val CUSTOM_API_ENDPOINT = "10.0.2.2:8080"

        const val CUSTOM_API_ENDPOINT = "cbs-server.afidingcapital.com"
        const val CUSTOM_API_PATH = "/custom-provider/api/v1/"
    }
}
