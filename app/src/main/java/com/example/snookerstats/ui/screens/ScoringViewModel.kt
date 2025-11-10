package com.example.snookerstats.ui.screens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScoringViewModel @Inject constructor(
    // Tutaj wstrzykniemy MatchRepository
) : ViewModel() {
    // Tutaj dodamy całą logikę stanu meczu
}
