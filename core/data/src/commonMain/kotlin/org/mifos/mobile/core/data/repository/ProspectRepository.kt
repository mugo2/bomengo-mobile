/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-mobile/blob/master/LICENSE.md
 */
package org.mifos.mobile.core.data.repository

import io.ktor.client.request.forms.MultiPartFormDataContent
import kotlinx.coroutines.flow.Flow
import org.mifos.mobile.core.common.DataState
import org.mifos.mobile.core.model.entity.prospects.Prospect
import org.mifos.mobile.core.model.entity.templates.prospects.ProspectTemplate

interface ProspectRepository {

    suspend fun createProspect(prospect: Prospect): DataState<String>

    suspend fun uploadPicture(prospectId: Long, image: MultiPartFormDataContent): DataState<String>

    fun loadTemplate(): Flow<DataState<ProspectTemplate>>
}
