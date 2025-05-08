package com.wellbite.wellbite_picker.wellbite_selector

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellBiteSelectionPicker(
    modifier: Modifier = Modifier,
    selections: List<String>,
    selectedItem: String,
    onSelected: (String) -> Unit,
    onSelectedColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectionItemContent: @Composable (item: String, isSelected: Boolean, onClick: () -> Unit) -> Unit = { item, isSelected, onClick ->
        DefaultSelectionItem(
            item = item,
            isSelected = isSelected,
            onClick = onClick,
            onSelectedColor = onSelectedColor
        )
    }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            selections.forEach { item ->
                DefaultSelectionItem(
                    item = item,
                    isSelected = selectedItem == item,
                    onClick = { onSelected(item) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
internal fun DefaultSelectionItem(
    item: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onSelectedColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) onSelectedColor else Color(0xFFF5F5F5)
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}


data class SelectionPickerConfig(
    val defaultSelection: List<String> = listOf("Male", "Female", "Other"),
    val maxCustomSelection: Int? = null,
)
