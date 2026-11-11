package com.danielagapov.spawn.ui.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danielagapov.spawn.BuildConfig
import com.danielagapov.spawn.R
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.ui.components.PrimaryButton
import com.danielagapov.spawn.ui.components.SpawnTextField
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest

@Composable
private fun AuthScaffold(
    onBack: (() -> Unit)?,
    title: String,
    subtitle: String,
    error: String?,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(SpawnTheme.colors.background)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
    ) {
        Box(Modifier.height(56.dp).fillMaxWidth()) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).offset(x = (-12).dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = SpawnTheme.colors.text)
                }
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(24.dp))
            Text(title, style = onest(28.sp, FontWeight.Bold), color = SpawnTheme.colors.text, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Text(subtitle, style = onest(16.sp), color = SpawnTheme.colors.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(32.dp))
            content()
            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error, style = onest(14.sp), color = Palette.Red600, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
        bottom()
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(SpawnTheme.colors.background)
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Image(painterResource(R.drawable.spawn_wordmark), "Spawn", modifier = Modifier.width(150.dp))
        Spacer(Modifier.height(28.dp))
        Text("Welcome to Spawn", style = onest(28.sp, FontWeight.Bold), color = SpawnTheme.colors.text)
        Spacer(Modifier.height(6.dp))
        Text("Spontaneity made easy.", style = onest(17.sp), color = SpawnTheme.colors.textSecondary)
        Spacer(Modifier.weight(1.2f))
        PrimaryButton("Get Started", onGetStarted)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun GetInScreen(onCreateAccount: () -> Unit, onLogin: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(SpawnTheme.colors.background)
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Image(painterResource(R.drawable.spawn_wordmark), "Spawn", modifier = Modifier.width(96.dp))
        Spacer(Modifier.weight(1f))
        Text("Let’s Get You In", style = onest(28.sp, FontWeight.Bold), color = SpawnTheme.colors.text)
        Spacer(Modifier.height(8.dp))
        Text(
            "Join your friends or start a hang. It’s all happening here.",
            style = onest(16.sp), color = SpawnTheme.colors.textSecondary, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1.2f))
        PrimaryButton("Create an Account", onCreateAccount)
        Spacer(Modifier.height(20.dp))
        Text(
            buildAnnotatedString {
                append("Have an account already? ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline)) { append("Log in") }
            },
            style = onest(14.sp), color = SpawnTheme.colors.text,
            modifier = Modifier.clickable(onClick = onLogin).padding(8.dp),
        )
    }
}

@Composable
private fun OrDivider() {
    Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = SpawnTheme.colors.divider)
        Text("or", style = onest(13.sp), color = SpawnTheme.colors.textSecondary, modifier = Modifier.padding(horizontal = 12.dp))
        HorizontalDivider(Modifier.weight(1f), color = SpawnTheme.colors.divider)
    }
}

@Composable
private fun GoogleButton(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SpawnTheme.colors.input)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(R.drawable.logo_google), null, Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = onest(16.sp, FontWeight.Medium), color = SpawnTheme.colors.text)
    }
}

@Composable
fun LoginScreen(vm: AuthViewModel, onBack: () -> Unit) {
    var user by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    AuthScaffold(onBack, "Welcome Back", "Your plans are waiting — time to spawn.", vm.error) {
        SpawnTextField(user, { user = it; vm.clearError() }, "Enter your email or username", label = "Email / Username", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(16.dp))
        SpawnTextField(pass, { pass = it; vm.clearError() }, "Enter your password", label = "Password", isPassword = true)
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Continue", { vm.login(user, pass) }, enabled = user.isNotBlank() && pass.isNotBlank(), loading = vm.loading)
        OrDivider()
        GoogleButton("Sign in with Google") { vm.googleSignIn(context, isSignup = false) }
    }
}

@Composable
fun SignupEmailScreen(vm: AuthViewModel, onBack: () -> Unit, onCodeSent: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    AuthScaffold(onBack, "Create Your Account", "Choose how you’d like to set up your account.", vm.error) {
        SpawnTextField(email, { email = it; vm.clearError() }, "yourname@gmail.com", label = "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Continue", { vm.sendCode(email, onCodeSent) }, enabled = email.contains("@"), loading = vm.loading)
        OrDivider()
        GoogleButton("Sign up with Google") { vm.googleSignIn(context, isSignup = true) }
    }
}

