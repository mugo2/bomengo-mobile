/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-mobile/blob/master/LICENSE.md
 */
package org.mifos.mobile.core.data.repositoryImpl

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import org.mifos.mobile.core.common.DataState
import org.mifos.mobile.core.common.asDataStateFlow
import org.mifos.mobile.core.data.repository.ProspectRepository
import org.mifos.mobile.core.data.util.extractErrorMessage
import org.mifos.mobile.core.model.entity.prospects.Prospect
import org.mifos.mobile.core.model.entity.templates.prospects.ProspectTemplate
import org.mifos.mobile.core.network.CustomDataManager

class ProspectRepositoryImp(

    private val customDataManager: CustomDataManager,
    private val ioDispatcher: CoroutineDispatcher,
) : ProspectRepository {

    override suspend fun createProspect(prospect: Prospect): DataState<String> {
        return withContext(ioDispatcher) {
            try {
                val prospectsApi = customDataManager.prospectsApi
                val response = prospectsApi.createProspect(prospect)
                DataState.Success(response.bodyAsText())
            } catch (e: ClientRequestException) {
                val errorMessage = extractErrorMessage(e.response)
                DataState.Error(Exception(errorMessage), null)
            } catch (e: IOException) {
                DataState.Error(Exception("Network error: ${e.message ?: "Please check your connection"}"), null)
            } catch (e: ServerResponseException) {
                DataState.Error(Exception("Server error: ${e.message}"), null)
            }
        }
    }

    override suspend fun uploadPicture(
        prospectId: Long,
        image: MultiPartFormDataContent,
    ): DataState<String> {
        return withContext(ioDispatcher) {
            try {
                val prospectsApi = customDataManager.prospectsApi
                val response = prospectsApi.uploadProspectImage(prospectId, image)
                DataState.Success(response.bodyAsText())
            } catch (e: ClientRequestException) {
                val errorMessage = extractErrorMessage(e.response)
                DataState.Error(Exception(errorMessage), null)
            } catch (e: IOException) {
                DataState.Error(Exception("Network error: ${e.message ?: "Please check your connection"}"), null)
            } catch (e: ServerResponseException) {
                DataState.Error(Exception("Server error: ${e.message}"), null)
            }
        }
    }

    override fun loadTemplate(): Flow<DataState<ProspectTemplate>> {
        return customDataManager.prospectsApi.getTemplate()
            .asDataStateFlow().flowOn(ioDispatcher)
    }
}
