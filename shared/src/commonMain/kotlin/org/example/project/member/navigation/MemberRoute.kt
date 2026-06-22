package org.example.project.member.navigation

// MemberRoute defines all screens that can be navigated to in the app.
sealed class MemberRoute {
    object Home : MemberRoute()
    object Schedule : MemberRoute()
    object MyClasses : MemberRoute()
    object Subscription : MemberRoute()
    object Profile : MemberRoute()
    object InstructorLessons : MemberRoute()
}
