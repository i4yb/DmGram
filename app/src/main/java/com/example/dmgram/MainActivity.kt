package com.example.dmgram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- Data ----------
class Chat(
    val name: String,
    val color: Color,
    val hasStory: Boolean,
    val note: String? = null,
    val msgs: SnapshotStateList<Pair<Boolean, String>> = mutableStateListOf()
)

class Instant(val from: String, val color: Color, val text: String)

val palette = listOf(Color(0xFFE1306C), Color(0xFF833AB4), Color(0xFFF77737), Color(0xFF1DA1F2), Color(0xFF2ECC71), Color(0xFFFFB300))

// ---------- Activity ----------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                Surface(Modifier.fillMaxSize()) { App() }
            }
        }
    }
}

@Composable
fun App() {
    val chats = remember {
        listOf(
            Chat("maya", palette[0], true, "coffee anyone? ☕", mutableStateListOf(false to "hey!", true to "hi maya 👋")),
            Chat("leo", palette[1], true, null, mutableStateListOf(false to "did you see the game?")),
            Chat("sara", palette[2], false, "on a plane ✈️", mutableStateListOf(false to "landing at 6")),
            Chat("omar", palette[3], true, null, mutableStateListOf(true to "send me the pic")),
            Chat("nour", palette[4], true, "studying 📚", mutableStateListOf(false to "call later?")),
            Chat("ali", palette[5], false)
        )
    }
    val instants = remember { mutableStateListOf(Instant("maya", palette[0], "📸 lunch")) }
    var myNote by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var openChat by remember { mutableStateOf<Chat?>(null) }
    var openStory by remember { mutableStateOf<Chat?>(null) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(bottomBar = {
            NavigationBar {
                listOf("Stories" to Icons.Default.Star, "Messages" to Icons.Default.Email, "Account" to Icons.Default.Person)
                    .forEachIndexed { i, (label, icon) ->
                        NavigationBarItem(selected = tab == i, onClick = { tab = i },
                            icon = { Icon(icon, label) }, label = { Text(label) })
                    }
            }
        }) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                when (tab) {
                    0 -> StoriesTab(chats) { openStory = it }
                    1 -> MessagesTab(chats, myNote, { myNote = it }, instants) { openChat = it }
                    else -> AccountTab()
                }
            }
        }
        openChat?.let { ChatScreen(it) { openChat = null } }
        openStory?.let { StoryViewer(it) { openStory = null } }
    }
}

// ---------- Shared ----------
@Composable
fun Avatar(color: Color, name: String, size: Dp, ring: Boolean = false) {
    val base = if (ring) Modifier.border(3.dp, Brush.sweepGradient(listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF), Color(0xFFF58529))), CircleShape).padding(5.dp) else Modifier
    Box(Modifier.size(size).then(base)) {
        Box(Modifier.fillMaxSize().clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
            Text(name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value / 2.5).sp)
        }
    }
}

@Composable
fun Label(t: String) = Text(t, Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp)

