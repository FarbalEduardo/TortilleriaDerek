package com.example.tortilleriaderek.ui.components.metricas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Diálogo interactivo para seleccionar un rango de fechas de hasta 30 días.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogRangoFechasPersonalizado(
    onDismiss: () -> Unit,
    onConfirmarRango: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoneUtc = ZoneId.of("UTC")
    val hoyUtc = LocalDate.now(zoneUtc)
    val selectableDates = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val fecha = Instant.ofEpochMilli(utcTimeMillis).atZone(zoneUtc).toLocalDate()
                return !fecha.isAfter(hoyUtc)
            }
        }
    }
    val datePickerState = rememberDateRangePickerState(selectableDates = selectableDates)

    val startMillis = datePickerState.selectedStartDateMillis
    val endMillis = datePickerState.selectedEndDateMillis

    val startDate = startMillis?.let { Instant.ofEpochMilli(it).atZone(zoneUtc).toLocalDate() }
    val endDate = endMillis?.let { Instant.ofEpochMilli(it).atZone(zoneUtc).toLocalDate() }

    val rangoCompletoSeleccionado = startDate != null && endDate != null
    val diasSeleccionados = if (rangoCompletoSeleccionado) {
        ChronoUnit.DAYS.between(startDate, endDate) + 1
    } else 0

    val excede30Dias = diasSeleccionados > 30

    DatePickerDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_rango_fechas_personalizado"),
        confirmButton = {
            if (rangoCompletoSeleccionado && !excede30Dias) {
                Button(
                    onClick = {
                        onConfirmarRango(startMillis!!, endMillis!!)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("btn_aplicar_rango_fechas")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Aplicar a la gráfica ($diasSeleccionados días)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = CircleShape,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_cancelar_rango_fechas")
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.btn_cancelar),
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            if (excede30Dias) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFDE8E8),
                    border = BorderStroke(1.dp, Color(0xFFF87171)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_error_max_dias),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = "Seleccionaste $diasSeleccionados días. Por favor reduce el rango a 30 días o menos.",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }
            } else if (startDate != null && endDate == null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF7ED),
                    border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaizPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Inicio seleccionado. Ahora selecciona la fecha final (máx. 30 días).",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFC2410C)
                        )
                    }
                }
            }

            DateRangePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Seleccionar Rango Personalizado (Máx. 30 días)",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                },
                modifier = Modifier.weight(1f, fill = false),
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextSecondary,
                    navigationContentColor = MaizPrimary,
                    yearContentColor = TextPrimary,
                    currentYearContentColor = MaizPrimary,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = MaizPrimary,
                    dayContentColor = Color(0xFF1F2937),
                    selectedDayContainerColor = MaizPrimary,
                    dayInSelectionRangeContainerColor = Color(0xFFFFEDD5),
                    dayInSelectionRangeContentColor = Color(0xFF9A3412),
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = MaizPrimary,
                    todayContentColor = MaizPrimary
                )
            )
        }
    }
}
