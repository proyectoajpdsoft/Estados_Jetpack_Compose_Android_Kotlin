/**
 * ProyectoA - Estados en Jetpack Compose con cálculo de importe de beneficio
 * https://proyectoa.com
 */

package proyectoa.com.proyectoaejemploestado

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    EstadoLayout()
                }
       }
    }
}

/**
 * Mostrar el layout completo de la app
 */
@Composable
fun EstadoLayout() {
    //Declaramos las variables mutables para controlar los estados de los controles
    var importeEntrada by remember { mutableStateOf("") }
    var porcBeneficioEntrada by remember { mutableStateOf("") }
    var redondeoEntradaAlza by remember { mutableStateOf(false) }
    var redondeoEntradaNormal by remember { mutableStateOf(false) }
    var noRedondeoEntrada by remember { mutableStateOf(true)}

    // Convertimos los valores de entrada y realizamos los cálculos
    val importe = importeEntrada.toDoubleOrNull() ?: 0.0
    val porcBeneficio = porcBeneficioEntrada.toDoubleOrNull() ?: 0.0
    val importeBeneficio = calcularBeneficio(importe, porcBeneficio,
        redondeoEntradaAlza, redondeoEntradaNormal)

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = 1.dp, top = 10.dp)
                .align(alignment = Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(R.string.texto_informativo),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(bottom = 16.dp, top = 10.dp)
                .align(alignment = Alignment.Start)
        )
        val focusManager = LocalFocusManager.current
        // Mostramos el campo para introducir el importe
        // Establecemos el teclado de tipo numérico
        // Establecemos la tecla de acción "Siguiente"
        CampoNumero(
            label = R.string.importe_base,
            leadingIcon = R.drawable.moneda,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions (
                onNext = { focusManager.moveFocus(FocusDirection.Down) }

            ),
            value = importeEntrada,
            onValueChanged = { importeEntrada = it },
            modifier = Modifier
                .padding(bottom = 20.dp)
                .fillMaxWidth(),
        )
        // Mostramos el campo para introducir el porcentaje de beneficio
        // Establecemos el teclado de tipo numérico
        // Establecemos la tecla de acción "Validar"
        CampoNumero(
            label = R.string.porcentaje_beneficio,
            leadingIcon = R.drawable.porcentaje,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }),
            value = porcBeneficioEntrada,
            onValueChanged = { porcBeneficioEntrada = it },
            modifier = Modifier
                .padding(bottom = 25.dp)
                .fillMaxWidth(),
        )
        // Mostramos el campo de switch (interruptor) para Sin redondeo
        CampoNoRedondeo(
            roundUp = noRedondeoEntrada,
            onRoundUpChanged = {
                // Si se activa el No redondeo, se desactivan los redondeos
                noRedondeoEntrada = it
                if (noRedondeoEntrada) {
                    redondeoEntradaAlza = false
                    redondeoEntradaNormal = false
                }
                // Si no se activa el No redondeo, activar el redondeo al alza
                if (!noRedondeoEntrada && !redondeoEntradaAlza && !redondeoEntradaNormal)
                    redondeoEntradaAlza = true
            },
            modifier = Modifier.padding(bottom = 10.dp)
        )
        // Mostramos el campo de switch (interruptor) para Redondear benieficio al alza
        CampoRedondeoAlza(
            roundUp = redondeoEntradaAlza,
            onRoundUpChanged = {
                redondeoEntradaAlza = it
                // Si se activa el redondeo al alza, desactivar el redondeo normal y el sin redondeo
                if (redondeoEntradaAlza) {
                    redondeoEntradaNormal = false
                    noRedondeoEntrada = false
                }
            },
            modifier = Modifier.padding(bottom = 10.dp)
        )
        // Mostramos el campo de switch (interruptor) para Redondear benieficio normal
        CampoRedondeoNormal(
            roundUp = redondeoEntradaNormal,
            onRoundUpChanged = {
                redondeoEntradaNormal = it
                // Si se activa el redondeo normal, desactivar el redondeo al alza y el sin redondeo
                if (redondeoEntradaNormal) {
                    redondeoEntradaAlza = false
                    noRedondeoEntrada = false
                }
            },
            modifier = Modifier.padding(bottom = 20.dp)
        )
        // Mostramos el importe de beneficio calculado
        Text(
            text = stringResource(R.string.importe_beneficio, importeBeneficio),
            style = MaterialTheme.typography.titleLarge
        )
        // Mostramos el botón copiar el beneficio al portapapeles
        BotonCopiar {
            copiarTextoPortapapeles(importeBeneficio, context)
        }
    }
}

/**
 * Mostrar un campo de introducción de texto con teclado numérico
 * Reutilizable: esta función servirá para dibujar todos los cuadros que se necesiten
*/
@Composable
fun CampoNumero(
    @StringRes label: Int,
    @DrawableRes leadingIcon: Int,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        singleLine = true,
        leadingIcon = { Icon(painter = painterResource(id = leadingIcon), null) },
        modifier = modifier,
        onValueChange = onValueChanged,
        label = { Text(stringResource(label)) },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

/**
 * Mostrar un switch (interruptor) para redondear el beneficio
 * con redondeo al alza
*/
@Composable
fun CampoRedondeoAlza(
    roundUp: Boolean,
    onRoundUpChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.redondear_beneficio_alza))
        Switch(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            checked = roundUp,
            onCheckedChange = onRoundUpChanged
        )
    }
}

/**
 * Mostrar un switch (interruptor) para redondear el beneficio
 * con redondeo normal
*/
@Composable
fun CampoRedondeoNormal(
    roundUp: Boolean,
    onRoundUpChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.redondear_beneficio))
        Switch(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            checked = roundUp,
            onCheckedChange = onRoundUpChanged
        )
    }
}

/**
 * Mostrar un switch (interruptor) para no redondear el beneficio
*/
@Composable
fun CampoNoRedondeo(
    roundUp: Boolean,
    onRoundUpChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.sin_redondeo))
        Switch(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            checked = roundUp,
            onCheckedChange = onRoundUpChanged
        )
    }
}

/**
 * Calcula el importe de beneficio de un importe dado, aplicando un porcentaje dado
 * Devolverá el valor en string formateado
*/
private fun calcularBeneficio(importe: Double,
                              porcentajeBeneficio: Double = 10.0,
                              redondearAlza: Boolean, redondearNormal: Boolean): String {
    var beneficio = porcentajeBeneficio / 100 * importe
    if (redondearAlza) {
        beneficio = kotlin.math.ceil(beneficio)
    }
    if (redondearNormal) {
        beneficio = kotlin.math.round(beneficio)
    }
    val beneficioFormateado = formatearNumeroSeparadorMiles(beneficio,"€")
    return beneficioFormateado
}

/**
 * Botón para copiar el beneficio al portapapeles
 */
@Composable
fun BotonCopiar(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text("Copiar")
    }
}

/**
 * Formatea un número para devolver separador de miles y decimal en ES
*/
fun formatearNumeroSeparadorMiles(numero: Double, moneda: String = "€"): String {
    try {
        val formato = NumberFormat.getNumberInstance(Locale("es", "ES"))
        formato.minimumFractionDigits = 2
        formato.maximumFractionDigits = 2

        val numFormateado = formato.format(numero) + moneda
        return  numFormateado
    } catch (e: Exception) {
        return "0,00"
    }
}

/**
 * Copia el texto pasado como parámetro al portapapeles
 */
fun copiarTextoPortapapeles (texto: String, context: Context) {
    val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("label", texto)
    clipboard.setPrimaryClip(clipData)
}