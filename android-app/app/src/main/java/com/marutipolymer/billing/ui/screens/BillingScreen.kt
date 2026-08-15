package com.marutipolymer.billing.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
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
import com.marutipolymer.billing.models.Customer
import com.marutipolymer.billing.models.InvoiceItemRequest
import com.marutipolymer.billing.models.Product
import com.marutipolymer.billing.viewmodel.BillingUiState
import com.marutipolymer.billing.viewmodel.BillingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(viewModel: BillingViewModel = viewModel()) {
    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var qtyText by remember { mutableStateOf("") }
    
    // Cart items state mapping to a local data class for display
    data class CartItem(val product: Product, val qty: Double, val total: Double)
    val cartItems = remember { mutableStateListOf<CartItem>() }

    var paidAmountText by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is BillingUiState.Success) {
            Toast.makeText(context, "Invoice Generated Successfully!", Toast.LENGTH_LONG).show()
            selectedCustomer = null
            cartItems.clear()
            paidAmountText = ""
            viewModel.resetState()
        } else if (uiState is BillingUiState.Error) {
            Toast.makeText(context, (uiState as BillingUiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Invoice", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (uiState is BillingUiState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F7FA))
                    .padding(16.dp)
            ) {
                // Customer Selection
                ExposedDropdownMenuBox(
                    expanded = customerDropdownExpanded,
                    onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.customer_name ?: "Select Customer",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = customerDropdownExpanded,
                        onDismissRequest = { customerDropdownExpanded = false }
                    ) {
                        customers.forEach { customer ->
                            DropdownMenuItem(
                                text = { Text(customer.customer_name) },
                                onClick = {
                                    selectedCustomer = customer
                                    customerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product Selection & Add to Cart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = productDropdownExpanded,
                            onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedProduct?.product_name ?: "Select Product",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = productDropdownExpanded,
                                onDismissRequest = { productDropdownExpanded = false }
                            ) {
                                products.forEach { product ->
                                    DropdownMenuItem(
                                        text = { Text("${product.product_name} (₹${product.default_sell_price})") },
                                        onClick = {
                                            selectedProduct = product
                                            productDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = qtyText,
                                onValueChange = { qtyText = it },
                                label = { Text("Quantity") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val qty = qtyText.toDoubleOrNull()
                                    if (selectedProduct != null && qty != null && qty > 0) {
                                        val total = qty * selectedProduct!!.default_sell_price
                                        cartItems.add(CartItem(selectedProduct!!, qty, total))
                                        selectedProduct = null
                                        qtyText = ""
                                    } else {
                                        Toast.makeText(context, "Invalid product or quantity", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.height(56.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Text("Add")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cart List
                Text("Cart Items", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(cartItems) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.product_name, fontWeight = FontWeight.Bold)
                                    Text("Qty: ${item.qty} x ₹${item.product.default_sell_price}", color = Color.Gray, fontSize = 14.sp)
                                }
                                Text("₹${item.total}", fontWeight = FontWeight.Bold)
                                IconButton(onClick = { cartItems.remove(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Totals & Submit
                val subtotal = cartItems.sumOf { it.total }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 16.sp)
                            Text("₹$subtotal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        
                        OutlinedTextField(
                            value = paidAmountText,
                            onValueChange = { paidAmountText = it },
                            label = { Text("Amount Paid Now (₹)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        Button(
                            onClick = {
                                if (selectedCustomer != null && cartItems.isNotEmpty()) {
                                    val reqItems = cartItems.map { InvoiceItemRequest(it.product.id, it.qty, 0.0) }
                                    viewModel.submitInvoice(
                                        customerId = selectedCustomer!!.id,
                                        items = reqItems,
                                        discount = 0.0,
                                        paidAmount = paidAmountText.toDoubleOrNull() ?: 0.0,
                                        paymentMode = "CASH" // Defaulting to Cash for MVP
                                    )
                                } else {
                                    Toast.makeText(context, "Select customer and add items", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Generate Invoice", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
