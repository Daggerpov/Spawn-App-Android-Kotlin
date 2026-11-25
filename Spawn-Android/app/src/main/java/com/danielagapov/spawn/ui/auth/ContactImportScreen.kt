package com.danielagapov.spawn.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danielagapov.spawn.core.model.BaseUserDTO
import com.danielagapov.spawn.core.model.ContactCrossReferenceRequestDTO
import com.danielagapov.spawn.core.network.bodyOrThrow
import com.danielagapov.spawn.core.network.friendlyMessage
import com.danielagapov.spawn.core.session.Spawn
import com.danielagapov.spawn.core.util.DeviceContacts
import com.danielagapov.spawn.ui.components.OutlinePillButton
import com.danielagapov.spawn.ui.components.PrimaryButton
import com.danielagapov.spawn.ui.components.UserAvatar
import com.danielagapov.spawn.ui.theme.Palette
import com.danielagapov.spawn.ui.theme.SpawnTheme
import com.danielagapov.spawn.ui.theme.onest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Sign-up "Bring your Friends" step (Figma Contact Import 2328:2676 / Contact Import Selected 2429:2409, iOS
 * ContactImportView): finds the contacts already on Spawn. People the user adds get friend requests once the terms are
 * accepted, because the backend only takes friend requests from active accounts.
 */
class ContactImportViewModel : ViewModel() {
    enum class Phase { Loading, NoPermission, Ready }

    var phase by mutableStateOf(Phase.Loading)
        private set
    var matches by mutableStateOf<List<BaseUserDTO>>(emptyList())
        private set
    /** Ids of matched users the user added, in the order they were added. */
    var added by mutableStateOf<List<String>>(emptyList())
        private set
    var saving by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    private var loaded = false

    fun noPermission() { if (!loaded) phase = Phase.NoPermission }

    fun load(context: Context) {
        if (loaded) return
        loaded = true
        phase = Phase.Loading
        val me = Spawn.currentUser ?: return
        viewModelScope.launch {
            matches = try {
                val numbers = withContext(Dispatchers.IO) { DeviceContacts.phoneNumbers(context.applicationContext) }
                if (numbers.isEmpty()) emptyList()
                else Spawn.api.crossReferenceContacts(ContactCrossReferenceRequestDTO(numbers, me.id)).bodyOrThrow().users
                    .distinctBy { it.id }.sortedBy { it.displayName.lowercase() }
            } catch (e: Exception) {
                Log.w("ContactImport", "matching contacts failed", e)
                emptyList()
            }
            phase = Phase.Ready
        }
    }

    fun toggle(id: String) { added = if (id in added) added - id else added + id }

    /** Marks the step done on the server and hands back who to friend (nobody when skipping). */
    fun finish(skip: Boolean, onDone: (friendIds: List<String>) -> Unit) {
        if (saving) return
        val me = Spawn.currentUser ?: return
        saving = true
        error = null
        viewModelScope.launch {
            try {
                Spawn.updateUser(Spawn.api.completeContactImport(me.id).bodyOrThrow())
                onDone(if (skip) emptyList() else added)
            } catch (e: Exception) {
                error = e.friendlyMessage()
            } finally {
                saving = false
            }
        }
    }
}

