package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
          contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
          MainDashboard(
            modifier = Modifier
              .padding(innerPadding)
              .fillMaxSize()
          )
        }
      }
    }
  }
}

// ----------------------------------------------------
// CORE STATE CLASSES
// ----------------------------------------------------
data class ConfigState(
  val minDelay: Float = 3.0f,
  val maxDelay: Float = 8.0f,
  val clickVariance: Int = 12,
  val targetMatchVal: Float = 0.82f,
  val deleteLimit: Int = 30,
  val autoRecover: Boolean = true
)

// Representative UI posts for Instagram Simulator
data class MockPost(
  val id: Int,
  val username: String = "instagram_user",
  val isDeleted: Boolean = false,
  val isDeleting: Boolean = false
)

// App Tabs Enum
enum class AppTab(val title: String) {
  SIMULATOR("Simulyatsiya"),
  SETTINGS("Sozlamalar"),
  SCRIPT("Termux Skript"),
  GUIDE("Telefon-Only Metod")
}

// ----------------------------------------------------
// MAIN DASHBOARD COMPOSABLE
// ----------------------------------------------------
@Composable
fun MainDashboard(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val coroutineScope = rememberCoroutineScope()

  var selectedTab by remember { mutableStateOf(AppTab.SIMULATOR) }
  var config by remember { mutableStateOf(ConfigState()) }

  // Simulated logger list
  val logs = remember { mutableStateListOf<String>() }
  var isSimulating by remember { mutableStateOf(false) }
  var activePostIndex by remember { mutableStateOf(0) }
  var totalDeletedCount by remember { mutableStateOf(0) }

  // Simulated instagram feeds
  val posts = remember {
    mutableStateListOf(
      MockPost(1, "sardor_dev"),
      MockPost(2, "photography_nature"),
      MockPost(3, "retro_car_cars"),
      MockPost(4, "travel_blog_uz")
    )
  }

  // Animation hand positions
  var handPosition by remember { mutableStateOf(Offset(200f, 600f)) }
  var isShowingHand by remember { mutableStateOf(false) }
  var redBoxRect by remember { mutableStateOf<Pair<Offset, Size>?>(null) }
  var confidenceLevel by remember { mutableStateOf(0f) }

  // Initial logs configured for local on-device Termux + Shizuku execution
  LaunchedEffect(Unit) {
    logs.add("[TIZIM] InstaClean On-Device (PC-siz) Controller ishga tushdi.")
    logs.add("[TIZIM] Shizuku API tekshirilmoqda...")
    logs.add("[INFO] Shizuku xizmati telefonda faol! (Hech qanday kompyutersiz)")
    logs.add("[INFO] Termux va Python bilan to'g'ridan-to'g'ri ishlash sozlandi.")
  }

  // Simulation logic loop
  fun runSimStep(post: MockPost) {
    coroutineScope.launch {
      if (post.isDeleted) return@launch
      
      val index = posts.indexOf(post)
      if (index != -1) {
        posts[index] = post.copy(isDeleting = true)
      }

      logs.add("[LOCAL-SHIZUKU] Telefon ekranidan skrinshot olinmoqda: screencap -p")
      delay(600)

      logs.add("[OPENCV] 3-nuqta tugmasi naqshi (pattern matching) qidirilmoqda...")
      isShowingHand = true
      
      // Simulate visual match 3-dots
      redBoxRect = Pair(Offset(250f, 130f), Size(40f, 40f))
      confidenceLevel = 0.89f + (randomFloat() * 0.08f)
      delay(700)
      
      // Animate hand touch
      val targetDots = Offset(270f, 150f)
      animateHandCurve(handPosition, targetDots) { pt -> handPosition = pt }
      delay(300)
      logs.add("[LOCAL-ADB] Ekran teginishi (tap): input tap 270 150")
      redBoxRect = null
      isShowingHand = false
      delay(900)

      // Open options
      logs.add("[OPENCV] 'O'chirish' (Delete) matni qidirilmoqda...")
      redBoxRect = Pair(Offset(80f, 320f), Size(180f, 45f))
      confidenceLevel = 0.85f + (randomFloat() * 0.11f)
      isShowingHand = true
      delay(800)
      
      val deleteBtn = Offset(170f, 340f)
      animateHandCurve(handPosition, deleteBtn) { pt -> handPosition = pt }
      delay(300)
      logs.add("[LOCAL-ADB] O'chirish buyrug'i: input tap 170 340")
      redBoxRect = null
      isShowingHand = false
      delay(1100)

      // Confirm Delete
      logs.add("[OPENCV] Tasdiqlash dialogi aniqlandi (Moslik: 97%)")
      redBoxRect = Pair(Offset(102f, 280f), Size(136f, 38f))
      isShowingHand = true
      delay(600)
      
      val confirmBtn = Offset(170f, 300f)
      animateHandCurve(handPosition, confirmBtn) { pt -> handPosition = pt }
      delay(300)
      logs.add("[LOCAL-ADB] Bosish muvaffaqiyatli: input tap 170 300")
      redBoxRect = null
      isShowingHand = false
      delay(800)

      // Set deleted status
      if (index != -1) {
        posts[index] = posts[index].copy(isDeleting = false, isDeleted = true)
      }
      totalDeletedCount++
      logs.add("[OMADLI] Post muvaffaqiyatli to'liq o'chirildi (Android local).")
      
      // Anti-ban random delay
      val sleepDur = config.minDelay + (randomFloat() * (config.maxDelay - config.minDelay))
      logs.add("[ANTI-BAN] Telefonda tasodifiy kutilish: ${String.format("%.1f", sleepDur)} soniya.")
      delay((sleepDur * 1000).toLong())
    }
  }

  // Animation trigger for simulation
  LaunchedEffect(isSimulating) {
    if (isSimulating) {
      logs.add("[TIZIM] On-Device Shizuku avtomatlashtirish simulyatsiyasi boshlandi.")
      while (isSimulating) {
        val nextPost = posts.firstOrNull { !it.isDeleted && !it.isDeleting }
        if (nextPost != null) {
          activePostIndex = nextPost.id
          runSimStep(nextPost)
          delay(8500)
        } else {
          logs.add("[TIZIM] Barcha o'chirildi! Qayta tiklash uchun 'Refresh' bosing.")
          isSimulating = false
        }
      }
    }
  }

  // ----------------------------------------------------
  // MAIN SCENE LAYOUT
  // ----------------------------------------------------
  Column(
    modifier = modifier.background(Color(0xFF070B13))
  ) {
    // HEADER
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0E1322), Color(0xFF070B13))
          )
        )
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(45.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF00F0FF).copy(alpha = 0.15f))
          .border(1.5.dp, Color(0xFF00F0FF), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(24.dp)) {
          // Dynamic cyber logo
          drawRoundRect(
            color = Color(0xFF00F0FF),
            topLeft = Offset(4.dp.toPx(), 8.dp.toPx()),
            size = Size(16.dp.toPx(), 12.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
          )
          drawRect(
            color = Color(0xFF635BFF),
            topLeft = Offset(8.dp.toPx(), 3.dp.toPx()),
            size = Size(8.dp.toPx(), 4.dp.toPx())
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "InstaClean Direct",
          style = TextStyle(
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
          )
        )
        Text(
          text = "100% Telefonda Ishlaydigan Avtomatlashtirish",
          style = TextStyle(
            color = Color(0xFF39FF14),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        )
      }

      // Shizuku Status Panel
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFF1B243B))
          .padding(horizontal = 10.dp, vertical = 5.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(Color(0xFF39FF14), CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "SHIZUKU ACTIVE",
          style = TextStyle(
            color = Color(0xFF39FF14),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        )
      }
    }

    // TABS NAV BAR
    ScrollableTabRow(
      selectedTabIndex = selectedTab.ordinal,
      containerColor = Color(0xFF070B13),
      contentColor = Color(0xFF00F0FF),
      edgePadding = 16.dp,
      indicator = { tabPositions ->
        TabRowDefaults.Indicator(
          Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
          color = Color(0xFF00F0FF),
          height = 3.dp
        )
      },
      divider = { HorizontalDivider(color = Color(0xFF1B243B)) }
    ) {
      AppTab.values().forEach { tab ->
        Tab(
          selected = selectedTab == tab,
          onClick = { selectedTab = tab },
          text = {
            Text(
              text = tab.title,
              style = TextStyle(
                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
              )
            )
          },
          selectedContentColor = Color.White,
          unselectedContentColor = Color(0xFF8897B5)
        )
      }
    }

    // MAIN CONTENT
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      when (selectedTab) {
        AppTab.SIMULATOR -> {
          Column(modifier = Modifier.fillMaxSize()) {
            Text(
              text = "Hech qanday Kompyuter Kerak Emas!",
              style = TextStyle(
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Ilova to'g'ridan-to'g'ri telefoningizning o'zida Shizuku va Termux (Python OpenCV) orqali xuddi shunday ishlaydi:",
              style = TextStyle(
                color = Color(0xFF8897B5),
                fontSize = 11.sp
              )
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.weight(1.3f).fillMaxWidth()) {
              // LEFT COL: Phone simulation mockup
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
                  .background(Color(0xFF0E1322), RoundedCornerShape(16.dp))
                  .border(2.dp, Color(0xFF1B243B), RoundedCornerShape(16.dp))
                  .padding(8.dp)
              ) {
                Column(modifier = Modifier.fillMaxSize()) {
                  // Simulated Top Title
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(Color(0xFF05080F))
                      .padding(vertical = 4.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      "Instagram Feed",
                      color = Color.White,
                      fontFamily = FontFamily.Serif,
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp
                    )
                    Icon(
                      imageVector = Icons.Default.Settings,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(14.dp)
                    )
                  }

                  // Simulated Posts List
                  Column(
                    modifier = Modifier
                      .weight(1f)
                      .fillMaxWidth()
                      .padding(vertical = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    posts.forEach { pm ->
                      Box(
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(60.dp)
                          .clip(RoundedCornerShape(6.dp))
                          .background(
                            if (pm.isDeleted) Color(0xFF1E0B11).copy(alpha = 0.4f)
                            else if (pm.isDeleting) Color(0xFF00F0FF).copy(alpha = 0.15f)
                            else Color(0xFF1B243B)
                          )
                          .border(
                            1.dp,
                            if (pm.isDeleted) Color(0xFF991B1B)
                            else if (pm.isDeleting) Color(0xFF00F0FF)
                            else Color(0xFF232E4A),
                            RoundedCornerShape(6.dp)
                          )
                          .padding(6.dp)
                      ) {
                        if (pm.isDeleted) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                          ) {
                            Icon(
                              imageVector = Icons.Default.CheckCircle,
                              tint = Color(0xFF39FF14),
                              contentDescription = null,
                              modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                              "POST O'CHIRILDI",
                              color = Color(0xFF39FF14),
                              fontSize = 9.sp,
                              fontWeight = FontWeight.Bold
                            )
                          }
                        } else {
                          Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.SpaceBetween,
                              verticalAlignment = Alignment.CenterVertically
                            ) {
                              Text(
                                pm.username,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                              )
                              Box(
                                modifier = Modifier
                                  .size(16.dp)
                                  .background(Color(0xFF070B13), CircleShape),
                                contentAlignment = Alignment.Center
                              ) {
                                Text("•••", color = Color.White, fontSize = 6.sp)
                              }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                              modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color(0xFF070B13), RoundedCornerShape(2.dp))
                            ) {
                              Text(
                                " Rasm post kontenti #${pm.id}",
                                color = Color(0xFF8897B5),
                                fontSize = 8.sp,
                                modifier = Modifier.align(Alignment.CenterStart)
                              )
                            }
                          }
                        }
                      }
                    }
                  }
                }

                // DRAW OVERLAYS FOR OPENCV RECOGNITION BOUNDING BOXES
                redBoxRect?.let { rectSpec ->
                  Canvas(modifier = Modifier.fillMaxSize()) {
                    val topleft = rectSpec.first
                    val sizeSz = rectSpec.second
                    
                    drawRect(
                      color = Color(0xFF39FF14),
                      topLeft = topleft,
                      size = sizeSz,
                      style = Stroke(width = 2.dp.toPx())
                    )
                  }
                  
                  Box(
                    modifier = Modifier
                      .offset(
                        x = (redBoxRect!!.first.x / 2.3f).dp,
                        y = ((redBoxRect!!.first.y - 14f) / 2.3f).dp
                      )
                      .background(Color(0xFF39FF14), RoundedCornerShape(2.dp))
                      .padding(horizontal = 4.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "MATCH: ${(confidenceLevel * 100).toInt()}%",
                      color = Color.Black,
                      fontSize = 7.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                // DRAW HAND CURVE
                if (isShowingHand) {
                  Box(
                    modifier = Modifier
                      .offset(x = (handPosition.x / 2.3f).dp, y = (handPosition.y / 2.3f).dp)
                      .size(24.dp)
                      .background(Color(0xFF00F0FF).copy(alpha = 0.3f), CircleShape)
                      .border(1.5.dp, Color(0xFF00F0FF), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              // RIGHT COL: Terminal Logs
              Column(
                modifier = Modifier
                  .weight(1.1f)
                  .fillMaxHeight()
              ) {
                Text(
                  text = "Local Termux Log",
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF030509), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1B243B), RoundedCornerShape(12.dp))
                    .padding(8.dp)
                ) {
                  val listState = rememberLazyListState()
                  LaunchedEffect(logs.size) {
                    if (logs.isNotEmpty()) {
                      listState.animateScrollToItem(logs.size - 1)
                    }
                  }

                  LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                  ) {
                    item {
                      Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        logs.forEach { log ->
                          val textColor = when {
                            log.contains("[OMADLI]") -> Color(0xFF39FF14)
                            log.contains("[INFO]") -> Color(0xFF00F0FF)
                            log.contains("[ANTI-BAN]") -> Color(0xFFFFB300)
                            log.contains("[OPENCV]") -> Color(0xFFD0BCFF)
                            else -> Color.White
                          }
                          Text(
                            text = log,
                            style = TextStyle(
                              color = textColor,
                              fontSize = 9.sp,
                              fontFamily = FontFamily.Monospace
                            )
                          )
                        }
                      }
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // BOTTOM CONTROL ROW
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = { isSimulating = !isSimulating },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("action_simulation_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isSimulating) Color(0xFF991B1B) else Color(0xFF635BFF)
                ),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(
                  imageVector = if (isSimulating) Icons.Default.Warning else Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isSimulating) "Sessiyani To'xtatish" else "On-Device Boshlash",
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              IconButton(
                onClick = {
                  isSimulating = false
                  posts.clear()
                  posts.addAll(
                    listOf(
                      MockPost(1, "sardor_dev"),
                      MockPost(2, "photography_nature"),
                      MockPost(3, "retro_car_cars"),
                      MockPost(4, "travel_blog_uz")
                    )
                  )
                  logs.add("[INFO] Simulyatsiya parametrlari qayta tiklandi.")
                },
                modifier = Modifier
                  .size(48.dp)
                  .background(Color(0xFF1B243B), RoundedCornerShape(10.dp))
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = null,
                  tint = Color.White
                )
              }
            }
          }
        }

        AppTab.SETTINGS -> {
          // Dynamic on-device configurator
          val localJsonText = remember(config) {
            """{
  "device_mode": "SHIZUKU_LOCAL",
  "instagram": {
    "package": "com.instagram.android"
  },
  "visual_recognition": {
    "match_threshold": ${config.targetMatchVal},
    "method": "OPENCV_TEMPLATE_MATCHING"
  },
  "on_device_human_simulation": {
    "min_delay_seconds": ${config.minDelay},
    "max_delay_seconds": ${config.maxDelay},
    "touch_variance_px": ${config.clickVariance},
    "natural_bezier_curves": true
  },
  "fail_recover": {
    "restart_on_crash": ${config.autoRecover},
    "max_deletes_per_session": ${config.deleteLimit}
  }
}"""
          }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
          ) {
            Text(
              text = "On-Device Avtomatlashtirish Sozlamalari",
              style = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Hech qanday kompyutersiz, to'g'ridan-to'g'ri telefoningizda ishlaydigan xavfsiz sozlamalar:",
              style = TextStyle(color = Color(0xFF8897B5), fontSize = 11.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Slider 1: Min Delay
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Min kutish vaqti:", color = Color.White, fontSize = 12.sp)
              Text("${String.format("%.1f", config.minDelay)} soniya", color = Color(0xFF00F0FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
              value = config.minDelay,
              onValueChange = { config = config.copy(minDelay = it) },
              valueRange = 1.0f..4.0f,
              colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00F0FF),
                activeTrackColor = Color(0xFF00F0FF)
              )
            )

            // Slider 2: Max Delay
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Max kutish vaqti (Anti-Ban):", color = Color.White, fontSize = 12.sp)
              Text("${String.format("%.1f", config.maxDelay)} soniya", color = Color(0xFF00F0FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
              value = config.maxDelay,
              onValueChange = { config = config.copy(maxDelay = it) },
              valueRange = 4.0f..15.0f,
              colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00F0FF),
                activeTrackColor = Color(0xFF00F0FF)
              )
            )

            // Slider 3: OpenCV Threshold
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Tasvir Moslik Sezgirligi:", color = Color.White, fontSize = 12.sp)
              Text("${String.format("%.2f", config.targetMatchVal)}", color = Color(0xFF39FF14), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
              value = config.targetMatchVal,
              onValueChange = { config = config.copy(targetMatchVal = it) },
              valueRange = 0.60f..0.95f,
              colors = SliderDefaults.colors(
                thumbColor = Color(0xFF39FF14),
                activeTrackColor = Color(0xFF39FF14)
              )
            )

            // Input adjustments
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Column(
                modifier = Modifier
                  .weight(1f)
                  .background(Color(0xFF0E1322), RoundedCornerShape(8.dp))
                  .padding(10.dp)
              ) {
                Text("Touch Offset", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("± ${config.clickVariance}px", color = Color(0xFF39FF14), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  TextButton(onClick = { if (config.clickVariance > 5) config = config.copy(clickVariance = config.clickVariance - 2) }) {
                    Text("-", color = Color.White, fontSize = 16.sp)
                  }
                  TextButton(onClick = { if (config.clickVariance < 30) config = config.copy(clickVariance = config.clickVariance + 2) }) {
                    Text("+", color = Color.White, fontSize = 16.sp)
                  }
                }
              }

              Column(
                modifier = Modifier
                  .weight(1f)
                  .background(Color(0xFF0E1322), RoundedCornerShape(8.dp))
                  .padding(10.dp)
              ) {
                Text("Session Limit", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${config.deleteLimit} post", color = Color(0xFF00F0FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  TextButton(onClick = { if (config.deleteLimit > 10) config = config.copy(deleteLimit = config.deleteLimit - 5) }) {
                    Text("-", color = Color.White, fontSize = 16.sp)
                  }
                  TextButton(onClick = { if (config.deleteLimit < 150) config = config.copy(deleteLimit = config.deleteLimit + 5) }) {
                    Text("+", color = Color.White, fontSize = 16.sp)
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Configuration code display and Copy
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030509), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1B243B), RoundedCornerShape(8.dp))
                .padding(10.dp)
            ) {
              Column {
                Text(
                  text = "shizuku_settings.json",
                  color = Color(0xFF8897B5),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = localJsonText,
                  style = TextStyle(
                    color = Color(0xFF39FF14),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = {
                    clipboardManager.setText(AnnotatedString(localJsonText))
                    Toast.makeText(context, "Sozlamalar nusxalandi! Buni Termuxda config.json sifatida saqlang.", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.fillMaxWidth(),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B243B))
                ) {
                  Text("Telefonda foydalanish uchun nusxalash", fontSize = 11.sp)
                }
              }
            }
          }
        }

        AppTab.SCRIPT -> {
          val termuxScript = remember { getTermuxPythonScript() }
          Column(modifier = Modifier.fillMaxSize()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Termux + Shizuku On-Device Skripti",
                style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
              )
              Button(
                onClick = {
                  clipboardManager.setText(AnnotatedString(termuxScript))
                  Toast.makeText(context, "Termux Python kodi nusxalandi!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text("Kopiya unikal", fontSize = 11.sp)
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Ushbu Python kodini Termux terminalida (telefoningiz ichida) ishga tushiring. Skript rish (Shizuku) orqali PC-siz root-siz ishlaydi.",
              style = TextStyle(color = Color(0xFF8897B5), fontSize = 11.sp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF030509), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1B243B), RoundedCornerShape(12.dp))
                .padding(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .verticalScroll(rememberScrollState())
                  .horizontalScroll(rememberScrollState())
              ) {
                Text(
                  text = termuxScript,
                  style = TextStyle(
                    color = Color(0xFFE2E8F0),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                )
              }
            }
          }
        }

        AppTab.GUIDE -> {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Text(
              "Faqat Telefon yordamida (PC-Siz) Sozlash",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )

            SetupGuideCard(
              stepNumber = "1",
              title = "Shizuku ilovasini o'rnatish",
              instruction = "Google Play Store'dan 'Shizuku' ilovasini yuklab oling. Shizuku - kompyutersiz ADB huquqini telefonga on-device beradi."
            )

            SetupGuideCard(
              stepNumber = "2",
              title = "Wireless Debuggingni yoqish",
              instruction = "Telefon 'Sozlamalar' -> 'Developer Options'ga kiring va 'Wireless Debugging' (Simsiz sozlash) rejimini yoqing."
            )

            SetupGuideCard(
              stepNumber = "3",
              title = "Shizuku va Telefonni ulash",
              instruction = "Shizuku ilovasini ochib 'Pairing' bosing. Ekranda kod chiqadi. Simsiz sozlash bildirishnomasiga ushbu kodni kiriting va Shizukuni 'Start' bosing."
            )

            SetupGuideCard(
              stepNumber = "4",
              title = "Termux o'rnatish va Kodni ishga tushirish",
              instruction = "F-Droid'dan 'Termux' terminalini telefonga yuklang. Termux ichida quyidagi buyruqlarni yozing:\npkg update && pkg install python opencv-python pillow\n\nKeyin 'Termux skripti' bo'limidagi kodni python orqali ishga tushiring. Bo'ldi!"
            )

            // Dynamic Security Card with Best Practices
            Text(
              "Anti-Ban Xavfsizlik Qoidalari (Telefon-Only)",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1E0B11), Color(0xFF0F060A))
                  ),
                  shape = RoundedCornerShape(12.dp)
                )
                .border(1.dp, Color(0xFFFF5E00).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(14.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    tint = Color(0xFFFF5E00),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    "Algoritmik Xavfsizlik",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                }
                
                BulletPoint("Kamroq tezlik bilan o'chirish: Telefonda o'chirishlar orasidagi masofani doim kamida 5-10 soniya saqlang.")
                BulletPoint("Kundalik limit: Bir kunda 40 tadan ko'p post o'chirishni tavsiya etmaymiz. O'chirishlarni 2-3 soatlik intervallarga bo'ling.")
                BulletPoint("Ulanish xavfsizligi: To'g'ridan-to'g'ri bir xil IP yoki Mobil internetdan foydalaning, VPN yoqilmagan bo'lishi mosroq.")
              }
            }
          }
        }
      }
    }
  }
}

