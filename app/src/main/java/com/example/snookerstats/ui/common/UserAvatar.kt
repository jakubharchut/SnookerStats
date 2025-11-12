package com.example.snookerstats.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.example.snookerstats.domain.model.User

@Composable
fun UserAvatar(
    user: User?, // Zmieniono na nullable
    modifier: Modifier = Modifier
) {
    if (user?.profileImageUrl != null) {
        Image(
            painter = rememberAsyncImagePainter(user.profileImageUrl),
            contentDescription = "Avatar",
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else if (user != null) {
        // Fallback to initials if no image URL
        val initials = (user.firstName.firstOrNull()?.toString() ?: "") + (user.lastName.firstOrNull()?.toString() ?: "")
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    } else {
        // Fallback to default icon if user is null
        Image(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Default Avatar",
            modifier = modifier,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}
