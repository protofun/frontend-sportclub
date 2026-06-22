package org.example.project.navigation

// Only these routes exists and no other routes can be created
sealed class Route {
    // Public pages
    object Home          : Route()
    object Schedule      : Route()
    object Subscriptions : Route()
    object Register      : Route()
    object Login         : Route()
    object Downloads     : Route()

    // Admin pages
    object AdminDashboard   : Route()
    object AdminWorkouts    : Route()
    object AdminInstructors : Route()
    object AdminSchedule    : Route()
    object AdminMembers     : Route()
    object AdminOccupancy   : Route()
}

// Only the admin can access these pages
fun Route.isAdminRoute() =
    this is Route.AdminDashboard || this is Route.AdminWorkouts ||
    this is Route.AdminInstructors || this is Route.AdminSchedule ||
    this is Route.AdminMembers || this is Route.AdminOccupancy
