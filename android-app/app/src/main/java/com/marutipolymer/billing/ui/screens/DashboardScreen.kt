package com.marutipolymer.billing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marutipolymer.billing.models.DashboardSummary
import com.marutipolymer.billing.viewmodel.DashboardUiState
import com.marutipolymer.billing.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FA))
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DashboardUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${state.message}", color = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchDashboardData() }) {
                            Text("Retry")
                        }
                    }
                }
                is DashboardUiState.Success -> {
                    DashboardContent(summary = state.data)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(summary: DashboardSummary) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = "Today's Sales",
                    value = "₹${summary.today_sales}",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE8F5E9)
                )
                SummaryCard(
                    title = "Today's Collection",
                    value = "₹${summary.today_collection}",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE3F2FD)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = "Total Outstanding",
                    value = "₹${summary.total_outstanding}",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFFEBEE)
                )
                SummaryCard(
                    title = "Pending Invoices",
                    value = "${summary.pending_invoices_count}",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFFF3E0)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = "Today's Profit",
                    value = "₹${summary.today_profit}",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE8EAF6) // Light Indigo
                )
                SummaryCard(
                    title = "Total Profit",
                    value = "₹${summary.total_profit}",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFEDE7F6) // Light Deep Purple
                )
            }
        }

        item {
            Text(
                text = "Recent Invoices",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(summary.recent_invoices) { invoice ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(invoice.invoice_no, fontWeight = FontWeight.Bold)
                        Text(invoice.customer_name, color = Color.Gray, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${invoice.total_amount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(invoice.status, fontSize = 12.sp, color = if (invoice.status == "PAID") Color(0xFF4CAF50) else Color(0xFFFF9800))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}
