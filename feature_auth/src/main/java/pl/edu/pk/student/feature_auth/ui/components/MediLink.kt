package pl.edu.pk.student.feature_auth.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun MediLink(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
){
    Text(
        text = text,
        modifier = modifier
            .clickable(onClick = onClick),
        style = MaterialTheme.typography.titleSmall.copy(
            textDecoration = TextDecoration.Underline
        ),
        color = MaterialTheme.colorScheme.primary,
    )
}