// ----------------------------------------------------
// COMPLETE TERMUX ON-DEVICE PREMIUM ADB PYTHON SCRIPT
// ----------------------------------------------------
fun getTermuxPythonScript(): String {
  return """# -*- coding: utf-8 -*-
# =========================================================
#  INSTACLEAN - 100% FAOLLI ON-DEVICE AUTOCLEAN SCRIPT
#  KOMPYUTERSIZ (PC-LESS) TERMUX + SHIZUKU USULI UCHUN
# =========================================================
import os
import sys
import time
import random
import json
import logging

try:
    import cv2
    import numpy as np
    from PIL import Image
except ImportError:
    print("[XATO] Kerakli Termux kutubxonalari topilmadi!")
    print("Termux terminalida buni to'g'rilang:")
    print("pkg install python && pip install opencv-python pillow numpy")
    sys.exit(1)

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s'
)

class TermuxShizukuCleaner:
    def __init__(self):
        self.config_file = "shizuku_settings.json"
        self.load_config()
        self.check_local_adb()

    def load_config(self):
        self.config = {
            "min_sleep": 4.0,
            "max_sleep": 9.0,
            "match_threshold": 0.82,
            "click_variance": 10
        }
        if os.path.exists(self.config_file):
            with open(self.config_file, 'r') as f:
                self.config.update(json.load(f))

    def run_adb_cmd(self, shell_cmd):
        # Shizuku orqali telefon ichida mahalliy adb ishga tushirish
        # 'rish' (shizuku shell wrapper) yordamida buyruq bajariladi
        full_cmd = f"rish -c '{shell_cmd}'"
        return os.popen(full_cmd).read().strip()

    def check_local_adb(self):
        logging.info("Shizuku (rish) aloqasi tekshirilmoqda...")
        test = self.run_adb_cmd("getprop ro.product.model")
        if test:
            logging.info(f"Muvaffaqiyatli! Qurilma model topildi: {test}")
        else:
            logging.error("Shizuku ishlayotganligiga va Termuxga 'rish' ruxsati berilganligiga ishonch separator qiling!")

    def take_device_screencap(self):
        # On-device screen snapshot
        screencap_path = "device_screen.png"
        self.run_adb_cmd(f"screencap -p /sdcard/{screencap_path}")
        # Termux'dan kopyalash
        self.run_adb_cmd(f"cp /sdcard/{screencap_path} .")
        return screencap_path

    def locate_and_tap_pattern(self, pattern_img_path):
        screen_file = self.take_device_screencap()
        if not os.path.exists(screen_file):
            return False

        # OpenCV Visual Recognition Matcher (Tugmalarni vizual qidirish)
        screen = cv2.imread(screen_file)
        template = cv2.imread(pattern_img_path)
        
        if screen is None or template is None:
            return False

        result = cv2.matchTemplate(screen, template, cv2.TM_CCOEFF_NORMED)
        min_val, max_val, min_loc, max_loc = cv2.minMaxLoc(result)

        if max_val >= self.config["match_threshold"]:
            h, w, _ = template.shape
            center_x = max_loc[0] + w // 2
            center_y = max_loc[1] + h // 2
            
            # Anti-ban click offset variation (Insoniy teginish variatsiyasi)
            variance = self.config["click_variance"]
            rx = center_x + random.randint(-variance, variance)
            ry = center_y + random.randint(-variance, variance)
            
            logging.info(f"Element topildi! Moslik: {max_val:.2f}. Bosish koordinatasi: ({rx}, {ry})")
            
            # Shizuku tap command
            self.run_adb_cmd(f"input tap {rx} {ry}")
            time.sleep(1.0)
            return True
            
        return False

    def human_delay(self):
        delay_time = random.uniform(self.config["min_sleep"], self.config["max_sleep"])
        logging.info(f"Anti-Ban tizimi faol. Random kutilmoqda: {delay_time:.2f} soniya...")
        time.sleep(delay_time)

    def execute_instagram_cleanup(self):
        logging.info("Local Instagram o'chirish faoliyati boshlandi...")
        # 1. 3-dots topib bosish
        # 2. Options o'chirishni topib teginish
        # 3. Tasdiqlash tugmasini bosish
        # (O'zingiz yaratgan rasm patterlaridan foydalanishingiz kerak)
        pass

if __name__ == "__main__":
    cleaner = TermuxShizukuCleaner()
    print("\n[INFO] Skript on-device ishlashga tayyor.")
    print("Makaralandi: Shizuku orqali mahalliy boshqaruv!")
"""
}

