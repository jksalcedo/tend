package com.jksalcedo.tend.ui.add

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.jksalcedo.tend.domain.model.Person
import android.graphics.Color as AndroidColor

data class SharedPerson(
    val name: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val frequencyDays: Int = 14,
    val notes: String? = null,
    val socialLinks: List<SharedSocialLink> = emptyList(),
    val events: List<SharedEvent> = emptyList()
)

data class SharedSocialLink(
    val platform: String,
    val handle: String
)

data class SharedEvent(
    val label: String,
    val date: Long,
    val type: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScanSheet(
    person: Person,
    onDismissRequest: () -> Unit
) {
    val jsonString = remember(person) {
        val sharedPerson = SharedPerson(
            name = person.name,
            phoneNumber = person.phoneNumber,
            email = person.email,
            frequencyDays = person.frequencyDays,
            notes = person.notes.firstOrNull()?.content,
            socialLinks = person.socialLinks.map { SharedSocialLink(it.platform, it.handle) },
            events = person.events.map { SharedEvent(it.label, it.date, it.type.name) }
        )
        Gson().toJson(sharedPerson)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Share Connection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Let someone else scan this QR code to add ${person.name} directly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // White container for QR Code to ensure high contrast/quiet zone
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                QrCodeImage(text = jsonString, size = 208)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = person.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
fun QrCodeImage(
    text: String,
    size: Int,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(text, size) {
        generateQRCode(text, size)
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = modifier.size(size.dp)
        )
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("Failed to generate QR")
        }
    }
}

private fun generateQRCode(text: String, size: Int): Bitmap? {
    return try {
        val bitMatrix = MultiFormatWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            size,
            size
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}