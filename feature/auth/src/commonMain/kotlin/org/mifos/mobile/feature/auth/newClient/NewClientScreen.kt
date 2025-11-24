/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-mobile/blob/master/LICENSE.md
 */
package org.mifos.mobile.feature.auth.newClient

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import mifos_mobile.feature.auth.generated.resources.Res
import mifos_mobile.feature.auth.generated.resources.feature_auth_ic_person
import mifos_mobile.feature.auth.generated.resources.feature_newclient_cancel_date_label
import mifos_mobile.feature.auth.generated.resources.feature_newclient_dob_label
import mifos_mobile.feature.auth.generated.resources.feature_newclient_gender_label
import mifos_mobile.feature.auth.generated.resources.feature_newclient_please_select_action
import mifos_mobile.feature.auth.generated.resources.feature_newclient_remove_existing_photo
import mifos_mobile.feature.auth.generated.resources.feature_newclient_select_date_label
import mifos_mobile.feature.auth.generated.resources.feature_newclient_sub_title
import mifos_mobile.feature.auth.generated.resources.feature_newclient_take_a_photo
import mifos_mobile.feature.auth.generated.resources.feature_newclient_title
import mifos_mobile.feature.auth.generated.resources.feature_newclient_upload_photo
import mifos_mobile.feature.auth.generated.resources.feature_signup_already_have_an_account
import mifos_mobile.feature.auth.generated.resources.feature_signup_cell_phone_label
import mifos_mobile.feature.auth.generated.resources.feature_signup_email_label
import mifos_mobile.feature.auth.generated.resources.feature_signup_first_name_label
import mifos_mobile.feature.auth.generated.resources.feature_signup_last_name_label
import mifos_mobile.feature.auth.generated.resources.feature_signup_log_in
import mifos_mobile.feature.auth.generated.resources.feature_signup_middle_name_label
import mifos_mobile.feature.auth.generated.resources.feature_signup_submit
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.mobile.core.common.DateHelper
import org.mifos.mobile.core.designsystem.component.BasicDialogState
import org.mifos.mobile.core.designsystem.component.MifosBasicDialog
import org.mifos.mobile.core.designsystem.component.MifosButton
import org.mifos.mobile.core.designsystem.component.MifosOutlinedTextField
import org.mifos.mobile.core.designsystem.component.MifosScaffold
import org.mifos.mobile.core.designsystem.component.MifosTextFieldConfig
import org.mifos.mobile.core.designsystem.icon.MifosIcons
import org.mifos.mobile.core.designsystem.theme.DesignToken
import org.mifos.mobile.core.designsystem.theme.MifosMobileTheme
import org.mifos.mobile.core.designsystem.theme.MifosTypography
import org.mifos.mobile.core.model.entity.templates.prospects.CodeValueOption
import org.mifos.mobile.core.ui.component.MifosDatePickerTextField
import org.mifos.mobile.core.ui.component.MifosDropDownTextField
import org.mifos.mobile.core.ui.component.MifosPoweredCard
import org.mifos.mobile.core.ui.component.MifosProgressIndicatorOverlay
import org.mifos.mobile.core.ui.utils.EventsEffect
import org.mifos.mobile.core.ui.utils.ScreenUiState
import org.mifos.mobile.feature.auth.utils.rememberPlatformCameraLauncher
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
internal fun NewClientScreen(
    navigateToRegisterScreen: () -> Unit,
    navigateToLoginScreen: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewClientViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            is SignUpEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }

            is SignUpEvent.NavigateToRegistration -> navigateToRegisterScreen.invoke()

            is SignUpEvent.NavigateToLogin -> navigateToLoginScreen.invoke()
        }
    }

    SignUpDialog(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(SignUpAction.ErrorDialogDismiss) }
        },
    )

    NewClientScreen(
        state = state,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
        modifier = modifier,
    )
}

@Composable
private fun SignUpDialog(
    dialogState: SignUpState.SignUpDialog?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is SignUpState.SignUpDialog.Error -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        null -> Unit
    }
}

