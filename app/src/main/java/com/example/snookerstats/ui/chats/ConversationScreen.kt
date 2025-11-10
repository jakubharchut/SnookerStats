package com.example.snookerstats.ui.chats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.Message
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.util.Resource
import java.text.SimpleDateFormat
import java.util.*

// Helper extension function to group consecutive items by a key
fun <T, K> List<T>.groupByConsecutive(keySelector: (T) -> K): List<List<T>> {
    if (this.isEmpty()) return emptyList()
    val result = mutableListOf<MutableList<T>>()
    var currentGroup = mutableListOf(this.first())
    result.add(currentGroup)
    for (i in 1 until this.size) {
        if (keySelector(this[i]) == keySelector(this[i - 1])) {
            currentGroup.add(this[i])
        } else {
            currentGroup = mutableListOf(this[i])
            result.add(currentGroup)
        }
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    navController: NavController,
    viewModel: ConversationViewModel = hiltViewModel(),
    authRepository: IAuthRepository
) {
    val messagesState by viewModel.messagesState.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val otherUser by viewModel.otherUser.collectAsState()
    val listState = rememberLazyListState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }


    val currentUserId = authRepository.currentUser?.uid

    DisposableEffect(Unit) {
        viewModel.setUserPresence(true)
        onDispose {
            viewModel.setUserPresence(false)
        }
    }

    LaunchedEffect(messagesState) {
        if (messagesState is Resource.Success) {
            val messages = (messagesState as Resource.Success<List<Message>>).data
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(index = messages.size - 1)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Potwierdź usunięcie") },
            text = { Text("Czy na pewno chcesz trwale usunąć tę konwersację? Ta operacja jest nieodwracalna.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteChat()
                        showDeleteDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    otherUser?.let { user ->
                        val title = "${user.firstName} ${user.lastName}"
                        val subtitle = "@${user.username}"
                        Column {
                            Text(text = title, fontWeight = FontWeight.Bold)
                            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    otherUser?.let { user ->
                        IconButton(onClick = { navController.navigate("user_profile/${user.uid}") }) {
                            Icon(Icons.Default.Person, contentDescription = "Zobacz profil")
                        }
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Więcej opcji")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Usuń czat") },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = messagesState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val messageGroups = remember(state.data) {
                        state.data.groupByConsecutive { it.senderId }
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messageGroups) { group ->
                            MessageGroup(
                                messages = group,
                                isSentByCurrentUser = group.first().senderId == currentUserId
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("Błąd: ${state.message}")
                    }
                }
            }

            MessageInput(
                value = messageText,
                onValueChange = viewModel::onMessageChange,
                onSendClick = viewModel::sendMessage
            )
        }
    }
}

@Composable
fun MessageGroup(messages: List<Message>, isSentByCurrentUser: Boolean) {
    val alignment = if (isSentByCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = if (isSentByCurrentUser) Alignment.End else Alignment.Start) {
            messages.forEach { message ->
                MessageBubble(
                    message = message,
                    isSentByCurrentUser = isSentByCurrentUser
                )
            }
            Text(
                text = dateFormat.format(messages.last().timestamp.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun MessageBubble(message: Message, isSentByCurrentUser: Boolean) {
    val backgroundColor = if (isSentByCurrentUser) Color(0xFFD0E4FF) else Color(0xFFEBEBEB)
    val bubbleShape = if (isSentByCurrentUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Surface(
        color = backgroundColor,
        shape = bubbleShape,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = message.text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = Color.Black
        )
    }
}


@Composable
fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Wpisz wiadomość...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendClick() })
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSendClick, enabled = value.isNotBlank()) {
            Icon(Icons.Default.Send, contentDescription = "Wyślij")
        }
    }
}
