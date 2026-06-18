package org.example.project.member.navigation

sealed class MemberRoute {
    object Home : MemberRoute()
    object Schedule : MemberRoute()
    object MyClasses : MemberRoute()
    object Subscription : MemberRoute()
    object Profile : MemberRoute()
    object InstructorLessons : MemberRoute()
}