@Composable
private fun NewClientScreen(
    state: SignUpState,
    onAction: (SignUpAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosScaffold(
        bottomBar = {
            Surface {
                MifosPoweredCard(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                )
            }
        },
    ) {
        when (state.uiState) {
            ScreenUiState.Success -> {
                NewClientScreenContent(
                    state = state,
                    onAction = onAction,
                    modifier = modifier,
                )

                if (state.showOverlay) {
                    MifosProgressIndicatorOverlay()
                }
            }

            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun NewClientScreenContent(
    state: SignUpState,
    onAction: (SignUpAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val scrollState = rememberScrollState()

    var showImagePickerDialog by rememberSaveable { mutableStateOf(false) }

    var selectedImagePath by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(scrollState.canScrollForward) {
        if (scrollState.canScrollForward) scrollState.scrollTo(scrollState.maxValue)
    }

    val galleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
    ) { file ->
        file?.let {
            selectedImagePath = file.path
            onAction(SignUpAction.OnPictureChange(it))
        }
    }

    val cameraLauncher = rememberPlatformCameraLauncher { file ->
        file?.let {
            selectedImagePath = file.path
            onAction(SignUpAction.OnPictureChange(it))
        }
    }

    if (showImagePickerDialog) {
        MifosSelectImageDialog(
            onDismissRequest = { showImagePickerDialog = false },
            takeImage = {
                showImagePickerDialog = false
                cameraLauncher.launch()
            },
            uploadImage = {
                showImagePickerDialog = false
                galleryLauncher.launch()
            },
            removeImage = {
                showImagePickerDialog = false
                selectedImagePath = null
            },
        )
    }

    var showDateOfBirthDatepicker by rememberSaveable { mutableStateOf(false) }

    val dateOfBirthDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.dateOfBirth,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
            }
        },
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    keyboardController?.hide()
                }
            }
            .padding(DesignToken.padding.large)
            .padding(top = DesignToken.padding.large)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(DesignToken.spacing.medium),
        contentPadding = PaddingValues(
            bottom = DesignToken.spacing.extraLarge,
        ),
    ) {
        item {
            Text(
                text = stringResource(Res.string.feature_newclient_title),
                style = MifosTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        item {
            Text(
                text = stringResource(Res.string.feature_newclient_sub_title),
                style = MifosTypography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        item {
            ClientImageSection(selectedImagePath = selectedImagePath) {
                showImagePickerDialog = true
            }

            Spacer(modifier = Modifier.height(DesignToken.spacing.small))

            MifosDatePickerTextField(
                value = DateHelper.getDateMonthYearString(state.dateOfBirth),
                label = stringResource(Res.string.feature_newclient_dob_label),
                openDatePicker = { showDateOfBirthDatepicker = !showDateOfBirthDatepicker },
                error = state.dateOfBirthError != null,
                supportingText = state.dateOfBirthError?.let { stringResource(it) } ?: "",
            )

            if (showDateOfBirthDatepicker) {
                DatePickerDialog(
                    onDismissRequest = {
                        showDateOfBirthDatepicker = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                dateOfBirthDatePickerState.selectedDateMillis?.let {
                                    onAction(SignUpAction.OnDateOfBirthChange(dateOfBirth = it))
                                }
                                showDateOfBirthDatepicker = false
                            },
                        ) { Text(stringResource(Res.string.feature_newclient_select_date_label)) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDateOfBirthDatepicker = false
                            },
                        ) { Text(stringResource(Res.string.feature_newclient_cancel_date_label)) }
                    },
                ) {
                    DatePicker(state = dateOfBirthDatePickerState)
                }
            }

            Spacer(modifier = Modifier.height(DesignToken.spacing.small))

            FormSection(
                inputConfigs = getInputConfigs(state, onAction),
            )
        }

        item {
            Spacer(modifier = Modifier.height(DesignToken.spacing.small))

            Text(
                modifier = Modifier.clickable {
                    onAction(SignUpAction.OnNavigateToLogin)
                },
                text = stringResource(Res.string.feature_signup_log_in),
                style = MifosTypography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(DesignToken.spacing.small))
            MifosButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesignToken.sizes.inputHeight),
                onClick = { onAction(SignUpAction.SubmitClick) },
                shape = DesignToken.shapes.medium,
                enabled = state.isSubmitButtonEnabled,
            ) {
                Text(
                    text = stringResource(Res.string.feature_signup_submit),
                    style = MifosTypography.titleMedium,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(DesignToken.spacing.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.feature_signup_already_have_an_account),
                    style = MifosTypography.labelMedium,
                )
                Spacer(modifier = Modifier.width(DesignToken.spacing.extraSmall))
                Text(
                    modifier = Modifier.clickable {
                        onAction(SignUpAction.OnNavigateToLogin)
                    },
                    text = stringResource(Res.string.feature_signup_log_in),
                    style = MifosTypography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun FormSection(
    inputConfigs: List<InputFieldConfig>,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = DesignToken.spacing.largeIncreased,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
    ) {
        inputConfigs.forEach { config ->
            MifosInputField(config)
        }
    }
}

@Composable
fun MifosInputField(
    config: InputFieldConfig,
    modifier: Modifier = Modifier,
) {
    val visualTransformation =
        VisualTransformation.None

    val trailingIcon: @Composable (() -> Unit)? = when {
        config.errorText != null -> {
            {
                Icon(
                    imageVector = MifosIcons.ErrorCircle,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        else -> null
    }

    if (config.fieldType == InputFieldType.LIST) {
        MifosDropDownTextField(
            onClick = { index, _ ->
                val selected = config.options?.filter { it.value != null }
                    ?.get(index)?.value.toString()
                config.onValueChange(selected)
            },
            labelResId = config.labelRes,
            optionsList = config.options?.mapNotNull { it.description } ?: listOf(),
            selectedOption = config.options?.firstOrNull { it.value == config.state.gender }?.value ?: "",

            error = config.errorText != null,
            //        isEnabled = config.state != Sign.UPDATE,
            supportingText = config.errorText?.let { stringResource(it) } ?: "",
        )
    } else {
        MifosOutlinedTextField(
            value = config.value,
            onValueChange = config.onValueChange,
            label = stringResource(config.labelRes),
            shape = DesignToken.shapes.medium,
            textStyle = MifosTypography.bodyLarge,
            config = MifosTextFieldConfig(
                isError = config.errorText != null,
                errorText = config.errorText?.let { stringResource(it) },
                trailingIcon = trailingIcon,
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(
                    keyboardType = when (config.fieldType) {
                        InputFieldType.NUMBER -> {
                            KeyboardType.Number
                        }

                        InputFieldType.PHONE -> {
                            KeyboardType.Phone
                        }

                        else -> {
                            KeyboardType.Text
                        }
                    },
                    imeAction = ImeAction.Next,
                ),
            ),
        )
    }
}

enum class InputFieldType {
    TEXT,
    NUMBER,
    PHONE,
    LIST,
}

@Composable
private fun MifosSelectImageDialog(
    onDismissRequest: () -> Unit,
    takeImage: () -> Unit,
    uploadImage: () -> Unit,
    removeImage: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Card(
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(30.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.feature_newclient_please_select_action),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { takeImage() },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_newclient_take_a_photo),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
                Button(
                    onClick = { uploadImage() },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_newclient_upload_photo),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
                Button(
                    onClick = { removeImage() },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_newclient_remove_existing_photo),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

data class InputFieldConfig constructor(
    val state: SignUpState,
    val value: String,
    val labelRes: StringResource,
    val onValueChange: (String) -> Unit,
    val errorText: StringResource? = null,
    val fieldType: InputFieldType = InputFieldType.TEXT,
    val options: List<CodeValueOption>? = listOf(),
)

@Composable
fun getInputConfigs(
    state: SignUpState,
    onAction: (SignUpAction) -> Unit,
): List<InputFieldConfig> {
    return listOf(
        InputFieldConfig(
            state = state,
            value = state.firstName,
            labelRes = Res.string.feature_signup_first_name_label,
            onValueChange = { onAction(SignUpAction.OnFirstNameChange(it)) },
            errorText = state.firstNameError,
        ),
        InputFieldConfig(
            state = state,
            value = state.middleName,
            labelRes = Res.string.feature_signup_middle_name_label,
            onValueChange = { onAction(SignUpAction.OnMiddleNameChange(it)) },
            errorText = state.middleNameError,
        ),
        InputFieldConfig(
            state = state,
            value = state.lastName,
            labelRes = Res.string.feature_signup_last_name_label,
            onValueChange = { onAction(SignUpAction.OnLastNameChange(it)) },
            errorText = state.lastNameError,
        ),
        InputFieldConfig(
            state = state,
            value = state.gender,
            labelRes = Res.string.feature_newclient_gender_label,
            onValueChange = { onAction(SignUpAction.OnGenderChange(it)) },
            errorText = state.genderError,
            fieldType = InputFieldType.LIST,
            options = state.template?.genderList,
        ),
        InputFieldConfig(
            state = state,
            value = state.email,
            labelRes = Res.string.feature_signup_email_label,
            onValueChange = { onAction(SignUpAction.OnEmailChange(it)) },
            errorText = state.emailError,
        ),
        InputFieldConfig(
            state = state,
            value = state.mobileNumber,
            labelRes = Res.string.feature_signup_cell_phone_label,
            onValueChange = { onAction(SignUpAction.OnMobileNumberChange(it)) },
            errorText = state.mobileNumberError,
            fieldType = InputFieldType.PHONE,
        ),
    )
}

@Preview
@Composable
private fun NewClientScreenPreview() {
    MifosMobileTheme {
        NewClientScreen(
            state = SignUpState(dialogState = null),
            onAction = {},
            modifier = Modifier,
            // onImageSelected = {},
        )
    }
}

@Composable
private fun ClientImageSection(selectedImagePath: String?, onImageClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        Image(
            painter = if (selectedImagePath != null) {
                rememberAsyncImagePainter(
                    selectedImagePath,
                    ImageLoader(LocalPlatformContext.current),
                ) // FIXME
            } else {
                painterResource(Res.drawable.feature_auth_ic_person)
            },
            // painter = painterResource(Res.drawable.feature_auth_ic_person),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .clickable { onImageClick() }
                .border(
                    color = MaterialTheme.colorScheme.outline,
                    width = 2.dp,
                    shape = CircleShape,
                )
                .size(80.dp)
                .clip(CircleShape),
        )
    }
}
