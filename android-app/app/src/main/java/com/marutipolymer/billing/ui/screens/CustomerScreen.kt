package com.marutipolymer.billing.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.marutipolymer.billing.models.Customer
import com.marutipolymer.billing.models.CustomerPrice
import com.marutipolymer.billing.models.PaymentRequest
import com.marutipolymer.billing.models.PriceUpdateItem
import com.marutipolymer.billing.models.Product
import com.marutipolymer.billing.viewmodel.CustomerUiState
import com.marutipolymer.billing.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(viewModel: CustomerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }

    var pricingCustomer by remember { mutableStateOf<Customer?>(null) }
    var paymentCustomer by remember { mutableStateOf<Customer?>(null) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    customerToEdit = null
                    showCustomerDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Customer", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FA))
        ) {
            when (val state = uiState) {
                is CustomerUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CustomerUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${state.message}", color = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchCustomers() }) {
                            Text("Retry")
                        }
                    }
                }
                is CustomerUiState.Success -> {
                    CustomerList(
                        customers = state.data,
                        onEditClick = { customer ->
                            customerToEdit = customer
                            showCustomerDialog = true
                        },
                        onPricesClick = { customer ->
                            pricingCustomer = customer
                        },
                        onPaymentClick = { customer ->
                            paymentCustomer = customer
                        }
                    )
                }
            }
        }
    }

    if (showCustomerDialog) {
        CustomerDialog(
            customer = customerToEdit,
            onDismiss = { showCustomerDialog = false },
            onSave = { updatedCustomer ->
                if (customerToEdit == null) {
                    viewModel.addCustomer(updatedCustomer) { success, msg -> 
                        showCustomerDialog = !success 
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    viewModel.updateCustomer(customerToEdit!!.id, updatedCustomer) { success, msg -> 
                        showCustomerDialog = !success
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (pricingCustomer != null) {
        CustomerPricingDialog(
            customer = pricingCustomer!!,
            viewModel = viewModel,
            onDismiss = { pricingCustomer = null }
        )
    }

    if (paymentCustomer != null) {
        ReceivePaymentDialog(
            customer = paymentCustomer!!,
            onDismiss = { paymentCustomer = null },
            onSave = { req ->
                viewModel.receivePayment(req) { success, msg ->
                    if (success) paymentCustomer = null
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun CustomerList(
    customers: List<Customer>, 
    onEditClick: (Customer) -> Unit,
    onPricesClick: (Customer) -> Unit,
    onPaymentClick: (Customer) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(customers) { customer ->
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
                            Text(text = customer.customer_name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Code: ${customer.customer_code}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(
                            text = if (customer.is_active) "Active" else "Inactive",
                            color = if (customer.is_active) Color(0xFF4CAF50) else Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Mobile: ${customer.mobile ?: "N/A"}", color = Color.DarkGray, fontSize = 14.sp)
                        Text(text = "City: ${customer.city ?: "N/A"}", color = Color.DarkGray, fontSize = 14.sp)
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onPricesClick(customer) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Custom Rates", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prices", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onPaymentClick(customer) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Receive Payment", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Payment", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { onEditClick(customer) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer?.customer_name ?: "") }
    var mobile by remember { mutableStateOf(customer?.mobile ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var city by remember { mutableStateOf(customer?.city ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer == null) "Add Customer" else "Edit Customer") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            Customer(
                                id = customer?.id ?: "",
                                customer_name = name,
                                mobile = mobile,
                                address = address,
                                city = city,
                                opening_balance = customer?.opening_balance ?: 0.0,
                                customer_code = customer?.customer_code ?: "",
                                is_active = customer?.is_active ?: true
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CustomerPricingDialog(
    customer: Customer,
    viewModel: CustomerViewModel,
    onDismiss: () -> Unit
) {
    var prices by remember { mutableStateOf<List<CustomerPrice>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val customPricesMap = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(customer.id) {
        try {
            val prodRes = com.marutipolymer.billing.api.RetrofitClient.apiService.getProducts()
            if (prodRes.success) {
                products = prodRes.data.filter { it.is_active }
            }
        } catch (e: Exception) {
            products = emptyList()
        }

        viewModel.fetchCustomerPrices(customer.id) { fetchedPrices ->
            prices = fetchedPrices
            fetchedPrices.forEach { cp ->
                customPricesMap[cp.product_id] = cp.selling_price.toString()
            }
            loading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Rates: ${customer.customer_name}", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(products) { prod ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.product_name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Default: ₹${prod.default_sell_price}", fontSize = 12.sp, color = Color.Gray)
                                }
                                OutlinedTextField(
                                    value = customPricesMap[prod.id] ?: "",
                                    onValueChange = { customPricesMap[prod.id] = it },
                                    label = { Text("Custom ₹") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(110.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updateItems = customPricesMap.mapNotNull { (prodId, rateStr) ->
                        val rate = rateStr.toDoubleOrNull()
                        if (rate != null) PriceUpdateItem(prodId, rate) else null
                    }
                    viewModel.saveCustomerPrices(customer.id, updateItems) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) onDismiss()
                    }
                }
            ) {
                Text("Save Rates")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ReceivePaymentDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onSave: (PaymentRequest) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("CASH") }
    var referenceNo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Receive Payment: ${customer.customer_name}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Received (₹)") },
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
                    if (amt != null && amt > 0) {
                        onSave(
                            PaymentRequest(
                                customer_id = customer.id,
                                amount = amt,
                                payment_mode = paymentMode,
                                payment_date = null,
                                reference_no = referenceNo.ifBlank { null },
                                remarks = "Direct Payment Collection"
                            )
                        )
                    }
                }
            ) {
                Text("Record Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
