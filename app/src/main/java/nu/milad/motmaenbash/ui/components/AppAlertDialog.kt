package nu.milad.motmaenbash.ui.components

import PermissionGuideDialog
import UpdateDialog
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import nu.milad.motmaenbash.ui.activities.TutorialDialog
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.viewmodels.MainViewModel

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
        containerColor = colorScheme.surface,
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
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
                    .padding(bottom = 4.dp)
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
        } else null
    )
}

@Preview(showBackground = true)
@Composable
fun AppAlertDialogUpdatePreview() {
    MotmaenBashTheme {
        // Mock forced update state
        val mockUpdateState = MainViewModel.UpdateDialogState(
            latestVersionName = "2.1.0",
            forceUpdate = true,
            links = listOf("دانلود از سایت" to "https://motmaenbash.ir")
        )
        Surface(modifier = Modifier.fillMaxSize()) {
            UpdateDialog(
                updateDialogState = mockUpdateState,
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppAlertDialogPermissionPreview() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PermissionGuideDialog(
                permissionType = MainViewModel.PermissionType.ACCESSIBILITY,
                onConfirm = { }
            ) { }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppAlertDialogTutorialPreview() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            TutorialDialog()
        }
    }
}