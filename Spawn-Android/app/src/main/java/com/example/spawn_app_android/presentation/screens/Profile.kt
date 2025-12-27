package com.example.spawn_app_android.presentation.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.spawn_app_android.R
import com.example.spawn_app_android.presentation.theme.SpawnAppAndroidTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MaterialTheme
import com.example.spawn_app_android.presentation.screens.Utils.getNotifBarPadding

// preview
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage() {

    val image = painterResource(R.drawable.daniel_lee)
    val name = "Daniel Lee"
    val tag = "daniellee"
    val hobbies = listOf("Poker", "League", "Leetcode", "Raving", "Basketball")
    val stats = mapOf("People Met" to 49, "Spawns Made" to 4, "Spawns Joined" to 16)

    SpawnAppAndroidTheme {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .padding(top = getNotifBarPadding())
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VerticalSpacer(20)
            ProfileHead(image, name, tag)
            VerticalSpacer(20)
            ProfileButtons()
            HobbiesBlock(hobbies)
            VerticalSpacer(15)
            StatsBlock(stats)
            VerticalSpacer(20)
            Calendar()

        }
    }
}

// takes in a reduced user object with only pfp, name
@Composable
fun ProfileHead(image: Painter, name: String, tag: String) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier // Use a new Modifier instance here
                .size(128.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = name,
            color = Color(0xFF1B1B1B),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "@" + tag,
            color = Color(0xFF857C7C)
        )

    }

}

@Composable
fun ProfileButtons() {

    Row() {
        ProfileButton("Edit Profile", Icons.Outlined.Edit)
        Spacer(modifier = Modifier.width(10.dp))
        ProfileButton("Share Profile", Icons.Outlined.Share)
    }

}

@Composable
fun ProfileButton(label: String, icon: ImageVector) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(30.dp)
            .border(1.dp, Color(0xFF6B81FB), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6B81FB),
                modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = label, fontSize = 16.sp, color = Color(0xFF6B81FB))
        }
    }
}

@Composable
fun HobbiesBlock(hobbies: List<String>) {

    val randomX = listOf(0.dp, 100.dp, 50.dp, 200.dp, 250.dp)
    val randomY = listOf(20.dp, 10.dp, 35.dp, 55.dp)
    val socials = mapOf("Instagram" to "https://www.instagram.com/_daniel__lee_/")

    Row (
        modifier = Modifier
            .offset(y = 25.dp)
            .zIndex(1f)
    ) {
        Box(modifier = Modifier.absoluteOffset(y = 10.dp)) {
            Row(
                modifier = Modifier
                    .zIndex(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFF7270))
                    .padding(5.dp)
            ) {
                Text(text = "Interests + Hobbies", color = Color(0xFFFFFFFF), fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.width(50.dp))
        Socials(socials)
    }
    Box(
        modifier = Modifier
            .border(1.dp, Color(0xFFFF7270), RoundedCornerShape(10.dp))
            .width(400.dp)
            .height(100.dp)
            .padding(15.dp),

    ) {
        hobbies.forEachIndexed { i, text ->
            Text(
                text,
                modifier = Modifier
                    .absoluteOffset(
                        x = randomX[i % randomX.size],
                        y = randomY[i % randomY.size]
                    ),
                fontSize = 16.sp

            )
        }
    }

}

@Composable
fun Socials(socials: Map<String, String>) {

    val rotationMax = 15

    Row(modifier = Modifier.width(150.dp), horizontalArrangement = Arrangement.End) {

        if (socials.contains("Instagram")) {
            Image(
                painter = painterResource(R.drawable.instagram_icon),
                contentDescription = null,
                modifier = Modifier
                    .width(55.dp)
                    .height(55.dp)
                    .padding(10.dp)
                    .rotate((-rotationMax..rotationMax).random().toFloat())
            )
        }

        if (socials.contains("Instagram")) {
            Image(
                painter = painterResource(R.drawable.instagram_icon),
                contentDescription = null,
                modifier = Modifier
                    .width(55.dp)
                    .height(55.dp)
                    .padding(10.dp)
                    .rotate((-rotationMax..rotationMax).random().toFloat())
            )
        }

    }

}

@Composable
fun StatsBlock(stats: Map<String, Int>) {

    Row (
        modifier = Modifier
            .width(400.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        for (entry in stats) {
            StatComponent(entry.key, entry.value)
        }
    }

}

@Composable
fun StatComponent(statName: String, stat: Int) {
    Column (
        modifier = Modifier
            .width(110.dp)
            .padding(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(text = stat.toString(), color = Color(0xFF857C7C))
        }
        Text(text = statName,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF857C7C))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Calendar() {

    val date = LocalDate.now()
    val monthLength = YearMonth.now().lengthOfMonth()
    val monthFirstDay = YearMonth.now().atDay(1).dayOfWeek
    var monthFirstDayIndex = 0
    val monthLastDay = YearMonth.now().atEndOfMonth().dayOfWeek
    var monthLastDayIndex = 0
    val first = YearMonth.now().atDay(1)
    val last = YearMonth.now().atEndOfMonth()
    val start = first.with(DayOfWeek.MONDAY)
    val end = last.with(DayOfWeek.SUNDAY)
    val numWeeks = ChronoUnit.WEEKS.between(start, end.plusDays(1))

    val DaysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    monthFirstDayIndex = when (monthFirstDay) {

        DayOfWeek.MONDAY -> { -1 }
        DayOfWeek.TUESDAY -> { -2 }
        DayOfWeek.WEDNESDAY -> { -3 }
        DayOfWeek.THURSDAY -> { -4 }
        DayOfWeek.FRIDAY -> { -5 }
        DayOfWeek.SATURDAY -> { -6 }
        DayOfWeek.SUNDAY -> { 0 }
        null -> { -999 }

    }

    monthLastDayIndex = when (monthLastDay) {

        DayOfWeek.MONDAY -> { 1 }
        DayOfWeek.TUESDAY -> { 2 }
        DayOfWeek.WEDNESDAY -> { 3 }
        DayOfWeek.THURSDAY -> { 4 }
        DayOfWeek.FRIDAY -> { 5 }
        DayOfWeek.SATURDAY -> { 6 }
        DayOfWeek.SUNDAY -> { 0 }
        null -> { -999 }

    }


    Column() {

        for (i in 0 until numWeeks + 1) {
            
            if (i == 0.toLong()) {
                Row() {
                    for (j in 0 until 7) {
                        Text(
                            modifier = Modifier
                                .padding(2.dp)
                                .width(35.dp)
                                .height(20.dp),
                            textAlign = TextAlign.Center,
                            text =  "" + DaysOfWeek[j],
                            fontSize = 12.sp,
                            color = Color(0xFF8E8484)
                        )

                    }
                }
                continue
            }

            Row() {

                for (j in 0 until 7) {
                    if (i == 1.toLong()) {
                        if (monthFirstDayIndex + j < 0) {
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .width(35.dp)
                                    .height(35.dp)
                                    .border(1.dp, Color(0xFFDCD6D6), RoundedCornerShape(5.dp))
                            )
                            continue
                        }
                    }
                    if (i == numWeeks) {
                        if (j > monthLastDayIndex) {
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .width(35.dp)
                                    .height(35.dp)
                                    .border(1.dp, Color(0xFFDCD6D6), RoundedCornerShape(5.dp))
                            )
                            continue
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .width(35.dp)
                            .height(35.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFDCD6D6))
                    )
                }
    

            }

        }

    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true,
    showSystemUi = true,
    name = "Profile")
@Composable
fun PreviewEventPage() {
    ProfilePage()
}