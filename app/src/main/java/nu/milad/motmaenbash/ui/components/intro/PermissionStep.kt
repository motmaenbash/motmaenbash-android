package nu.milad.motmaenbash.ui.components.intro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowRight
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AppCard
import nu.milad.motmaenbash.ui.components.Divider
import nu.milad.motmaenbash.ui.screens.AnimatedPermissionIcon
import nu.milad.motmaenbash.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.theme.GreyMiddle
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme


@Composable
fun PermissionStep(
    title: String,
    permissionName: String = "",
    description: String,
    isGranted: Boolean,
    isLastStep: Boolean = false,
    optionalDescription: String? = null,
    icon: ImageVector? = null,
    onGrant: () -> Unit,
    onNext: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated icon for the permission
        icon?.let {
            AnimatedPermissionIcon(it)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Title and description
        Text(
            title,
            style = typography.headlineSmall,
            color = colorScheme.primary,
            fontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (permissionName.isNotEmpty()) {
            Text(
                permissionName,
                style = typography.bodySmall,
                color = GreyMiddle,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            description,
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier
                .padding(horizontal = 12.dp),
            color = colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGrant,
            enabled = !isGranted,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGranted) Color.Gray else ColorPrimary,
                contentColor = Color.White
            )
        ) {
            Text(if (isGranted) "تکمیل شده" else "فعال‌سازی دسترسی")
        }

        // Show "Next Step" button if not the last step
        if (!isLastStep && onNext != null) {
            Spacer(modifier = Modifier.width(16.dp))
            TextButton(
                onClick = onNext,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorScheme.primary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardDoubleArrowRight,
                        contentDescription = "Next",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "گام بعدی",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (optionalDescription != null) {

            AppCard(
                border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f)),
                padding = 12.dp,
                content = {

                    Column(modifier = Modifier.padding(8.dp)) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Outlined.AdminPanelSettings,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = ColorPrimary

                            )
                            Text(
                                text = "دسترسی اختیاری",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp),
                                style = typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = colorScheme.onSurface
                            )
                        }

                        Divider(verticalPadding = 2.dp, horizontalPadding = 8.dp)
                        Text(
                            optionalDescription,
                            fontSize = 12.sp,
                            color = colorScheme.onSurface

                        )

                    }

                },
            )
        }

        // Custom content section
        content?.invoke()
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun PermissionStepPreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PermissionStep(
                        title = stringResource(id = R.string.permission_overlay),
                        permissionName = "SYSTEM_ALERT_WINDOW",
                        description = "برای نمایش هشدارهای فوری هنگام شناسایی تهدید، نیاز به این دسترسی داریم",
                        optionalDescription = "این دسترسی اختیاری است و بعدا هم می‌توانید آن را فعال کنید.",
                        icon = Icons.Outlined.PhoneAndroid,
                        isGranted = false,
                        onGrant = { },
                        onNext = { }
                    )
                }

                DeveloperCredit()
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun PermissionLastStepPreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PermissionStep(
                        title = stringResource(id = R.string.permission_overlay),
                        permissionName = "SYSTEM_ALERT_WINDOW",
                        description = "برای نمایش هشدارهای فوری هنگام شناسایی تهدید، نیاز به این دسترسی داریم",
                        icon = Icons.Outlined.PhoneAndroid,
                        isGranted = false,
                        isLastStep = true,
                        onGrant = { },
                        onNext = { }
                    )
                }

                DeveloperCredit()
            }
        }
    }
}