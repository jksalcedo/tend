package com.jksalcedo.tend.ui.archived

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.ui.home.PersonCard
import com.jksalcedo.tend.ui.theme.TendPastels
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArchivedScreen(
    viewModel: ArchivedViewModel = koinViewModel(),
    onPersonClick: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val people by viewModel.people.collectAsState()
    ArchivedScreenContent(
        people = people,
        onPersonClick = onPersonClick,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchivedScreenContent(
    people: List<Person>,
    onPersonClick: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val mintColor = if (isDarkTheme) TendPastels.MintDark else TendPastels.Mint
    val mintAccent = if (isDarkTheme) TendPastels.Mint else TendPastels.MintDark

    val yellowColor = if (isDarkTheme) TendPastels.YellowDark else TendPastels.Yellow
    val yellowAccent = if (isDarkTheme) TendPastels.Yellow else TendPastels.YellowDark

    val purpleColor = if (isDarkTheme) TendPastels.PurpleDark else TendPastels.Purple
    val purpleAccent = if (isDarkTheme) TendPastels.Purple else TendPastels.PurpleDark

    val pinkColor = if (isDarkTheme) TendPastels.PinkDark else TendPastels.Pink
    val pinkAccent = if (isDarkTheme) TendPastels.Pink else TendPastels.PinkDark

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Archived Connections",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (people.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No archived connections",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(people) { person ->
                        PersonCard(
                            person = person,
                            mintColor = mintColor,
                            mintAccent = mintAccent,
                            yellowColor = yellowColor,
                            yellowAccent = yellowAccent,
                            purpleColor = purpleColor,
                            purpleAccent = purpleAccent,
                            pinkColor = pinkColor,
                            pinkAccent = pinkAccent,
                            onClick = { onPersonClick(person.id) }
                        )
                    }
                }
            }
        }
    }
}