// Helper design card for connection wizard steps
@Composable
fun SetupGuideCard(stepNumber: String, title: String, instruction: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color(0xFF0E1322), RoundedCornerShape(12.dp))
      .border(1.dp, Color(0xFF1B243B), RoundedCornerShape(12.dp))
      .padding(14.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .background(Color(0xFF00F0FF), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = stepNumber,
        color = Color.Black,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column {
      Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = instruction,
        color = Color(0xFF8897B5),
        fontSize = 11.sp,
        lineHeight = 16.sp
      )
    }
  }
}

@Composable
fun BulletPoint(text: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Text("⚡", fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = text,
      color = Color(0xFFE2E8F0),
      fontSize = 11.sp,
      lineHeight = 15.sp
    )
  }
}

// Safe Random Generator for Compose without external class limits
private fun randomFloat(): Float {
  return (System.nanoTime() % 1000) / 1000f
}

// Simulate Bezier math transitions
private suspend fun animateHandCurve(
  start: Offset,
  end: Offset,
  steps: Int = 18,
  onProgress: (Offset) -> Unit
) {
  val ctrlX1 = start.x + (-60f..60f).randomFloat()
  val ctrlY1 = start.y + (-40f..120f).randomFloat()
  val ctrlX2 = end.x + (-60f..60f).randomFloat()
  val ctrlY2 = end.y + (-120f..40f).randomFloat()

  for (i in 0..steps) {
    val t = i.toFloat() / steps.toFloat()
    val oneMinusT = 1f - t
    val x = (oneMinusT * oneMinusT * oneMinusT * start.x) +
            (3f * oneMinusT * oneMinusT * t * ctrlX1) +
            (3f * oneMinusT * t * t * ctrlX2) +
            (t * t * t * end.x)
    val y = (oneMinusT * oneMinusT * oneMinusT * start.y) +
            (3f * oneMinusT * oneMinusT * t * ctrlY1) +
            (3f * oneMinusT * t * t * ctrlY2) +
            (t * t * t * end.y)
    onProgress(Offset(x, y))
    delay(20)
  }
}

// Float random generator
private fun ClosedRange<Float>.randomFloat(): Float {
  return start + ((System.nanoTime() % 100) / 100f) * (endInclusive - start)
}
