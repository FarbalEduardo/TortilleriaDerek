package com.example.tortilleriaderek.presentation.login

import app.cash.turbine.test
import com.example.tortilleriaderek.domain.model.Usuario
import com.example.tortilleriaderek.domain.usecase.AbrirTurnoUseCase
import com.example.tortilleriaderek.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert.
 * Pruebas unitarias MVI utilizando Turbine para evaluar flujos y efectos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var abrirTurnoUseCase: AbrirTurnoUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
        abrirTurnoUseCase = mockk(relaxed = true) // Relaxed para no tener que mockear void returns
        viewModel = LoginViewModel(loginUseCase, abrirTurnoUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `al cambiar username o password, el UiState se actualiza correctamente`() = runTest {
        viewModel.uiState.test {
            // Estado inicial
            val estadoInicial = awaitItem()
            assertEquals("", estadoInicial.usernameInput)
            assertEquals("", estadoInicial.passwordInput)

            // Evento
            viewModel.onEvent(LoginUiEvent.OnUsernameChanged("admin_derek"))
            val estadoActualizadoUser = awaitItem()
            assertEquals("admin_derek", estadoActualizadoUser.usernameInput)

            // Evento
            viewModel.onEvent(LoginUiEvent.OnPasswordChanged("secreto"))
            val estadoActualizadoPass = awaitItem()
            assertEquals("secreto", estadoActualizadoPass.passwordInput)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `al cambiar modo seleccionado o visibilidad de password, el UiState se actualiza`() = runTest {
        viewModel.uiState.test {
            val estadoInicial = awaitItem()
            assertEquals(LoginMode.ABRIR_TURNO, estadoInicial.selectedMode)
            assertFalse(estadoInicial.isPasswordVisible)

            // Cambiar a Solo Consulta
            viewModel.onEvent(LoginUiEvent.OnModeSelected(LoginMode.SOLO_CONSULTA))
            val estadoConsulta = awaitItem()
            assertEquals(LoginMode.SOLO_CONSULTA, estadoConsulta.selectedMode)

            // Toggle visibilidad password
            viewModel.onEvent(LoginUiEvent.OnTogglePasswordVisibility)
            val estadoVisibilidad = awaitItem()
            assertTrue(estadoVisibilidad.isPasswordVisible)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `click en IniciarSesion con modo AbrirTurno, abre turno y emite NavigateToMostrador`() = runTest {
        // Given
        viewModel.onEvent(LoginUiEvent.OnUsernameChanged("admin1"))
        viewModel.onEvent(LoginUiEvent.OnPasswordChanged("123"))
        viewModel.onEvent(LoginUiEvent.OnModeSelected(LoginMode.ABRIR_TURNO))
        
        val usuario = Usuario("1", "admin1", "ADMIN")
        coEvery { loginUseCase("admin1", "123") } returns Result.success(usuario)

        viewModel.effect.test {
            // When
            viewModel.onEvent(LoginUiEvent.OnIniciarSesionClick)

            // Then
            val efectoEmitido = awaitItem()
            assertEquals(LoginUiEffect.NavigateToMostrador, efectoEmitido)
            
            coVerify(exactly = 1) { abrirTurnoUseCase(usuarioId = "1") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `click en IniciarSesion con modo SoloConsulta, NO abre turno y emite NavigateToMetricas`() = runTest {
        // Given
        viewModel.onEvent(LoginUiEvent.OnUsernameChanged("admin1"))
        viewModel.onEvent(LoginUiEvent.OnPasswordChanged("123"))
        viewModel.onEvent(LoginUiEvent.OnModeSelected(LoginMode.SOLO_CONSULTA))
        
        val usuario = Usuario("1", "admin1", "ADMIN")
        coEvery { loginUseCase("admin1", "123") } returns Result.success(usuario)

        viewModel.effect.test {
            // When
            viewModel.onEvent(LoginUiEvent.OnIniciarSesionClick)

            // Then
            val efectoEmitido = awaitItem()
            assertEquals(LoginUiEffect.NavigateToMetricas, efectoEmitido)
            
            coVerify(exactly = 0) { abrirTurnoUseCase(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `click en AbrirTurno legacy con credenciales correctas, abre turno y emite NavigateToMostrador`() = runTest {
        // Given
        viewModel.onEvent(LoginUiEvent.OnUsernameChanged("admin1"))
        viewModel.onEvent(LoginUiEvent.OnPasswordChanged("123"))
        
        val usuario = Usuario("1", "admin1", "ADMIN")
        coEvery { loginUseCase("admin1", "123") } returns Result.success(usuario)

        // Verificamos UiEffect con Turbine
        viewModel.effect.test {
            // When
            viewModel.onEvent(LoginUiEvent.OnAbrirTurnoClick)

            // Then
            val efectoEmitido = awaitItem()
            assertEquals(LoginUiEffect.NavigateToMostrador, efectoEmitido)
            
            coVerify(exactly = 1) { abrirTurnoUseCase(usuarioId = "1") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `click en SoloConsulta legacy con credenciales correctas, NO abre turno y emite NavigateToMetricas`() = runTest {
        // Given
        viewModel.onEvent(LoginUiEvent.OnUsernameChanged("admin1"))
        viewModel.onEvent(LoginUiEvent.OnPasswordChanged("123"))
        
        val usuario = Usuario("1", "admin1", "ADMIN")
        coEvery { loginUseCase("admin1", "123") } returns Result.success(usuario)

        viewModel.effect.test {
            // When
            viewModel.onEvent(LoginUiEvent.OnSoloConsultaClick)

            // Then
            val efectoEmitido = awaitItem()
            assertEquals(LoginUiEffect.NavigateToMetricas, efectoEmitido)
            
            // Verificamos que al ser Solo Consulta, nunca se mandó abrir el turno
            coVerify(exactly = 0) { abrirTurnoUseCase(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `click en Settings, emite NavigateToSettings sin llamar a base de datos`() = runTest {
        viewModel.effect.test {
            // When
            viewModel.onEvent(LoginUiEvent.OnSettingsClick)

            // Then
            val efectoEmitido = awaitItem()
            assertEquals(LoginUiEffect.NavigateToSettings, efectoEmitido)
            
            coVerify(exactly = 0) { loginUseCase(any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
