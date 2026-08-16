package com.marutipolymer.billing.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marutipolymer.billing.models.Invoice
import com.marutipolymer.billing.models.PaymentRequest
import com.marutipolymer.billing.utils.PdfHelper
import com.marutipolymer.billing.viewmodel.InvoiceUiState
import com.marutipolymer.billing.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHistoryScreen(viewModel: InvoiceViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    var invoiceToCancel by remember { mutableStateOf<Invoice?>(null) }
    var invoiceToPay by remember { mutableStateOf<Invoice?>(null) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchInvoices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.fetchInvoices() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
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
                                "PENDING" -> inv.status == "PENDING" || inv.status == "PARTIAL"
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
                                            PdfHelper.generateAndShareInvoicePdf(
                                                context = context,
                                                invoiceNo = invoice.invoice_no,
                                                customerName = invoice.customer_name ?: "Customer",
                                                totalAmount = invoice.total_amount,
                                                subtotal = invoice.subtotal,
                                                discount = invoice.discount,
                                                paidAmount = invoice.paid_amount,
                                                pendingAmount = invoice.pending_amount,
                                                items = invoice.invoice_items
                                            )
                                        },
                                        onPay = {
                                            invoiceToPay = invoice
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

    if (invoiceToPay != null) {
        PayInvoiceDialog(
            invoice = invoiceToPay!!,
            onDismiss = { invoiceToPay = null },
            onConfirm = { req ->
                viewModel.receivePayment(req) { success, msg ->
                    if (success) invoiceToPay = null
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
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
    onPay: () -> Unit,
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Paid: ₹${invoice.paid_amount}", fontSize = 12.sp, color = Color(0xFF2E7D32))
                if (invoice.pending_amount > 0) {
                    Text(
                        text = "Pending: ₹${invoice.pending_amount}",
                        fontSize = 12.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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

                if (invoice.pending_amount > 0 && invoice.status != "CANCELLED") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onPay,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Pay Bill", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay Bill", fontSize = 12.sp)
                    }
                }

                if (invoice.status != "CANCELLED") {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel Invoice", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun PayInvoiceDialog(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onConfirm: (PaymentRequest) -> Unit
) {
    var amountText by remember { mutableStateOf(invoice.pending_amount.toString()) }
    var paymentMode by remember { mutableStateOf("CASH") }
    var referenceNo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pay Invoice ${invoice.invoice_no}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pending Amount: ₹${invoice.pending_amount}", fontWeight = FontWeight.Bold, color = Color.Red)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Payment Mode", fontSize = 12.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CASH", "UPI", "BANK").forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode) }
                        )
                    }
                }

                OutlinedTextField(
                    value = referenceNo,
                    onValueChange = { referenceNo = it },
                    label = { Text("Ref / Transaction No (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt != null && amt > 0 && amt <= invoice.pending_amount) {
                        onConfirm(
                            PaymentRequest(
                                customer_id = invoice.customer_id,
                                invoice_id = invoice.id,
                                amount = amt,
                                payment_mode = paymentMode,
                                reference_no = referenceNo.ifBlank { null },
                                remarks = "Payment for ${invoice.invoice_no}"
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Record Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
