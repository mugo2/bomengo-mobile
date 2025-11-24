/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-mobile/blob/master/LICENSE.md
 */
package org.mifos.mobile.core.network

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.mifos.mobile.core.network.services.createProspectService
import org.mifos.mobile.core.network.utils.FlowConverterFactory

class CustomKtorfitClient(
    ktorfit: Ktorfit,
) {

    internal val prospectsApi by lazy { ktorfit.createProspectService() }

    class Builder internal constructor() {
        private lateinit var baseURL: String
        private lateinit var httpClient: HttpClient

        fun baseURL(baseURL: String): Builder {
            this.baseURL = baseURL
            return this
        }

        fun httpClient(ktorHttpClient: HttpClient): Builder {
            this.httpClient = ktorHttpClient
            return this
        }

        fun build(): CustomKtorfitClient {
            val ktorfitBuilder = Ktorfit.Builder()
                .httpClient(httpClient)
                .baseUrl(baseURL)
                .converterFactories(FlowConverterFactory())
                .build()

            return CustomKtorfitClient(ktorfitBuilder)
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}
