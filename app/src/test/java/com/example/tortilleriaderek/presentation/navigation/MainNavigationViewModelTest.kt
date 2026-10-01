package com.example.tortilleriaderek.presentation.navigation

import app.cash.turbine.test
import com.example.tortilleriaderek.domain.usecase.VerificarTurnoActivoUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert.
 * Pruebas unitarias para MainNavigationViewModel (US8).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainNavigationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var verificarTurnoActivoUseCase: VerificarTurnoActivoUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        verificarTurnoActivoUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando hay turno activo en Room, navigationState emite Ready con Screen Mostrador`() = runTest {
        // Given
        val turnoFlow = MutableStateFlow(true)
        coEvery { verificarTurnoActivoUseCase.observar() } returns turnoFlow

        // When
        val viewModel = MainNavigationViewModel(verificarTurnoActivoUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.navigationState.value
        assertTrue(state is NavigationStartState.Ready)
        assertEquals(Screen.Mostrador, (state as NavigationStartState.Ready).startDestination)
    }

    @Test
    fun `cuando el turno esta cerrado o es nulo en Room, navigationState emite Ready con Screen Login`() = runTest {
        // Given
        val turnoFlow = MutableStateFlow(false)
        coEvery { verificarTurnoActivoUseCase.observar() } returns turnoFlow

        // When
        val viewModel = MainNavigationViewModel(verificarTurnoActivoUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.navigationState.value
        assertTrue(state is NavigationStartState.Ready)
        assertEquals(Screen.Login, (state as NavigationStartState.Ready).startDestination)
    }
}
