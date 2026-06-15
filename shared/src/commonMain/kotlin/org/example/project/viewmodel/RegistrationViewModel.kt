package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*
import org.example.project.util.todayDateString

enum class RegistrationStep { PERSONAL_INFO, SUBSCRIPTION, PAYMENT, CONFIRMATION }

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

    fun updatePersonalInfo(firstName: String, lastName: String, email: String, password: String, confirmPassword: String) {
        state = state.copy(firstName = firstName, lastName = lastName, email = email, password = password, confirmPassword = confirmPassword)
    }

    fun goToSubscription() {
        if (validate()) state = state.copy(step = RegistrationStep.SUBSCRIPTION, error = null)
    }

    private fun validate(): Boolean {
        return when {
            state.firstName.isBlank() -> { state = state.copy(error = "First name is required"); false }
            state.lastName.isBlank() -> { state = state.copy(error = "Last name is required"); false }
            state.email.isBlank() -> { state = state.copy(error = "Email is required"); false }
            state.password.length < 8 -> { state = state.copy(error = "Password must be at least 8 characters"); false }
            state.password != state.confirmPassword -> { state = state.copy(error = "Passwords do not match"); false }
            else -> true
        }
    }

    fun selectPlan(plan: MembershipPrice) {
        state = state.copy(selectedPlan = plan, step = RegistrationStep.PAYMENT)
    }

    fun setStartDate(date: String) { state = state.copy(startDate = date) }

    fun back() {
        state = when (state.step) {
            RegistrationStep.SUBSCRIPTION -> state.copy(step = RegistrationStep.PERSONAL_INFO)
            RegistrationStep.PAYMENT -> state.copy(step = RegistrationStep.SUBSCRIPTION)
            else -> state
        }
    }

    fun submitRegistration() {
        val plan = state.selectedPlan ?: return
        scope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val req = RegistrationRequest(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    email = state.email,
                    password = state.password,
                    membershipType = plan.type,
                    billingCycle = plan.billingCycle,
                    startDate = state.startDate
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
