package com.marutipolymer.billing.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marutipolymer.billing.models.Invoice
import com.marutipolymer.billing.utils.PdfGenerator
import com.marutipolymer.billing.viewmodel.InvoiceUiState
import com.marutipolymer.billing.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHistoryScreen(viewModel: InvoiceViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    var invoiceToCancel by remember { mutableStateOf<Invoice?>(null) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FA))
        ) {
            // Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "PAID", "PENDING", "CANCELLED").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is InvoiceUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is InvoiceUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Error: ${state.message}", color = Color.Red)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.fetchInvoices() }) {
                                Text("Retry")
                            }
                        }
                    }
                    is InvoiceUiState.Success -> {
                        val filteredList = state.data.filter { inv ->
                            when (selectedFilter) {
                                "ALL" -> true
                                else -> inv.status == selectedFilter
                            }
                        }

                        if (filteredList.isEmpty()) {
                            Text(
                                text = "No invoices found",
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.Gray
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredList) { invoice ->
                                    InvoiceItemCard(
                                        invoice = invoice,
                                        onShare = {
                                            val pdfFile = PdfGenerator.generateInvoicePdf(
                                                context = context,
                                                invoiceNo = invoice.invoice_no,
                                                customerName = invoice.customer_name ?: "Customer",
                                                items = emptyList(), // Fallback summary view
                                                subtotal = invoice.subtotal,
                                                discount = invoice.discount,
                                                totalAmount = invoice.total_amount,
                                                paidAmount = invoice.paid_amount,
                                                pendingAmount = invoice.pending_amount
                                            )
                                            PdfGenerator.sharePdf(context, pdfFile)
                                        },
                                        onCancel = {
                                            invoiceToCancel = invoice
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (invoiceToCancel != null) {
        CancelInvoiceDialog(
            invoice = invoiceToCancel!!,
            onDismiss = { invoiceToCancel = null },
            onConfirm = { reason ->
                viewModel.cancelInvoice(invoiceToCancel!!.id, reason) { success, msg ->
                    if (success) invoiceToCancel = null
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun InvoiceItemCard(
    invoice: Invoice,
    onShare: () -> Unit,
    onCancel: () -> Unit
) {
    val statusColor = when (invoice.status) {
        "PAID" -> Color(0xFF4CAF50)
        "PARTIAL" -> Color(0xFFFF9800)
        "PENDING" -> Color(0xFFE53935)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = invoice.invoice_no, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = invoice.customer_name ?: "N/A", color = Color.Gray, fontSize = 14.sp)
                }
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = invoice.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Date: ${invoice.invoice_date.take(10)}", fontSize = 12.sp, color = Color.Gray)
                Text(text = "Total: ₹${invoice.total_amount}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (invoice.pending_amount > 0) {
                Text(
                    text = "Pending: ₹${invoice.pending_amount}",
                    fontSize = 12.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onShare,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share PDF", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Bill", fontSize = 12.sp)
                }

                if (invoice.status != "CANCELLED") {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Cancel, contentDescription = "Cancel Invoice", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun CancelInvoiceDialog(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Invoice ${invoice.invoice_no}") },
        text = {
            Column {
                Text("Are you sure you want to cancel this bill? This action cannot be undone.", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Cancellation Reason (Min 5 chars)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.length >= 5) {
                        onConfirm(reason)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Confirm Cancel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Back") }
        }
    )
}
