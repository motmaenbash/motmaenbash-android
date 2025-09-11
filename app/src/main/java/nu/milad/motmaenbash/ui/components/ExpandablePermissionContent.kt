package nu.milad.motmaenbash.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.utils.NumberUtils

@Composable
fun ExpandablePermissionContent(
    descriptions: List<String>,
    modifier: Modifier = Modifier,
    maxVisibleItems: Int = 5
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "ترکیب دسترسی‌های حساس:",
            fontSize = 13.sp,
            fontWeight = Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.background)
                .animateContentSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                when {
                    descriptions.isEmpty() -> {
                        // There are no descriptions to show
                    }

                    descriptions.size <= maxVisibleItems -> {
                        // Show all descriptions at once
                        descriptions.forEach { description ->
                            Text(
                                text = "• $description",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                lineHeight = 21.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }

                    else -> {
                        descriptions.take(maxVisibleItems).forEach { description ->
                            Text(
                                text = "• $description",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                lineHeight = 21.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }

                        // Show remaining descriptions
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                descriptions.drop(maxVisibleItems).forEach { description ->
                                    Text(
                                        text = "• $description",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        lineHeight = 21.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Show "View more" button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { isExpanded = !isExpanded },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = if (isExpanded) {
                                    "بستن"
                                } else {
                                    "و ${NumberUtils.toPersianNumbers((descriptions.size - maxVisibleItems).toString())} ترکیب دیگر"
                                },
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "بستن" else "باز کردن",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(
    name = "حالت خالی",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
private fun EmptyStatePreview() {
    MotmaenBashTheme {
        Surface {
            ExpandablePermissionContent(
                descriptions = emptyList(),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "یک آیتم",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
private fun SingleItemPreview() {
    MotmaenBashTheme {
        Surface {
            ExpandablePermissionContent(
                descriptions = listOf("دسترسی به دوربین برای عکس‌برداری"),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "سه آیتم",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
private fun ThreeItemsPreview() {
    MotmaenBashTheme {
        Surface {
            ExpandablePermissionContent(
                descriptions = listOf(
                    "دسترسی به دوربین برای عکس‌برداری",
                    "دسترسی به گالری برای انتخاب تصاویر",
                    "دسترسی به اینترنت برای ارسال اطلاعات"
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "پنج آیتم (حد مجاز نمایش)",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
private fun FiveItemsPreview() {
    MotmaenBashTheme {
        Surface {
            ExpandablePermissionContent(
                descriptions = listOf(
                    "دسترسی به دوربین برای عکس‌برداری",
                    "دسترسی به گالری برای انتخاب تصاویر",
                    "دسترسی به اینترنت برای ارسال اطلاعات",
                    "دسترسی به مکان برای تشخیص موقعیت",
                    "دسترسی به مخاطبین برای اشتراک‌گذاری"
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "هشت آیتم (با دکمه نمایش بیشتر)",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
private fun EightItemsPreview() {
    MotmaenBashTheme {
        Surface {
            ExpandablePermissionContent(
                descriptions = listOf(
                    "دسترسی به دوربین برای عکس‌برداری",
                    "دسترسی به گالری برای انتخاب تصاویر",
                    "دسترسی به اینترنت برای ارسال اطلاعات",
                    "دسترسی به مکان برای تشخیص موقعیت",
                    "دسترسی به مخاطبین برای اشتراک‌گذاری",
                    "دسترسی به میکروفن برای ضبط صدا",
                    "دسترسی به حافظه داخلی برای ذخیره فایل‌ها",
                    "دسترسی به تقویم برای یادداشت قرارملاقات‌ها"
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "ده آیتم (با حد مجاز متفاوت)",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
private fun TenItemsCustomLimitPreview() {
    MotmaenBashTheme {
        Surface {
            ExpandablePermissionContent(
                descriptions = listOf(
                    "دسترسی به دوربین برای عکس‌برداری",
                    "دسترسی به گالری برای انتخاب تصاویر",
                    "دسترسی به اینترنت برای ارسال اطلاعات",
                    "دسترسی به مکان برای تشخیص موقعیت",
                    "دسترسی به مخاطبین برای اشتراک‌گذاری",
                    "دسترسی به میکروفن برای ضبط صدا",
                    "دسترسی به حافظه داخلی برای ذخیره فایل‌ها",
                    "دسترسی به تقویم برای یادداشت قرارملاقات‌ها",
                    "دسترسی به بلوتوث برای اتصال به دستگاه‌ها",
                    "دسترسی به اعلان‌ها برای ارسال پیام‌های مهم"
                ),
                maxVisibleItems = 3,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "متن‌های طولانی",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
private fun LongTextsPreview() {
    MotmaenBashTheme {
        Surface {
            ExpandablePermissionContent(
                descriptions = listOf(
                    "دسترسی به دوربین برای عکس‌برداری از محیط اطراف و تشخیص اشیاء مختلف در تصاویر",
                    "دسترسی به گالری و حافظه داخلی برای انتخاب، ذخیره و مدیریت تصاویر و فایل‌های چندرسانه‌ای",
                    "دسترسی به اینترنت و شبکه برای ارسال و دریافت اطلاعات، همگام‌سازی داده‌ها و ارتباط با سرورهای مربوطه",
                    "دسترسی به مکان جغرافیایی کاربر برای تعیین موقعیت مکانی دقیق و ارائه خدمات مبتنی بر موقعیت",
                    "دسترسی به فهرست مخاطبین برای اشتراک‌گذاری محتوا و ارسال دعوت‌نامه به دوستان و آشنایان",
                    "دسترسی به میکروفن برای ضبط پیام‌های صوتی، تماس‌های تصویری و فرمان‌های صوتی",
                    "دسترسی به تقویم سیستم برای مدیریت رویداد‌ها، قرارملاقات‌ها و یادآوری‌های مهم"
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "حالت تاریک",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF121212
)
@Composable
private fun DarkThemePreview() {
    MotmaenBashTheme {
        Surface {
            ExpandablePermissionContent(
                descriptions = listOf(
                    "دسترسی به دوربین برای عکس‌برداری",
                    "دسترسی به گالری برای انتخاب تصاویر",
                    "دسترسی به اینترنت برای ارسال اطلاعات",
                    "دسترسی به مکان برای تشخیص موقعیت",
                    "دسترسی به مخاطبین برای اشتراک‌گذاری",
                    "دسترسی به میکروفن برای ضبط صدا",
                    "دسترسی به حافظه داخلی برای ذخیره فایل‌ها"
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "تمام حالات در یک صفحه",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    heightDp = 2000
)
@Composable
private fun AllStatesPreview() {
    MotmaenBashTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Empty state
                ExpandablePermissionContent(
                    descriptions = emptyList()
                )

                // Single item
                ExpandablePermissionContent(
                    descriptions = listOf("دسترسی به دوربین")
                )

                // Multiple items under limit
                ExpandablePermissionContent(
                    descriptions = listOf(
                        "دسترسی به دوربین",
                        "دسترسی به گالری",
                        "دسترسی به اینترنت"
                    )
                )

                // Multiple items over limit
                ExpandablePermissionContent(
                    descriptions = listOf(
                        "دسترسی به دوربین برای عکس‌برداری",
                        "دسترسی به گالری برای انتخاب تصاویر",
                        "دسترسی به اینترنت برای ارسال اطلاعات",
                        "دسترسی به مکان برای تشخیص موقعیت",
                        "دسترسی به مخاطبین برای اشتراک‌گذاری",
                        "دسترسی به میکروفن برای ضبط صدا",
                        "دسترسی به حافظه داخلی",
                        "دسترسی به تقویم"
                    )
                )
            }
        }
    }
}