package org.example.project.navigation

sealed class Route {
    object Home : Route()
    object Schedule : Route()
    object Subscriptions : Route()
    object Register : Route()
    object Login : Route()
    object Downloads : Route()
    object AdminDashboard : Route()
    object AdminWorkouts : Route()
    object AdminInstructors : Route()
    object AdminSchedule : Route()
    object AdminMembers : Route()
    object AdminOccupancy : Route()
}

fun Route.isAdminRoute() = this is Route.AdminDashboard || this is Route.AdminWorkouts ||
    this is Route.AdminInstructors || this is Route.AdminSchedule ||
    this is Route.AdminMembers || this is Route.AdminOccupancy
