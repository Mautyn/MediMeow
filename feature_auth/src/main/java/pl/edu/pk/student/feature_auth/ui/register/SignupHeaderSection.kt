package pl.edu.pk.student.feature_auth.ui.register

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.pk.student.feature_auth.ui.components.MediImage


@Composable
fun SignupHeaderSection(
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal,
    @DrawableRes imageRes: Int
){
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        Text(
            text = "Sing up",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(alignment)
        )
        Text(
            text = "Start your journey to better care",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(alignment)
        )
        Spacer(modifier = Modifier.height(12.dp))
        MediImage(
            imageRes,
            modifier
                .padding(horizontal = 40.dp)
                .align(alignment)
        )
    }
}