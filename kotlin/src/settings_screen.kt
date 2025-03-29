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
    var centerX by remember { mutableStateOf(generator.centerX) }
    var centerY by remember { mutableStateOf(generator.centerY) }
    var range by remember { mutableStateOf(generator.range) }
    var bitmapSize by remember { mutableStateOf(generator.bitmapSize) }
    var maxIter by remember { mutableStateOf(generator.maxIter) }
    
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
                    onValueChange = {
                        val v = it.toDoubleOrNull()
                        if (v != null) {
                            centerX = v
                        }
                    },
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
                    onValueChange = {
                        val v = it.toDoubleOrNull()
                        if (v != null) {
                            centerY = v
                        }
                    },
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
                onValueChange = {
                    val v = it.toDoubleOrNull()
                    if (v != null && v > 0) {
                        range = v
                    }
                },
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
                onValueChange = {
                    val v = it.toIntOrNull()
                    if (v != null && bitmapSize > 1) {
                        bitmapSize = v
                    }
                },
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
                onValueChange = {
                    val v = it.toIntOrNull()
                    if (v != null && maxIter > 1) {
                        maxIter = v
                    }
                },
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
                gen.centerX = centerX
                gen.centerY = centerY
                gen.range = range
                gen.bitmapSize = bitmapSize
                gen.maxIter = maxIter
                onMandelbrotGeneratorChanged(gen)
            },
            modifier = Modifier.padding(5.dp)
        ) {
            Text("Apply")
        }
    }
}
