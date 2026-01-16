package com.example.mindshield.ui.Screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindshield.R
import com.example.mindshield.ui.theme.BeigeBackground
import com.example.mindshield.ui.theme.Emerald600
import com.example.mindshield.ui.theme.Stone300
import com.example.mindshield.ui.theme.Stone500
import com.example.mindshield.ui.theme.Stone600
import com.example.mindshield.ui.theme.Stone800
import com.example.mindshield.ui.theme.Stone900
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.mindshield.domain.calibration.UserBaseline
import com.example.mindshield.ui.viewmodel.OnboardingScreenViewModel

// =======================
// 1. 核心引导页容器
// =======================
@Composable
fun OnboardingScreen(onFinish: () -> Unit, viewModel: OnboardingScreenViewModel) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    Scaffold(containerColor = BeigeBackground) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false, // 禁止用户随意滑动，必须完成校准或点击按钮
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage(
                        onNext = { scope.launch { pagerState.animateScrollToPage(1) } }
                    )
                    1 -> FeatureIntroPage(
                        onNext = { scope.launch { pagerState.animateScrollToPage(2) } }
                    )
                    2 -> CalibrationPage(
                        onCalibrationComplete = { scope.launch { pagerState.animateScrollToPage(3) } },
                        viewModel
                    )
                    3 -> ConclusionPage(
                        onEnterApp = onFinish
                    )
                }
            }
        }
    }
}

// =======================
// Page 1: 欢迎页面 (图标 + 文字)
// =======================
@Composable
fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp).background(Emerald600, CircleShape).padding(20.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text("Welcome to MindShield", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Stone900)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "your well-rounded mental health protector\nLet's begin to establish your boundaries.",
            textAlign = TextAlign.Center,
            color = Stone600,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(80.dp))
        Button(
            onClick = onNext,
            // 修改：透明度颜色
            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("NEXT")
        }
    }
}

// =======================
// Page 2: 隐私介绍
// =======================
@Composable
fun FeatureIntroPage(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.AdminPanelSettings,
            contentDescription = "Privacy",
            tint = Emerald600,
            modifier = Modifier.size(90.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text("We Care About Your Privacy", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Stone900)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Locally Deployed Model\nNo data submitted\nNo worry about your privacy",
            textAlign = TextAlign.Center,
            color = Stone600,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(80.dp))
        Button(
            onClick = onNext,
            // 修改：透明度颜色
            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("NEXT")
        }
    }
}

// =======================
// Page 3: 心率校准
// =======================
@Composable
fun CalibrationPage(onCalibrationComplete: () -> Unit,
                    viewModel: OnboardingScreenViewModel) {
    var isTesting by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var timer by remember { mutableIntStateOf(30) }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(isTesting) {
        if (isTesting) {
            timer = 30
            progressAnim.snapTo(0f)
            launch {
                progressAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 30000,
                        easing = LinearEasing
                    )
                )
            }
            while (timer > 0) {
                delay(1000)
                timer--
            }
            isTesting = false
            isFinished = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Baseline Calibration", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Stone900)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "We need to test your standard HR and HRV\nto build your personal profile\nPLease STAY CALM during the test.",
                textAlign = TextAlign.Center,
                color = Stone600,
                fontSize = 14.sp
            )
        }

        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier
                .size(300.dp)
                .background(Brush.radialGradient(listOf(Emerald600.copy(0.2f), Color.Transparent))))

            Box(modifier = Modifier
                .size(256.dp)
                .scale(scale)
                .border(2.dp, Emerald600.copy(0.3f), CircleShape))

            Box(modifier = Modifier
                .size(192.dp)
                .border(4.dp, Emerald600.copy(0.4f), CircleShape))

            if (isTesting || isFinished) {
                CircularProgressIndicator(
                    progress = { if(isFinished) 1f else progressAnim.value },
                    modifier = Modifier.size(192.dp),
                    color = Emerald600,
                    strokeWidth = 4.dp,
                    trackColor = Color.Transparent
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isTesting && !isFinished) {
                    Icon(Icons.Outlined.ArrowCircleRight, null, tint = Stone500, modifier = Modifier.size(48.dp))
                    Text("I'm prepared.", color = Stone500, fontWeight = FontWeight.Medium)
                } else if (isTesting) {
                    Text("$timer", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Stone800)
                    Text("  Collecting...", color = Emerald600, fontWeight = FontWeight.Medium)
                } else {
                    // 修改：这里添加一个Spacer，把下面的内容整体下移
                    Spacer(modifier = Modifier.height(24.dp))

                    Icon(Icons.Filled.CheckCircle, null, tint = Emerald600, modifier = Modifier.size(48.dp))
                    Text("Finished", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Stone900)
                    Text("HR: 72  HRV: 45ms", color = Stone600, fontSize = 14.sp)
                    Text("")  //这个不要删，是用来调整上面两行字的位置的
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isTesting && !isFinished) {
                Button(
                    onClick = {
                        isTesting = true
                        viewModel.startCalibration()
                    },
                    // 修改：透明度颜色
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Start the Test", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            } else if (isTesting) {
                Button(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(containerColor = Stone300),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("   Please Stay Calm...", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = {
                            isFinished = false
                            isTesting = true
                            viewModel.startCalibration()
                        },
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Re-test", color = Stone600, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onCalibrationComplete,
                        // 修改：透明度颜色
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Finish Setting", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// =======================
// Page 4: 结束语
// =======================
@Composable
fun ConclusionPage(onEnterApp: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.VerifiedUser,
            contentDescription = null,
            tint = Emerald600,
            modifier = Modifier.size(90.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text("Everything is ready", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Stone900)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Your personal profile is established.\nMindShield will be by your side.",
            textAlign = TextAlign.Center,
            color = Stone600,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(80.dp))
        Button(
            onClick = onEnterApp,
            // 修改：透明度颜色
            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Enter MindShield", fontSize = 16.sp)
        }
    }
}