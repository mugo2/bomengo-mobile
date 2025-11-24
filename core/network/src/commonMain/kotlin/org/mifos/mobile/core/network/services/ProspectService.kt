/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-mobile/blob/master/LICENSE.md
 */
package org.mifos.mobile.core.network.services

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import org.mifos.mobile.core.model.entity.prospects.Prospect
import org.mifos.mobile.core.model.entity.templates.prospects.ProspectTemplate
import org.mifos.mobile.core.network.utils.ApiEndPoints

interface ProspectService {

    @POST(ApiEndPoints.PROSPECTS)
    suspend fun createProspect(@Body prospect: Prospect?): HttpResponse

    @POST(ApiEndPoints.PROSPECTS + "/{prospectId}/images")
    suspend fun uploadProspectImage(
        @Path("prospectId") prospectId: Long,
        @Body body: MultiPartFormDataContent,
    ): HttpResponse

    @GET(ApiEndPoints.PROSPECTS + "/tpt")
    fun getTemplate(): Flow<ProspectTemplate>

    companion object {
        const val PROSPECT_ID = "prospectId"
    }
}
