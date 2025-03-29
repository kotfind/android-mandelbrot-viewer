package org.kotfind.android_course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.text.*
import androidx.compose.ui.text.input.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    generator: MandelbrotGenerator,
    onMandelbrotGeneratorChanged: (MandelbrotGenerator) -> Unit,
) {
    var generatorType by remember { mutableStateOf(generator.getType()) }
    var centerX by remember { mutableStateOf(generator.centerX.toString()) }
    var centerY by remember { mutableStateOf(generator.centerY.toString()) }
    var range by remember { mutableStateOf(generator.range.toString()) }
    var bitmapSize by remember { mutableStateOf(generator.bitmapSize.toString()) }
    var maxIter by remember { mutableStateOf(generator.maxIter.toString()) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                OutlinedTextField(
                    label = { Text("Center X") },
                    placeholder = { Text("0.0") },
                    value = centerX.toString(),
                    onValueChange = { centerX = it },
                    isError = centerX.toDoubleOrNull() == null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(5.dp),
                )

                OutlinedTextField(
                    label = { Text("Center Y") },
                    placeholder = { Text("0.0") },
                    value = centerY.toString(),
                    onValueChange = { centerY = it },
                    isError = centerY.toDoubleOrNull() == null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(5.dp),
                )
            }

            OutlinedTextField(
                label = { Text("Range") },
                placeholder = { Text("3.0") },
                value = range.toString(),
                onValueChange = { range = it },
                isError = range.toDoubleOrNull() == null,
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
            )

            OutlinedTextField(
                label = { Text("Bitmap Size") },
                placeholder = { Text("512") },
                value = bitmapSize.toString(),
                onValueChange = { bitmapSize = it },
                isError = !checkBitmapSize(bitmapSize),
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
            )

            OutlinedTextField(
                label = { Text("Max Iterations") },
                placeholder = { Text("100") },
                value = maxIter.toString(),
                onValueChange = { maxIter = it },
                isError = !checkMaxIter(maxIter),
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    value = generatorType,
                    onValueChange = {},
                    label = { Text("Generator Type") },
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("rust") },
                        onClick = {
                            expanded = false
                            generatorType = "rust"
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("kotlin") },
                        onClick = {
                            expanded = false
                            generatorType = "kotlin"
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                val gen = MandelbrotGenerator.fromType(generatorType)
                gen.centerX = centerX.toDoubleOrNull()!!
                gen.centerY = centerY.toDoubleOrNull()!!
                gen.range = range.toDoubleOrNull()!!
                gen.bitmapSize = bitmapSize.toIntOrNull()!!
                gen.maxIter = maxIter.toIntOrNull()!!

                if (!checkMaxIter(maxIter) || !checkBitmapSize(bitmapSize)) {
                    throw RuntimeException("unreachable")
                }

                onMandelbrotGeneratorChanged(gen)
            },
            enabled = true &&
                centerX.toDoubleOrNull() != null &&
                centerY.toDoubleOrNull() != null &&
                range.toDoubleOrNull() != null &&
                checkBitmapSize(bitmapSize) &&
                checkMaxIter(maxIter),

            modifier = Modifier.padding(5.dp)
        ) {
            Text("Apply")
        }
    }
}

private fun checkBitmapSize(bitmapSize: String): Boolean {
    val v = bitmapSize.toIntOrNull()
    return v != null && v >= 1
}

private fun checkMaxIter(maxIter: String): Boolean {
    val v = maxIter.toIntOrNull()
    return v != null && v >= 1
}
