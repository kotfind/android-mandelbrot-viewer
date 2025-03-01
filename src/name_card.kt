package com.kotfind.android_course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource

@Composable
fun NameCard() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.profile_pic),
            contentDescription = "profile pic",
            modifier = Modifier.size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
        )

        Column(modifier = Modifier.padding(all = 10.dp)) {
            Text(
                text = LocalContext.current.getString(R.string.author_name),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                text = LocalContext.current.getString(R.string.author_group),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}