@Composable
fun VerifyCodeScreen(vm: AuthViewModel, onBack: () -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    AuthScaffold(onBack, "Verify Your Email", "We’ve sent a 6-digit code to\n${vm.signupEmail}", vm.error) {
        Text("Verification Code", style = onest(14.sp, FontWeight.Medium), color = SpawnTheme.colors.text)
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = code,
            onValueChange = { v -> code = v.filter { it.isDigit() }.take(6); vm.clearError(); if (code.length == 6) vm.verifyCode(code) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.focusRequester(focus),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    repeat(6) { i ->
                        val ch = code.getOrNull(i)?.toString() ?: ""
                        val active = i == code.length
                        Box(
                            Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SpawnTheme.colors.input)
                                .border(if (active) 1.5.dp else 0.dp, if (active) Palette.Indigo500 else Color.Transparent, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text(ch, style = onest(22.sp, FontWeight.SemiBold), color = SpawnTheme.colors.text) }
                    }
                }
            },
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Verify", { vm.verifyCode(code) }, enabled = code.length == 6, loading = vm.loading)
        Spacer(Modifier.height(16.dp))
        Row {
            Text("Didn’t get it? ", style = onest(14.sp), color = SpawnTheme.colors.textSecondary)
            if (vm.resendSeconds > 0) {
                Text("Resend code in 0:%02d".format(vm.resendSeconds), style = onest(14.sp), color = SpawnTheme.colors.textSecondary)
            } else {
                Text(
                    "Resend code", style = onest(14.sp, FontWeight.SemiBold).copy(textDecoration = TextDecoration.Underline),
                    color = SpawnTheme.colors.text, modifier = Modifier.clickable { vm.resend() },
                )
            }
        }
    }
}

@Composable
fun AccountDetailsScreen(vm: AuthViewModel, needsPassword: Boolean, onDone: () -> Unit) {
    var username by rememberSaveable { mutableStateOf(Spawn.currentUser?.username ?: "") }
    var phone by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    val mismatch = needsPassword && confirm.isNotEmpty() && pass != confirm
    val e164 = remember(phone) {
        val digits = phone.filter { it.isDigit() }
        when {
            phone.trim().startsWith("+") -> "+$digits"
            digits.length == 10 -> "+1$digits"
            digits.length == 11 && digits.startsWith("1") -> "+$digits"
            else -> "+$digits"
        }
    }
    AuthScaffold(null, "Create Your Account", "Just a few details to get started.", vm.error, bottom = {
        PrimaryButton(
            "Continue",
            { vm.submitAccountDetails(username, if (needsPassword) pass else null, e164, onDone) },
            enabled = username.length >= 3 && phone.filter { it.isDigit() }.length >= 10 &&
                (!needsPassword || (pass.length >= 8 && pass == confirm)),
            loading = vm.loading,
        )
    }) {
        SpawnTextField(username, { username = it.trim(); vm.clearError() }, "yourusername", label = "Username")
        Spacer(Modifier.height(16.dp))
        SpawnTextField(phone, { phone = it; vm.clearError() }, "(604) 555-0123", label = "Phone Number", keyboardType = KeyboardType.Phone)
        if (needsPassword) {
            Spacer(Modifier.height(16.dp))
            SpawnTextField(pass, { pass = it; vm.clearError() }, "At least 8 characters", label = "Password", isPassword = true)
            Spacer(Modifier.height(16.dp))
            SpawnTextField(confirm, { confirm = it }, "Re-enter your password", label = "Confirm Password", isPassword = true, error = if (mismatch) "Passwords don’t match" else null)
        }
    }
}

@Composable
fun NameScreen(vm: AuthViewModel, onDone: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(Spawn.currentUser?.name ?: "") }
    AuthScaffold(null, "What’s Your Name?", "This is how your friends will see you on Spawn.", vm.error, bottom = {
        PrimaryButton("Continue", { vm.submitName(name, onDone) }, enabled = name.isNotBlank(), loading = vm.loading)
    }) {
        SpawnTextField(name, { name = it; vm.clearError() }, "Your name", label = "Name")
    }
}

@Composable
fun TermsScreen(vm: AuthViewModel) {
    val context = LocalContext.current
    var agreed by rememberSaveable { mutableStateOf(false) }
    AuthScaffold(null, "One Last Thing", "Spawn is for real plans with real friends. Keep it kind.", vm.error, bottom = {
        PrimaryButton("Let’s Spawn", { vm.acceptTerms() }, enabled = agreed, loading = vm.loading)
    }) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SpawnTheme.colors.input)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            listOf(
                "🤝" to "Only invite people you actually know.",
                "🛡️" to "No harassment, hate, or explicit content — we remove it and the accounts behind it.",
                "🚩" to "Report or block anyone, anytime, from their profile or a chat message.",
                "📍" to "Activity locations are only shared with the friends you invite.",
            ).forEach { (e, t) ->
                Row { Text(e, fontSize = 18.sp); Spacer(Modifier.width(10.dp)); Text(t, style = onest(15.sp), color = SpawnTheme.colors.text) }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { agreed = !agreed }) {
            Checkbox(agreed, { agreed = it }, colors = CheckboxDefaults.colors(checkedColor = Palette.Indigo500))
            Text(
                buildAnnotatedString {
                    append("I agree to the Terms of Service and ")
                    withStyle(SpanStyle(color = Palette.Indigo500, textDecoration = TextDecoration.Underline)) { append("Privacy Policy") }
                },
                style = onest(14.sp), color = SpawnTheme.colors.text,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.PRIVACY_POLICY_URL)))
                },
            )
        }
    }
}
