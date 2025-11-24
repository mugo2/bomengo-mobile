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

import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mifos_mobile.feature.auth.generated.resources.Res
import mifos_mobile.feature.auth.generated.resources.feature_newclient_error_date_legaldate
import mifos_mobile.feature.auth.generated.resources.feature_newclient_error_invalid_gender
import mifos_mobile.feature.auth.generated.resources.feature_newclient_error_picture_empty
import mifos_mobile.feature.auth.generated.resources.feature_recover_now_phone_number_error
import mifos_mobile.feature.auth.generated.resources.feature_signup_error_first_name_empty
import mifos_mobile.feature.auth.generated.resources.feature_signup_error_invalid_email
import mifos_mobile.feature.auth.generated.resources.feature_signup_error_invalid_name
import mifos_mobile.feature.auth.generated.resources.feature_signup_error_last_name_empty
import mifos_mobile.feature.auth.generated.resources.feature_signup_error_middle_name_empty
import org.jetbrains.compose.resources.StringResource
import org.mifos.mobile.core.common.DataState
import org.mifos.mobile.core.data.repository.ProspectRepository
import org.mifos.mobile.core.model.entity.prospects.Prospect
import org.mifos.mobile.core.model.entity.templates.prospects.CodeValueOption
import org.mifos.mobile.core.model.entity.templates.prospects.ProspectTemplate
import org.mifos.mobile.core.ui.utils.BaseViewModel
import org.mifos.mobile.core.ui.utils.ScreenUiState
import org.mifos.mobile.core.ui.utils.ValidationHelper
import org.mifos.mobile.core.ui.utils.multipartRequestBody
import org.mifos.mobile.feature.auth.utils.compressImage
import org.mifos.mobile.feature.auth.utils.formatDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * ViewModel responsible for handling client creation logic.
 *
 * It manages the state of the client form, handles user input actions,
 * performs validation, and communicates with the [prospectRepositoryImpl] for API calls.
 *
 * @property prospectRepositoryImpl Repository to handle creation logic.
 */
