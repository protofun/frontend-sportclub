package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*
import org.example.project.util.todayDateString

// The four steps of the registration wizard (in order).
// ordinal gives the zero-based index used by StepIndicator in RegisterScreen.
enum class RegistrationStep { PERSONAL_INFO, SUBSCRIPTION, PAYMENT, CONFIRMATION }

// All form data collected across the four steps lives here as a single state object.
data class RegistrationState(
    val step: RegistrationStep = RegistrationStep.PERSONAL_INFO,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedPlan: MembershipPrice? = null,
    val startDate: String = todayDateString(),
    val plans: List<MembershipPrice> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val registeredUser: LoginResponse? = null,
    val isComplete: Boolean = false
)

class RegistrationViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(RegistrationState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Fetches available plans from GET /api/v1/memberships/prices.
    fun loadPlans() {
        scope.launch {
            state = state.copy(isLoading = true)
            try {
                state = state.copy(isLoading = false, plans = api.getMembershipPrices())
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    // Stores the personal info fields into state
    fun updatePersonalInfo(firstName: String, lastName: String, email: String, password: String, confirmPassword: String) {
        state = state.copy(firstName = firstName, lastName = lastName, email = email, password = password, confirmPassword = confirmPassword)
    }

    // Validates step 1 fields
    fun goToSubscription() {
        if (validate()) state = state.copy(step = RegistrationStep.SUBSCRIPTION, error = null)
    }

    // Returns false and sets an error message if any field is invalid.
    private fun validate(): Boolean = when {
        state.firstName.isBlank()              -> { state = state.copy(error = "First name is required"); false }
        state.lastName.isBlank()               -> { state = state.copy(error = "Last name is required"); false }
        state.email.isBlank()                  -> { state = state.copy(error = "Email is required"); false }
        state.password.length < 8             -> { state = state.copy(error = "Password must be at least 8 characters"); false }
        state.password != state.confirmPassword -> { state = state.copy(error = "Passwords do not match"); false }
        else -> true
    }

    // Called when the user clicks a plan card
    fun selectPlan(plan: MembershipPrice) {
        state = state.copy(selectedPlan = plan, step = RegistrationStep.PAYMENT)
    }

    fun setStartDate(date: String) { state = state.copy(startDate = date) }

    // Moves one step back in the wizard.
    fun back() {
        state = when (state.step) {
            RegistrationStep.SUBSCRIPTION -> state.copy(step = RegistrationStep.PERSONAL_INFO)
            RegistrationStep.PAYMENT      -> state.copy(step = RegistrationStep.SUBSCRIPTION)
            else -> state
        }
    }

    // Sends the final registration to the backend:
    //   1. POST /auth/register  → creates the user account, returns a token
    //   2. POST /memberships    → activates the chosen plan
    fun submitRegistration() {
        // can't be null
        val plan = state.selectedPlan ?: return
        scope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val req = RegistrationRequest(
                    firstName      = state.firstName,
                    lastName       = state.lastName,
                    email          = state.email,
                    password       = state.password,
                    membershipType = plan.type,
                    billingCycle   = plan.billingCycle,
                    startDate      = state.startDate
                )
                val user = api.registerMember(req)
                state = state.copy(isLoading = false, registeredUser = user, step = RegistrationStep.CONFIRMATION, isComplete = true)
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearError() { state = state.copy(error = null) }
}