@Composable
fun TextDialog(title: String, onDone: (String) -> Unit, onDismiss: () -> Unit) {
    var t by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = { OutlinedTextField(t, { t = it.take(60) }, singleLine = true) },
        confirmButton = { TextButton({ if (t.isNotBlank()) onDone(t.trim()); onDismiss() }) { Text("Share") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

// ---------- Tab 1: Stories only ----------
@Composable
fun StoriesTab(chats: List<Chat>, onOpen: (Chat) -> Unit) {
    Column {
        Text("Stories", Modifier.padding(16.dp), fontSize = 26.sp, fontWeight = FontWeight.Bold)
        val withStory = chats.filter { it.hasStory }
        LazyVerticalGrid(GridCells.Fixed(3), contentPadding = PaddingValues(8.dp)) {
            gridItems(withStory) { c ->
                Column(Modifier.padding(8.dp).clickable { onOpen(c) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Avatar(c.color, c.name, 88.dp, ring = true)
                    Text(c.name, Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
fun StoryViewer(c: Chat, onClose: () -> Unit) {
    var i by remember { mutableIntStateOf(0) }
    BackHandler(onBack = onClose)
    Box(Modifier.fillMaxSize().background(c.color).clickable { if (i < 2) i++ else onClose() }) {
        Row(Modifier.statusBarsPadding().padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) {
                Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(Color.White.copy(alpha = if (it <= i) 1f else 0.4f)))
            }
        }
        Text(c.name, Modifier.align(Alignment.TopStart).statusBarsPadding().padding(top = 28.dp, start = 16.dp), color = Color.White, fontWeight = FontWeight.Bold)
        Text("${c.name}'s story ${i + 1}/3", Modifier.align(Alignment.Center), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------- Tab 2: DMs, notes, instants ----------
@Composable
fun MessagesTab(chats: List<Chat>, myNote: String?, onNote: (String) -> Unit, instants: SnapshotStateList<Instant>, onOpen: (Chat) -> Unit) {
    var noteDlg by remember { mutableStateOf(false) }
    var instDlg by remember { mutableStateOf(false) }
    if (noteDlg) TextDialog("Share a note", onNote) { noteDlg = false }
    if (instDlg) TextDialog("Send an instant", { instants.add(0, Instant("you", palette[instants.size % palette.size], it)) }) { instDlg = false }

    LazyColumn(Modifier.fillMaxSize()) {
        item { Text("Messages", Modifier.padding(16.dp), fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        item { Label("Notes") }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 12.dp)) {
                item { NoteItem("Your note", myNote ?: "Share a note", Color.Gray) { noteDlg = true } }
                items(chats.filter { it.note != null }) { NoteItem(it.name, it.note!!, it.color) { onOpen(it) } }
            }
        }
        item { Label("Instants") }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Box(Modifier.size(90.dp, 120.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { instDlg = true }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, "Add instant")
                    }
                }
                items(instants) {
                    Box(Modifier.size(90.dp, 120.dp).clip(RoundedCornerShape(14.dp)).background(it.color).padding(8.dp)) {
                        Text(it.from, color = Color.White, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopStart))
                        Text(it.text, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart))
                    }
                }
            }
        }
        item { Label("Chats") }
        items(chats) { c ->
            Row(Modifier.fillMaxWidth().clickable { onOpen(c) }.padding(16.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(c.color, c.name, 52.dp, ring = c.hasStory)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(c.name, fontWeight = FontWeight.SemiBold)
                    Text(c.msgs.lastOrNull()?.second ?: "Say hi 👋", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun NoteItem(name: String, text: String, color: Color, onClick: () -> Unit) {
    Column(Modifier.width(84.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 4.dp) {
            Text(text, Modifier.padding(8.dp, 4.dp), fontSize = 11.sp, maxLines = 2)
        }
        Spacer(Modifier.height(4.dp))
        Avatar(color, name, 56.dp)
        Text(name, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
fun ChatScreen(c: Chat, onClose: () -> Unit) {
    var input by remember { mutableStateOf("") }
    BackHandler(onBack = onClose)
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.statusBarsPadding().navigationBarsPadding().imePadding()) {
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClose) { Icon(Icons.Default.ArrowBack, "Back") }
                Avatar(c.color, c.name, 36.dp)
                Text(c.name, Modifier.padding(start = 10.dp), fontWeight = FontWeight.Bold)
            }
            LazyColumn(Modifier.weight(1f).padding(horizontal = 12.dp), reverseLayout = true, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(c.msgs.reversed()) { (mine, text) ->
                    Box(Modifier.fillMaxWidth(), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
                        Text(text, Modifier.clip(RoundedCornerShape(18.dp)).background(if (mine) Color(0xFF3797F0) else MaterialTheme.colorScheme.surfaceVariant).padding(12.dp, 8.dp),
                            color = if (mine) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("Message…") }, shape = RoundedCornerShape(24.dp))
                IconButton({ if (input.isNotBlank()) { c.msgs.add(true to input.trim()); input = "" } }) { Icon(Icons.Default.Send, "Send") }
            }
        }
    }
}

// ---------- Tab 3: Account & settings ----------
@Composable
fun AccountTab() {
    var username by remember { mutableStateOf("your_username") }
    var private by remember { mutableStateOf(false) }
    var receipts by remember { mutableStateOf(true) }
    var activity by remember { mutableStateOf(true) }
    var notif by remember { mutableStateOf(true) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(Color(0xFF8134AF), username, 96.dp, ring = true)
                OutlinedTextField(username, { username = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' || ch == '.' } },
                    Modifier.padding(top = 12.dp), label = { Text("Username") }, singleLine = true)
            }
        }
        item { Text("Settings", Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        item { SettingRow("Private account", private) { private = it } }
        item { SettingRow("Read receipts", receipts) { receipts = it } }
        item { SettingRow("Show activity status", activity) { activity = it } }
        item { SettingRow("Message notifications", notif) { notif = it } }
        item { OutlinedButton({}, Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Log out") } }
    }
}

@Composable
fun SettingRow(title: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f))
        Switch(value, onChange)
    }
}
