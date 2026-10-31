package com.asiyestore.admin.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asiyestore.admin.R
import com.asiyestore.admin.data.DashboardViewModel
import com.asiyestore.admin.data.ProductDomainModel

// Luxury Brand Color Palette
val ChampagneGold = Color(0xFFC6A16C)
val SoftGold = Color(0xFFD8BB8B)
val Ivory = Color(0xFFF8F6F1)
val WarmWhite = Color(0xFFFFFFFF)
val DeepCharcoal = Color(0xFF242321)
val LuxuryBlack = Color(0xFF171717)
val RoseBeige = Color(0xFFE8D8CE)
val PlatinumGray = Color(0xFFB9B6B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: DashboardViewModel) {
    val stats by viewModel.stats.collectAsState()
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Enforce Right-to-Left (RTL) layout regardless of device system language
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Ivory)
        ) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuxuryBlack,
                    titleContentColor = WarmWhite
                ),
                title = {
                    Text(
                        text = "داشبورد مدیریت ASIYE STORE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notification),
                            contentDescription = "بروزرسانی و اعلانات",
                            tint = ChampagneGold
                        )
                    }
                }
            )

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = ChampagneGold,
                    trackColor = LuxuryBlack
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // FIXED: Responsive 2x2 KPI Grid instead of cramped 4-column single row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "فروش امروز",
                                value = "%,d تومان".format(stats.totalSalesToday),
                                accent = ChampagneGold
                            )
                            StatCard(
                                title = "سفارش‌های جدید",
                                value = "${stats.newOrdersCount}",
                                accent = SoftGold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "هشدار کسری انبار",
                                value = "${stats.lowInventoryCount}",
                                accent = RoseBeige
                            )
                            StatCard(
                                title = "سفارش‌های اقساطی",
                                value = "${stats.installmentOrdersCount}",
                                accent = PlatinumGray
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "کاتالوگ محصولات لوکس",
                        style = MaterialTheme.typography.titleLarge,
                        color = DeepCharcoal,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }

                // FIXED: items(...) placed directly in LazyListScope (not illegally inside item {})
                items(
                    items = products,
                    key = { it.id }
                ) { product ->
                    ProductCard(product = product)
                }
            }
        }
    }
}

// FIXED: Scoped cleanly to RowScope so Modifier.weight(1f) compiles properly
@Composable
fun RowScope.StatCard(title: String, value: String, accent: Color) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, PlatinumGray.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .height(3.dp)
                    .background(accent, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, color = PlatinumGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = DeepCharcoal,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProductCard(product: ProductDomainModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, PlatinumGray.copy(alpha = 0.35f))
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.ic_product_placeholder),
                contentDescription = product.nameFa,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = product.nameFa,
                    color = DeepCharcoal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "%,d تومان".format(product.salePrice),
                    color = ChampagneGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "کد کالا: ${product.sku}  |  موجودی: ${product.inventory} عدد",
                    color = PlatinumGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
