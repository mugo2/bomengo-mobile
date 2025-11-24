/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-mobile/blob/master/LICENSE.md
 */
package org.mifos.mobile.feature.auth.utils

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

expect class PlatformCameraLauncher {
    fun launch()
}

@Composable
expect fun rememberPlatformCameraLauncher(
    onImageCapturedPath: (PlatformFile?) -> Unit,
): PlatformCameraLauncher