@Suppress("TooManyFunctions")
class NewClientViewModel(
    private val prospectRepositoryImpl: ProspectRepository,
) : BaseViewModel<SignUpState, SignUpEvent, SignUpAction>(
    initialState = SignUpState(),
) {

    private var validationJob: Job? = null

    /**
     * load prospects template.
     */
    init {
        viewModelScope.launch {
            loadProspectTemplate()
        }
    }

    /**
     * Updates the current UI state using a state reducer lambda.
     */
    private fun updateState(update: (SignUpState) -> SignUpState) {
        mutableStateFlow.update(update)
    }

    /**
     * Handles all actions triggered from the UI by delegating to corresponding methods.
     */
    override fun handleAction(action: SignUpAction) {
        when (action) {
            is SignUpAction.OnFirstNameChange -> {
                handleFirstNameChange(action.firstName)
            }

            is SignUpAction.OnLastNameChange -> {
                handleLastNameChange(action.lastName)
            }

            is SignUpAction.OnEmailChange -> {
                handleEmailChange(action.email.trim())
            }

            is SignUpAction.OnMobileNumberChange -> {
                handleMobileNumberChange(action.mobileNumber)
            }

            is SignUpAction.OnGenderChange -> {
                handleGenderChange(action.gender)
            }

            is SignUpAction.OnMiddleNameChange -> {
                handleMiddleNameChange(action.middleName)
            }

            is SignUpAction.OnPictureChange -> {
                handlePictureChange(action.picture)
            }

            is SignUpAction.OnDateOfBirthChange -> {
                handleDateOfBirthChange(action.dateOfBirth)
            }

            is SignUpAction.Internal.ReceiveRegisterResult -> handleRegisterResult(action)

            is SignUpAction.Internal.ReceiveUploadResult -> handleUploadResult(action)

            is SignUpAction.Internal.ReceiveTemplateResult -> handleTemplateResult(action)

            is SignUpAction.SubmitClick -> handleSubmit()

            is SignUpAction.OnNavigateToLogin -> sendEvent(SignUpEvent.NavigateToLogin)

            SignUpAction.ErrorDialogDismiss -> updateState { it.copy(dialogState = null) }
        }
    }

    /**
     * Handles first name input changes and validates the name.
     */
    private fun handleFirstNameChange(name: String) {
        mutableStateFlow.update {
            it.copy(
                firstName = name,
                firstNameError = null,
            )
        }

        debounceValidation {
            val result = validateName(name, "first")
            mutableStateFlow.update {
                it.copy(
                    firstNameError = if (result is ValidationResult.Error) result.message else null,
                )
            }
        }
    }

    /**
     * Handles middle name input changes and validates the name.
     */
    private fun handleMiddleNameChange(name: String) {
        mutableStateFlow.update {
            it.copy(
                middleName = name,
                middleNameError = null,
            )
        }

        debounceValidation {
            val result = validateName(name, "middle")
            mutableStateFlow.update {
                it.copy(
                    middleNameError = if (result is ValidationResult.Error) result.message else null,
                )
            }
        }
    }

    /**
     * Handles last name input changes and validates the name.
     */
    private fun handleLastNameChange(name: String) {
        mutableStateFlow.update {
            it.copy(
                lastName = name,
                lastNameError = null,
            )
        }

        debounceValidation {
            val result = validateName(name, "last")
            mutableStateFlow.update {
                it.copy(
                    lastNameError = if (result is ValidationResult.Error) result.message else null,
                )
            }
        }
    }

    /**
     * Validates the given name depending on the type (first, middle, last).
     */
    @Suppress("ReturnCount")
    private fun validateName(name: String, nameType: String): ValidationResult? {
        if (name.isEmpty()) {
            return when (nameType) {
                "first" -> ValidationResult.Error(Res.string.feature_signup_error_first_name_empty)
                "middle" -> ValidationResult.Error(Res.string.feature_signup_error_middle_name_empty)
                "last" -> ValidationResult.Error(Res.string.feature_signup_error_last_name_empty)
                else -> ValidationResult.Error(Res.string.feature_signup_error_invalid_name)
            }
        }

        if (!ValidationHelper.isValidName(name)) {
            return ValidationResult.Error(Res.string.feature_signup_error_invalid_name)
        }

        return ValidationResult.Success
    }

    /**
     * Handles last name input changes and validates the name.
     */
    private fun handleGenderChange(name: String) {
        mutableStateFlow.update {
            it.copy(
                gender = name,
                genderError = null,
            )
        }

        debounceValidation {
            val result = validateGender(name, state.template.genderList)
            mutableStateFlow.update {
                it.copy(
                    genderError = if (result is ValidationResult.Error) result.message else null,
                )
            }
        }
    }

    /**
     * Validates the mobile number using helper methods.
     */
    private fun validateGender(gender: String, genderTypeOptions: List<CodeValueOption>?): ValidationResult? {
        return if (!ValidationHelper.isValidGender(gender)) {
            ValidationResult.Error(Res.string.feature_newclient_error_invalid_gender)
        } else {
            ValidationResult.Success
        }
    }

    /**
     * Handles email input changes and validates the email address.
     */
    private fun handleEmailChange(email: String) {
        mutableStateFlow.update {
            it.copy(
                email = email,
                emailError = null,
            )
        }

        debounceValidation {
            val result = validateEmail(email)
            mutableStateFlow.update {
                it.copy(
                    emailError = if (result is ValidationResult.Error) result.message else null,
                )
            }
        }
    }

    /**
     * Validates the email format using helper methods.
     */
    private fun validateEmail(email: String): ValidationResult? {
        return if (!ValidationHelper.isValidEmail(email)) {
            ValidationResult.Error(Res.string.feature_signup_error_invalid_email)
        } else {
            ValidationResult.Success
        }
    }

    /**
     * Handles mobile number input changes and validates it.
     */
    private fun handleMobileNumberChange(mobileNumber: String) {
        mutableStateFlow.update {
            it.copy(
                mobileNumber = mobileNumber,
                mobileNumberError = null,
            )
        }

        debounceValidation {
            val result = validateMobileNumber(mobileNumber)
            mutableStateFlow.update {
                it.copy(
                    mobileNumberError = if (result is ValidationResult.Error) result.message else null,
                )
            }
        }
    }

    /**
     * Validates the mobile number using helper methods.
     */
    private fun validateMobileNumber(mobileNumber: String): ValidationResult? {
        return if (!ValidationHelper.isValidPhoneNumber(mobileNumber)) {
            ValidationResult.Error(Res.string.feature_recover_now_phone_number_error)
        } else {
            ValidationResult.Success
        }
    }

    /**
     * Handles picture input changes and validates it.
     */
    private fun handlePictureChange(picture: PlatformFile) {
        mutableStateFlow.update {
            it.copy(
                picture = picture,
                pictureError = null,
            )
        }

        debounceValidation {
            val result = validatePicture(picture)
            mutableStateFlow.update {
                it.copy(
                    pictureError = if (result is ValidationResult.Error) result.message else null,
                )
            }
        }
    }

    /**
     * Validates the mobile number using helper methods.
     */
    private fun validatePicture(picture: PlatformFile?): ValidationResult? {
        return if (!ValidationHelper.isValidPicture(picture)) {
            ValidationResult.Error(Res.string.feature_newclient_error_picture_empty)
        } else {
            ValidationResult.Success
        }
    }

    /**
     * Handles date of birth input changes and validates it.
     */
    private fun handleDateOfBirthChange(dateOfBirth: Long) {
        mutableStateFlow.update {
            it.copy(
                dateOfBirth = dateOfBirth,
                dateOfBirthError = null,
            )
        }

        debounceValidation {
            val result = validateDateOfBirth(dateOfBirth)
            mutableStateFlow.update {
                it.copy(
                    dateOfBirthError = if (result is ValidationResult.Error) result.message else null,
                )
            }
        }
    }

    private fun validateDateOfBirth(dateOfBirth: Long): ValidationResult? {
        return if (!ValidationHelper.isValidDateOfBirth(dateOfBirth)) {
            ValidationResult.Error(Res.string.feature_newclient_error_date_legaldate)
        } else {
            ValidationResult.Success
        }
    }

    private fun isSuccess(result: ValidationResult?) = result is ValidationResult.Success

    /**
     * Validates all form fields and triggers user registration if valid.
     */
    private fun handleSubmit() {
        validationJob?.cancel()

        val firstNameError = validateName(state.firstName, "first")
        val middleNameError = validateName(state.middleName, "middle")
        val lastNameError = validateName(state.lastName, "last")
        val emailError = validateEmail(state.email)
        val mobileNumberError = validateMobileNumber(state.mobileNumber)
        val genderError = validateGender(state.gender, state.template.genderList)
        val pictureError = validatePicture(state.picture)
        val dateOfBirthError = validateDateOfBirth(state.dateOfBirth)

        mutableStateFlow.update {
            it.copy(
                firstNameError = if (firstNameError is ValidationResult.Error) firstNameError.message else null,
                middleNameError = if (middleNameError is ValidationResult.Error) {
                    middleNameError.message
                } else {
                    null
                },
                lastNameError = if (lastNameError is ValidationResult.Error) lastNameError.message else null,
                emailError = if (emailError is ValidationResult.Error) emailError.message else null,
                mobileNumberError = if (mobileNumberError is ValidationResult.Error) {
                    mobileNumberError.message
                } else {
                    null
                },
                pictureError = if (pictureError is ValidationResult.Error) pictureError.message else null,
                dateOfBirthError = if (dateOfBirthError is ValidationResult.Error) dateOfBirthError.message else null,
            )
        }

        val errorFree = isSuccess(firstNameError) &&
            isSuccess(middleNameError) &&
            isSuccess(lastNameError) &&
            isSuccess(emailError) &&
            isSuccess(mobileNumberError) &&
            isSuccess(genderError) &&
            isSuccess(pictureError) &&
            isSuccess(dateOfBirthError)

        if (errorFree) {
            registerUser()
        }
    }

    /**
     * Calls the repository to register the user with provided form data.
     */
    private fun registerUser() {
        // TODO uncomment when we get api for upload id until then make api call for registration
//        viewModelScope.launch {
//            updateState { it.copy(dialogState = SignUpState.SignUpDialog.Loading) }
//
//            delay(3000)
//
//            sendEvent(SignUpEvent.NavigateToUploadDocuments)
//        }
        updateState { it.copy(showOverlay = true) }
        viewModelScope.launch {
            val response = prospectRepositoryImpl.createProspect(
                prospect = Prospect(
                    id = state.id,
                    email = state.email,
                    firstname = state.firstName,
                    middlename = state.middleName,
                    lastname = state.lastName,
                    mobileNo = state.mobileNumber,
                    gender = state.gender,
                    dateOfBirth = formatDate(state.dateOfBirth),
                ),
            )

            sendAction(
                SignUpAction.Internal.ReceiveRegisterResult(
                    response,
                ),
            )
        }
    }

    /**
     * Handles the result of the user registration API call and updates UI state.
     */
    private fun handleRegisterResult(action: SignUpAction.Internal.ReceiveRegisterResult) {
        when (val result = action.registerResult) {
            is DataState.Success -> {
                updateState { it.copy(dialogState = null, showOverlay = false, id = result.data.toLong()) }
                uploadPicture(result.data)
            }

            is DataState.Error -> {
                updateState {
                    it.copy(
                        showOverlay = false,
                        dialogState = SignUpState.SignUpDialog.Error(result.message),
                    )
                }
            }

            DataState.Loading -> updateState { it.copy(showOverlay = true) }
        }
    }

    /**
     * Handles the result of the prospect image upload API call and updates UI state.
     */
    private fun handleUploadResult(action: SignUpAction.Internal.ReceiveUploadResult) {
        when (val result = action.uploadResult) {
            is DataState.Success -> {
                updateState { it.copy(dialogState = null, showOverlay = false) }

                sendEvent(
                    SignUpEvent.NavigateToRegistration,
                )
            }
            is DataState.Error -> {
                updateState {
                    it.copy(
                        showOverlay = false,
                        dialogState = SignUpState.SignUpDialog.Error(result.message),
                    )
                }
            }
            DataState.Loading -> updateState { it.copy(showOverlay = true) }
        }
    }

    fun uploadPicture(id: String) {
        if (state.picture == null) {
            return
        }
        updateState { it.copy(showOverlay = true) }

        viewModelScope.launch {
            val compressedImage = compressImage(state.picture!!, id)
            val requestFile = multipartRequestBody(compressedImage)

            val response = prospectRepositoryImpl.uploadPicture(id.toLong(), requestFile)

            sendAction(
                SignUpAction.Internal.ReceiveUploadResult(
                    response,
                ),
            )
        }
    }

    /**
     * Load prospects template.
     */
    private fun loadProspectTemplate() {
        updateState { it.copy(showOverlay = true) }
        viewModelScope.launch {
            prospectRepositoryImpl.loadTemplate()
                .collect { prospectTemplate ->
                    sendAction(
                        SignUpAction.Internal.ReceiveTemplateResult(
                            prospectTemplate,
                        ),
                    )
                }
        }
    }

    /**
     * Handles the result of the prospect template call and updates UI state.
     */
    private fun handleTemplateResult(action: SignUpAction.Internal.ReceiveTemplateResult) {
        when (val result = action.uploadResult) {
            is DataState.Success -> {
                updateState { it.copy(dialogState = null, showOverlay = false, template = result.data) }
            }
            is DataState.Error -> {
                updateState {
                    it.copy(
                        showOverlay = false,
                        dialogState = SignUpState.SignUpDialog.Error(result.message),
                    )
                }
            }
            DataState.Loading -> updateState { it.copy(showOverlay = true) }
        }
    }

    /**
     * Cancels any ongoing validation and launches the given validation block after a delay.
     * Used for debounced validation of form fields.
     */
    private fun debounceValidation(validation: suspend () -> Unit) {
        validationJob?.cancel()
        validationJob = viewModelScope.launch {
            delay(300)
            validation()
        }
    }
}

