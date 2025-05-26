package com.example.spawn_app_android.presentation.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.theme.SpawnAppAndroidTheme

// TODO: could be made more abstract, lots of reused code
@Composable
fun EventMapToggle(mapState: Boolean, height: Int, onNavigatePage: () -> Unit) {

    var state = mapState

    Box(
        modifier = Modifier
            .width(110.dp)
            .height(height.dp)
            .background(
                color = Color.Black,
                shape = RoundedCornerShape(100)
            )
            .border(
                width = 4.dp,
                color = Color.Black,
                shape = RoundedCornerShape(100)
            )
            .clickable(
                onClick = onNavigatePage
            )
    ) {
        // check toggle state
        if (state) {
            // map state -> globe is highlighted
            Row {
                Box(
                    modifier = Modifier
                        .width(55.dp)
                        .padding(4.dp)
                        .height((height - 4).dp)

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.list_icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFFE7E7DD)),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(30.dp)
                            .height(30.dp)

                    )
                }
                Box(
                    modifier = Modifier
                        .width(55.dp)
                        .padding(4.dp)
                        .height((height - 4).dp)
                        .background(
                            color = Color(0xFFE7E7DD),
                            shape = RoundedCornerShape(100)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.globe_icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFF163030)),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(30.dp)
                            .height(30.dp)

                    )
                }
            }
        } else {
            // list state -> list highlighted
            Row {
                Box(
                    modifier = Modifier
                        .width(55.dp)
                        .padding(4.dp)
                        .height((height - 4).dp)
                        .background(
                            color = Color(0xFFE7E7DD),
                            shape = RoundedCornerShape(100)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.list_icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFF163030)),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(30.dp)
                            .height(30.dp)

                    )
                }
                Box(
                    modifier = Modifier
                        .width(55.dp)
                        .padding(4.dp)
                        .height((height - 4).dp)

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.globe_icon),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFFE7E7DD)),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(30.dp)
                            .height(30.dp)

                    )
                }
            }
        }

    }

} // MapToggle

@Preview
@Composable
fun EventMapTogglePreview() {
    SpawnAppAndroidTheme {
        //EventMapToggle(false, 40)
    }
}