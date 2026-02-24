package com.example.fuelcompare.presentation.theme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: 사용할 폰트가 있다면 FontFamily를 정의하세요.
// 예: val Pretendard = FontFamily(
//          Font(R.font.pretendard_bold, FontWeight.Bold),
//          Font(R.font.pretendard_normal, FontWeight.Normal)
//      )
// 아래 코드에서는 기본 폰트를 사용합니다.
val AppFontFamily = FontFamily.Default

// 앱의 Typography 객체 정의
val AppTypography = Typography(
    // Headline -> Display 로 매핑
    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 60.sp,
        lineHeight = 80.sp
    ),
    displayMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 56.sp,
        lineHeight = 72.sp
    ),
    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 64.sp
    ),

    // Title
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold, // Extra_strong, Strong, Normal 중 대표값으로 ExtraBold 선택
        fontSize = 40.sp,
        lineHeight = 52.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold, // Extra_strong, Strong, Normal 중 대표값으로 ExtraBold 선택
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold, // Extra_strong, Strong, Normal 중 대표값으로 ExtraBold 선택
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),

    // Body
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold, // Strong, Normal 중 대표값으로 Bold 선택
        fontSize = 30.sp,
        lineHeight = 38.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold, // Strong, Normal 중 대표값으로 Bold 선택
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold, // Strong, Normal 중 대표값으로 Bold 선택
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),

    // Label
    // Medium -> labelLarge, Small -> labelMedium으로 매핑
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 24.sp
    )
    /* labelSmall은 정의되지 않았으므로 기본값을 사용합니다.
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)