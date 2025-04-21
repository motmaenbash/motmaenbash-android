package nu.milad.motmaenbash.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri

@Composable
fun AppAlertDialog(
    title: String,
    icon: ImageVector,
    confirmText: String? = null,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    dismissText: String? = null,
    //message text, links and dynamic content
    message: String? = null,
    content: (@Composable () -> Unit)? = null,
    links: List<Pair<String, String>>? = null,
) {
    AlertDialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .padding(horizontal = 24.dp),
        containerColor = colorScheme.surface,
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp),
                    tint = colorScheme.primary
                )
                Text(
                    text = title,
                    style = typography.headlineSmall
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 0.dp)
            ) {
                // show body message
                if (message != null) {
                    Text(
                        text = message,
                        style = typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp),
                        fontSize = 14.sp
                    )
                }
                // show links
                if (links != null) {
                    val context = LocalContext.current
                    links.forEach { (title, link) ->
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, link.toUri())
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Text(
                                text = title,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                // show content if it's not null
                if (content != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    content()
                }
            }
        },

        confirmButton = {
            if (confirmText != null) {
                Button(
                    onClick = onConfirm,
                ) {
                    Text(
                        text = confirmText,
                        fontSize = 13.sp
                    )
                }
            }
        },
        dismissButton = if (dismissText != null) {
            {
                TextButton(
                    onClick = onDismiss,
                ) {
                    Text(
                        text = dismissText,
                        color = colorScheme.primary,
                        fontSize = 13.sp
                    )
                }
            }
        } else null,


        )
}
