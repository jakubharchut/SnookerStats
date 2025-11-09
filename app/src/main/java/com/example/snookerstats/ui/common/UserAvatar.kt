package com.example.snookerstats.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.snookerstats.domain.model.User
import coil.compose.rememberAsyncImagePainter

@Composable
fun UserAvatar(
    user: User,
    modifier: Modifier = Modifier
) {
    if (user.profileImageUrl.isNullOrBlank()) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar",
            modifier = modifier
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.profileImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar użytkownika ${user.username}",
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = rememberAsyncImagePainter(model = Icons.Default.AccountCircle)
        )
    }
}
