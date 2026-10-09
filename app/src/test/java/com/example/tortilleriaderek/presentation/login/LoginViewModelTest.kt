package com.example.tortilleriaderek.presentation.login

import app.cash.turbine.test
import com.example.tortilleriaderek.domain.model.Usuario
import com.example.tortilleriaderek.domain.usecase.AbrirTurnoUseCase
import com.example.tortilleriaderek.domain.usecase.LoginPinUseCase
import com.example.tortilleriaderek.domain.usecase.LoginUseCase
import com.example.tortilleriaderek.domain.usecase.RecuperarAccesoMasterKeyUseCase
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
import org.junit.Before
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert.
 * Pruebas unitarias MVI utilizando Turbine para evaluar flujos y efectos con PIN y patrón táctil.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var loginPinUseCase: LoginPinUseCase
    private lateinit var recuperarAccesoMasterKeyUseCase: RecuperarAccesoMasterKeyUseCase
    private lateinit var abrirTurnoUseCase: AbrirTurnoUseCase
    private lateinit var authRepository: com.example.tortilleriaderek.domain.repository.AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk(relaxed = true)
        loginPinUseCase = mockk(relaxed = true)
        recuperarAccesoMasterKeyUseCase = mockk(relaxed = true)
        abrirTurnoUseCase = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        io.mockk.every { authRepository.getSeguridadConfig() } returns kotlinx.coroutines.flow.flowOf(
            com.example.tortilleriaderek.domain.model.SeguridadConfig()
        )
        viewModel = LoginViewModel(
            loginUseCase = loginUseCase,
            loginPinUseCase = loginPinUseCase,
            recuperarAccesoMasterKeyUseCase = recuperarAccesoMasterKeyUseCase,
            abrirTurnoUseCase = abrirTurnoUseCase,
            authRepository = authRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `al cambiar modo seleccionado o metodo de acceso, el UiState se actualiza correctamente`() = runTest {
        viewModel.uiState.test {
            val estadoInicial = awaitItem()
            assertEquals(LoginMode.ABRIR_TURNO, estadoInicial.selectedMode)
            assertEquals(MetodoAcceso.CODIGO_PIN, estadoInicial.metodoAcceso)

            // Cambiar a Solo Consulta
            viewModel.onEvent(LoginUiEvent.OnModeSelected(LoginMode.SOLO_CONSULTA))
            val estadoConsulta = awaitItem()
            assertEquals(LoginMode.SOLO_CONSULTA, estadoConsulta.selectedMode)

            // Cambiar a Patrón
            viewModel.onEvent(LoginUiEvent.OnCambiarMetodoAcceso(MetodoAcceso.PATRON))
            val estadoPatron = awaitItem()
            assertEquals(MetodoAcceso.PATRON, estadoPatron.metodoAcceso)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `al teclear digitos de PIN, actualiza pinInput y al llegar a 4 intenta login`() = runTest {
        val usuario = Usuario("1", "admin1", "ADMIN")
        coEvery { loginPinUseCase.autenticarConPin("1234") } returns Result.success(usuario)

        viewModel.effect.test {
            viewModel.onEvent(LoginUiEvent.OnDigitoPresionado("1"))
            viewModel.onEvent(LoginUiEvent.OnDigitoPresionado("2"))
            viewModel.onEvent(LoginUiEvent.OnDigitoPresionado("3"))
            viewModel.onEvent(LoginUiEvent.OnDigitoPresionado("4"))

            val efectoEmitido = awaitItem()
            assertEquals(LoginUiEffect.NavigateToMostrador, efectoEmitido)
            coVerify(exactly = 1) { abrirTurnoUseCase(usuarioId = "1") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `al seleccionar nodos de patron y confirmar, autentica y abre turno`() = runTest {
        val usuario = Usuario("1", "admin1", "ADMIN")
        coEvery { loginPinUseCase.autenticarConPatron("1258") } returns Result.success(usuario)

        viewModel.onEvent(LoginUiEvent.OnCambiarMetodoAcceso(MetodoAcceso.PATRON))
        viewModel.onEvent(LoginUiEvent.OnPatronNodoSeleccionado(1))
        viewModel.onEvent(LoginUiEvent.OnPatronNodoSeleccionado(2))
        viewModel.onEvent(LoginUiEvent.OnPatronNodoSeleccionado(5))
        viewModel.onEvent(LoginUiEvent.OnPatronNodoSeleccionado(8))

        viewModel.effect.test {
            viewModel.onEvent(LoginUiEvent.OnConfirmarPatron)

            val efectoEmitido = awaitItem()
            assertEquals(LoginUiEffect.NavigateToMostrador, efectoEmitido)
            coVerify(exactly = 1) { abrirTurnoUseCase(usuarioId = "1") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `click en Settings, emite NavigateToSettings sin llamar a base de datos`() = runTest {
        viewModel.effect.test {
            viewModel.onEvent(LoginUiEvent.OnSettingsClick)

            val efectoEmitido = awaitItem()
            assertEquals(LoginUiEffect.NavigateToSettings, efectoEmitido)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
