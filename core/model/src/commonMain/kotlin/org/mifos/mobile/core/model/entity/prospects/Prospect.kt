/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-mobile/blob/master/LICENSE.md
 */
package org.mifos.mobile.core.model.entity.prospects

import kotlinx.serialization.Serializable
import org.mifos.mobile.core.model.Parcelable
import org.mifos.mobile.core.model.Parcelize
import org.mifos.mobile.core.model.entity.client.Status

@Serializable
@Parcelize
data class Prospect(
    val id: Long? = null,

    val email: String? = null,

    private val status: Status? = null,

    val activationDate: Long? = null,

    val dateOfBirth: String? = null,

    val firstname: String? = null,

    val middlename: String? = null,

    val lastname: String? = null,

    val displayName: String? = null,

    val isImagePresent: Boolean = false,

    val mobileNo: String? = null,

    val gender: String? = null,

) : Parcelable
