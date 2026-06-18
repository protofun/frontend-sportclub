package org.example.project

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.example.project.member.screens.ScheduleLessonCard
import org.example.project.member.screens.SpinningBikePickerDialog
import org.example.project.model.Bike
import org.example.project.model.Lesson
import org.example.project.model.LocationType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SportClubUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    /**
     * UI Test 1 — Leskaart toont de juiste informatie
     *
     * Geeft een leskaart weer met een spinning les en controleert
     * dat de naam, instructeur en bezetting zichtbaar zijn op het scherm.
     */
    @Test
    fun lessonCard_displaysLessonInfo() {
        val lesson = Lesson(
            id = "1",
            workoutId = "w1",
            workoutName = "Spinning",
            instructorName = "Sophie Janssen",
            locationId = "5",
            locationName = "Spinning Room",
            locationType = LocationType.SPINNING,
            startTime = "2026-06-20T09:00:00",
            durationMinutes = 60,
            maxCapacity = 24,
            enrolledCount = 10
        )

        composeRule.setContent {
            ScheduleLessonCard(
                lesson = lesson,
                isEnrolled = false,
                isWaitlisted = false,
                onReserve = {},
                onCancel = {},
                onJoinWaitlist = {},
                onLeaveWaitlist = {}
            )
        }

        composeRule.onNodeWithText("Spinning").assertIsDisplayed()
        composeRule.onNodeWithText("Sophie Janssen · 60 min", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("10/24").assertIsDisplayed()
    }

    /**
     * UI Test 2 — Fiets selecteren in de SpinningBikePickerDialog
     *
     * Opent de fietskiezerdialog met 3 beschikbare fietsen.
     * Klikt op rij 1 fiets 2 en controleert dat de bevestigingsknop actief wordt
     * en de geselecteerde fiets wordt weergegeven.
     */
    @Test
    fun spinningBikePicker_selectBike_enablesConfirm() {
        val bikes = listOf(
            Bike("id-1-1", rowNumber = 1, seatNumber = 1),
            Bike("id-1-2", rowNumber = 1, seatNumber = 2),
            Bike("id-1-3", rowNumber = 1, seatNumber = 3)
        )
        var confirmed = false

        composeRule.setContent {
            SpinningBikePickerDialog(
                availableBikes = bikes,
                enrolledCount = 1,
                maxCapacity = 24,
                isLoading = false,
                onConfirm = { confirmed = true },
                onDismiss = {}
            )
        }

        // Confirm-knop is uitgeschakeld zolang er geen fiets gekozen is
        composeRule.onNodeWithText("Confirm").assertIsNotEnabled()

        // Klik op de fiets met nummer "2" (rij 1, stoel 2)
        composeRule.onAllNodesWithText("2").filterToOne(hasClickAction()).performClick()

        // Na selectie is de bevestigingsknop actief
        composeRule.onNodeWithText("Confirm").assertIsEnabled()

        // Geselecteerde fiets wordt weergegeven
        composeRule.onNodeWithText("Row 1, Bike 2", substring = true).assertIsDisplayed()
    }

    /**
     * UI Test 3 — Reserve-knop zichtbaar voor een beschikbare les
     *
     * Geeft een leskaart weer waar de gebruiker nog niet is ingeschreven
     * en de les niet vol is. Controleert dat de Reserve-knop zichtbaar is
     * en dat de callback wordt aangeroepen bij klikken.
     */
    @Test
    fun lessonCard_showsReserveButton_andCallsOnReserve() {
        val lesson = Lesson(
            id = "2",
            workoutId = "w2",
            workoutName = "Yoga",
            instructorName = "Emma de Vries",
            locationId = "1",
            locationName = "Hall 1",
            locationType = LocationType.GROUP_INDOOR,
            startTime = "2026-06-20T10:30:00",
            durationMinutes = 60,
            maxCapacity = 20,
            enrolledCount = 5
        )
        var reserveCalled = false

        composeRule.setContent {
            ScheduleLessonCard(
                lesson = lesson,
                isEnrolled = false,
                isWaitlisted = false,
                onReserve = { reserveCalled = true },
                onCancel = {},
                onJoinWaitlist = {},
                onLeaveWaitlist = {}
            )
        }

        composeRule.onNodeWithText("Reserve").assertIsDisplayed()
        composeRule.onNodeWithText("Reserve").performClick()
        assert(reserveCalled) { "onReserve callback was not called" }
    }
}