@Composable
fun ContactImportScreen(onDone: (friendIds: List<String>) -> Unit, vm: ContactImportViewModel = viewModel()) {
    val context = LocalContext.current
    val c = SpawnTheme.colors
    var query by rememberSaveable { mutableStateOf("") }
    var showAdded by rememberSaveable { mutableStateOf(true) }
    var showSuggested by rememberSaveable { mutableStateOf(true) }
    // After a second "Don't allow" Android stops showing the prompt, so the button opens app settings instead.
    var blocked by rememberSaveable { mutableStateOf(false) }

    val hasPermission = { ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.load(context) else {
            vm.noPermission()
            blocked = context.findActivity()?.let { !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_CONTACTS) } ?: false
        }
    }
    LaunchedEffect(Unit) { if (hasPermission()) vm.load(context) else permission.launch(Manifest.permission.READ_CONTACTS) }
    // Coming back from app settings with access turned on.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME && hasPermission()) vm.load(context) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val q = query.trim()
    val shown = vm.matches.filter {
        q.isEmpty() || it.displayName.contains(q, ignoreCase = true) || (it.username ?: "").contains(q.removePrefix("@"), ignoreCase = true)
    }
    val addedUsers = vm.added.mapNotNull { id -> shown.find { it.id == id } }
    val suggested = shown.filter { it.id !in vm.added }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
    ) {
        Box(Modifier.height(56.dp).fillMaxWidth()) {
            Text(
                "Skip for now", style = onest(14.sp, FontWeight.Medium), color = c.textSecondary,
                modifier = Modifier.align(Alignment.CenterEnd).clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !vm.saving) { vm.finish(skip = true, onDone) }.padding(8.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text("Bring your Friends", style = onest(28.sp, FontWeight.Bold), color = c.text, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        Text(
            "Import your contacts to invite friends directly to Spawn.", style = onest(16.sp), color = c.textSecondary,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            query, { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search contacts", style = onest(16.sp), color = c.placeholder) },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = c.placeholder) },
            singleLine = true,
            textStyle = onest(16.sp).copy(color = c.text),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = c.placeholder, focusedBorderColor = Palette.Indigo500, cursorColor = Palette.Indigo500,
            ),
        )
        Spacer(Modifier.height(16.dp))
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                vm.phase == ContactImportViewModel.Phase.Loading ->
                    CircularProgressIndicator(color = Palette.Indigo500, modifier = Modifier.align(Alignment.Center).size(32.dp))
                vm.phase == ContactImportViewModel.Phase.NoPermission -> Column(
                    Modifier.align(Alignment.Center).padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Allow access to your contacts to find friends who are already on Spawn.",
                        style = onest(15.sp), color = c.textSecondary, textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinePillButton(if (blocked) "Open settings" else "Allow contacts", {
                        if (blocked) context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        ) else permission.launch(Manifest.permission.READ_CONTACTS)
                    })
                }
                vm.matches.isEmpty() -> Text(
                    "None of your contacts are on Spawn yet. You can add friends later from the Friends tab.",
                    style = onest(15.sp), color = c.textSecondary, textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 8.dp),
                )
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                    if (addedUsers.isNotEmpty()) {
                        contactSection("Added (${addedUsers.size})", showAdded, { showAdded = !showAdded }, addedUsers, true, vm::toggle)
                    }
                    if (suggested.isNotEmpty()) {
                        contactSection("Suggested", showSuggested, { showSuggested = !showSuggested }, suggested, false, vm::toggle)
                    }
                    if (shown.isEmpty()) item {
                        Text("No contacts match “$q”.", style = onest(15.sp), color = c.textSecondary, modifier = Modifier.padding(vertical = 12.dp))
                    }
                }
            }
        }
        if (vm.error != null) {
            Text(vm.error!!, style = onest(14.sp), color = Palette.Red600, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }
        PrimaryButton("Continue", { vm.finish(skip = false, onDone) }, enabled = vm.added.isNotEmpty(), loading = vm.saving)
        Spacer(Modifier.height(16.dp))
    }
}

private fun LazyListScope.contactSection(
    title: String,
    expanded: Boolean,
    onToggleSection: () -> Unit,
    users: List<BaseUserDTO>,
    added: Boolean,
    onToggle: (String) -> Unit,
) {
    item(key = "header-$title-$added") {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onToggleSection).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = onest(16.sp, FontWeight.Medium), color = SpawnTheme.colors.textSecondary, modifier = Modifier.weight(1f))
            Icon(if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, if (expanded) "Collapse" else "Expand", tint = SpawnTheme.colors.text)
        }
    }
    if (expanded) items(users, key = { it.id }) { user -> ContactRow(user, added) { onToggle(user.id) } }
}

@Composable
private fun ContactRow(user: BaseUserDTO, added: Boolean, onToggle: () -> Unit) {
    val c = SpawnTheme.colors
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(user, 36.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(user.displayName, style = onest(14.sp, FontWeight.SemiBold), color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(user.handle, style = onest(12.sp), color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (added) Icon(Icons.Filled.CheckCircle, "Added", tint = Palette.Green500, modifier = Modifier.size(28.dp))
        else Icon(Icons.Outlined.AddCircleOutline, "Add ${user.displayName}", tint = c.textSecondary, modifier = Modifier.size(28.dp))
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
