package com.example.spawn_app_android.presentation.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.theme.spawnIndigo
import com.example.spawn_app_android.presentation.theme.white

@Composable
fun SpawnButton(
    modifier: Modifier = Modifier,
    iconRes: Int,
    buttonText: String,
    txtColor: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(id = iconRes),
                contentDescription = null,
                tint = txtColor,
            )
            Text(
                text = buttonText,
                color = txtColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 17.sp,
                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
            )
        }
    }
}

@Preview
@Composable
fun SpawnButtonPreview() {
    SpawnButton(
        iconRes = R.drawable.ic_spawn_button,
        buttonText = "Spawn In!",
        txtColor = spawnIndigo,
        bgColor = white,
        onClick = {}
    )
}