/**
 * Holds the UI state of the registration screen.
 */
data class SignUpState
@OptIn(ExperimentalTime::class)
constructor(
    val id: Long? = null,
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val email: String = "",
    val mobileNumber: String = "",
    val dateOfBirth: Long = Clock.System.now().toEpochMilliseconds(),
    val picture: PlatformFile? = null,

    val dialogState: SignUpDialog? = null,
    val uiState: ScreenUiState = ScreenUiState.Success,
    val showOverlay: Boolean = false,

    val template: ProspectTemplate = ProspectTemplate(
        listOf(),
        listOf(),
    ),

    val firstNameError: StringResource? = null,
    val middleNameError: StringResource? = null,
    val lastNameError: StringResource? = null,
    val genderError: StringResource? = null,
    val dateOfBirthError: StringResource? = null,
    val emailError: StringResource? = null,
    val mobileNumberError: StringResource? = null,
    val pictureError: StringResource? = null,

    val showDateOfBirthDatepicker: Boolean = false,

) {
    /**
     * Dialogs to show loading or error states during sign-up.
     */
    sealed interface SignUpDialog {
        data class Error(val message: String) : SignUpDialog
    }

    /**
     * Whether the submit button should be enabled based on required fields.
     */
    val isSubmitButtonEnabled: Boolean get() = firstName.isNotBlank() &&
        middleName.isNotBlank() &&
        lastName.isNotBlank() &&
        email.isNotBlank() &&
        mobileNumber.isNotBlank() &&
        gender.isNotBlank() &&
        picture != null && picture.size() > 0 &&
        dateOfBirth != 0L
}

