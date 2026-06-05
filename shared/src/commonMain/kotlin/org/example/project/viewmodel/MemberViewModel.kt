package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*

data class MemberState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val members: List<Member> = emptyList(),
    val searchQuery: String = "",
    val selectedMember: Member? = null
) {
    val filteredMembers: List<Member>
        get() = if (searchQuery.isBlank()) members
        else members.filter { m ->
            m.fullName.contains(searchQuery, ignoreCase = true) ||
            m.email.contains(searchQuery, ignoreCase = true)
        }
}

class MemberViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(MemberState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun load() {
        scope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                state = state.copy(isLoading = false, members = api.getMembers())
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun setSearch(query: String) { state = state.copy(searchQuery = query) }
    fun selectMember(m: Member?) { state = state.copy(selectedMember = m) }
    fun clearError() { state = state.copy(error = null) }
}