/**
 * Events that the UI layer listens to for side effects (navigation, toasts).
 */
sealed interface SignUpEvent {
    data class ShowToast(val message: String) : SignUpEvent
    data object NavigateToRegistration : SignUpEvent
    data object NavigateToLogin : SignUpEvent
}

/**
 * Represents the result of a field validation operation.
 */
internal sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: StringResource) : ValidationResult()
}

/**
 * Defines all user-triggered or internal actions related to the Sign-Up screen.
 */
sealed interface SignUpAction {
    data class OnFirstNameChange(val firstName: String) : SignUpAction
    data class OnMiddleNameChange(val middleName: String) : SignUpAction
    data class OnLastNameChange(val lastName: String) : SignUpAction
    data class OnGenderChange(val gender: String) : SignUpAction
    data class OnEmailChange(val email: String) : SignUpAction
    data class OnMobileNumberChange(val mobileNumber: String) : SignUpAction
    data class OnPictureChange(val picture: PlatformFile) : SignUpAction
    data class OnDateOfBirthChange(val dateOfBirth: Long) : SignUpAction
    data object SubmitClick : SignUpAction
    data object OnNavigateToLogin : SignUpAction
    data object ErrorDialogDismiss : SignUpAction

    /**
     * Internal actions triggered inside ViewModel.
     */
    sealed class Internal : SignUpAction {
        data class ReceiveRegisterResult(
            val registerResult: DataState<String>,
        ) : Internal()

        data class ReceiveUploadResult(
            val uploadResult: DataState<String>,
        ) : Internal()

        data class ReceiveTemplateResult(
            val uploadResult: DataState<ProspectTemplate>,
        ) : Internal()
    }
}
